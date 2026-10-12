package com.example.sensorplayground.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensorplayground.R
import com.example.sensorplayground.sensor.CompassState
import java.util.Locale

@Composable
fun CompassView(
    compassState: CompassState,
    hueColor: Float,
    modifier: Modifier = Modifier
) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = compassState.smoothedAzimuth,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "compassRotation"
    )

    val ringColor = Color.hsl(hue = hueColor, saturation = 0.8f, lightness = 0.5f)

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧭 Kompas Position",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = String.format(Locale.US, "%.0f°", compassState.smoothedAzimuth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ringColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compass Dial Canvas
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    drawCircle(
                        color = ringColor.copy(alpha = 0.6f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4f)
                    )

                    rotate(-animatedAzimuth, pivot = center) {
                        val northNeedle = Path().apply {
                            moveTo(center.x, center.y - radius + 10f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x + 12f, center.y)
                            close()
                        }
                        drawPath(northNeedle, color = Color(0xFFEF4444))

                        val southNeedle = Path().apply {
                            moveTo(center.x, center.y + radius - 10f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x + 12f, center.y)
                            close()
                        }
                        drawPath(southNeedle, color = Color(0xFF94A3B8))
                    }

                    drawCircle(
                        color = Color.White,
                        radius = 7f,
                        center = center
                    )
                }

                Text(
                    text = compassState.directionLabel.take(2).trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = compassState.directionLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (compassState.isLowAccuracy) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.calibration_low_accuracy),
                    fontSize = 11.sp,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
