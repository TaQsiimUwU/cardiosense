package com.taqsiim.cardiologic.service.monitoring

import com.taqsiim.cardiologic.domain.model.SensorData

/**
 * Extracts heart rate from ECG signal data.
 * Uses peak detection to count R-peaks in the ECG waveform.
 */
class HeartRateExtractor {

    companion object {
        private const val SAMPLING_RATE = 100 // Hz (ECG samples per second)
        private const val MIN_PEAK_DISTANCE_MS = 300 // Minimum 300ms between peaks (200 BPM max)
        private const val PEAK_THRESHOLD = 0.5f // Threshold for detecting R-peaks
    }

    private val recentPeaks = mutableListOf<Long>()
    private var lastPeakValue: Float = 0f
    private var lastPeakTime: Long = 0L

    /**
     * Processes ECG data and returns the current heart rate.
     *
     * @param dataBuffer List of recent ECG samples (typically 10 seconds worth)
     * @return Estimated heart rate in BPM, or null if insufficient data
     */
    fun extractHeartRate(dataBuffer: List<SensorData>): Int? {
        if (dataBuffer.size < SAMPLING_RATE * 5) {
            // Need at least 5 seconds of data for reliable HR estimation
            return null
        }

        // Clear old peaks (keep only last 10 seconds)
        val currentTime = System.currentTimeMillis()
        recentPeaks.removeAll { currentTime - it > 10000L }

        // Detect R-peaks in the buffer
        detectPeaks(dataBuffer)

        // Calculate heart rate from peak intervals
        return calculateHeartRate()
    }

    /**
     * Simple peak detection algorithm for R-peaks in ECG.
     */
    private fun detectPeaks(dataBuffer: List<SensorData>) {
        if (dataBuffer.size < 3) return

        // Calculate mean and standard deviation for adaptive thresholding
        val ecgValues = dataBuffer.map { it.ecg }
        val mean = ecgValues.average().toFloat()
        val stdDev = calculateStdDev(ecgValues, mean)
        val threshold = mean + (stdDev * PEAK_THRESHOLD)

        // Scan for peaks
        for (i in 1 until dataBuffer.size - 1) {
            val prev = dataBuffer[i - 1].ecg
            val current = dataBuffer[i].ecg
            val next = dataBuffer[i + 1].ecg
            val timestamp = dataBuffer[i].timestamp

            // Check if this is a local maximum above threshold
            if (current > prev && current > next && current > threshold) {
                // Check minimum distance from last peak
                if (timestamp - lastPeakTime >= MIN_PEAK_DISTANCE_MS) {
                    recentPeaks.add(timestamp)
                    lastPeakTime = timestamp
                    lastPeakValue = current
                }
            }
        }
    }

    /**
     * Calculates heart rate from detected peak intervals.
     */
    private fun calculateHeartRate(): Int? {
        if (recentPeaks.size < 2) {
            return null
        }

        // Calculate average interval between peaks
        val intervals = mutableListOf<Long>()
        for (i in 1 until recentPeaks.size) {
            intervals.add(recentPeaks[i] - recentPeaks[i - 1])
        }

        if (intervals.isEmpty()) return null

        // Average interval in milliseconds
        val avgInterval = intervals.average()

        // Convert to BPM: (60000 ms/min) / (interval in ms)
        val bpm = (60000.0 / avgInterval).toInt()

        // Sanity check: reasonable heart rate range
        return if (bpm in 30..250) bpm else null
    }

    /**
     * Calculates standard deviation.
     */
    private fun calculateStdDev(values: List<Float>, mean: Float): Float {
        if (values.isEmpty()) return 0f
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance).toFloat()
    }

    /**
     * Gets the most recent heart rate without processing new data.
     */
    fun getLastHeartRate(): Int? {
        return calculateHeartRate()
    }

    /**
     * Resets the extractor state.
     */
    fun reset() {
        recentPeaks.clear()
        lastPeakTime = 0L
        lastPeakValue = 0f
    }
}
