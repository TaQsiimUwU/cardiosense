package com.taqsiim.cardiologic.ui.alert

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
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
 * Full-screen alert activity shown when critical heart rate anomaly is detected.
 * This appears over the lock screen like an incoming call.
 */
class CriticalAlertActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HEART_RATE = "extra_heart_rate"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_ACTIVITY_STATE = "extra_activity_state"

        private const val VIBRATION_PATTERN_DURATION = 1000L // 1 second
        private const val VIBRATION_PAUSE_DURATION = 500L // 0.5 seconds
    }

    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up the screen and show over lock screen
        turnScreenOnAndKeyguardDismiss()

        // Extract data from intent
        val heartRate = intent.getIntExtra(EXTRA_HEART_RATE, 0)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Critical heart rate detected"
        val activityState = intent.getStringExtra(EXTRA_ACTIVITY_STATE) ?: "Unknown"

        // Start alert sound and vibration
        startAlertEffects()

        // Prevent dismissing the alert by back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // User must explicitly choose an action - do nothing
            }
        })

        setContent {
            CardiosenseTheme {
                CriticalAlertScreen(
                    heartRate = heartRate,
                    message = message,
                    activityState = activityState,
                    onSosClick = { handleSOSAction(heartRate) },
                    onCallEmergency = { handleCallEmergency() },
                    onImFineClick = { handleImFineAction() }
                )
            }
        }
    }

    private fun turnScreenOnAndKeyguardDismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun startAlertEffects() {
        // Start alarm sound
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start vibration pattern
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, VIBRATION_PATTERN_DURATION, VIBRATION_PAUSE_DURATION,
                                      VIBRATION_PATTERN_DURATION, VIBRATION_PAUSE_DURATION)
            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, VIBRATION_PATTERN_DURATION, VIBRATION_PAUSE_DURATION)
            vibrator?.vibrate(pattern, 0) // 0 = repeat
        }
    }

    private fun stopAlertEffects() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun handleSOSAction(heartRate: Int) {
        stopAlertEffects()

        // TODO: Implement SOS logic
        // - Send SMS to emergency contacts
        // - Share location
        // - Optionally call emergency services

        // For now, show that SOS was triggered
        val sosIntent = Intent(this, SOSActivity::class.java)
        sosIntent.putExtra("heartRate", heartRate)
        startActivity(sosIntent)

        finish()
    }

    private fun handleImFineAction() {
        stopAlertEffects()

        // User confirmed they're fine
        // Send broadcast to pause monitoring temporarily
        val pauseIntent = Intent("com.taqsiim.cardiologic.PAUSE_MONITORING")
        pauseIntent.putExtra("duration_minutes", 10)
        sendBroadcast(pauseIntent)

        finish()
    }

    private fun handleCallEmergency() {
        stopAlertEffects()

        // Initiate emergency call
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:911") // Change based on region
        startActivity(callIntent)

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlertEffects()
    }
}

@Composable
fun CriticalAlertScreen(
    heartRate: Int,
    message: String,
    activityState: String,
    onSosClick: () -> Unit,
    onCallEmergency: () -> Unit,
    onImFineClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD32F2F)) // Material Red 700
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Warning Icon
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Critical Alert",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Alert Title
        Text(
            text = "CRITICAL ALERT",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Heart Rate Display
        Text(
            text = "$heartRate BPM",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Alert Message
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Activity State
        Text(
            text = "Activity: $activityState",
            color = Color(0xFFFFCDD2), // Light red
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // SOS Button
        Button(
            onClick = onSosClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6F00) // Deep Orange
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SEND SOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Call Emergency Button
        Button(
            onClick = onCallEmergency,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC62828) // Dark Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CALL EMERGENCY (911)",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // I'm Fine Button
        OutlinedButton(
            onClick = onImFineClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "I'm Fine (Dismiss)",
                fontSize = 16.sp
            )
        }
    }
}
