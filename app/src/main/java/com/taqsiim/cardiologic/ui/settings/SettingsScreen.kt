package com.taqsiim.cardiologic.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taqsiim.cardiologic.service.notification.CriticalAlertNotification
import com.taqsiim.cardiologic.service.notification.MonitoringNotification
import com.taqsiim.cardiologic.ui.alert.CriticalAlertActivity
import com.taqsiim.cardiologic.ui.alert.SOSActivity

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Test Notification Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notification Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test if notifications are working correctly on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val notification = MonitoringNotification(context)
                        notification.update("Test notification - Heart Rate: 75 bpm")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Send Test Notification")
                }
            }
        }

        // Critical Alert Test Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Critical Alert Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test the full-screen critical alert that wakes up the device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Test Critical Alert Notification
                Button(
                    onClick = {
                        val criticalAlert = CriticalAlertNotification(context)
                        criticalAlert.triggerCriticalAlert(
                            heartRate = 135,
                            message = "🚨 TEST ALERT: Tachycardia at rest detected (HR: 135 BPM)\n" +
                                     "This is a test of the critical alert system.",
                            activityState = "SEDENTARY (Test)"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Trigger Critical Alert")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Test Critical Alert Activity Directly
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, CriticalAlertActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(CriticalAlertActivity.EXTRA_HEART_RATE, 140)
                            putExtra(CriticalAlertActivity.EXTRA_MESSAGE,
                                "TEST: Critical heart rate anomaly detected")
                            putExtra(CriticalAlertActivity.EXTRA_ACTIVITY_STATE, "SEDENTARY")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Alert Screen Directly")
                }
            }
        }

        // SOS Screen Test Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SOS Screen Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preview the emergency SOS confirmation screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, SOSActivity::class.java).apply {
                            putExtra("heartRate", 145)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Preview SOS Screen")
                }
            }
        }
    }
}
