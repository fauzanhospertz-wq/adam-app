package com.adam.fitness.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val todayDistanceM: Double = 0.0,
    val todayWorkouts: Int = 0,
    val weekDistanceM: Double = 0.0,
    val monthDistanceM: Double = 0.0,
    val recent: List<WorkoutEntity> = emptyList()
)

class HomeViewModel(dao: WorkoutDao) : ViewModel() {

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfWeek(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val uiState: StateFlow<HomeUiState> = combine(
        dao.observeSince(startOfMonth()),
        dao.observeRecent(8)
    ) { monthWorkouts, recent ->
        val today = startOfDay()
        val week = startOfWeek()
        val todayList = monthWorkouts.filter { it.startTime >= today }
        val weekList = monthWorkouts.filter { it.startTime >= week }
        HomeUiState(
            todayDistanceM = todayList.sumOf { it.distanceMeters },
            todayWorkouts = todayList.size,
            weekDistanceM = weekList.sumOf { it.distanceMeters },
            monthDistanceM = monthWorkouts.sumOf { it.distanceMeters },
            recent = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
