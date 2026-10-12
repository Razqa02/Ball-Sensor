package com.example.sensorplayground.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.sensorplayground.sensor.AppSettings
import com.example.sensorplayground.sensor.ControlMode
import com.example.sensorplayground.sensor.LightControlMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sensor_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val CONTROL_MODE = stringPreferencesKey("control_mode")
        val LIGHT_CONTROL_MODE = stringPreferencesKey("light_control_mode")
        val INVERT_AXES = booleanPreferencesKey("invert_axes")
        val AUTO_THEME = booleanPreferencesKey("auto_theme")
        val MANUAL_DARK_MODE = booleanPreferencesKey("manual_dark_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        val SENSITIVITY = floatPreferencesKey("sensitivity")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val savedModeStr = prefs[PreferencesKeys.CONTROL_MODE] ?: ControlMode.TILT.name
        val mode = try {
            ControlMode.valueOf(savedModeStr)
        } catch (@Suppress("UNUSED_VARIABLE") e: Exception) {
            ControlMode.TILT
        }

        val savedLightModeStr = prefs[PreferencesKeys.LIGHT_CONTROL_MODE] ?: LightControlMode.REALTIME.name
        val lightMode = try {
            LightControlMode.valueOf(savedLightModeStr)
        } catch (@Suppress("UNUSED_VARIABLE") e: Exception) {
            LightControlMode.REALTIME
        }

        AppSettings(
            controlMode = mode,
            lightControlMode = lightMode,
            isInvertAxes = prefs[PreferencesKeys.INVERT_AXES] ?: false,
            isAutoTheme = prefs[PreferencesKeys.AUTO_THEME] ?: true,
            isManualDarkMode = prefs[PreferencesKeys.MANUAL_DARK_MODE] ?: false,
            isSoundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
            isVibrationEnabled = prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            isReduceAnimations = prefs[PreferencesKeys.REDUCE_ANIMATIONS] ?: false,
            isDebugMode = prefs[PreferencesKeys.DEBUG_MODE] ?: false,
            sensitivity = prefs[PreferencesKeys.SENSITIVITY] ?: 1.0f
        )
    }

    suspend fun setControlMode(mode: ControlMode) {
        context.dataStore.edit { it[PreferencesKeys.CONTROL_MODE] = mode.name }
    }

    suspend fun setLightControlMode(mode: LightControlMode) {
        context.dataStore.edit { it[PreferencesKeys.LIGHT_CONTROL_MODE] = mode.name }
    }

    suspend fun setInvertAxes(invert: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.INVERT_AXES] = invert }
    }

    suspend fun setAutoTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_THEME] = enabled }
    }

    suspend fun setManualDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MANUAL_DARK_MODE] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setReduceAnimations(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.REDUCE_ANIMATIONS] = enabled }
    }

    suspend fun setDebugMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEBUG_MODE] = enabled }
    }

    suspend fun setSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[PreferencesKeys.SENSITIVITY] = sensitivity }
    }
}
