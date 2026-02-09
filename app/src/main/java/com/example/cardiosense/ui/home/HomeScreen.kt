package com.example.cardiosense.ui.home

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardiosense.ui.theme.CardiosenseTheme
import com.example.cardiosense.ui.theme.cardioSenseColors
import kotlin.math.sin

// Main Dashboard screen
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConnectionStatusBar()
        EcgWaveformCard()
        VitalsRow()
    }
}

@Composable
private fun ConnectionStatusBar() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = MaterialTheme.cardioSenseColors.successGreen
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Connected to Cloud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.cardioSenseColors.successGreen
            )
        }
    }
}

@Composable
private fun EcgWaveformCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Live ECG",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            EcgWaveform(modifier = Modifier.fillMaxWidth().height(180.dp))
        }
    }
}

@Composable
private fun EcgWaveform(modifier: Modifier = Modifier) {
    val baseLine = remember { List(140) { index ->
        val base = sin(index / 6f) * 10f
        val spike = if (index % 28 == 0) 60f else 0f
        base + spike
    } }

    val infiniteTransition = rememberInfiniteTransition(label = "ecg")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shift"
    )

    val ecgBackgroundColor = MaterialTheme.cardioSenseColors.ecgBackground
    val ecgLineColor = MaterialTheme.cardioSenseColors.ecgLineGreen

    Canvas(modifier = modifier.background(ecgBackgroundColor)) {
        val path = Path()
        val midY = size.height / 2
        val step = size.width / (baseLine.size - 1)
        var started = false

        baseLine.forEachIndexed { index, value ->
            val x = (index * step) - (shift * step / 10f)
            if (x < 0f) return@forEachIndexed
            val y = midY - value
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = ecgLineColor,
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun VitalsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VitalCard(label = "Heart Rate", value = "85", unit = "bpm")
        AiInsightCompactCard(status = "Normal")
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.VitalCard(label: String, value: String, unit: String) {
    Card(modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = unit, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.AiInsightCompactCard(status: String) {
    val color = when (status) {
        "Arrhythmia Detected" -> MaterialTheme.cardioSenseColors.errorRed
        "Moderate Risk" -> MaterialTheme.cardioSenseColors.warningYellow
        else -> MaterialTheme.cardioSenseColors.successGreen
    }

    Card(modifier = Modifier.weight(1f)) {
        Column(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Insight",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CardiosenseTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    CardiosenseTheme {
        HomeScreen()
    }
}
