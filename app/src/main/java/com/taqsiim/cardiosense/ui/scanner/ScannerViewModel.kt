package com.taqsiim.cardiosense.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taqsiim.cardiosense.data.ble.BleScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceItem(
    val name: String,
    val address: String,
    val rssi: Int
)

data class ScannerUiState(
    val devices: List<DeviceItem> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val bleScanner = BleScanner(application.applicationContext)

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        // Observe BLE scanner state and map to UI state
        viewModelScope.launch {
            bleScanner.scanState.collect { scanState ->
                _uiState.update {
                    ScannerUiState(
                        devices = scanState.devices.map { scannedDevice ->
                            DeviceItem(
                                name = scannedDevice.name,
                                address = scannedDevice.address,
                                rssi = scannedDevice.rssi
                            )
                        },
                        isScanning = scanState.isScanning,
                        error = scanState.error
                    )
                }
            }
        }

        // Start scanning automatically
        startScanning()
    }

    fun startScanning() {
        bleScanner.startScanning()

        // Auto-stop after 10 seconds to save battery
        viewModelScope.launch {
            delay(10000)
            stopScanning()
        }
    }

    fun stopScanning() {
        bleScanner.stopScanning()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}

