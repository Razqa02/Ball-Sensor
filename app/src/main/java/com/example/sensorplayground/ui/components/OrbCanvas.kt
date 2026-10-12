package com.example.sensorplayground.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.example.sensorplayground.sensor.ControlMode
import com.example.sensorplayground.sensor.OrbPhysicsState
import com.example.sensorplayground.viewmodel.SensorViewModel
import kotlin.math.sqrt

@Composable
fun OrbCanvas(
    viewModel: SensorViewModel,
    orbPhysics: OrbPhysicsState,
    controlMode: ControlMode,
    glowIntensity: Float,
    isReduceAnimations: Boolean,
    modifier: Modifier = Modifier
) {
    val orbRadius = 38f

    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathPulse"
    )

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(canvasWidth, canvasHeight) {
        var lastNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameTimeNanos ->
                val dtNanos = frameTimeNanos - lastNanos
                lastNanos = frameTimeNanos
                viewModel.updatePhysicsFrame(
                    dtNanos = dtNanos,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    orbRadius = orbRadius
                )
            }
        }
    }

    val hue = orbPhysics.hueColor
    val orbMainColor = remember(hue) {
        Color.hsl(hue = hue, saturation = 0.85f, lightness = 0.55f)
    }
    val orbGlowColor = remember(hue, glowIntensity) {
        Color.hsl(hue = hue, saturation = 0.9f, lightness = 0.65f).copy(alpha = glowIntensity.coerceIn(0.2f, 0.85f))
    }

    // Capture state paling stabil dengan rememberUpdatedState
    val currentOrbPhysics by rememberUpdatedState(orbPhysics)
    val currentCanvasWidth by rememberUpdatedState(canvasWidth)
    val currentCanvasHeight by rememberUpdatedState(canvasHeight)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .onSizeChanged { size ->
                canvasWidth = size.width.toFloat()
                canvasHeight = size.height.toFloat()
                viewModel.updateCanvasBounds(canvasWidth, canvasHeight, orbRadius)
            }
            .pointerInput(controlMode) {
                if (controlMode == ControlMode.TOUCH) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()

                        val w = if (currentCanvasWidth > 0) currentCanvasWidth else 1000f
                        val h = if (currentCanvasHeight > 0) currentCanvasHeight else 1000f
                        val clampedX = down.position.x.coerceIn(orbRadius, w - orbRadius)
                        val clampedY = down.position.y.coerceIn(orbRadius, h - orbRadius)

                        viewModel.updateTouchPositionAndVelocity(
                            px = clampedX,
                            py = clampedY,
                            vx = 0f,
                            vy = 0f,
                            isCaptured = true
                        )

                        var lastTimeNanos = System.nanoTime()
                        var touchVel = Offset.Zero
                        val pointerId = down.id

                        while (true) {
                            val event = awaitPointerEvent()
                            val dragChange = event.changes.firstOrNull { it.id == pointerId }
                            if (dragChange == null || !dragChange.pressed) {
                                // Jari dilepas -> Lempar bola (Fling)
                                val flingX = touchVel.x.coerceIn(-3000f, 3000f)
                                val flingY = touchVel.y.coerceIn(-3000f, 3000f)

                                viewModel.updateTouchPositionAndVelocity(
                                    px = currentOrbPhysics.posX,
                                    py = currentOrbPhysics.posY,
                                    vx = flingX,
                                    vy = flingY,
                                    isCaptured = false
                                )
                                break
                            }

                            dragChange.consume()
                            val newX = dragChange.position.x.coerceIn(orbRadius, w - orbRadius)
                            val newY = dragChange.position.y.coerceIn(orbRadius, h - orbRadius)

                            val now = System.nanoTime()
                            val dtSec = ((now - lastTimeNanos) / 1_000_000_000f).coerceAtLeast(0.005f)
                            lastTimeNanos = now

                            val instVx = (newX - currentOrbPhysics.posX) / dtSec
                            val instVy = (newY - currentOrbPhysics.posY) / dtSec

                            touchVel = Offset(
                                x = touchVel.x * 0.4f + instVx * 0.6f,
                                y = touchVel.y * 0.4f + instVy * 0.6f
                            )

                            viewModel.updateTouchPositionAndVelocity(
                                px = newX,
                                py = newY,
                                vx = 0f,
                                vy = 0f,
                                isCaptured = true
                            )
                        }
                    }
                } else {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            viewModel.isSimulationMode = true
                            viewModel.setSimulatedAccel(
                                x = -(startOffset.x - currentCanvasWidth / 2f) / 30f,
                                y = (startOffset.y - currentCanvasHeight / 2f) / 30f
                            )
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.setSimulatedAccel(
                                x = -(dragAmount.x) / 5f,
                                y = (dragAmount.y) / 5f
                            )
                        },
                        onDragEnd = { viewModel.setSimulatedAccel(0f, 0f) },
                        onDragCancel = { viewModel.setSimulatedAccel(0f, 0f) }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val speed = sqrt(orbPhysics.velX * orbPhysics.velX + orbPhysics.velY * orbPhysics.velY)
            val baseScale = if (orbPhysics.isCapturedByTouch) 1.15f else if (isReduceAnimations || speed > 10f) 1f else breathingScale

            val orbCenter = Offset(orbPhysics.posX, orbPhysics.posY)

            // 1. Shadow
            val shadowOffset = Offset(
                x = orbCenter.x - orbPhysics.velX * 0.05f,
                y = orbCenter.y - orbPhysics.velY * 0.05f + 10f
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = orbRadius * 1.1f * baseScale,
                center = shadowOffset
            )

            // 2. Trail
            if (!isReduceAnimations && !orbPhysics.isCapturedByTouch) {
                orbPhysics.trailPoints.forEachIndexed { index, point ->
                    val fraction = (index + 1) / orbPhysics.trailPoints.size.toFloat()
                    val trailRadius = orbRadius * 0.7f * fraction
                    val trailAlpha = 0.35f * fraction
                    drawCircle(
                        color = orbMainColor.copy(alpha = trailAlpha),
                        radius = trailRadius,
                        center = point
                    )
                }
            }

            // 3. Glow
            val activeGlow = if (orbPhysics.isCapturedByTouch) (glowIntensity * 1.4f).coerceAtMost(1.0f) else glowIntensity
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbGlowColor, Color.Transparent),
                    center = orbCenter,
                    radius = orbRadius * 2.8f * activeGlow
                ),
                radius = orbRadius * 2.8f * activeGlow,
                center = orbCenter
            )

            // 4. Orb Body
            scale(
                scaleX = orbPhysics.scaleX * baseScale,
                scaleY = orbPhysics.scaleY * baseScale,
                pivot = orbCenter
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            orbMainColor,
                            orbMainColor.copy(alpha = 0.8f)
                        ),
                        center = Offset(orbCenter.x - orbRadius * 0.3f, orbCenter.y - orbRadius * 0.3f),
                        radius = orbRadius * 1.4f
                    ),
                    radius = orbRadius,
                    center = orbCenter
                )
            }
        }
    }
}
