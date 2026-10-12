package com.example.sensorplayground.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.log10

class SensorController(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val availability = SensorAvailability(
        hasAccelerometer = accelSensor != null,
        hasLightSensor = lightSensor != null,
        hasCompass = rotationVectorSensor != null || (magSensor != null && accelSensor != null),
        hasGyroscope = gyroSensor != null
    )

    private val _availabilityFlow = MutableStateFlow(availability)
    val availabilityFlow: StateFlow<SensorAvailability> = _availabilityFlow.asStateFlow()

    private val _motionState = MutableStateFlow(MotionState())
    val motionState: StateFlow<MotionState> = _motionState.asStateFlow()

    private val _gyroState = MutableStateFlow(GyroState())
    val gyroState: StateFlow<GyroState> = _gyroState.asStateFlow()

    private val _lightState = MutableStateFlow(LightState())
    val lightState: StateFlow<LightState> = _lightState.asStateFlow()

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var isGyroListening = false

    // Pre-allocated arrays
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var hasGravity = false
    private var hasGeomagnetic = false

    // Gyroscope integration variables
    private var lastGyroTimestampNanos: Long = 0L
    private var virtualRoll = 0f  // Sumbu Y
    private var virtualPitch = 0f // Sumbu X

    // Buffer sampel lux
    private val luxSamples = ArrayList<Float>()

    /**
     * Memulai listener sensor (Akselerometer, Cahaya, Kompas).
     * Gyroscope HANYA diaktifkan jika mode GYRO dipilih via setGyroEnabled(true).
     */
    @Synchronized
    fun start(enableGyro: Boolean = false) {
        if (_isListening.value) return

        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            magSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        _isListening.value = true
        setGyroEnabled(enableGyro)
        Log.d("SensorController", "Sensor listeners REGISTERED (Gyro enabled=$enableGyro)")
    }

    /**
     * Mengaktifkan atau menonaktifkan listener Gyroscope secara dinamik
     */
    @Synchronized
    fun setGyroEnabled(enabled: Boolean) {
        if (gyroSensor == null) return

        if (enabled && _isListening.value && !isGyroListening) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
            isGyroListening = true
            lastGyroTimestampNanos = 0L
            Log.d("SensorController", "Gyroscope REGISTERED")
        } else if (!enabled && isGyroListening) {
            sensorManager.unregisterListener(this, gyroSensor)
            isGyroListening = false
            Log.d("SensorController", "Gyroscope UNREGISTERED")
        }
    }

    /**
     * Menghentikan SEMUA listener sensor (Wajib dipanggil di onPause)
     */
    @Synchronized
    fun stop() {
        if (!_isListening.value) return
        sensorManager.unregisterListener(this)
        _isListening.value = false
        isGyroListening = false
        Log.d("SensorController", "ALL Sensor listeners UNREGISTERED")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isListening.value) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val rx = event.values[0]
                val ry = event.values[1]
                val rz = event.values[2]

                val currentMotion = _motionState.value
                val fx = SensorFilters.lowPass(currentMotion.filteredX, rx, 0.75f)
                val fy = SensorFilters.lowPass(currentMotion.filteredY, ry, 0.75f)

                _motionState.value = MotionState(
                    rawX = rx, rawY = ry, rawZ = rz,
                    filteredX = fx, filteredY = fy
                )

                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
                if (rotationVectorSensor == null) {
                    calculateCompassOrientation()
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0] // Rotasi sumbu X (Pitch)
                val gy = event.values[1] // Rotasi sumbu Y (Roll)
                val gz = event.values[2] // Rotasi sumbu Z (Yaw)

                if (lastGyroTimestampNanos != 0L) {
                    val dt = (event.timestamp - lastGyroTimestampNanos) / 1_000_000_000f
                    if (dt in 0.001f..0.1f) {
                        // Deadzone (|omega| < 0.03 rad/s) untuk mencegah drift saat HP diam
                        val cleanGy = if (abs(gy) > 0.03f) gy else 0f
                        val cleanGx = if (abs(gx) > 0.03f) gx else 0f

                        // Integrasi sudut kemiringan virtual
                        virtualRoll = (virtualRoll + Math.toDegrees((cleanGy * dt).toDouble()).toFloat()) * 0.98f
                        virtualPitch = (virtualPitch + Math.toDegrees((cleanGx * dt).toDouble()).toFloat()) * 0.98f

                        virtualRoll = virtualRoll.coerceIn(-45f, 45f)
                        virtualPitch = virtualPitch.coerceIn(-45f, 45f)
                    }
                }
                lastGyroTimestampNanos = event.timestamp

                _gyroState.value = GyroState(
                    rawX = gx, rawY = gy, rawZ = gz,
                    virtualRoll = virtualRoll,
                    virtualPitch = virtualPitch
                )
            }

            Sensor.TYPE_LIGHT -> {
                val rawLux = event.values[0]

                synchronized(luxSamples) {
                    luxSamples.add(rawLux)
                    if (luxSamples.size > 5) luxSamples.removeAt(0)
                }
                val avgLux = SensorFilters.averageLux(luxSamples)

                val currentMode = _lightState.value.conditionMode
                val newMode = SensorFilters.determineLightMode(avgLux, currentMode)

                val clampedLux = avgLux.coerceIn(1f, 10000f)
                val glow = (log10(clampedLux.toDouble()) / 4.0).toFloat().coerceIn(0.2f, 1.0f)

                _lightState.value = LightState(
                    lux = avgLux,
                    conditionMode = newMode,
                    glowIntensity = glow
                )
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                updateCompassState(azimuthDeg)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                hasGeomagnetic = true
                calculateCompassOrientation()
            }
        }
    }

    private fun calculateCompassOrientation() {
        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                updateCompassState(azimuthDeg)
            }
        }
    }

    private fun updateCompassState(rawAzimuth: Float) {
        val currentCompass = _compassState.value
        val smoothed = SensorFilters.smoothAngle(currentCompass.smoothedAzimuth, rawAzimuth, 0.2f)
        val label = SensorFilters.azimuthToCardinalLabel(smoothed)

        _compassState.value = currentCompass.copy(
            rawAzimuth = rawAzimuth,
            smoothedAzimuth = smoothed,
            directionLabel = label
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val isLow = accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
                    accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
            _compassState.value = _compassState.value.copy(
                accuracy = accuracy,
                isLowAccuracy = isLow
            )
        }
    }

    fun updateSimulatedLight(simulatedLux: Float) {
        val currentMode = _lightState.value.conditionMode
        val newMode = SensorFilters.determineLightMode(simulatedLux, currentMode)
        val clampedLux = simulatedLux.coerceIn(1f, 10000f)
        val glow = (log10(clampedLux.toDouble()) / 4.0).toFloat().coerceIn(0.2f, 1.0f)

        _lightState.value = LightState(
            lux = simulatedLux,
            conditionMode = newMode,
            glowIntensity = glow
        )
    }
}
