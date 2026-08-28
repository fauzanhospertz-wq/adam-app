package com.adam.fitness.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.adam.fitness.MainActivity
import com.adam.fitness.R
import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.LocationPoint
import com.adam.fitness.data.Sex
import com.adam.fitness.data.SettingsRepository
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.util.CalorieCalculator
import com.adam.fitness.util.DistanceCalculator
import com.adam.fitness.util.PaceFormatter
import com.adam.fitness.util.VoiceAnnouncer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    companion object {
        const val ACTION_START = "com.adam.fitness.action.START"
        const val ACTION_PAUSE = "com.adam.fitness.action.PAUSE"
        const val ACTION_RESUME = "com.adam.fitness.action.RESUME"
        const val ACTION_STOP = "com.adam.fitness.action.STOP"
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"

        private const val CHANNEL_ID = "adam_tracking_channel"
        private const val NOTIF_ID = 4201

        private const val AUTO_PAUSE_STATIONARY_MS = 15_000L
        private const val AUTO_PAUSE_SPEED_THRESHOLD_MPS = 0.4
    }

    private val scope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var settingsRepository: SettingsRepository
    private var voiceAnnouncer: VoiceAnnouncer? = null

    private var phase = TrackingPhase.IDLE
    private var activityType = ActivityType.RUN
    private var startTime = 0L
    private var pausedAccumMs = 0L
    private var lastResumeTime = 0L
    private var movingTimeMs = 0L
    private var lastTickTime = 0L

    private val route = mutableListOf<LocationPoint>()
    private var lastAccepted: LocationPoint? = null
    private var lastMovementTime = 0L
    private var isAutoPaused = false

    private var weightKg = 70f
    private var age = 30
    private var sex = Sex.OTHER
    private var units = UnitSystem.KM
    private var autoPauseEnabled = true
    private var voiceEnabled = false

    private var lastAnnouncedKm = 0
    private var lastAnnouncedMinute = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            onNewLocation(loc)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        settingsRepository = SettingsRepository(this)
        createChannel()
        scope.launch {
            weightKg = settingsRepository.weightKg.first()
            age = settingsRepository.age.first()
            sex = settingsRepository.sex.first()
            units = settingsRepository.unitSystem.first()
            autoPauseEnabled = settingsRepository.autoPause.first()
            voiceEnabled = settingsRepository.voiceAnnouncements.first()
            if (voiceEnabled) voiceAnnouncer = VoiceAnnouncer(this@LocationTrackingService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val typeName = intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: ActivityType.RUN.name
                startTracking(ActivityType.valueOf(typeName))
            }
            ACTION_PAUSE -> pauseTracking(manual = true)
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(type: ActivityType) {
        activityType = type
        phase = TrackingPhase.TRACKING
        startTime = System.currentTimeMillis()
        lastResumeTime = startTime
        lastTickTime = startTime
        lastMovementTime = startTime
        pausedAccumMs = 0
        movingTimeMs = 0
        route.clear()
        lastAccepted = null
        lastAnnouncedKm = 0
        lastAnnouncedMinute = 0
        isAutoPaused = false

        startForeground(NOTIF_ID, buildNotification("Starting…", "0.00 km"))
        requestLocationUpdates()
        startTicker()
        publish()
    }

    private fun pauseTracking(manual: Boolean) {
        if (phase != TrackingPhase.TRACKING) return
        phase = TrackingPhase.PAUSED
        pausedAccumMs += System.currentTimeMillis() - lastResumeTime
        if (!manual) isAutoPaused = true
        lastAccepted = null // avoid a fake jump across the pause gap
        publish()
        updateNotification()
    }

    private fun resumeTracking() {
        if (phase != TrackingPhase.PAUSED) return
        phase = TrackingPhase.TRACKING
        lastResumeTime = System.currentTimeMillis()
        lastMovementTime = lastResumeTime
        isAutoPaused = false
        publish()
        updateNotification()
    }

    private fun stopTracking() {
        phase = TrackingPhase.FINISHED
        fusedClient.removeLocationUpdates(locationCallback)
        tickerJob?.cancel()
        publish()
        voiceAnnouncer?.shutdown()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(0f)
            .build()
        try {
            fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
        } catch (_: SecurityException) {
            // Permission was revoked between check and request; surface via snapshot.
        }
    }

    private fun onNewLocation(loc: Location) {
        val point = LocationPoint(
            lat = loc.latitude,
            lon = loc.longitude,
            timestamp = System.currentTimeMillis(),
            accuracy = loc.accuracy,
            speed = if (loc.hasSpeed()) loc.speed else 0f,
            altitude = if (loc.hasAltitude()) loc.altitude else null
        )

        if (phase != TrackingPhase.TRACKING) {
            // still track GPS availability while paused, but do not accumulate
            publish(gpsAvailable = true, accuracy = loc.accuracy)
            return
        }

        val accepted = DistanceCalculator.isValidSample(lastAccepted, point)
        if (accepted) {
            route.add(point)
            lastAccepted = point

            val speedMps = point.speed.toDouble().let { if (it < 0) 0.0 else it }
            if (speedMps > AUTO_PAUSE_SPEED_THRESHOLD_MPS) {
                lastMovementTime = point.timestamp
            }
        }
        publish(gpsAvailable = true, accuracy = loc.accuracy)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(1000)
                if (phase == TrackingPhase.TRACKING) {
                    val now = System.currentTimeMillis()
                    movingTimeMs += (now - lastTickTime)
                    lastTickTime = now

                    if (autoPauseEnabled && now - lastMovementTime > AUTO_PAUSE_STATIONARY_MS) {
                        pauseTracking(manual = false)
                    }
                    checkVoiceMilestones()
                } else {
                    lastTickTime = System.currentTimeMillis()
                }
                if (phase == TrackingPhase.TRACKING || phase == TrackingPhase.PAUSED) {
                    publish()
                    updateNotification()
                }
                if (phase == TrackingPhase.FINISHED) break
            }
        }
    }

    private fun checkVoiceMilestones() {
        if (!voiceEnabled) return
        val distMeters = DistanceCalculator.totalDistanceMeters(route)
        val km = (distMeters / 1000).toInt()
        if (km > lastAnnouncedKm && km > 0) {
            lastAnnouncedKm = km
            voiceAnnouncer?.announceDistanceMilestone(km)
        }
        val elapsedMin = (elapsedMs() / 60000).toInt()
        if (elapsedMin > lastAnnouncedMinute && elapsedMin > 0 && elapsedMin % 5 == 0) {
            lastAnnouncedMinute = elapsedMin
            val avgPace = PaceFormatter.paceSecPerKm(distMeters, movingTimeMs)
            voiceAnnouncer?.announceAveragePace(avgPace, units)
        }
    }

    private fun elapsedMs(): Long {
        val runningPart = if (phase == TrackingPhase.TRACKING) System.currentTimeMillis() - lastResumeTime else 0L
        return pausedAccumMs + runningPart
    }

    private fun publish(gpsAvailable: Boolean = true, accuracy: Float = 0f) {
        val distMeters = DistanceCalculator.totalDistanceMeters(route)
        val (gain, loss) = DistanceCalculator.elevationGainLoss(route)
        val elapsed = elapsedMs()
        val avgSpeed = if (movingTimeMs > 0) distMeters / (movingTimeMs / 1000.0) else 0.0
        val avgPace = PaceFormatter.paceSecPerKm(distMeters, movingTimeMs)
        val currentSpeed = (lastAccepted?.speed ?: 0f).toDouble()
        val currentPace = if (currentSpeed > 0.3) 1000.0 / currentSpeed else 0.0
        val maxSpeed = route.maxOfOrNull { it.speed.toDouble() } ?: 0.0
        val avgSpeedKmh = avgSpeed * 3.6
        val calories = CalorieCalculator.estimateCalories(activityType, movingTimeMs, avgSpeedKmh, weightKg, age, sex)

        // best pace over any accepted 1km-ish window is complex; approximate with avg for now, refined at save time
        TrackingRepository.update(
            TrackingSnapshot(
                phase = phase,
                activityType = activityType,
                startTime = startTime,
                elapsedMs = elapsed,
                movingTimeMs = movingTimeMs,
                distanceMeters = distMeters,
                currentSpeedMps = currentSpeed,
                avgSpeedMps = avgSpeed,
                currentPaceSecPerKm = currentPace,
                avgPaceSecPerKm = avgPace,
                bestPaceSecPerKm = avgPace,
                maxSpeedMps = maxSpeed,
                calories = calories,
                elevationGain = gain,
                elevationLoss = loss,
                gpsAccuracy = accuracy,
                gpsAvailable = gpsAvailable,
                route = route.toList(),
                isAutoPaused = isAutoPaused
            )
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Workout Tracking", NotificationManager.IMPORTANCE_LOW)
            mgr.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(timeText: String, distText: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ADAM is tracking your workout")
            .setContentText("Distance: $distText   Time: $timeText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val distMeters = DistanceCalculator.totalDistanceMeters(route)
        val distText = PaceFormatter.formatDistance(distMeters, units)
        val timeText = PaceFormatter.formatDuration(elapsedMs())
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(NOTIF_ID, buildNotification(timeText, distText))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        fusedClient.removeLocationUpdates(locationCallback)
        voiceAnnouncer?.shutdown()
    }
}
