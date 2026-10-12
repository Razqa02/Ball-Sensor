package com.example.sensorplayground.sensor

object SensorFilters {

    /**
     * Low-pass filter untuk menghaluskan pembacaan akselerometer
     */
    fun lowPass(current: Float, target: Float, alpha: Float = 0.8f): Float {
        return current * alpha + target * (1.0f - alpha)
    }

    /**
     * Smoothing sirkular untuk azimuth sudut (0 - 360 derajat)
     * Mencegah jarum kompas berputar balik saat melintasi batas 359 -> 0
     */
    fun smoothAngle(current: Float, target: Float, factor: Float = 0.15f): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        var result = current + diff * factor
        if (result < 0f) result += 360f
        if (result >= 360f) result -= 360f
        return result
    }

    /**
     * Rata-rata bergerak (moving average) untuk 5 sampel lux terakhir guna stabilisasi
     */
    fun averageLux(sampleHistory: List<Float>): Float {
        if (sampleHistory.isEmpty()) return 100f
        return sampleHistory.average().toFloat()
    }

    /**
     * Filter histeresis untuk penentuan mode cahaya
     * GELAP saat lux < 40, TERANG/NORMAL saat lux > 80, SANGAT TERANG saat lux > 1000
     */
    fun determineLightMode(lux: Float, currentMode: LightConditionMode): LightConditionMode {
        return when (currentMode) {
            LightConditionMode.DARK -> {
                if (lux > 80f) LightConditionMode.NORMAL else LightConditionMode.DARK
            }
            LightConditionMode.NORMAL -> {
                if (lux < 40f) LightConditionMode.DARK
                else if (lux > 1000f) LightConditionMode.BRIGHT
                else LightConditionMode.NORMAL
            }
            LightConditionMode.BRIGHT -> {
                if (lux < 800f) LightConditionMode.NORMAL else LightConditionMode.BRIGHT
            }
        }
    }

    /**
     * Konversi azimuth (derajat) ke label arah mata angin dalam Bahasa Indonesia
     */
    fun azimuthToCardinalLabel(azimuth: Float): String {
        val norm = (azimuth % 360f + 360f) % 360f
        return when {
            norm >= 337.5f || norm < 22.5f -> "U (Utara)"
            norm >= 22.5f && norm < 67.5f -> "TL (Timur Laut)"
            norm >= 67.5f && norm < 112.5f -> "T (Timur)"
            norm >= 112.5f && norm < 157.5f -> "TG (Tenggara)"
            norm >= 157.5f && norm < 202.5f -> "S (Selatan)"
            norm >= 202.5f && norm < 247.5f -> "BD (Barat Daya)"
            norm >= 247.5f && norm < 292.5f -> "B (Barat)"
            else -> "BL (Barat Laut)"
        }
    }
}
