package com.adam.fitness.util

import com.adam.fitness.data.UnitSystem

object PaceFormatter {

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    /** distance in meters -> display string honoring unit system */
    fun formatDistance(meters: Double, units: UnitSystem): String {
        val value = if (units == UnitSystem.KM) meters / 1000.0 else meters / 1609.344
        val unit = if (units == UnitSystem.KM) "km" else "mi"
        return String.format("%.2f %s", value, unit)
    }

    /** seconds per km -> "m:ss /km" or "/mi" depending on units (converts appropriately) */
    fun formatPace(secPerKm: Double, units: UnitSystem): String {
        if (secPerKm <= 0 || secPerKm.isInfinite() || secPerKm.isNaN()) return "--:-- /km"
        val secPerUnit = if (units == UnitSystem.KM) secPerKm else secPerKm * 1.609344
        val m = (secPerUnit / 60).toInt()
        val s = (secPerUnit % 60).toInt()
        val label = if (units == UnitSystem.KM) "/km" else "/mi"
        return String.format("%d:%02d %s", m, s, label)
    }

    fun formatSpeed(mps: Double, units: UnitSystem): String {
        val kmh = mps * 3.6
        val value = if (units == UnitSystem.KM) kmh else kmh / 1.609344
        val unit = if (units == UnitSystem.KM) "km/h" else "mph"
        return String.format("%.1f %s", value, unit)
    }

    fun paceSecPerKm(distanceMeters: Double, elapsedMs: Long): Double {
        if (distanceMeters <= 0) return 0.0
        val km = distanceMeters / 1000.0
        return (elapsedMs / 1000.0) / km
    }
}
