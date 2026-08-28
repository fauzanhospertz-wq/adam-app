package com.adam.fitness.location

import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.LocationPoint

data class TrackingSnapshot(
    val phase: TrackingPhase = TrackingPhase.IDLE,
    val activityType: ActivityType = ActivityType.RUN,
    val startTime: Long = 0L,
    val elapsedMs: Long = 0L,
    val movingTimeMs: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedMps: Double = 0.0,
    val avgSpeedMps: Double = 0.0,
    val currentPaceSecPerKm: Double = 0.0,
    val avgPaceSecPerKm: Double = 0.0,
    val bestPaceSecPerKm: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val calories: Int = 0,
    val elevationGain: Double = 0.0,
    val elevationLoss: Double = 0.0,
    val gpsAccuracy: Float = 0f,
    val gpsAvailable: Boolean = false,
    val route: List<LocationPoint> = emptyList(),
    val isAutoPaused: Boolean = false
)
