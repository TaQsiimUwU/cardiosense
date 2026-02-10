package com.taqsiim.cardiosense.domain.model

data class SensorData(
    val timestamp: Long ,
    val ecg: Float,
    val accelX: Float, val accelY: Float, val accelZ: Float,
    val gyroX: Float, val gyroY: Float, val gyroZ: Float,
    val temperature: Float,
    val battery: Int,
)
