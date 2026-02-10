package com.taqsiim.cardiosense.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.taqsiim.cardiosense.ui.theme.CardiosenseTheme
// Ensure you have this import for the colors we defined earlier
// import com.example.cardiosense.ui.theme.cardioSenseColors

@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Use centralized logic from PermissionManager instead of recreating the list here
    val requiredPermissions = remember { PermissionManager.getRequiredPermissions() }

    // 2. State Management
    var areStandardPermissionsGranted by remember { mutableStateOf(false) }
    var isBatteryOptimizationIgnored by remember { mutableStateOf(false) }

    // Helper to check standard permissions
    fun checkStandardPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Helper to check battery optimization (The "God Mode" check)
    fun checkBatteryOptimization(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not needed on very old Android
        }
    }

    // --- 3. Lifecycle Observer (Auto-refresh state when user returns from Settings) ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                areStandardPermissionsGranted = checkStandardPermissions()
                isBatteryOptimizationIgnored = checkBatteryOptimization()

                // If everything is ready, move on
                if (areStandardPermissionsGranted && isBatteryOptimizationIgnored) {
                    onPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- 4. Launchers ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        areStandardPermissionsGranted = checkStandardPermissions()
    }

    // --- 5. UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PhoneHeartIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Let's get connected.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "To monitor your heart continuously, CardioSense needs access to Bluetooth, Location, and Background running.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // --- Status Cards ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Determine label based on Android Version
                val titleLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    "Bluetooth Access"
                } else {
                    "Location Access" // Android 10 requires Location to scan
                }

                val descLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     "Required to connect to strap."
                } else {
                     "Required to scan for Bluetooth devices on Android 10/11."
                }

                PermissionStatusRow(
                    label = titleLabel,
                    icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Icons.Default.Bluetooth else Icons.Default.LocationOn,
                    isGranted = areStandardPermissionsGranted,
                    description = descLabel
                )

                PermissionStatusRow(
                    label = "Always-On Monitoring",
                    icon = Icons.Default.BatteryAlert, // Or BatteryStd
                    isGranted = isBatteryOptimizationIgnored,
                    description = "Prevents the app from stopping when screen is off."
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Action Button ---
        Button(
            onClick = {
                if (!areStandardPermissionsGranted) {
                    permissionLauncher.launch(requiredPermissions)
                } else if (!isBatteryOptimizationIgnored) {
                    // Request Ignore Battery Optimization
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !(areStandardPermissionsGranted && isBatteryOptimizationIgnored)
        ) {
            Text(
                text = when {
                    !areStandardPermissionsGranted -> "Grant Permissions"
                    !isBatteryOptimizationIgnored -> "Allow Background Run"
                    else -> "All Set!"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PhoneHeartIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Simple visualization
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Phone Icon
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            // Connecting Dots
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            // Heart Icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red, // Use MaterialTheme.cardioSenseColors.heartRed if available
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    icon: ImageVector,
    isGranted: Boolean,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary, // Green if granted
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = Color(0xFF4CAF50), // Green
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsPreview() {
    CardiosenseTheme {
        PermissionsScreen(onPermissionsGranted = {})
    }
}
