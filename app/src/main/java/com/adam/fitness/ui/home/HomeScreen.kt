package com.adam.fitness.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutEntity
import com.adam.fitness.ui.components.StatCard
import com.adam.fitness.util.PaceFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    units: UnitSystem,
    onStartActivity: () -> Unit,
    onOpenWorkout: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("ADAM", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                Text("Your GPS fitness tracker", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("TODAY", PaceFormatter.formatDistance(state.todayDistanceM, units), Modifier.weight(1f))
                    StatCard("WORKOUTS", "${state.todayWorkouts}", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("THIS WEEK", PaceFormatter.formatDistance(state.weekDistanceM, units), Modifier.weight(1f))
                    StatCard("THIS MONTH", PaceFormatter.formatDistance(state.monthDistanceM, units), Modifier.weight(1f))
                }
            }

            item {
                Button(
                    onClick = onStartActivity,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("START ACTIVITY", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("Recent Activities", style = MaterialTheme.typography.titleMedium)
            }

            if (state.recent.isEmpty()) {
                item { Text("No workouts yet — start your first activity!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
            } else {
                items(state.recent) { workout ->
                    RecentWorkoutRow(workout, units) { onOpenWorkout(workout.id) }
                }
            }
        }
    }
}

@Composable
private fun RecentWorkoutRow(workout: WorkoutEntity, units: UnitSystem, onClick: () -> Unit) {
    val df = remember_dateFormat()
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(workout.activityType.name, fontWeight = FontWeight.Bold)
                Text(df.format(java.util.Date(workout.startTime)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(PaceFormatter.formatDistance(workout.distanceMeters, units), fontWeight = FontWeight.Bold)
                Text(PaceFormatter.formatDuration(workout.durationMs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

private fun remember_dateFormat(): SimpleDateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
