package com.taqsiim.cardiologic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taqsiim.cardiologic.ui.theme.cardioSenseColors
import kotlin.math.sin

@Composable
fun EcgWaveformCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Live ECG",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LiveEcgGraph(modifier = Modifier.fillMaxWidth().height(180.dp))
        }
    }
}

@Composable
fun LiveEcgGraph(modifier: Modifier = Modifier) {
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
