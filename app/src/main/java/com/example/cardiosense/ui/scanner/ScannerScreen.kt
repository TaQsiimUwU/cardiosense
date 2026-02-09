package com.example.cardiosense.ui.scanner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardiosense.ui.theme.CardiosenseTheme
import com.example.cardiosense.ui.theme.cardioSenseColors
import kotlin.math.max

private data class DeviceItem(
    val name: String,
    val rssi: Int
)

@Composable
fun ScannerScreen() {
    val devices = remember {
        listOf(
            DeviceItem("CardioSense Device #123", -48),
            DeviceItem("CardioSense Device #214", -62),
            DeviceItem("CardioSense Device #351", -78)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Scanning for your chest strap",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "UUID: 0x180D",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RadarAnimation(modifier = Modifier.fillMaxWidth().height(200.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(devices) { device ->
                DeviceRow(device = device)
            }
        }
    }
}

@Composable
private fun RadarAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val radius = size.minDimension / 2

        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.cardioSenseColors.scannerCenter, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalStrengthBars(rssi = device.rssi)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${device.rssi} dBm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(onClick = { }) {
                Icon(imageVector = Icons.Default.Link, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Connect")
            }
        }
    }
}

@Composable
private fun SignalStrengthBars(rssi: Int) {
    val level = when {
        rssi >= -55 -> 4
        rssi >= -65 -> 3
        rssi >= -75 -> 2
        else -> 1
    }
    Row(verticalAlignment = Alignment.Bottom) {
        for (i in 1..4) {
            val height = max(6, i * 6)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(height.dp)
                    .background(
                        if (i <= level) MaterialTheme.cardioSenseColors.signalStrengthGood
                        else MaterialTheme.cardioSenseColors.signalStrengthPoor
                    )
                    .alpha(if (i <= level) 1f else 0.6f)
            )
            if (i < 4) Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScannerScreenPreview() {
    CardiosenseTheme {
        ScannerScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScannerScreenDarkPreview() {
    CardiosenseTheme {
        ScannerScreen()
    }
}
