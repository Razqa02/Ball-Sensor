package com.example.sensorplayground.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensorplayground.R
import com.example.sensorplayground.sensor.LightConditionMode
import com.example.sensorplayground.sensor.LightState
import java.util.Locale

@Composable
fun LightMeterCard(
    lightState: LightState,
    modifier: Modifier = Modifier
) {
    val (conditionLabel, themeColor, icon) = when (lightState.conditionMode) {
        LightConditionMode.DARK -> Triple(stringResource(R.string.light_dark), Color(0xFF1E293B), "🌙")
        LightConditionMode.NORMAL -> Triple(stringResource(R.string.light_normal), Color(0xFF0F766E), "💡")
        LightConditionMode.BRIGHT -> Triple(stringResource(R.string.light_bright), Color(0xFFD97706), "☀️")
    }

    val animatedCardBg by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(durationMillis = 500),
        label = "lightCardBg"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☀️ Light Meter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Ambient Lux Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(animatedCardBg)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = conditionLabel,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f Lux", lightState.lux),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge progress bar
            val progress = (lightState.lux / 2000f).coerceIn(0f, 1f)

            Text(
                text = "Indikator Cahaya Ambient",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = animatedCardBg,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
