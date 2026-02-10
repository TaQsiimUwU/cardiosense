package com.taqsiim.cardiologic

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.taqsiim.cardiologic.service.monitoring.MonitoringService
import com.taqsiim.cardiologic.ui.onboarding.PermissionManager
import com.taqsiim.cardiologic.ui.theme.CardiosenseTheme
import com.taqsiim.cardiologic.ui.navigation.AppScreen
import com.taqsiim.cardiologic.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- 1. FORCE START SERVICE (For Testing) ---
        // This makes the notification appear instantly when you open the app
        val intent = Intent(this, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        // ---------------------------------------------

        val startScreen = if (PermissionManager.hasAllPermissions(this)) {
            AppScreen.Home
        } else {
            AppScreen.Permissions
        }

        setContent {
            CardiosenseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startScreen
                    )
                }
            }
        }
    }
}