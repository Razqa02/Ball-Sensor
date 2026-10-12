package com.example.sensorplayground.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sensorplayground.R
import com.example.sensorplayground.sensor.AppSettings
import com.example.sensorplayground.sensor.CompassState
import com.example.sensorplayground.sensor.ControlMode
import com.example.sensorplayground.sensor.GyroState
import com.example.sensorplayground.sensor.LightConditionMode
import com.example.sensorplayground.sensor.LightControlMode
import com.example.sensorplayground.sensor.LightState
import com.example.sensorplayground.sensor.MotionState
import com.example.sensorplayground.sensor.OrbPhysicsState
import com.example.sensorplayground.sensor.SensorAvailability
import com.example.sensorplayground.ui.components.CompassView
import com.example.sensorplayground.ui.components.LightMeterCard
import com.example.sensorplayground.ui.components.OrbCanvas
import com.example.sensorplayground.ui.components.SensorStatusChip
import com.example.sensorplayground.viewmodel.SensorViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SensorViewModel) {

    val availability by viewModel.availabilityState.collectAsStateWithLifecycle()
    val motionState by viewModel.motionState.collectAsStateWithLifecycle()
    val gyroState by viewModel.gyroState.collectAsStateWithLifecycle()
    val lightState by viewModel.lightState.collectAsStateWithLifecycle()
    val compassState by viewModel.compassState.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val orbPhysics by viewModel.orbPhysics.collectAsStateWithLifecycle()
    val isListening by viewModel.sensorController.isListening.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    val themeStatusText = when (settings.lightControlMode) {
        LightControlMode.MANUAL -> {
            val modeStr = if (settings.isManualDarkMode) "Gelap" else "Terang"
            "Manual • $modeStr"
        }
        LightControlMode.REALTIME -> {
            String.format(Locale.US, "Realtime • %.0f lux", lightState.lux)
        }
        LightControlMode.REACTIVE -> {
            val stateStr = if (lightState.lux < 30f) "Tutup (Gelap)" else "Senter (Terang)"
            "Reaktif • $stateStr"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedTabIndex) {
                                    0 -> stringResource(R.string.tab_orb)
                                    1 -> stringResource(R.string.tab_compass)
                                    else -> stringResource(R.string.tab_light)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = themeStatusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Adjust, contentDescription = stringResource(R.string.tab_orb)) },
                    label = { Text(stringResource(R.string.tab_orb)) },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.tab_compass)) },
                    label = { Text(stringResource(R.string.tab_compass)) },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.LightMode, contentDescription = stringResource(R.string.tab_light)) },
                    label = { Text(stringResource(R.string.tab_light)) },
                    alwaysShowLabel = true
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(
                targetState = selectedTabIndex,
                label = "tabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> OrbTabContent(
                        viewModel = viewModel,
                        availability = availability,
                        motionState = motionState,
                        gyroState = gyroState,
                        lightState = lightState,
                        compassState = compassState,
                        isListening = isListening,
                        settings = settings,
                        orbPhysics = orbPhysics
                    )

                    1 -> CompassTabContent(
                        viewModel = viewModel,
                        compassState = compassState,
                        motionState = motionState,
                        gyroState = gyroState,
                        lightState = lightState,
                        hueColor = orbPhysics.hueColor,
                        settings = settings,
                        onShowCalibration = { showCalibrationDialog = true }
                    )

                    2 -> LightTabContent(
                        viewModel = viewModel,
                        lightState = lightState,
                        settings = settings,
                        hasLightSensor = availability.hasLightSensor
                    )
                }
            }
        }
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.calibration_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = stringResource(R.string.calibration_msg))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "♾️ Gerakkan HP membentuk angka 8",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalibrationDialog = false }) {
                    Text(text = stringResource(R.string.close))
                }
            }
        )
    }
}

