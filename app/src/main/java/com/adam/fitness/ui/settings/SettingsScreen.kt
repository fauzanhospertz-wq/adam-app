package com.adam.fitness.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.Sex
import com.adam.fitness.data.ThemeMode
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutDao
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, dao: WorkoutDao) {
    val scope = rememberCoroutineScope()
    val units by viewModel.units.collectAsState(initial = UnitSystem.KM)
    val theme by viewModel.theme.collectAsState(initial = ThemeMode.SYSTEM)
    val autoPause by viewModel.autoPause.collectAsState(initial = true)
    val voice by viewModel.voice.collectAsState(initial = false)
    val keepAwake by viewModel.keepAwake.collectAsState(initial = true)
    val weight by viewModel.weight.collectAsState(initial = 70f)
    val age by viewModel.age.collectAsState(initial = 30)
    val sex by viewModel.sex.collectAsState(initial = Sex.OTHER)

    var confirmClear by remember { mutableStateOf(false) }
    var weightText by remember(weight) { mutableStateOf(weight.toInt().toString()) }
    var ageText by remember(age) { mutableStateOf(age.toString()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection("Units") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = units == UnitSystem.KM, onClick = { viewModel.setUnits(UnitSystem.KM) }, label = { Text("Kilometers") })
                        FilterChip(selected = units == UnitSystem.MILES, onClick = { viewModel.setUnits(UnitSystem.MILES) }, label = { Text("Miles") })
                    }
                }
            }

            item {
                SettingsSection("Appearance") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = theme == ThemeMode.LIGHT, onClick = { viewModel.setTheme(ThemeMode.LIGHT) }, label = { Text("Light") })
                        FilterChip(selected = theme == ThemeMode.DARK, onClick = { viewModel.setTheme(ThemeMode.DARK) }, label = { Text("Dark") })
                        FilterChip(selected = theme == ThemeMode.SYSTEM, onClick = { viewModel.setTheme(ThemeMode.SYSTEM) }, label = { Text("System") })
                    }
                }
            }

            item {
                SettingsSection("Workout") {
                    SettingsToggleRow("Auto Pause", autoPause) { viewModel.setAutoPause(it) }
                    SettingsToggleRow("Voice Announcements", voice) { viewModel.setVoice(it) }
                    SettingsToggleRow("Keep Screen Awake", keepAwake) { viewModel.setKeepAwake(it) }
                }
            }

            item {
                SettingsSection("Personal (used for calorie estimate)") {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it; it.toFloatOrNull()?.let(viewModel::setWeight) },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it; it.toIntOrNull()?.let(viewModel::setAge) },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Sex.entries.forEach { s ->
                            FilterChip(selected = sex == s, onClick = { viewModel.setSex(s) }, label = { Text(s.name) })
                        }
                    }
                }
            }

            item {
                SettingsSection("Data") {
                    TextButton(onClick = { confirmClear = true }) { Text("Clear all workout history") }
                }
            }

            item {
                SettingsSection("Privacy") {
                    Text(
                        "All GPS and workout data stays on this device. ADAM has no account, no cloud backend, and does not transmit your location anywhere.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                SettingsSection("About") {
                    Text("ADAM — GPS Fitness Tracker", style = MaterialTheme.typography.bodyLarge)
                    Text("Version 1.0.0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all workout history?") },
            text = { Text("This permanently deletes every saved workout. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { dao.clearAll() }
                    confirmClear = false
                }) { Text("Clear all") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
