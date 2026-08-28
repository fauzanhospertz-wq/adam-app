package com.adam.fitness.data

data class LocationPoint(
    val lat: Double,
    val lon: Double,
    val timestamp: Long,
    val accuracy: Float,
    val speed: Float,
    val altitude: Double?
)
