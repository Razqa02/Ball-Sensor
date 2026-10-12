package com.example.sensorplayground.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorplayground.data.SettingsRepository
import com.example.sensorplayground.sensor.AppSettings
import com.example.sensorplayground.sensor.CompassState
import com.example.sensorplayground.sensor.ControlMode
import com.example.sensorplayground.sensor.GyroState
import com.example.sensorplayground.sensor.LightControlMode
import com.example.sensorplayground.sensor.LightState
import com.example.sensorplayground.sensor.MotionState
import com.example.sensorplayground.sensor.OrbPhysicsState
import com.example.sensorplayground.sensor.SensorAvailability
import com.example.sensorplayground.sensor.SensorController
import com.example.sensorplayground.util.FeedbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    val sensorController = SensorController(application)
    val settingsRepository = SettingsRepository(application)
    val feedbackManager = FeedbackManager(application)

    val availabilityState: StateFlow<SensorAvailability> = sensorController.availabilityFlow
    val motionState: StateFlow<MotionState> = sensorController.motionState
    val gyroState: StateFlow<GyroState> = sensorController.gyroState
    val lightState: StateFlow<LightState> = sensorController.lightState
    val compassState: StateFlow<CompassState> = sensorController.compassState

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _orbPhysics = MutableStateFlow(OrbPhysicsState())
    val orbPhysics: StateFlow<OrbPhysicsState> = _orbPhysics.asStateFlow()

    var isSimulationMode = false
    private var simAccelX = 0f
    private var simAccelY = 0f

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val prevMode = _appSettings.value.controlMode
                _appSettings.value = settings

                if (settings.controlMode != prevMode) {
                    sensorController.setGyroEnabled(settings.controlMode == ControlMode.GYRO)
                }
            }
        }
    }

    fun updateCanvasBounds(width: Float, height: Float, orbRadius: Float) {
        if (width <= 0 || height <= 0) return
        val current = _orbPhysics.value
        if (current.posX == 0f && current.posY == 0f) {
            _orbPhysics.value = current.copy(
                posX = width / 2f,
                posY = height / 2f
            )
        }
    }

    fun resetOrbPosition(width: Float, height: Float) {
        _orbPhysics.value = OrbPhysicsState(
            posX = width / 2f,
            posY = height / 2f,
            velX = 0f,
            velY = 0f,
            scaleX = 1f,
            scaleY = 1f,
            hueColor = compassState.value.smoothedAzimuth
        )
    }

    fun setControlMode(mode: ControlMode) {
        val currentSettings = _appSettings.value
        if (currentSettings.controlMode == mode) return

        _orbPhysics.value = _orbPhysics.value.copy(
            velX = 0f,
            velY = 0f,
            scaleX = 1.15f,
            scaleY = 1.15f,
            isCapturedByTouch = false
        )

        feedbackManager.testVibration()

        viewModelScope.launch {
            settingsRepository.setControlMode(mode)
        }
    }

    fun setLightControlMode(mode: LightControlMode) {
        viewModelScope.launch {
            settingsRepository.setLightControlMode(mode)
        }
    }

    fun setInvertAxes(invert: Boolean) = viewModelScope.launch {
        settingsRepository.setInvertAxes(invert)
    }

    fun updateTouchPositionAndVelocity(
        px: Float,
        py: Float,
        vx: Float = 0f,
        vy: Float = 0f,
        isCaptured: Boolean = true
    ) {
        val current = _orbPhysics.value
        val currentTrail = current.trailPoints.toMutableList()
        if (currentTrail.isEmpty() || (Offset(px, py) - currentTrail.last()).getDistance() > 10f) {
            currentTrail.add(Offset(px, py))
            if (currentTrail.size > 16) {
                currentTrail.removeAt(0)
            }
        }

        _orbPhysics.value = current.copy(
            posX = px,
            posY = py,
            velX = vx,
            velY = vy,
            isCapturedByTouch = isCaptured,
            trailPoints = currentTrail
        )
    }

    fun setSimulatedLightLux(lux: Float) {
        sensorController.updateSimulatedLight(lux)
    }

    fun testVibration() {
        feedbackManager.testVibration()
    }

    /**
     * Integrasi pergerakan fisika Orb tiap frame (60fps)
     */
    fun updatePhysicsFrame(
        dtNanos: Long,
        canvasWidth: Float,
        canvasHeight: Float,
        orbRadius: Float
    ) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return

        val settings = _appSettings.value
        val dt = (dtNanos / 1_000_000_000f).coerceIn(0.005f, 0.05f)

        if (_orbPhysics.value.isCapturedByTouch && settings.controlMode == ControlMode.TOUCH) {
            val current = _orbPhysics.value
            var sx = current.scaleX
            var sy = current.scaleY
            sx += (1.15f - sx) * 0.2f
            sy += (1.15f - sy) * 0.2f
            _orbPhysics.value = current.copy(
                scaleX = sx,
                scaleY = sy,
                hueColor = compassState.value.smoothedAzimuth
            )
            return
        }

        var forceX = 0f
        var forceY = 0f

        when (settings.controlMode) {
            ControlMode.TILT -> {
                forceX = if (isSimulationMode) simAccelX else motionState.value.filteredX
                forceY = if (isSimulationMode) simAccelY else motionState.value.filteredY
            }
            ControlMode.GYRO -> {
                val gyro = gyroState.value
                forceX = (gyro.virtualRoll / 6f) + (gyro.rawY * 1.5f)
                forceY = (gyro.virtualPitch / 6f) + (gyro.rawX * 1.5f)
            }
            ControlMode.TOUCH -> {
                forceX = 0f
                forceY = 0f
            }
        }

        if (settings.isInvertAxes && settings.controlMode != ControlMode.TOUCH) {
            forceX = -forceX
            forceY = -forceY
        }

        val sensitivity = settings.sensitivity * 380f

        var vx = _orbPhysics.value.velX
        var vy = _orbPhysics.value.velY
        var px = _orbPhysics.value.posX
        var py = _orbPhysics.value.posY
        var sx = _orbPhysics.value.scaleX
        var sy = _orbPhysics.value.scaleY

        vx += (-forceX) * sensitivity * dt
        vy += (forceY) * sensitivity * dt

        val friction = 2.2f
        vx *= (1f - friction * dt).coerceIn(0f, 1f)
        vy *= (1f - friction * dt).coerceIn(0f, 1f)

        px += vx * dt
        py += vy * dt

        val minX = orbRadius
        val maxX = canvasWidth - orbRadius
        val minY = orbRadius
        val maxY = canvasHeight - orbRadius

        val restitution = 0.65f
        var collided = false
        var impactSpeed = 0f

        if (px < minX) {
            px = minX
            impactSpeed = abs(vx)
            vx = -vx * restitution
            collided = true
            if (!settings.isReduceAnimations) {
                sx = 0.75f
                sy = 1.25f
            }
        } else if (px > maxX) {
            px = maxX
            impactSpeed = abs(vx)
            vx = -vx * restitution
            collided = true
            if (!settings.isReduceAnimations) {
                sx = 0.75f
                sy = 1.25f
            }
        }

        if (py < minY) {
            py = minY
            impactSpeed = abs(vy)
            vy = -vy * restitution
            collided = true
            if (!settings.isReduceAnimations) {
                sy = 0.75f
                sx = 1.25f
            }
        } else if (py > maxY) {
            py = maxY
            impactSpeed = abs(vy)
            vy = -vy * restitution
            collided = true
            if (!settings.isReduceAnimations) {
                sy = 0.75f
                sx = 1.25f
            }
        }

        if (collided && impactSpeed > 15f) {
            feedbackManager.triggerWallCollision(
                speed = impactSpeed / 100f,
                soundEnabled = settings.isSoundEnabled,
                vibrationEnabled = settings.isVibrationEnabled
            )
        }

        sx += (1f - sx) * 0.15f
        sy += (1f - sy) * 0.15f

        val currentTrail = _orbPhysics.value.trailPoints.toMutableList()
        if (currentTrail.isEmpty() || (Offset(px, py) - currentTrail.last()).getDistance() > 12f) {
            currentTrail.add(Offset(px, py))
            if (currentTrail.size > 16) {
                currentTrail.removeAt(0)
            }
        }

        val currentHue = compassState.value.smoothedAzimuth

        _orbPhysics.value = OrbPhysicsState(
            posX = px,
            posY = py,
            velX = vx,
            velY = vy,
            scaleX = sx,
            scaleY = sy,
            hueColor = currentHue,
            isCapturedByTouch = false,
            trailPoints = currentTrail
        )
    }

    fun setAutoTheme(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoTheme(enabled) }
    fun setManualDarkMode(enabled: Boolean) = viewModelScope.launch { settingsRepository.setManualDarkMode(enabled) }
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setVibrationEnabled(enabled) }
    fun setReduceAnimations(enabled: Boolean) = viewModelScope.launch { settingsRepository.setReduceAnimations(enabled) }
    fun setDebugMode(enabled: Boolean) = viewModelScope.launch { settingsRepository.setDebugMode(enabled) }
    fun setSensitivity(value: Float) = viewModelScope.launch { settingsRepository.setSensitivity(value) }

    fun setSimulatedAccel(x: Float, y: Float) {
        simAccelX = x
        simAccelY = y
    }

    override fun onCleared() {
        sensorController.stop()
        feedbackManager.release()
    }
}
