package com.adam.fitness.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "adam_settings")

enum class UnitSystem { KM, MILES }
enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class Sex { MALE, FEMALE, OTHER }

class SettingsRepository(private val context: Context) {

    private object Keys {
        val UNITS = stringPreferencesKey("units")
        val THEME = stringPreferencesKey("theme")
        val AUTO_PAUSE = booleanPreferencesKey("auto_pause")
        val VOICE = booleanPreferencesKey("voice_announcements")
        val KEEP_AWAKE = booleanPreferencesKey("keep_awake")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val AGE = intPreferencesKey("age")
        val SEX = stringPreferencesKey("sex")
    }

    val unitSystem: Flow<UnitSystem> = context.dataStore.data.map {
        UnitSystem.valueOf(it[Keys.UNITS] ?: UnitSystem.KM.name)
    }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.valueOf(it[Keys.THEME] ?: ThemeMode.SYSTEM.name)
    }
    val autoPause: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_PAUSE] ?: true }
    val voiceAnnouncements: Flow<Boolean> = context.dataStore.data.map { it[Keys.VOICE] ?: false }
    val keepScreenAwake: Flow<Boolean> = context.dataStore.data.map { it[Keys.KEEP_AWAKE] ?: true }
    val weightKg: Flow<Float> = context.dataStore.data.map { it[Keys.WEIGHT_KG] ?: 70f }
    val age: Flow<Int> = context.dataStore.data.map { it[Keys.AGE] ?: 30 }
    val sex: Flow<Sex> = context.dataStore.data.map { Sex.valueOf(it[Keys.SEX] ?: Sex.OTHER.name) }

    suspend fun setUnitSystem(v: UnitSystem) = context.dataStore.edit { it[Keys.UNITS] = v.name }
    suspend fun setThemeMode(v: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = v.name }
    suspend fun setAutoPause(v: Boolean) = context.dataStore.edit { it[Keys.AUTO_PAUSE] = v }
    suspend fun setVoiceAnnouncements(v: Boolean) = context.dataStore.edit { it[Keys.VOICE] = v }
    suspend fun setKeepScreenAwake(v: Boolean) = context.dataStore.edit { it[Keys.KEEP_AWAKE] = v }
    suspend fun setWeightKg(v: Float) = context.dataStore.edit { it[Keys.WEIGHT_KG] = v }
    suspend fun setAge(v: Int) = context.dataStore.edit { it[Keys.AGE] = v }
    suspend fun setSex(v: Sex) = context.dataStore.edit { it[Keys.SEX] = v.name }
}
