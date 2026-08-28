package com.adam.fitness.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide bridge between the foreground Service (source of truth for
 * GPS tracking) and the UI layer. Avoids binding boilerplate for a
 * single-consumer, single-producer relationship within one app process.
 */
object TrackingRepository {
    private val _state = MutableStateFlow(TrackingSnapshot())
    val state: StateFlow<TrackingSnapshot> = _state.asStateFlow()

    fun update(snapshot: TrackingSnapshot) {
        _state.value = snapshot
    }

    fun reset() {
        _state.value = TrackingSnapshot()
    }
}
