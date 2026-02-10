package com.taqsiim.cardiologic.ui.alert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taqsiim.cardiologic.ui.theme.CardiosenseTheme

/**
 * SOS Activity - placeholder for future emergency contact functionality
 */
class SOSActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val heartRate = intent.getIntExtra("heartRate", 0)

        setContent {
            CardiosenseTheme {
                SOSScreen(
                    heartRate = heartRate,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun SOSScreen(
    heartRate: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32)) // Material Green 700
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "SOS Sent",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "SOS SENT",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Message
        Text(
            text = "Emergency alert sent!\n\nHeart Rate: $heartRate BPM\n\n" +
                   "Emergency contacts have been notified.\n" +
                   "Your location has been shared.",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Close Button
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1B5E20) // Dark Green
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Close",
                fontSize = 16.sp
            )
        }
    }
}
