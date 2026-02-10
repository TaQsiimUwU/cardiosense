package com.taqsiim.cardiosense.domain.usecase

import com.taqsiim.cardiosense.data.ai.ArrhythmiaClassifier
import com.taqsiim.cardiosense.domain.model.SensorData


sealed class AnalysisResult {
    object Normal : AnalysisResult()
    data class Abnormal(val conditions: List<String>, val isCritical: Boolean) : AnalysisResult()
}

class AnalyzeHeartRhythmUseCase(
    private val classifier: ArrhythmiaClassifier
) {
    operator fun invoke(dataBuffer: List<SensorData>): AnalysisResult {
        // 1. Run AI
        val detectedConditions = classifier.classify(dataBuffer)

        // 2. Business Logic
        if (detectedConditions.isEmpty() || detectedConditions.contains("Normal")) {
            return AnalysisResult.Normal
        }

        // 3. Determine Urgency (MI or AFib = Critical)
        val isCritical = detectedConditions.any {
            it == "Myocardial Infarction" || it == "Atrial Fibrillation"
        }

        return AnalysisResult.Abnormal(detectedConditions, isCritical)
    }
}