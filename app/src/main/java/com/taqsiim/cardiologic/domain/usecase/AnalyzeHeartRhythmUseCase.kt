package com.taqsiim.cardiologic.domain.usecase

import com.taqsiim.cardiologic.data.ai.ArrhythmiaClassifier
import com.taqsiim.cardiologic.domain.model.SensorData

sealed class AnalysisResult {
    object Normal : AnalysisResult()
    data class Abnormal(val conditions: List<String>, val isCritical: Boolean) : AnalysisResult()
    data class Skipped(val reason: String) : AnalysisResult()
}

class AnalyzeHeartRhythmUseCase(
    private val classifier: ArrhythmiaClassifier
) {
    operator fun invoke(dataBuffer: List<SensorData>): AnalysisResult {
        // --- 1. CRITICAL SAFETY CHECK: ASYSTOLE DETECTION ---
        // Must distinguish between "Technical Flatline" (handled in Monitor) vs "Medical Flatline" (Asystole)
        // Asystole has very low amplitude but is not a perfect digital zero/constant.
        
        if (isPotentialAsystole(dataBuffer)) {
            return AnalysisResult.Abnormal(
                conditions = listOf("POSSIBLE ASYSTOLE", "Clinical Cardiac Arrest Alert"),
                isCritical = true
            )
        }

        // --- 2. RUN AI MODEL ---
        val detectedConditions = classifier.classify(dataBuffer)

        // --- 3. BUSINESS LOGIC ---
        if (detectedConditions.isEmpty() || detectedConditions.contains("Normal")) {
            return AnalysisResult.Normal
        }

        // Determine Urgency (MI or AFib = Critical)
        val isCritical = detectedConditions.any {
            it == "Myocardial Infarction" || it == "Atrial Fibrillation"
        }

        return AnalysisResult.Abnormal(detectedConditions, isCritical)
    }

    private fun isPotentialAsystole(data: List<SensorData>): Boolean {
        if (data.isEmpty()) return false

        // Calculate variance (spread) of the signal
        val ecgValues = data.map { it.ecg }
        val mean = ecgValues.average()
        val variance = ecgValues.map { (it - mean) * (it - mean) }.average()
        
        // Threshold: Very low variance (e.g. < 0.05) but NOT zero (which would be Leads Off)
        // Note: Threshold needs calibration based on sensor gain (assuming mV here)
        val isLowAmplitude = variance < 0.05 
        val isNotDigitalFlatline = variance > 0.0001 // Ensure it's not a technical disconnect

        return isLowAmplitude && isNotDigitalFlatline
    }
}