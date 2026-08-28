package com.adam.fitness.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.ui.components.StatCard
import com.adam.fitness.util.PaceFormatter

@Composable
fun StatsScreen(viewModel: StatsViewModel, units: UnitSystem) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Statistics") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    WeeklyBarChart(state.weeklyBuckets, Modifier.fillMaxSize().padding(16.dp))
                }
            }

            item { SectionHeader("Weekly") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("DISTANCE", PaceFormatter.formatDistance(state.weekly.distanceM, units), Modifier.weight(1f))
                    StatCard("DURATION", PaceFormatter.formatDuration(state.weekly.durationMs), Modifier.weight(1f))
                    StatCard("WORKOUTS", "${state.weekly.count}", Modifier.weight(1f))
                }
            }

            item { SectionHeader("Monthly") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("DISTANCE", PaceFormatter.formatDistance(state.monthly.distanceM, units), Modifier.weight(1f))
                    StatCard("DURATION", PaceFormatter.formatDuration(state.monthly.durationMs), Modifier.weight(1f))
                    StatCard("WORKOUTS", "${state.monthly.count}", Modifier.weight(1f))
                }
            }

            item { SectionHeader("All Time") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("TOTAL DISTANCE", PaceFormatter.formatDistance(state.allTimeDistanceM, units), Modifier.weight(1f))
                    StatCard("TOTAL WORKOUTS", "${state.allTimeWorkouts}", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("TOTAL TIME", PaceFormatter.formatDuration(state.allTimeDurationMs), Modifier.weight(1f))
                    StatCard("LONGEST", PaceFormatter.formatDistance(state.longestWorkoutM, units), Modifier.weight(1f))
                }
            }
            item {
                StatCard("BEST PACE", PaceFormatter.formatPace(state.bestPaceSecPerKm, units), Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun WeeklyBarChart(values: List<Double>, modifier: Modifier = Modifier) {
    val maxVal = (values.maxOrNull() ?: 0.0).coerceAtLeast(0.001)
    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val barWidth = size.width / (values.size * 2f)
        values.forEachIndexed { i, v ->
            val barHeight = (v / maxVal).toFloat() * size.height
            val x = i * (size.width / values.size) + barWidth / 2
            drawRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
