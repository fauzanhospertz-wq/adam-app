package com.adam.fitness.ui.summary

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutDao
import com.adam.fitness.data.WorkoutEntity
import com.adam.fitness.ui.components.RouteMapView
import com.adam.fitness.ui.components.StatCard
import com.adam.fitness.util.PaceFormatter
import com.adam.fitness.util.RouteCodec
import com.adam.fitness.util.ShareCardGenerator

@Composable
fun WorkoutSummaryScreen(workoutId: Long, dao: WorkoutDao, units: UnitSystem, onDone: () -> Unit) {
    val context = LocalContext.current
    var workout by remember { mutableStateOf<WorkoutEntity?>(null) }

    LaunchedEffect(workoutId) {
        workout = dao.getById(workoutId)
    }

    val w = workout ?: return
    val route = remember(w.routeJson) { RouteCodec.decode(w.routeJson) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Workout complete!", style = MaterialTheme.typography.headlineLarge) }
            item { Text(w.activityType.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

            item {
                RouteMapView(points = route, modifier = Modifier.fillMaxWidth().height(240.dp), followLatest = false)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("DISTANCE", PaceFormatter.formatDistance(w.distanceMeters, units), Modifier.weight(1f))
                    StatCard("DURATION", PaceFormatter.formatDuration(w.durationMs), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("AVG PACE", PaceFormatter.formatPace(w.avgPaceSecPerKm, units), Modifier.weight(1f))
                    StatCard("CALORIES", "${w.calories} kcal", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("ELEV GAIN", "${w.elevationGain.toInt()} m", Modifier.weight(1f))
                    StatCard("ELEV LOSS", "${w.elevationLoss.toInt()} m", Modifier.weight(1f))
                }
            }

            item {
                Button(
                    onClick = {
                        val uri = ShareCardGenerator.generate(context, w, units, route)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share activity"))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("SHARE ACTIVITY") }
            }

            item {
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Done")
                }
            }
        }
    }
}
