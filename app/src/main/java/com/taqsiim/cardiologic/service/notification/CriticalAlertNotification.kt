package com.taqsiim.cardiologic.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.taqsiim.cardiologic.MainActivity
import com.taqsiim.cardiologic.R
import com.taqsiim.cardiologic.ui.alert.CriticalAlertActivity

/**
 * Manages critical alert notifications with full-screen intent capability.
 * Used for medical emergencies that require immediate user attention.
 */
class CriticalAlertNotification(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "CardioSense_Critical_Alert_Channel"
        const val CHANNEL_NAME = "Critical Heart Rate Alerts"
        const val NOTIFICATION_ID = 999 // High priority ID
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createCriticalAlertChannel()
    }

    /**
     * Creates a critical alert notification channel with maximum priority.
     */
    private fun createCriticalAlertChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Maximum importance
            ).apply {
                description = "Critical alerts for potentially dangerous heart rate conditions"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Triggers a critical alert with full-screen intent.
     * This will wake up the device and show a full-screen activity.
     *
     * @param heartRate Current heart rate
     * @param message Alert message
     * @param activityState Current activity state
     */
    fun triggerCriticalAlert(
        heartRate: Int,
        message: String,
        activityState: String
    ) {
        // Full-screen intent - launches CriticalAlertActivity
        val fullScreenIntent = Intent(context, CriticalAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(CriticalAlertActivity.EXTRA_HEART_RATE, heartRate)
            putExtra(CriticalAlertActivity.EXTRA_MESSAGE, message)
            putExtra(CriticalAlertActivity.EXTRA_ACTIVITY_STATE, activityState)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Regular tap intent (if user dismisses full screen)
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            1,
            tapIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // SOS Action Intent
        val sosIntent = Intent(context, CriticalAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(CriticalAlertActivity.EXTRA_HEART_RATE, heartRate)
            putExtra(CriticalAlertActivity.EXTRA_MESSAGE, message)
            putExtra(CriticalAlertActivity.EXTRA_ACTIVITY_STATE, activityState)
            action = "SOS"
        }
        val sosPendingIntent = PendingIntent.getActivity(
            context,
            2,
            sosIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 CRITICAL HEART RATE ALERT")
            .setContentText("HR: $heartRate BPM at rest - Tap to respond")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false) // Persistent - cannot be swiped away easily
            .setOngoing(true) // Cannot be dismissed by swipe
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true) // KEY: Full screen intent
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .addAction(
                android.R.drawable.ic_menu_call,
                "SOS",
                sosPendingIntent
            )
            .build()

        // Apply full screen intent flags for older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
        }

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancels the critical alert notification.
     */
    fun cancelAlert() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Updates an existing critical alert (if you need to update HR without re-triggering)
     */
    fun updateAlert(heartRate: Int, message: String) {
        // Similar to triggerCriticalAlert but without full screen re-launch
        val tapIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            1,
            tapIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 CRITICAL ALERT")
            .setContentText("HR: $heartRate BPM")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
