package com.example.cardiosense.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    // Use a list to maintain order and update existing devices
    private val discoveredDevices = mutableListOf<DeviceItem>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResult ->
                val device = scanResult.device
                val rssi = scanResult.rssi

                // Handle "Unknown Device" (null or blank name)
                val displayName = if (device.name.isNullOrBlank()) "Unknown Device" else device.name
                val address = device.address

                updateDevice(displayName, address, rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _uiState.update { it.copy(error = "Scan failed with error code: $errorCode", isScanning = false) }
        }
    }

    init {
        // No longer need BroadcastReceiver for BLE scanning
        // loadPairedDevices() // Optional: keep if you still want paired devices
        startScanning()
    }

    private fun updateDevice(name: String, address: String, rssi: Int) {
        val existingIndex = discoveredDevices.indexOfFirst { it.address == address }

        if (existingIndex >= 0) {
             // Update existing device with new RSSI
            discoveredDevices[existingIndex] = discoveredDevices[existingIndex].copy(rssi = rssi)
        } else {
             // Add new device
            discoveredDevices.add(DeviceItem(name, address, rssi))
        }

        // Sort by RSSI (descending)
        discoveredDevices.sortByDescending { it.rssi }

        _uiState.update { it.copy(devices = discoveredDevices.toList()) }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Runtime permissions handled differently below Android 12, but for BLE scanning ACCESS_FINE_LOCATION is usually needed
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter == null || scanner == null) {
            _uiState.update { it.copy(error = "Bluetooth not available on this device") }
            return
        }

        if (!hasBluetoothPermission()) {
            _uiState.update { it.copy(error = "Bluetooth permissions required") }
            return
        }

        try {
            // 1. Remove the Filters! Passing 'null' tells Android: "Give me EVERYTHING you see."
            val filters = null

            // 2. Keep Settings (Low Latency is still good for fast updates)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            _uiState.update { it.copy(isScanning = true, error = null) }

            // 3. Start the Wildcard Scan
            scanner.startScan(filters, settings, scanCallback)

            // Stop after 10s to save battery
            viewModelScope.launch {
                delay(10000)
                stopScanning()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Permission error: ${e.message}", isScanning = false) }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (hasBluetoothPermission() && scanner != null) {
                scanner.stopScan(scanCallback)
            }
             _uiState.update { it.copy(isScanning = false) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}

