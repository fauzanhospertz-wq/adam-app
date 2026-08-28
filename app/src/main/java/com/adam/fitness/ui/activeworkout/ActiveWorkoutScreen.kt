package com.adam.fitness.ui.activeworkout

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.location.TrackingPhase
import com.adam.fitness.ui.components.BigStat
import com.adam.fitness.ui.components.RouteMapView
import com.adam.fitness.util.PaceFormatter

@Composable
fun ActiveWorkoutScreen(
    activityType: ActivityType,
    units: UnitSystem,
    viewModel: ActiveWorkoutViewModel,
    onFinished: (Long) -> Unit,
    onCancelNoPermission: () -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        permissionGranted = fineGranted
        permissionDenied = !fineGranted
        if (fineGranted) viewModel.start(activityType)
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (android.os.Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        permissionLauncher.launch(perms.toTypedArray())
    }

    val snapshot by viewModel.snapshot.collectAsState()
    val savedId by viewModel.savedWorkoutId.collectAsState()

    LaunchedEffect(savedId) {
        savedId?.let { onFinished(it) }
    }

    if (permissionDenied) {
        PermissionDeniedContent(onBack = onCancelNoPermission)
        return
    }
    if (!permissionGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Requesting location permission…")
        }
        return
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                RouteMapView(points = snapshot.route, modifier = Modifier.fillMaxSize())
                GpsStatusBadge(
                    available = snapshot.gpsAvailable,
                    accuracy = snapshot.gpsAccuracy,
                    autoPaused = snapshot.isAutoPaused,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BigStat("TIME", PaceFormatter.formatDuration(snapshot.elapsedMs))
                    BigStat("DISTANCE", PaceFormatter.formatDistance(snapshot.distanceMeters, units), Modifier)
                }
                Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (activityType == ActivityType.CYCLE) {
                        BigStat("SPEED", PaceFormatter.formatSpeed(snapshot.currentSpeedMps, units))
                        BigStat("AVG SPEED", PaceFormatter.formatSpeed(snapshot.avgSpeedMps, units))
                    } else {
                        BigStat("PACE", PaceFormatter.formatPace(snapshot.currentPaceSecPerKm, units))
                        BigStat("AVG PACE", PaceFormatter.formatPace(snapshot.avgPaceSecPerKm, units))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    BigStat("CALORIES", "${snapshot.calories} kcal")
                    BigStat("ELEV GAIN", "${snapshot.elevationGain.toInt()} m")
                }

                Row(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (snapshot.phase == TrackingPhase.TRACKING) {
                        OutlinedButton(onClick = { viewModel.pause() }, modifier = Modifier.weight(1f).height(56.dp)) {
                            Text("PAUSE", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(onClick = { viewModel.resume() }, modifier = Modifier.weight(1f).height(56.dp)) {
                            Text("RESUME", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { viewModel.finish() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("FINISH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GpsStatusBadge(available: Boolean, accuracy: Float, autoPaused: Boolean, modifier: Modifier = Modifier) {
    val text = when {
        autoPaused -> "Auto-paused"
        !available -> "Acquiring GPS…"
        accuracy > 20f -> "Weak GPS signal"
        else -> "GPS locked"
    }
    Box(modifier) {
        androidx.compose.material3.Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PermissionDeniedContent(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Location permission is required to track your workout with GPS.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go back") }
        }
    }
}
