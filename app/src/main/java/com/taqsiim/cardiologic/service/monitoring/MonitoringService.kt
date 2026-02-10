package com.taqsiim.cardiologic.service.monitoring

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.taqsiim.cardiologic.data.ai.ArrhythmiaClassifier
import com.taqsiim.cardiologic.domain.model.SensorData
import com.taqsiim.cardiologic.domain.usecase.AnalysisResult
import com.taqsiim.cardiologic.domain.usecase.AnalyzeHeartRhythmUseCase
import com.taqsiim.cardiologic.service.notification.MonitoringNotification
import com.taqsiim.cardiologic.service.notification.CriticalAlertNotification
import com.taqsiim.cardiologic.service.monitoring.AiMonitor
import kotlinx.coroutines.*


class MonitoringService : Service() {

companion object {
    const val ACTION_START = "ACTION_START_MONITORING"
    const val ACTION_STOP = "ACTION_STOP_MONITORING"
    const val NOTIFICATION_ID = 1
    private const val TAG = "MonitoringService"
}
    // Components
    private lateinit var notificationHelper: MonitoringNotification
    private lateinit var criticalAlertHelper: CriticalAlertNotification
    private lateinit var sensorState: SensorState
    private lateinit var aiMonitor: AiMonitor

    // Context-Aware Monitoring
    private lateinit var contextAwareMonitor: ContextAwareMonitor
    private lateinit var heartRateExtractor: HeartRateExtractor

    // Data buffer for HR extraction
    private val ecgBuffer = mutableListOf<SensorData>()
    private val bufferMaxSize = 1000 // 10 seconds at 100Hz

    // Scope
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        notificationHelper = MonitoringNotification(this)
        criticalAlertHelper = CriticalAlertNotification(this)

        // 1. Initialize State Holder
        sensorState = SensorState()

        // 2. Initialize Context-Aware Monitor
        contextAwareMonitor = ContextAwareMonitor()
        heartRateExtractor = HeartRateExtractor()

        // 3. Initialize AI Logic
        val classifier = ArrhythmiaClassifier(this)
        val useCase = AnalyzeHeartRhythmUseCase(classifier)

        aiMonitor = AiMonitor(
            useCase = useCase,
            scope = serviceScope,
            onResult = { result -> handleAiResult(result) } // Callback
        )
    }

    // --- BLUETOOTH CALLS THESE FUNCTIONS ---

    // Call this when IMU packet arrives (e.g. 50Hz)
    fun onImuData(ax: Float, ay: Float, az: Float) {
        // Update sensor state
        sensorState.updateAccel(ax, ay, az)

        // Update context-aware monitor with activity data
        contextAwareMonitor.updateActivity(ax, ay, az)
    }

    fun onGyroData(gx: Float, gy: Float, gz: Float) {
        sensorState.updateGyro(gx, gy, gz)
    }

    // Call this when ECG packet arrives (e.g. 100Hz)
    fun onEcgData(ecg: Float) {
        // 1. Sync ECG with latest IMU
        val packet = sensorState.createPacket(ecg)

        // 2. Add to ECG buffer for HR extraction
        synchronized(ecgBuffer) {
            ecgBuffer.add(packet)
            if (ecgBuffer.size > bufferMaxSize) {
                ecgBuffer.removeAt(0)
            }
        }

        // 3. Send to AI Buffer
        aiMonitor.process(packet)

        // 4. Periodically extract heart rate and evaluate context
        if (ecgBuffer.size % 100 == 0) { // Every second
            evaluateContextAwareMonitoring()
        }

        // 5. Update Live Notification (Optional: every 10th sample to save UI)
        // notificationHelper.updateHeartRate(...)
    }

    // --- CONTEXT-AWARE MONITORING ---

    /**
     * Evaluates heart rate in context of activity to determine if alert is needed.
     * This implements the Decision Matrix logic.
     */
    private fun evaluateContextAwareMonitoring() {
        serviceScope.launch {
            try {
                // Extract current heart rate from ECG buffer
                val currentHeartRate = synchronized(ecgBuffer) {
                    heartRateExtractor.extractHeartRate(ecgBuffer.toList())
                } ?: return@launch

                // Evaluate using context-aware monitor
                val decision = contextAwareMonitor.evaluateHeartRate(currentHeartRate)

                // Log diagnostic info
                Log.d(TAG, """
                    Context-Aware Monitoring:
                    HR: ${decision.heartRate} BPM
                    Activity: ${decision.activityState}
                    SMA: %.3f m/s²
                    Cooldown: ${decision.inCooldown}
                    Alert: ${decision.shouldAlert}
                    Reason: ${decision.alertReason ?: "None"}
                """.trimIndent().format(decision.smaValue))

                // Update notification with current status
                val statusText = when {
                    decision.shouldAlert -> "⚠️ ${decision.heartRate} BPM - Alert Triggered"
                    decision.heartRate >= ContextAwareMonitor.HR_ELEVATED_THRESHOLD ->
                        "${decision.heartRate} BPM - ${decision.activityState} (${if (decision.inCooldown) "Recovery" else "Active"})"
                    else -> "${decision.heartRate} BPM - Normal"
                }
                notificationHelper.update(statusText)

                // Trigger critical alert if needed
                if (decision.shouldAlert) {
                    triggerCriticalAlert(decision)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in context-aware monitoring", e)
            }
        }
    }

    /**
     * Triggers a critical alert using full-screen intent.
     */
    private fun triggerCriticalAlert(decision: ContextAwareMonitor.MonitoringDecision) {
        criticalAlertHelper.triggerCriticalAlert(
            heartRate = decision.heartRate,
            message = decision.alertReason ?: "Critical heart rate detected",
            activityState = decision.activityState.toString()
        )
    }

    // --- RESULT HANDLING ---

    /**
     * Handles AI classification results from arrhythmia detection.
     * This works in conjunction with context-aware monitoring for comprehensive protection.
     */
    private fun handleAiResult(result: AnalysisResult) {
        when (result) {
            is AnalysisResult.Normal -> {
                // AI says rhythm is normal
                // Context-aware monitoring still watches for tachycardia
                Log.d(TAG, "AI: Normal rhythm detected")
            }
            is AnalysisResult.Abnormal -> {
                val msg = result.conditions.joinToString(", ")
                Log.d(TAG, "AI: Abnormal rhythm detected - $msg")

                if (result.isCritical) {
                    // Critical arrhythmia detected by AI (e.g., AFib, MI)
                    // This bypasses context-aware monitoring as these are always critical
                    notificationHelper.update("🚨 CRITICAL: $msg")

                    // Trigger full-screen alert
                    criticalAlertHelper.triggerCriticalAlert(
                        heartRate = heartRateExtractor.getLastHeartRate() ?: 0,
                        message = "Critical Arrhythmia Detected:\n$msg\n\nThis requires immediate medical attention.",
                        activityState = "AI Classification"
                    )
                } else {
                    // Non-critical abnormality (e.g., PVC)
                    notificationHelper.update("⚠ Warning: $msg")
                }
            }
            is AnalysisResult.Skipped -> {
                // Analysis skipped due to gating (running, leads off, etc.)
                Log.d(TAG, "AI Analysis Skipped: ${result.reason}")
            }
        }
    }

    // --- BOILERPLATE ---
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { 
            stopSelf()
            return START_NOT_STICKY 
        }
        startForeground(NOTIFICATION_ID, notificationHelper.build("Initializing..."))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
