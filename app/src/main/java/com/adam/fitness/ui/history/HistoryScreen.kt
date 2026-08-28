package com.adam.fitness.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutEntity
import com.adam.fitness.util.PaceFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, units: UnitSystem, onOpen: (Long) -> Unit) {
    val workouts by viewModel.workouts.collectAsState()
    var confirmClearAll by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<WorkoutEntity?>(null) }
    val df = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    if (workouts.isNotEmpty()) {
                        TextButton(onClick = { confirmClearAll = true }) { Text("Clear all") }
                    }
                }
            )
        }
    ) { padding ->
        if (workouts.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                Text("No workouts yet.", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workouts, key = { it.id }) { w ->
                    ElevatedCard(
                        onClick = { onOpen(w.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(w.activityType.name, fontWeight = FontWeight.Bold)
                                Text(df.format(java.util.Date(w.startTime)), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(
                                    "${PaceFormatter.formatDistance(w.distanceMeters, units)}  •  ${PaceFormatter.formatDuration(w.durationMs)}  •  ${PaceFormatter.formatPace(w.avgPaceSecPerKm, units)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { pendingDelete = w }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete all saved workouts.") },
            confirmButton = { TextButton(onClick = { viewModel.clearAll(); confirmClearAll = false }) { Text("Clear all") } },
            dismissButton = { TextButton(onClick = { confirmClearAll = false }) { Text("Cancel") } }
        )
    }

    pendingDelete?.let { w ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete workout?") },
            text = { Text("This workout will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(w); pendingDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}
