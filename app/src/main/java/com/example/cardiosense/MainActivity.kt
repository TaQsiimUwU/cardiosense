package com.example.cardiosense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.cardiosense.ui.navigation.NavGraph
import com.example.cardiosense.ui.navigation.AppScreen
import com.example.cardiosense.ui.onboarding.PermissionManager
import com.example.cardiosense.ui.theme.CardiosenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
