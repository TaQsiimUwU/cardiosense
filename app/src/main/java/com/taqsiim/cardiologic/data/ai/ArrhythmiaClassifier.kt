package com.taqsiim.cardiologic.data.ai

import android.content.Context
import android.util.Log
import com.taqsiim.cardiologic.domain.model.SensorData
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ArrhythmiaClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val modelInputSize = 1000

    // Your specific labels and thresholds
    private val labels = listOf(
        "Normal Sinus Rhythm",   // 0: NSR
        "Atrial Fibrillation",   // 1: AFib
        "Sinus Tachycardia",     // 2: STach
        "PVC",                   // 3: PVC
        "PAC",                   // 4: PAC
        "Sinus Bradycardia",     // 5: SBrad
        "Myocardial Infarction", // 6: MI
        "ST Depression",         // 7: STD
        "First Degree AV Block", // 8: IAVB
        "T-Wave Abnormality",    // 9: TAb
        "Left Axis Deviation"    // 10: LAD
    )

    private val thresholds = floatArrayOf(
        0.45f, // NSR
        0.50f, // AFib
        0.55f, // STach
        0.45f, // PVC
        0.45f, // PAC
        0.60f, // SBrad
        0.45f, // MI
        0.45f, // STD
        0.55f, // IAVB
        0.45f, // TAb
        0.45f  // LAD
    )

    init {
        try {
            // Updated to match your filename: ecg_model_quantized.tflite
            val model = loadModelFile(context, "ecg_model_quantized.tflite")
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            Log.d("AI", "Quantized model loaded. Ready for 1-second sliding window.")
        } catch (e: Exception) {
            Log.e("AI", "Model loading failed: ${e.message}")
        }
    }

    fun classify(dataBuffer: List<SensorData>): List<String> {
        val interp = interpreter ?: return emptyList()
        if (dataBuffer.size < modelInputSize) return emptyList()

        // 1. Prepare Input: ByteBuffer is better for performance and required for many quantized models
        // 1000 samples * 4 bytes per Float
        val inputBuffer = ByteBuffer.allocateDirect(modelInputSize * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val startIndex = dataBuffer.size - modelInputSize
        for (i in 0 until modelInputSize) {
            inputBuffer.putFloat(dataBuffer[startIndex + i].ecg)
        }

        // 2. Prepare Output: [1, 11]
        val output = Array(1) { FloatArray(labels.size) }

        // 3. Run Inference
        try {
            interp.run(inputBuffer, output)
        } catch (e: Exception) {
            Log.e("AI", "Inference Failed: ${e.message}")
            return emptyList()
        }

        // 4. Decode Results using your specific thresholds
        val detectedConditions = mutableListOf<String>()
        val probabilities = output[0]

        for (i in probabilities.indices) {
            if (probabilities[i] >= thresholds[i]) {
                detectedConditions.add(labels[i])
            }
        }

        // Log everything for debugging the F1 performance you noted
        // Log.d("AI", "Probabilities: ${probabilities.joinToString(", ")}")

        return detectedConditions
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }
}