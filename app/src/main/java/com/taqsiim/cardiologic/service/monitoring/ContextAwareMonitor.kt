package com.taqsiim.cardiologic.service.monitoring

/**
 * Context-Aware Monitor that combines Heart Rate with Activity State
 * to determine if an elevated heart rate is physiologically justified.
 *
 * Implements the Decision Matrix:
 * - High HR + High Activity = Exercise (NORMAL)
 * - High HR + Low Activity = Potential Pathology (ALERT)
 * - Normal HR = No Alert
 *
 * Also implements a Cooldown Buffer to account for post-exercise recovery.
 */
class ContextAwareMonitor {

    companion object {
        // Heart Rate Thresholds
        const val HR_ELEVATED_THRESHOLD = 100  // BPM - Start monitoring
        const val HR_CRITICAL_THRESHOLD = 120  // BPM - Potential arrhythmia at rest

        // Cooldown Configuration
        private const val COOLDOWN_DURATION_MS = 5 * 60 * 1000L  // 5 minutes
        private const val COOLDOWN_CHECK_INTERVAL_MS = 10 * 1000L  // Check every 10 seconds
    }

    private val activityClassifier = ActivityClassifier()

    // Cooldown tracking
    private var lastIntenseActivityTime: Long = 0L
    private var inCooldownPeriod: Boolean = false

    /**
     * Data class representing a monitoring decision
     */
    data class MonitoringDecision(
        val heartRate: Int,
        val activityState: ActivityState,
        val smaValue: Float,
        val shouldAlert: Boolean,
        val alertReason: String?,
        val inCooldown: Boolean
    )

    /**
     * Processes accelerometer data and updates activity state.
     * Must be called whenever IMU data arrives.
     *
     * @param ax Acceleration X-axis
     * @param ay Acceleration Y-axis
     * @param az Acceleration Z-axis
     */
    fun updateActivity(ax: Float, ay: Float, az: Float) {
        val activityState = activityClassifier.classify(ax, ay, az)

        // Track intense activity for cooldown logic
        if (activityState == ActivityState.MODERATE_ACTIVITY ||
            activityState == ActivityState.INTENSE_ACTIVITY) {
            lastIntenseActivityTime = System.currentTimeMillis()
            inCooldownPeriod = false
        }
    }

    /**
     * Evaluates if an alert should be triggered based on heart rate and activity context.
     *
     * @param heartRate Current heart rate in BPM
     * @return MonitoringDecision with alert recommendation
     */
    fun evaluateHeartRate(heartRate: Int): MonitoringDecision {
        val currentTime = System.currentTimeMillis()
        val activityState = getCurrentActivityState()
        val smaValue = activityClassifier.getCurrentSMA()

        // Update cooldown status
        updateCooldownStatus(currentTime)

        // Decision Matrix Logic
        val (shouldAlert, reason) = when {
            // Normal heart rate - no alert regardless of activity
            heartRate < HR_ELEVATED_THRESHOLD -> {
                false to null
            }

            // Elevated HR (100-119 BPM)
            heartRate < HR_CRITICAL_THRESHOLD -> {
                when {
                    // Elevated HR during activity is normal
                    activityState == ActivityState.MODERATE_ACTIVITY ||
                    activityState == ActivityState.INTENSE_ACTIVITY -> {
                        false to null
                    }
                    // Elevated HR in cooldown period - suppress alert
                    inCooldownPeriod -> {
                        false to "Recovery phase - HR returning to baseline"
                    }
                    // Elevated HR at rest - monitor but don't alarm yet
                    activityState == ActivityState.SEDENTARY -> {
                        false to "Monitoring: Elevated HR at rest"
                    }
                    else -> {
                        false to null
                    }
                }
            }

            // Critical HR (≥120 BPM)
            else -> {
                when {
                    // High HR during intense activity is expected
                    activityState == ActivityState.INTENSE_ACTIVITY -> {
                        false to "High HR justified by intense exercise"
                    }

                    // High HR during moderate activity - may be normal for some people
                    activityState == ActivityState.MODERATE_ACTIVITY -> {
                        false to "High HR during moderate activity"
                    }

                    // High HR in cooldown period - dampen alert
                    inCooldownPeriod -> {
                        // Still in recovery, but monitor closely
                        val timeSinceCooldown = currentTime - lastIntenseActivityTime
                        if (timeSinceCooldown < 2 * 60 * 1000L) { // First 2 minutes
                            false to "Recovery phase - monitoring elevated HR"
                        } else {
                            // After 2 minutes of cooldown, HR should be decreasing
                            // If still critical, this might be concerning
                            true to "⚠️ CRITICAL: Heart rate not recovering after exercise (HR: $heartRate)"
                        }
                    }

                    // ⚠️ CRITICAL: High HR at rest - potential arrhythmia
                    activityState == ActivityState.SEDENTARY ||
                    activityState == ActivityState.LIGHT_ACTIVITY -> {
                        true to "🚨 CRITICAL ALERT: Tachycardia at rest detected (HR: $heartRate BPM)\n" +
                               "Your heart is racing without physical exertion.\n" +
                               "This may indicate an arrhythmia or other cardiac event."
                    }

                    else -> {
                        false to null
                    }
                }
            }
        }

        return MonitoringDecision(
            heartRate = heartRate,
            activityState = activityState,
            smaValue = smaValue,
            shouldAlert = shouldAlert,
            alertReason = reason,
            inCooldown = inCooldownPeriod
        )
    }

    /**
     * Updates the cooldown status based on time elapsed since last intense activity.
     */
    private fun updateCooldownStatus(currentTime: Long) {
        if (lastIntenseActivityTime > 0) {
            val timeSinceActivity = currentTime - lastIntenseActivityTime

            if (timeSinceActivity < COOLDOWN_DURATION_MS) {
                inCooldownPeriod = true
            } else if (inCooldownPeriod) {
                // Exiting cooldown period
                inCooldownPeriod = false
            }
        }
    }

    /**
     * Gets the current activity state classification.
     */
    private fun getCurrentActivityState(): ActivityState {
        // Use a simple heuristic based on current SMA value
        val sma = activityClassifier.getCurrentSMA()
        return when {
            sma < 0.5f -> ActivityState.SEDENTARY
            sma < 2.0f -> ActivityState.LIGHT_ACTIVITY
            sma < 4.0f -> ActivityState.MODERATE_ACTIVITY
            else -> ActivityState.INTENSE_ACTIVITY
        }
    }

    /**
     * Gets diagnostic information for debugging/logging
     */
    fun getDiagnosticInfo(): String {
        val activityState = getCurrentActivityState()
        val sma = activityClassifier.getCurrentSMA()
        val cooldownStatus = if (inCooldownPeriod) {
            val timeInCooldown = System.currentTimeMillis() - lastIntenseActivityTime
            "In cooldown (${timeInCooldown / 1000}s elapsed)"
        } else {
            "Not in cooldown"
        }

        return """
            Activity: $activityState
            SMA: %.3f m/s²
            Cooldown: $cooldownStatus
        """.trimIndent().format(sma)
    }

    /**
     * Resets the monitor state
     */
    fun reset() {
        activityClassifier.reset()
        lastIntenseActivityTime = 0L
        inCooldownPeriod = false
    }
}
