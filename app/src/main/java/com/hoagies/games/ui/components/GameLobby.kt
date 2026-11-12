package com.hoagies.games.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hoagies.games.data.memory.VariantStats

// Building blocks shared by every game's front screen ("lobby").

/** "Your game is waiting" card with progress and a big Continue button. */
@Composable
fun ContinueCard(title: String, variantLabel: String, progress: Float, onContinue: () -> Unit) {
    val percent = (progress * 100).toInt()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                "$variantLabel · " + if (percent == 0) "Just started" else "$percent% done",
                style = MaterialTheme.typography.bodyLarge,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                drawStopIndicator = {},
            )
            BigButton("Continue", onClick = onContinue, icon = Icons.Rounded.PlayArrow)
        }
    }
}

/** A tappable option card, e.g. a difficulty level or a board. */
@Composable
fun ChoiceCard(title: String, description: String, spokenLabel: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "$spokenLabel. $description" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

/** Finished-game stats. [label] turns a variant id into a display name. */
@Composable
fun ProgressCard(
    stats: List<VariantStats>,
    completedWord: String,
    emptyMessage: String,
    label: (String) -> String,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp).fillMaxWidth()) {
            if (stats.isEmpty()) {
                Text(emptyMessage, style = MaterialTheme.typography.bodyLarge)
                return@Column
            }
            Text(
                "${completedWord.replaceFirstChar { it.uppercase() }}: ${stats.sumOf { it.completed }}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            stats.forEachIndexed { index, s ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                val name = label(s.variant)
                Column(
                    Modifier.clearAndSetSemantics {
                        contentDescription = "$name: ${s.completed} $completedWord. Best time ${formatDurationWords(s.bestSeconds)}."
                    },
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${s.completed} $completedWord · best ${formatClock(s.bestSeconds)} · average ${formatClock(s.averageSeconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Plain-language rules, one short paragraph per step. */
@Composable
fun HowToPlayDialog(title: String, steps: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it", style = MaterialTheme.typography.labelLarge) }
        },
    )
}
