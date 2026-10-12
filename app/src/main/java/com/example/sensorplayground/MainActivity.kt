package com.example.sensorplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sensorplayground.sensor.ControlMode
import com.example.sensorplayground.sensor.LightConditionMode
import com.example.sensorplayground.sensor.LightControlMode
import com.example.sensorplayground.ui.screens.HomeScreen
import com.example.sensorplayground.ui.theme.SensorPlaygroundTheme
import com.example.sensorplayground.viewmodel.SensorViewModel

class MainActivity : ComponentActivity() {

    private val sensorViewModel: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by sensorViewModel.appSettings.collectAsStateWithLifecycle()
            val lightState by sensorViewModel.lightState.collectAsStateWithLifecycle()

            // Evaluasi mode gelap/terang berdasarkan 3 Mode Cahaya:
            // 1. MANUAL -> Pilihan manual user (Terang vs Gelap)
            // 2. REALTIME -> Nilai Lux realtime kontinu (Histeresis)
            // 3. REACTIVE -> Tutup sensor (<30 lux) = GELAP, Senter/Terang (>=30 lux) = TERANG
            val isDark = when (settings.lightControlMode) {
                LightControlMode.MANUAL -> settings.isManualDarkMode
                LightControlMode.REALTIME -> lightState.conditionMode == LightConditionMode.DARK
                LightControlMode.REACTIVE -> lightState.lux < 30f
            }

            SensorPlaygroundTheme(darkTheme = isDark) {
                HomeScreen(viewModel = sensorViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isGyro = sensorViewModel.appSettings.value.controlMode == ControlMode.GYRO
        sensorViewModel.sensorController.start(enableGyro = isGyro)
    }

    override fun onPause() {
        super.onPause()
        sensorViewModel.sensorController.stop()
    }
}
