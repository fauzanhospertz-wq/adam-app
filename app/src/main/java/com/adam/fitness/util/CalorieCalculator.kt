package com.adam.fitness.util

import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.Sex

/** Simple MET-based estimate. Not medically precise — labeled as an estimate in the UI. */
object CalorieCalculator {

    private fun metFor(type: ActivityType, avgSpeedKmh: Double): Double = when (type) {
        ActivityType.WALK -> if (avgSpeedKmh > 6.0) 5.0 else 3.5
        ActivityType.RUN -> when {
            avgSpeedKmh < 8 -> 8.3
            avgSpeedKmh < 10 -> 9.8
            avgSpeedKmh < 12 -> 11.0
            avgSpeedKmh < 14 -> 12.8
            else -> 14.5
        }
        ActivityType.CYCLE -> when {
            avgSpeedKmh < 16 -> 4.0
            avgSpeedKmh < 20 -> 6.8
            avgSpeedKmh < 25 -> 8.0
            else -> 10.0
        }
    }

    fun estimateCalories(
        type: ActivityType,
        durationMs: Long,
        avgSpeedKmh: Double,
        weightKg: Float?,
        age: Int?,
        sex: Sex?
    ): Int {
        val w = weightKg?.takeIf { it > 0 } ?: 70f
        val met = metFor(type, avgSpeedKmh)
        val hours = durationMs / 3_600_000.0
        var kcal = met * w * hours
        // small adjustment for sex/age if provided, kept conservative
        if (sex == Sex.FEMALE) kcal *= 0.95
        if (age != null && age > 50) kcal *= 0.97
        return kcal.toInt().coerceAtLeast(0)
    }
}