// TAB 1: ORB
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrbTabContent(
    viewModel: SensorViewModel,
    availability: SensorAvailability,
    motionState: MotionState,
    gyroState: GyroState,
    lightState: LightState,
    compassState: CompassState,
    isListening: Boolean,
    settings: AppSettings,
    orbPhysics: OrbPhysicsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SensorStatusChip(
                title = "MOTION",
                valueText = String.format(Locale.US, "X:%.1f Y:%.1f", motionState.filteredX, motionState.filteredY),
                isAvailable = availability.hasAccelerometer,
                isListening = isListening,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            SensorStatusChip(
                title = "LIGHT",
                valueText = String.format(Locale.US, "%.0f Lux", lightState.lux),
                isAvailable = availability.hasLightSensor,
                isListening = isListening,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            SensorStatusChip(
                title = "COMPASS",
                valueText = String.format(Locale.US, "%.0f°", compassState.smoothedAzimuth),
                isAvailable = availability.hasCompass,
                isListening = isListening,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            SensorStatusChip(
                title = "GYRO",
                valueText = String.format(Locale.US, "P:%.0f R:%.0f", gyroState.virtualPitch, gyroState.virtualRoll),
                isAvailable = availability.hasGyroscope,
                isListening = isListening && settings.controlMode == ControlMode.GYRO,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = settings.controlMode == ControlMode.TILT,
                onClick = { viewModel.setControlMode(ControlMode.TILT) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text(stringResource(R.string.mode_tilt), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = settings.controlMode == ControlMode.GYRO,
                onClick = { viewModel.setControlMode(ControlMode.GYRO) },
                enabled = availability.hasGyroscope,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                val gyroLabel = if (availability.hasGyroscope) stringResource(R.string.mode_gyro) else "${stringResource(R.string.mode_gyro)} (N/A)"
                Text(gyroLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = settings.controlMode == ControlMode.TOUCH,
                onClick = { viewModel.setControlMode(ControlMode.TOUCH) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text(stringResource(R.string.mode_touch), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        AnimatedContent(
            targetState = settings.controlMode,
            label = "modeInstruction"
        ) { mode ->
            val instruction = when (mode) {
                ControlMode.TILT -> stringResource(R.string.instruction_tilt)
                ControlMode.GYRO -> stringResource(R.string.instruction_gyro)
                ControlMode.TOUCH -> stringResource(R.string.instruction_touch)
            }
            Text(
                text = "💡 $instruction",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (settings.controlMode == ControlMode.GYRO && !availability.hasGyroscope) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = stringResource(R.string.gyro_unsupported_card),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        OrbCanvas(
            viewModel = viewModel,
            orbPhysics = orbPhysics,
            controlMode = settings.controlMode,
            glowIntensity = lightState.glowIntensity,
            isReduceAnimations = settings.isReduceAnimations,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎛️ Kontrol Orb & Feedback",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (settings.controlMode != ControlMode.TOUCH) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.invert_axes), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.isInvertAxes,
                            onCheckedChange = { viewModel.setInvertAxes(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Suara", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = settings.isSoundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Getar", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = settings.isVibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.testVibration() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.vibration_test))
                }

                if (!viewModel.feedbackManager.hasVibratorHardware) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.vibration_unsupported),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Text(
                    text = String.format(Locale.US, "Sensitivitas Gerak: %.1fx", settings.sensitivity),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = settings.sensitivity,
                    onValueChange = { viewModel.setSensitivity(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kurangi Animasi", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = settings.isReduceAnimations,
                        onCheckedChange = { viewModel.setReduceAnimations(it) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.resetOrbPosition(400f, 600f) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.settings_reset_orb))
                }
            }
        }
    }
}

// TAB 2: KOMPAS
@Composable
private fun CompassTabContent(
    viewModel: SensorViewModel,
    compassState: CompassState,
    motionState: MotionState,
    gyroState: GyroState,
    lightState: LightState,
    hueColor: Float,
    settings: AppSettings,
    onShowCalibration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        CompassView(
            compassState = compassState,
            hueColor = hueColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧭 Kontrol Kompas & Debug",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onShowCalibration,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.settings_calibrate_compass))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mode Debug Sensor", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = settings.isDebugMode,
                        onCheckedChange = { viewModel.setDebugMode(it) }
                    )
                }

                AnimatedVisibility(
                    visible = settings.isDebugMode,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🛠️ DATA SENSOR MENTAH (DEBUG)",
                            color = Color.Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "Mode Kontrol: %s\nMode Cahaya: %s\nAccel Raw: X=%.2f, Y=%.2f, Z=%.2f\nGyro Raw: X=%.2f, Y=%.2f, Z=%.2f\nCompass Azimuth: Raw=%.1f°, Smooth=%.1f°\nLight: %.1f Lux",
                                settings.controlMode.name,
                                settings.lightControlMode.name,
                                motionState.rawX, motionState.rawY, motionState.rawZ,
                                gyroState.rawX, gyroState.rawY, gyroState.rawZ,
                                compassState.rawAzimuth, compassState.smoothedAzimuth,
                                lightState.lux
                            ),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// TAB 3: CAHAYA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LightTabContent(
    viewModel: SensorViewModel,
    lightState: LightState,
    settings: AppSettings,
    hasLightSensor: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Pilihan 3 Mode Cahaya
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = settings.lightControlMode == LightControlMode.MANUAL,
                onClick = { viewModel.setLightControlMode(LightControlMode.MANUAL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text(stringResource(R.string.light_mode_manual), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = settings.lightControlMode == LightControlMode.REALTIME,
                onClick = { viewModel.setLightControlMode(LightControlMode.REALTIME) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text(stringResource(R.string.light_mode_realtime), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = settings.lightControlMode == LightControlMode.REACTIVE,
                onClick = { viewModel.setLightControlMode(LightControlMode.REACTIVE) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text(stringResource(R.string.light_mode_reactive), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        AnimatedContent(
            targetState = settings.lightControlMode,
            label = "lightInstruction"
        ) { mode ->
            val instruction = when (mode) {
                LightControlMode.MANUAL -> stringResource(R.string.instruction_light_manual)
                LightControlMode.REALTIME -> stringResource(R.string.instruction_light_realtime)
                LightControlMode.REACTIVE -> stringResource(R.string.instruction_light_reactive)
            }
            Text(
                text = "💡 $instruction",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tampilan Utama Berdasarkan Mode Cahaya Terpilih
        when (settings.lightControlMode) {
            LightControlMode.MANUAL -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎨 Mode Tema Manual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pilih tampilan tema aplikasi secara langsung:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = !settings.isManualDarkMode,
                                    onClick = { viewModel.setManualDarkMode(false) }
                                )
                                Text(stringResource(R.string.theme_status_light), fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = settings.isManualDarkMode,
                                    onClick = { viewModel.setManualDarkMode(true) }
                                )
                                Text(stringResource(R.string.theme_status_dark), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            LightControlMode.REALTIME -> {
                LightMeterCard(
                    lightState = lightState,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LightControlMode.REACTIVE -> {
                val isCovered = lightState.lux < 30f
                val cardBgColor by animateColorAsState(
                    targetValue = if (isCovered) Color(0xFF1E293B) else Color(0xFFD97706),
                    animationSpec = tween(durationMillis = 400),
                    label = "reactiveCardBg"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDefaults.cardColors().containerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBgColor)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isCovered) stringResource(R.string.reactive_covered) else stringResource(R.string.reactive_shined),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = String.format(Locale.US, "Intensitas Terdeteksi: %.1f Lux", lightState.lux),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Cobalah menutupi sensor cahaya di bagian atas HP Anda dengan jari (GELAP) atau menyenterinya dengan lampu (TERANG).",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card Tambahan (Simulasi Cahaya jika sensor tidak ada / mode simulasi)
        if (!hasLightSensor || viewModel.isSimulationMode) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = String.format(Locale.US, "%s: %.0f Lux", stringResource(R.string.light_sim_label), lightState.lux),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Slider(
                        value = lightState.lux,
                        onValueChange = { newLux ->
                            viewModel.setSimulatedLightLux(newLux)
                        },
                        valueRange = 0f..2000f
                    )
                }
            }
        }
    }
}
