package com.example.cardiosense.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MonitoringService : Service() {
    // Foreground service - the "heart" of the app
    // Responsible for continuous monitoring

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
