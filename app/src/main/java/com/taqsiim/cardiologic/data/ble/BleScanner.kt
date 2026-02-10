package com.taqsiim.cardiosense.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

data class BleScanState(
    val devices: List<ScannedDevice> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

class BleScanner(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _scanState = MutableStateFlow(BleScanState())
    val scanState: StateFlow<BleScanState> = _scanState.asStateFlow()

    private val discoveredDevices = mutableListOf<ScannedDevice>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResult ->
                val device = scanResult.device
                val rssi = scanResult.rssi

                val displayName = if (device.name.isNullOrBlank()) "Unknown Device" else device.name
                val address = device.address

                updateDevice(displayName, address, rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _scanState.update {
                it.copy(
                    error = "Scan failed with error code: $errorCode",
                    isScanning = false
                )
            }
        }
    }

    private fun updateDevice(name: String, address: String, rssi: Int) {
        val existingIndex = discoveredDevices.indexOfFirst { it.address == address }

        if (existingIndex >= 0) {
            // Update existing device with new RSSI
            discoveredDevices[existingIndex] = discoveredDevices[existingIndex].copy(rssi = rssi)
        } else {
            // Add new device
            discoveredDevices.add(ScannedDevice(name, address, rssi))
        }

        // Sort by RSSI (descending)
        discoveredDevices.sortByDescending { it.rssi }

        _scanState.update { it.copy(devices = discoveredDevices.toList()) }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter == null || scanner == null) {
            _scanState.update { it.copy(error = "Bluetooth not available on this device") }
            return
        }

        if (!hasBluetoothPermission()) {
            _scanState.update { it.copy(error = "Bluetooth permissions required") }
            return
        }

        try {
            // No filters - scan for all BLE devices
            val filters = null

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            _scanState.update { it.copy(isScanning = true, error = null) }

            scanner.startScan(filters, settings, scanCallback)
        } catch (e: Exception) {
            _scanState.update {
                it.copy(
                    error = "Permission error: ${e.message}",
                    isScanning = false
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (hasBluetoothPermission() && scanner != null) {
                scanner.stopScan(scanCallback)
            }
            _scanState.update { it.copy(isScanning = false) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearDevices() {
        discoveredDevices.clear()
        _scanState.update { it.copy(devices = emptyList()) }
    }
}
