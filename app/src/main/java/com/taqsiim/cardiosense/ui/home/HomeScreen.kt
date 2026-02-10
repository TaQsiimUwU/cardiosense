package com.taqsiim.cardiosense.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taqsiim.cardiosense.ui.components.AiDiseaseList
import com.taqsiim.cardiosense.ui.components.ConnectionStatusCard
import com.taqsiim.cardiosense.ui.components.EcgWaveformCard
import com.taqsiim.cardiosense.ui.components.VitalCard
import com.taqsiim.cardiosense.ui.theme.CardiosenseTheme

// Main Dashboard screen
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val isConnected by viewModel.isDeviceConnected.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val detectedDiseases by viewModel.detectedDiseases.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConnectionStatusCard(isConnected = isConnected)

        EcgWaveformCard()

        VitalCard(
            label = "Heart Rate",
            value = heartRate?.toString() ?: "-",
            unit = "bpm",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VitalCard(
                label = "Battery",
                value = batteryLevel?.toString() ?: "-",
                unit = "%",
                modifier = Modifier.weight(1f)
            )
            VitalCard(
                label = "Temperature",
                value = temperature?.let { "%.1f".format(it) } ?: "-",
                unit = "°C",
                modifier = Modifier.weight(1f)
            )
        }

        AiDiseaseList(detectedDiseases = detectedDiseases)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CardiosenseTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    CardiosenseTheme {
        HomeScreen()
    }
}
