package com.taqsiim.cardiologic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taqsiim.cardiologic.ui.theme.cardioSenseColors

@Composable
fun ConnectionStatusCard(isConnected: Boolean) {
    val containerColor = if (isConnected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.cardioSenseColors.errorRed.copy(alpha = 0.1f)
    }

    val contentColor = if (isConnected) {
        MaterialTheme.cardioSenseColors.successGreen
    } else {
        MaterialTheme.cardioSenseColors.errorRed
    }

    val icon = if (isConnected) Icons.Default.CloudDone else Icons.Default.Error
    val statusText = if (isConnected) "Connected to Cloud" else "Device is not connected"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (!isConnected) MaterialTheme.cardioSenseColors.errorRed else MaterialTheme.colorScheme.onSurface
            )

            if (isConnected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.cardioSenseColors.successGreen
                )
            }
        }
    }
}
