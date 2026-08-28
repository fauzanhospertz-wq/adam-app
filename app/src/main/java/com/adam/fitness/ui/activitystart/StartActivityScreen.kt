package com.adam.fitness.ui.activitystart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adam.fitness.data.ActivityType

@Composable
fun StartActivityScreen(onPick: (ActivityType) -> Unit, onBack: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Choose an activity", style = MaterialTheme.typography.headlineLarge)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActivityButton("RUN") { onPick(ActivityType.RUN) }
                ActivityButton("WALK") { onPick(ActivityType.WALK) }
                ActivityButton("CYCLE") { onPick(ActivityType.CYCLE) }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ActivityButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}
