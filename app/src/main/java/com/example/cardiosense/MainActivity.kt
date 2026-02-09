package com.example.cardiosense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cardiosense.ui.theme.CardiosenseTheme
import com.example.cardiosense.ui.navigation.NavGraph
import com.example.cardiosense.ui.onboarding.PermissionsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasPermissions by remember { mutableStateOf(false) }

            if (hasPermissions){
            CardiosenseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(modifier = Modifier.fillMaxSize().padding(innerPadding))
                }
            }
            }
            else {
                PermissionsScreen(
                    onPermissionsGranted = {hasPermissions = true }
                )
            }
        }
    }
}

@Composable
fun AppPreview() {
    CardiosenseTheme {
        NavGraph(modifier = Modifier.fillMaxSize())
    }
}
