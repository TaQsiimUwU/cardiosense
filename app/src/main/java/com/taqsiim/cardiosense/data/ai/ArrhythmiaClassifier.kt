package com.taqsiim.cardiosense.data.ai

import android.content.Context
import android.util.Log
import com.taqsiim.cardiosense.domain.model.SensorData
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ArrhythmiaClassifier(context: Context) {

    private var interpreter: Interpreter? = null

    // --- CONFIGURATION ---
    // Kotlin Convention: Properties use camelCase
    private val modelInputSize = 1000

    // User specified: "ECG signal" -> Likely 1 feature.
    private val numFeatures = 1

    // Kotlin Convention: Read-only lists use camelCase
    private val labels = listOf(
        "Normal Sinus Rhythm",              // 0: NSR
        "Atrial Fibrillation",             // 1: AFib
        "Sinus Tachycardia",               // 2: STach
        "Premature Ventricular Contractions", // 3: PVC
        "Premature Atrial Contraction",    // 4: PAC
        "Sinus Bradycardia",               // 5: SBrad
        "Myocardial Infarction",           // 6: MI
        "ST Depression",                   // 7: STD
        "T Wave Inversion",                // 8: TInv
        "T Wave Abnormal",                 // 9: TAb
        "Left Axis Deviation"              // 10: LAD
    )

    private val outputClasses = labels.size // Should be 11

    init {
        try {
            val model = loadModelFile(context, "arrhythmia_model.tflite")
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            Log.d("AI", "Model loaded successfully. Classes: $outputClasses")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Returns a list of detected conditions (e.g. ["Atrial Fibrillation"] or ["Normal"])
     */
    fun classify(dataBuffer: List<SensorData>): List<String> {
        if (interpreter == null) return emptyList()
        if (dataBuffer.size < modelInputSize) return emptyList()

        // 1. Prepare Input: [1, 1000, 1]
        // We take the LAST 1000 samples if the buffer is larger
        val input = Array(1) { Array(modelInputSize) { FloatArray(numFeatures) } }
        val startIndex = dataBuffer.size - modelInputSize

        for (i in 0 until modelInputSize) {
            val d = dataBuffer[startIndex + i]
            input[0][i][0] = d.ecg
            // If your model needs IMU, uncomment these:
            // input[0][i][1] = d.accelX
        }

        // 2. Prepare Output: [1, 11]
        val output = Array(1) { FloatArray(outputClasses) }

        // 3. Run Inference
        try {
            interpreter?.run(input, output)
        } catch (e: Exception) {
            Log.e("AI", "Inference Failed: ${e.message}")
            return emptyList()
        }

        // 4. Decode Results
        // Assuming Multi-label (Sigmoid) where > 0.5 means "Present"
        val detectedConditions = mutableListOf<String>()
        val probabilities = output[0]

        // Kotlin Convention: Local variables use camelCase
        val threshold = 0.5f

        // Check for Normal (Index 0)
        if (probabilities[0] > threshold) {
            // Use labels[0] instead of hardcoded string to ensure consistency
            detectedConditions.add(labels[0])
        }

        // Check for Diseases (Indices 1 to 10)
        for (i in 1 until outputClasses) {
            if (probabilities[i] > threshold) {
                detectedConditions.add(labels[i])
            }
        }

        // Fallback: If nothing crossed threshold, return top prediction
        if (detectedConditions.isEmpty()) {
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            detectedConditions.add(labels[maxIndex])
        }

        return detectedConditions
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}