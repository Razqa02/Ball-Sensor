package com.example.sensorplayground.sensor

import androidx.compose.ui.geometry.Offset

/**
 * Mode kontrol pergerakan Orb
 */
enum class ControlMode {
    TILT,   // Akselerometer (kemiringan HP)
    GYRO,   // Gyroscope (rotasi/kecepatan sudut HP)
    TOUCH   // Layar sentuh (seret & lempar)
}

/**
 * Mode kontrol sensor cahaya / tema
 */
enum class LightControlMode {
    MANUAL,   // Mode Manual (Pilih Gelap / Terang)
    REALTIME, // Mode Realtime (Sensor Lux kontinu)
    REACTIVE  // Mode Sensor Reaktif (Tutup sensor = GELAP, Senter = TERANG)
}

/**
 * Ketersediaan sensor fisik pada perangkat
 */
data class SensorAvailability(
    val hasAccelerometer: Boolean = false,
    val hasLightSensor: Boolean = false,
    val hasCompass: Boolean = false,
    val hasGyroscope: Boolean = false
)

/**
 * Data status Motion Sensor (Akselerometer)
 */
data class MotionState(
    val rawX: Float = 0f,
    val rawY: Float = 0f,
    val rawZ: Float = 9.8f,
    val filteredX: Float = 0f,
    val filteredY: Float = 0f
)

/**
 * Data status Gyroscope Sensor
 */
data class GyroState(
    val rawX: Float = 0f,
    val rawY: Float = 0f,
    val rawZ: Float = 0f,
    val virtualRoll: Float = 0f,
    val virtualPitch: Float = 0f
)

/**
 * Mode pencahayaan berbasis Lux dengan histeresis
 */
enum class LightConditionMode {
    DARK, NORMAL, BRIGHT
}

/**
 * Data status Environment Sensor (Cahaya)
 */
data class LightState(
    val lux: Float = 100f,
    val conditionMode: LightConditionMode = LightConditionMode.NORMAL,
    val glowIntensity: Float = 0.5f
)

/**
 * Data status Position Sensor (Kompas / Magnetometer)
 */
data class CompassState(
    val rawAzimuth: Float = 0f,
    val smoothedAzimuth: Float = 0f,
    val directionLabel: String = "U",
    val accuracy: Int = 3,
    val isLowAccuracy: Boolean = false
)

/**
 * Data fisika dan animasi Orb (Bola)
 */
data class OrbPhysicsState(
    val posX: Float = 0f,
    val posY: Float = 0f,
    val velX: Float = 0f,
    val velY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val hueColor: Float = 180f,
    val isCapturedByTouch: Boolean = false,
    val trailPoints: List<Offset> = emptyList()
)

/**
 * Data Pengaturan Aplikasi
 */
data class AppSettings(
    val controlMode: ControlMode = ControlMode.TILT,
    val lightControlMode: LightControlMode = LightControlMode.REALTIME,
    val isInvertAxes: Boolean = false,
    val isAutoTheme: Boolean = true,
    val isManualDarkMode: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isReduceAnimations: Boolean = false,
    val isDebugMode: Boolean = false,
    val sensitivity: Float = 1.0f
)
