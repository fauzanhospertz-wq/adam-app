package com.adam.fitness.ui.activeworkout

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.Sex
import com.adam.fitness.data.SettingsRepository
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import com.adam.fitness.location.LocationTrackingService
import com.adam.fitness.location.TrackingPhase
import com.adam.fitness.location.TrackingRepository
import com.adam.fitness.location.TrackingSnapshot
import com.adam.fitness.util.CalorieCalculator
import com.adam.fitness.util.DistanceCalculator
import com.adam.fitness.util.RouteCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ActiveWorkoutViewModel(
    application: Application,
    private val dao: WorkoutDao,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val snapshot: StateFlow<TrackingSnapshot> = TrackingRepository.state

    private val _savedWorkoutId = MutableStateFlow<Long?>(null)
    val savedWorkoutId: StateFlow<Long?> = _savedWorkoutId.asStateFlow()

    fun start(type: ActivityType) {
        val ctx = getApplication<Application>()
        val intent = Intent(ctx, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
            putExtra(LocationTrackingService.EXTRA_ACTIVITY_TYPE, type.name)
        }
        ctx.startForegroundService(intent)
    }

    fun pause() = sendAction(LocationTrackingService.ACTION_PAUSE)
    fun resume() = sendAction(LocationTrackingService.ACTION_RESUME)

    private fun sendAction(action: String) {
        val ctx = getApplication<Application>()
        ctx.startService(Intent(ctx, LocationTrackingService::class.java).apply { this.action = action })
    }

    fun finish() {
        viewModelScope.launch {
            val snap = snapshot.first()
            val units = settingsRepository.unitSystem.first()
            val weight = settingsRepository.weightKg.first()
            val age = settingsRepository.age.first()
            val sex = settingsRepository.sex.first()

            val avgSpeedKmh = snap.avgSpeedMps * 3.6
            val calories = CalorieCalculator.estimateCalories(
                snap.activityType, snap.movingTimeMs, avgSpeedKmh, weight, age, sex
            )
            val (gain, loss) = DistanceCalculator.elevationGainLoss(snap.route)

            val entity = WorkoutEntity(
                activityType = snap.activityType,
                startTime = snap.startTime,
                endTime = System.currentTimeMillis(),
                durationMs = snap.elapsedMs,
                movingTimeMs = snap.movingTimeMs,
                distanceMeters = snap.distanceMeters,
                avgSpeedMps = snap.avgSpeedMps,
                avgPaceSecPerKm = snap.avgPaceSecPerKm,
                bestPaceSecPerKm = snap.bestPaceSecPerKm,
                maxSpeedMps = snap.maxSpeedMps,
                calories = calories,
                elevationGain = gain,
                elevationLoss = loss,
                routeJson = RouteCodec.encode(snap.route)
            )
            val id = dao.insert(entity)
            _savedWorkoutId.value = id

            sendAction(LocationTrackingService.ACTION_STOP)
            TrackingRepository.reset()
        }
    }

    suspend fun currentUnits(): UnitSystem = settingsRepository.unitSystem.first()
}
