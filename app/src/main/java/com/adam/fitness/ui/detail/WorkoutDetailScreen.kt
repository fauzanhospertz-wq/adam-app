package com.adam.fitness.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import com.adam.fitness.ui.components.RouteMapView
import com.adam.fitness.ui.components.StatCard
import com.adam.fitness.util.GpxExporter
import com.adam.fitness.util.PaceFormatter
import com.adam.fitness.util.RouteCodec
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WorkoutDetailScreen(workoutId: Long, dao: WorkoutDao, units: UnitSystem, onBack: () -> Unit) {
    val context = LocalContext.current
    var workout by remember { mutableStateOf<WorkoutEntity?>(null) }

    LaunchedEffect(workoutId) { workout = dao.getById(workoutId) }
    val w = workout ?: return
    val route = remember(w.routeJson) { RouteCodec.decode(w.routeJson) }
    val df = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text(w.activityType.name) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(df.format(java.util.Date(w.startTime)), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
            item { RouteMapView(points = route, modifier = Modifier.fillMaxWidth().height(260.dp), followLatest = false) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("DISTANCE", PaceFormatter.formatDistance(w.distanceMeters, units), Modifier.weight(1f))
                    StatCard("DURATION", PaceFormatter.formatDuration(w.durationMs), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("MOVING TIME", PaceFormatter.formatDuration(w.movingTimeMs), Modifier.weight(1f))
                    StatCard("AVG PACE", PaceFormatter.formatPace(w.avgPaceSecPerKm, units), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("BEST PACE", PaceFormatter.formatPace(w.bestPaceSecPerKm, units), Modifier.weight(1f))
                    StatCard("MAX SPEED", PaceFormatter.formatSpeed(w.maxSpeedMps, units), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("CALORIES", "${w.calories} kcal", Modifier.weight(1f))
                    StatCard("ELEVATION", "+${w.elevationGain.toInt()}m / -${w.elevationLoss.toInt()}m", Modifier.weight(1f))
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val uri = GpxExporter.export(context, w)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/gpx+xml"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export GPX"))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Export GPX") }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Back") }
            }
        }
    }
}
