package com.taqsiim.cardiosense.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    // UI State
    private val _isDeviceConnected = MutableStateFlow(false)
    val isDeviceConnected = _isDeviceConnected.asStateFlow()

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate = _heartRate.asStateFlow()

    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val _temperature = MutableStateFlow<Float?>(null)
    val temperature = _temperature.asStateFlow()

    private val _detectedDiseases = MutableStateFlow<List<String>>(emptyList())
    val detectedDiseases = _detectedDiseases.asStateFlow()
}
