package com.adam.fitness.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val dao: WorkoutDao) : ViewModel() {
    val workouts: StateFlow<List<WorkoutEntity>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(workout: WorkoutEntity) {
        viewModelScope.launch { dao.delete(workout) }
    }

    fun clearAll() {
        viewModelScope.launch { dao.clearAll() }
    }
}
