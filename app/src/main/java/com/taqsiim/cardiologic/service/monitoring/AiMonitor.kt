package com.taqsiim.cardiologic.service.monitoring

import android.util.Log
import com.taqsiim.cardiologic.domain.model.SensorData
import com.taqsiim.cardiologic.domain.usecase.AnalysisResult
import com.taqsiim.cardiologic.domain.usecase.AnalyzeHeartRhythmUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Collections

class AiMonitor(
    private val useCase: AnalyzeHeartRhythmUseCase,
    private val scope: CoroutineScope,
    private val onResult: (AnalysisResult) -> Unit // Callback for results
) {

    private val dataBuffer = Collections.synchronizedList(mutableListOf<SensorData>())
    private val windowSize = 1000
    private val slideStep = 100 // 1 Second overlap
    
    // 1. Integrate Activity Classifier
    private val activityClassifier = ActivityClassifier()
    private var currentActivityState: ActivityState = ActivityState.SEDENTARY

    fun process(data: SensorData) {
        // 2. Real-time Activity Classification (Feed every sample)
        // Update the classifier with the latest accelerometer data from this packet
        currentActivityState = activityClassifier.classify(data.accelX, data.accelY, data.accelZ)

        synchronized(dataBuffer) {
            dataBuffer.add(data)
        }

        // Check if window is full3
        if (dataBuffer.size >= windowSize) {
            runAnalysis()
        }
    }

    private fun runAnalysis() {
        scope.launch {
            // 1. Snapshot
            val snapshot = synchronized(dataBuffer) {
                ArrayList(dataBuffer.takeLast(windowSize))
            }

            // --- 3. SMART GATING LOGIC ---

            // A. Activity Gating (Battery Saver)
            // Rule: If User is Running (Intense Activity), skip AI.
            // Motion artifacts make arrhythmia detection unreliable during intense exercise anyway.
            if (currentActivityState == ActivityState.INTENSE_ACTIVITY) {
                Log.d("AiMonitor", "Skipping analysis: User is running (Intense Activity)")
                onResult(AnalysisResult.Skipped("User Running / Intense Activity"))
                slideWindow()
                return@launch
            }

            // B. Signal Quality Gating (Noise Filter)
            // Check for Technical Disconnection (Leads Off)
            if (isLeadsOff(snapshot)) {
                Log.d("AiMonitor", "Skipping analysis: Leads Off / Signal Lost")
                onResult(AnalysisResult.Skipped("Leads Off / Signal Lost"))
                slideWindow()
                return@launch
            }

            // --- 4. EXECUTE AI (If gates passed) ---
            
            // Note: Asystole check is done INSIDE the UseCase because it is a critical result,
            // not something to be skipped.
            val result = useCase(snapshot)

            // 5. Report Result
            onResult(result)

            // 6. Slide Window
            slideWindow()
        }
    }

    private fun slideWindow() {
        synchronized(dataBuffer) {
            if (dataBuffer.size >= slideStep) {
                dataBuffer.subList(0, slideStep).clear()
            }
        }
    }

    /**
     * Checks for technical signal loss (Leads Off).
     * Returns true if signal is a perfect flatline (all zeros/identical) or saturated.
     */
    private fun isLeadsOff(data: List<SensorData>): Boolean {
        if (data.isEmpty()) return true

        val firstValue = data[0].ecg
        
        // Check 1: Perfect Flatline (Digital Zero or Identical Values)
        // If all values in the buffer are exactly the same, the sensor is disconnected or stuck.
        val isFlatline = data.all { it.ecg == firstValue }
        
        if (isFlatline) return true
        
        // Check 2: Rail-to-Rail Saturation (e.g. ADC Max/Min)
        // Adjust these thresholds based on your ADC bit depth (e.g. 0 to 4095 or -2.5V to 2.5V)
        // Assuming normalized float input here:
        val saturationThresholdRaw = 32767f // Example for 16-bit signed
        // Or if values are typical ECG floats (mV):
        val extremeThreshold = 10.0f // > 10mV is physically impossible for surface ECG
        
        val isSaturated = data.any { kotlin.math.abs(it.ecg) > extremeThreshold }
        
        return isSaturated
    }
}