package com.adam.fitness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: ActivityType,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val movingTimeMs: Long,
    val distanceMeters: Double,
    val avgSpeedMps: Double,
    val avgPaceSecPerKm: Double,
    val bestPaceSecPerKm: Double,
    val maxSpeedMps: Double,
    val calories: Int,
    val elevationGain: Double,
    val elevationLoss: Double,
    val routeJson: String
)
