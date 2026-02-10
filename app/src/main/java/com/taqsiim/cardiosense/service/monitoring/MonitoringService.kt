package com.taqsiim.cardiosense.service.monitoring

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.taqsiim.cardiosense.data.ai.ArrhythmiaClassifier
import com.taqsiim.cardiosense.domain.usecase.AnalysisResult
import com.taqsiim.cardiosense.domain.usecase.AnalyzeHeartRhythmUseCase
import com.taqsiim.cardiosense.service.notification.MonitoringNotification
import kotlinx.coroutines.*


class MonitoringService : Service() {

companion object {
    const val ACTION_START = "ACTION_START_MONITORING"
    const val ACTION_STOP = "ACTION_STOP_MONITORING"
    const val NOTIFICATION_ID = 1
}
    // Components
    private lateinit var notificationHelper: MonitoringNotification
    private lateinit var sensorState: SensorState
    private lateinit var aiMonitor: AiMonitor

    // Scope
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        notificationHelper = MonitoringNotification(this)

        // 1. Initialize State Holder
        sensorState = SensorState()

        // 2. Initialize AI Logic
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
        // CHANGE THIS:
        // sensorState.updateImu(ax, ay, az)

        // TO THIS:
        sensorState.updateAccel(ax, ay, az)
    }

    fun onGyroData(gx: Float, gy: Float, gz: Float) {
        sensorState.updateGyro(gx, gy, gz)
    }
    // Call this when ECG packet arrives (e.g. 100Hz)
    fun onEcgData(ecg: Float) {
        // 1. Sync ECG with latest IMU
        val packet = sensorState.createPacket(ecg)

        // 2. Send to AI Buffer
        aiMonitor.process(packet)

        // 3. Update Live Notification (Optional: every 10th sample to save UI)
        // notificationHelper.updateHeartRate(...)
    }

    // --- RESULT HANDLING ---
    private fun handleAiResult(result: AnalysisResult) {
        when (result) {
            is AnalysisResult.Normal -> {
                notificationHelper.update("Status: Normal Rhythm")
            }
            is AnalysisResult.Abnormal -> {
                val msg = result.conditions.joinToString(", ")
                if (result.isCritical) {
                    notificationHelper.update("⚠ CRITICAL: $msg")
                    // triggerAlarm()
                } else {
                    notificationHelper.update("⚠ Warning: $msg")
                }
            }
        }
    }

    // --- BOILERPLATE ---
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        startForeground(1, notificationHelper.build("Initializing..."))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
