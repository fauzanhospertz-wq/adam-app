package com.adam.fitness

import android.app.Application
import com.adam.fitness.data.AppDatabase
import com.adam.fitness.data.SettingsRepository

class ADAMApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
