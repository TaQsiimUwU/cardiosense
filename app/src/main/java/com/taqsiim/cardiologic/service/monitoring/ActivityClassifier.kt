package com.taqsiim.cardiologic.service.monitoring

import kotlin.math.sqrt

/**
 * Classifies user activity state based on accelerometer data.
 * Uses Signal Magnitude Area (SMA) to quantify total motion intensity.
 */
class ActivityClassifier {

    companion object {
        // Thresholds for activity classification (in m/s²)
        private const val SEDENTARY_THRESHOLD = 0.5f  // Very little movement
        private const val LIGHT_ACTIVITY_THRESHOLD = 2.0f  // Walking
        private const val MODERATE_ACTIVITY_THRESHOLD = 4.0f  // Brisk walking/jogging
        // Above moderate = intense activity (running, jumping)

        // Smoothing window to prevent jitter
        private const val SMOOTHING_WINDOW_SIZE = 10
    }

    // Rolling buffer for smoothing
    private val smaBuffer = mutableListOf<Float>()

    /**
     * Calculates the Signal Magnitude Area (SMA) - the total motion intensity
     * combining X, Y, Z accelerometer data into a single scalar value.
     *
     * @param ax Acceleration X-axis (m/s²)
     * @param ay Acceleration Y-axis (m/s²)
     * @param az Acceleration Z-axis (m/s²)
     * @return Vector magnitude representing total motion intensity
     */
    private fun calculateSMA(ax: Float, ay: Float, az: Float): Float {
        // Remove gravity bias (assuming 1g = 9.81 m/s²)
        // For more accurate results, you could implement gravity filtering
        val magnitude = sqrt(ax * ax + ay * ay + az * az)

        // Subtract expected gravity (9.81 m/s²) to get pure motion
        // Note: This is simplified. A proper implementation would use a low-pass filter
        val motionMagnitude = kotlin.math.abs(magnitude - 9.81f)

        return motionMagnitude
    }

    /**
     * Updates the activity classifier with new accelerometer data
     * and returns the smoothed activity intensity.
     *
     * @param ax Acceleration X-axis
     * @param ay Acceleration Y-axis
     * @param az Acceleration Z-axis
     * @return Smoothed Signal Magnitude Area value
     */
    fun updateAndGetSMA(ax: Float, ay: Float, az: Float): Float {
        val currentSMA = calculateSMA(ax, ay, az)

        // Add to smoothing buffer
        smaBuffer.add(currentSMA)

        // Keep buffer size limited
        if (smaBuffer.size > SMOOTHING_WINDOW_SIZE) {
            smaBuffer.removeAt(0)
        }

        // Return smoothed average
        return smaBuffer.average().toFloat()
    }

    /**
     * Classifies the current activity state based on accelerometer data.
     *
     * @param ax Acceleration X-axis
     * @param ay Acceleration Y-axis
     * @param az Acceleration Z-axis
     * @return ActivityState enum
     */
    fun classify(ax: Float, ay: Float, az: Float): ActivityState {
        val sma = updateAndGetSMA(ax, ay, az)

        return when {
            sma < SEDENTARY_THRESHOLD -> ActivityState.SEDENTARY
            sma < LIGHT_ACTIVITY_THRESHOLD -> ActivityState.LIGHT_ACTIVITY
            sma < MODERATE_ACTIVITY_THRESHOLD -> ActivityState.MODERATE_ACTIVITY
            else -> ActivityState.INTENSE_ACTIVITY
        }
    }

    /**
     * Gets the current smoothed SMA value without updating.
     */
    fun getCurrentSMA(): Float {
        return if (smaBuffer.isEmpty()) 0f else smaBuffer.average().toFloat()
    }

    /**
     * Resets the classifier state.
     */
    fun reset() {
        smaBuffer.clear()
    }
}

/**
 * Represents the user's current activity state.
 */
enum class ActivityState {
    SEDENTARY,           // Sitting, standing still, sleeping
    LIGHT_ACTIVITY,      // Slow walking, light movement
    MODERATE_ACTIVITY,   // Brisk walking, climbing stairs
    INTENSE_ACTIVITY     // Running, jumping, intense exercise
}
