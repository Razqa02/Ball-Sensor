package com.example.sensorplayground.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class FeedbackManager(context: Context) {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 75)
    } catch (e: Exception) {
        Log.e("FeedbackManager", "Gagal inisialisasi ToneGenerator", e)
        null
    }

    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val hasVibratorHardware: Boolean
        get() = vibrator?.hasVibrator() == true

    private var lastFeedbackTime = 0L
    private val cooldownMs = 120L

    /**
     * Memicu haptic vibration & sound saat tabrakan
     */
    fun triggerWallCollision(
        speed: Float,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val now = System.currentTimeMillis()
        if (now - lastFeedbackTime < cooldownMs) return
        lastFeedbackTime = now

        Log.d("Haptic", "impact speed=$speed vibrate=$vibrationEnabled sound=$soundEnabled")

        // 1. Suara Tabrakan
        if (soundEnabled && toneGenerator != null) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (e: Exception) {
                Log.e("FeedbackManager", "Gagal memutar bunyi", e)
            }
        }

        // 2. Getaran Haptic
        if (vibrationEnabled && hasVibratorHardware) {
            performVibration(durationMs = 60L, speed = speed)
        }
    }

    /**
     * Uji Getaran Langsung (Tes Getar)
     */
    fun testVibration() {
        Log.d("Haptic", "Manual Test Vibration Triggered")
        if (hasVibratorHardware) {
            performVibration(durationMs = 80L, speed = 1.0f)
        }
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }

    private fun performVibration(durationMs: Long, speed: Float) {
        val vib = vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (vib.hasAmplitudeControl()) {
                    (speed * 180f).toInt().coerceIn(80, 255)
                } else {
                    VibrationEffect.DEFAULT_AMPLITUDE
                }

                val effect = VibrationEffect.createOneShot(durationMs, amplitude)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_MEDIA)
                    vib.vibrate(effect, attributes)
                } else {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    vib.vibrate(effect, audioAttributes)
                }
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("Haptic", "Gagal mengeksekusi getaran", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
