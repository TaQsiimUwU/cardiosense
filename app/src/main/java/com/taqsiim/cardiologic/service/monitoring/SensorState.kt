package com.taqsiim.cardiologic.service.monitoring

import com.taqsiim.cardiologic.domain.model.SensorData

class SensorState {
    // 1. IMU Data (Accelerometer)
    private var ax: Float = 0f
    private var ay: Float = 0f
    private var az: Float = 0f

    // 2. IMU Data (Gyroscope)
    private var gx: Float = 0f
    private var gy: Float = 0f
    private var gz: Float = 0f

    // 3. Health & Device Data
    private var temperature: Float = 0f
    private var batteryLevel: Int = 0

    // --- UPDATE FUNCTIONS (Call these from Bluetooth Manager) ---

    fun updateAccel(x: Float, y: Float, z: Float) {
        ax = x; ay = y; az = z
    }

    fun updateGyro(x: Float, y: Float, z: Float) {
        gx = x; gy = y; gz = z
    }

    fun updateTemperature(temp: Float) {
        temperature = temp
    }

    fun updateBattery(level: Int) {
        batteryLevel = level
    }

    // --- CREATION FUNCTION (Called when ECG arrives) ---

    fun createPacket(ecgValue: Float): SensorData {
        return SensorData(
            timestamp = System.currentTimeMillis(),
            ecg = ecgValue,
            // Pass the stored values:
            accelX = ax, accelY = ay, accelZ = az,
            gyroX = gx, gyroY = gy, gyroZ = gz,
            temperature = temperature,
            battery = batteryLevel
        )
    }
}