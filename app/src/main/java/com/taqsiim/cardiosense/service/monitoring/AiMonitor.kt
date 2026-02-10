package com.taqsiim.cardiosense.service.monitoring

import android.util.Log
import com.taqsiim.cardiosense.domain.model.SensorData
import com.taqsiim.cardiosense.domain.usecase.AnalysisResult
import com.taqsiim.cardiosense.domain.usecase.AnalyzeHeartRhythmUseCase
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

    fun process(data: SensorData) {
        synchronized(dataBuffer) {
            dataBuffer.add(data)
        }

        // Check if window is full
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

            // 2. Analyze
            val result = useCase(snapshot)

            // 3. Report Result (Callback to Service)
            onResult(result)

            // 4. Slide Window (Remove oldest 1 sec)
            synchronized(dataBuffer) {
                if (dataBuffer.size >= slideStep) {
                    dataBuffer.subList(0, slideStep).clear()
                }
            }
        }
    }
}