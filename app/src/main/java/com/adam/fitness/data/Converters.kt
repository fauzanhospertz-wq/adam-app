package com.adam.fitness.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromActivityType(value: ActivityType): String = value.name

    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)
}
