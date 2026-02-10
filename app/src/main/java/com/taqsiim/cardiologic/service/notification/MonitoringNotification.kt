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
import com.taqsiim.cardiologic.service.monitoring.MonitoringService

class MonitoringNotification(private val context: Context) {

    companion object {
        // CHANGED: Added "_v2" to force Android to reset settings!
        const val CHANNEL_ID = "CardioSense_Monitor_Channel_v2"
        const val CHANNEL_NAME = "CardioSense Live Monitoring"
        const val NOTIFICATION_ID = 1
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Create the channel immediately
        createNotificationChannel()
    }

    fun build(contentText: String = "Monitoring Heart Rate..."): Notification {
        // 1. Open App Intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // 2. Stop Action Intent
        val stopIntent = Intent(context, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 0, stopIntent, // Request Code 0 is fine
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // 3. Build Notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this exists!
            .setContentTitle("CardioSense Active")
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

            // CHANGED: Use DEFAULT priority for testing (High visibility)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

            // Add Stop Button
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )

        // Android 12+ Immediate display behavior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return builder.build()
    }

    fun update(contentText: String) {
        val notification = build(contentText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                // CHANGED: Use IMPORTANCE_DEFAULT (or HIGH) for testing.
                // LOW often hides the icon in the "Silent" section.
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows the active connection to the Chest Strap."
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}