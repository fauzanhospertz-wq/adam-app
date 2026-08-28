package com.adam.fitness.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class PeriodStats(val distanceM: Double = 0.0, val durationMs: Long = 0, val count: Int = 0)

data class StatsUiState(
    val weekly: PeriodStats = PeriodStats(),
    val monthly: PeriodStats = PeriodStats(),
    val allTimeDistanceM: Double = 0.0,
    val allTimeWorkouts: Int = 0,
    val allTimeDurationMs: Long = 0,
    val longestWorkoutM: Double = 0.0,
    val bestPaceSecPerKm: Double = 0.0,
    val weeklyBuckets: List<Double> = emptyList() // last 7 days distance, oldest first
)

class StatsViewModel(dao: WorkoutDao) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = dao.observeAll().map { all ->
        val now = Calendar.getInstance()
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis

        val weekList = all.filter { it.startTime >= weekStart }
        val monthList = all.filter { it.startTime >= monthStart }

        val buckets = DoubleArray(7)
        val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        for (w in all) {
            val daysAgo = ((todayStart - dayStart(w.startTime)) / 86_400_000L).toInt()
            if (daysAgo in 0..6) buckets[6 - daysAgo] += w.distanceMeters
        }

        StatsUiState(
            weekly = PeriodStats(weekList.sumOf { it.distanceMeters }, weekList.sumOf { it.durationMs }, weekList.size),
            monthly = PeriodStats(monthList.sumOf { it.distanceMeters }, monthList.sumOf { it.durationMs }, monthList.size),
            allTimeDistanceM = all.sumOf { it.distanceMeters },
            allTimeWorkouts = all.size,
            allTimeDurationMs = all.sumOf { it.durationMs },
            longestWorkoutM = all.maxOfOrNull { it.distanceMeters } ?: 0.0,
            bestPaceSecPerKm = all.filter { it.bestPaceSecPerKm > 0 }.minOfOrNull { it.bestPaceSecPerKm } ?: 0.0,
            weeklyBuckets = buckets.toList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    private fun dayStart(ts: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ts; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
