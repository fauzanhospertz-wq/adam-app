package com.adam.fitness.util

import com.adam.fitness.data.LocationPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Handles distance accumulation with GPS-jump filtering:
 * rejects points with poor accuracy or implausible instantaneous speed.
 */
object DistanceCalculator {

    private const val EARTH_RADIUS_M = 6371000.0
    private const val MAX_ACCEPTABLE_ACCURACY_M = 25f
    private const val MAX_PLAUSIBLE_SPEED_MPS = 12.0 // ~43 km/h, generous ceiling for cycling

    fun haversine(a: LocationPoint, b: LocationPoint): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return EARTH_RADIUS_M * c
    }

    /** Returns true if [next] should be accepted as a valid movement sample relative to [last]. */
    fun isValidSample(last: LocationPoint?, next: LocationPoint): Boolean {
        if (next.accuracy > MAX_ACCEPTABLE_ACCURACY_M) return false
        if (last == null) return true
        val dtSec = (next.timestamp - last.timestamp) / 1000.0
        if (dtSec <= 0) return false
        val dist = haversine(last, next)
        val impliedSpeed = dist / dtSec
        if (impliedSpeed > MAX_PLAUSIBLE_SPEED_MPS * 1.8) return false // hard reject GPS jump
        return true
    }

    fun totalDistanceMeters(points: List<LocationPoint>): Double {
        var total = 0.0
        for (i in 1 until points.size) total += haversine(points[i - 1], points[i])
        return total
    }

    fun elevationGainLoss(points: List<LocationPoint>): Pair<Double, Double> {
        var gain = 0.0
        var loss = 0.0
        for (i in 1 until points.size) {
            val a = points[i - 1].altitude
            val b = points[i].altitude
            if (a != null && b != null) {
                val diff = b - a
                if (diff > 0.5) gain += diff else if (diff < -0.5) loss += -diff
            }
        }
        return gain to loss
    }
}
