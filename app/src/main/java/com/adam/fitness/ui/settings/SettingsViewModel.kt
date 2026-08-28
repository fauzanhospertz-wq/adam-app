package com.adam.fitness.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.fitness.data.Sex
import com.adam.fitness.data.SettingsRepository
import com.adam.fitness.data.ThemeMode
import com.adam.fitness.data.UnitSystem
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    val units = repo.unitSystem
    val theme = repo.themeMode
    val autoPause = repo.autoPause
    val voice = repo.voiceAnnouncements
    val keepAwake = repo.keepScreenAwake
    val weight = repo.weightKg
    val age = repo.age
    val sex = repo.sex

    fun setUnits(v: UnitSystem) = viewModelScope.launch { repo.setUnitSystem(v) }
    fun setTheme(v: ThemeMode) = viewModelScope.launch { repo.setThemeMode(v) }
    fun setAutoPause(v: Boolean) = viewModelScope.launch { repo.setAutoPause(v) }
    fun setVoice(v: Boolean) = viewModelScope.launch { repo.setVoiceAnnouncements(v) }
    fun setKeepAwake(v: Boolean) = viewModelScope.launch { repo.setKeepScreenAwake(v) }
    fun setWeight(v: Float) = viewModelScope.launch { repo.setWeightKg(v) }
    fun setAge(v: Int) = viewModelScope.launch { repo.setAge(v) }
    fun setSex(v: Sex) = viewModelScope.launch { repo.setSex(v) }
}
