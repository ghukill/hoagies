package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.SectionHeader
import com.hoagies.games.ui.components.formatClock
import com.hoagies.games.ui.components.formatDurationWords

/**
 * Sudoku's front door: continue the saved puzzle, start a new one, see your progress.
 * [onPlay] with null means "continue the saved puzzle".
 */
@Composable
fun SudokuLobbyScreen(
    onBack: () -> Unit,
    onPlay: (Difficulty?) -> Unit,
    viewModel: SudokuLobbyViewModel = viewModel(factory = SudokuLobbyViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingNew by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { HoagiesTopBar(title = "Sudoku", onBack = onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                state.inProgress?.let { saved ->
                    Spacer(Modifier.height(8.dp))
                    ContinueCard(saved, onContinue = { onPlay(null) })
                }

                SectionHeader("Start a new puzzle")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Difficulty.entries.forEach { d ->
                        DifficultyCard(d) { if (state.inProgress != null) pendingNew = d else onPlay(d) }
                    }
                    Spacer(Modifier.height(4.dp))
                    BigButton(
                        "How to play",
                        onClick = { showHelp = true },
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        primary = false,
                    )
                }

                SectionHeader("Your progress")
                StatsCard(state)
            }
        }
    }

    pendingNew?.let { d ->
        ConfirmDialog(
            title = "Start a new puzzle?",
            message = "You have a puzzle in progress. Starting a ${d.label} puzzle will replace it.",
            confirmLabel = "Start new",
            dismissLabel = "Keep my puzzle",
            onConfirm = {
                pendingNew = null
                onPlay(d)
            },
            onDismiss = { pendingNew = null },
        )
    }
    if (showHelp) HowToPlayDialog(onDismiss = { showHelp = false })
}

@Composable
private fun ContinueCard(saved: SudokuGameState, onContinue: () -> Unit) {
    val percent = (saved.progress * 100).toInt()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your puzzle is waiting", style = MaterialTheme.typography.titleLarge)
            Text(
                "${saved.difficulty.label} · " + if (percent == 0) "Just started" else "$percent% done",
                style = MaterialTheme.typography.bodyLarge,
            )
            LinearProgressIndicator(
                progress = { saved.progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                drawStopIndicator = {},
            )
            BigButton("Continue", onClick = onContinue, icon = Icons.Rounded.PlayArrow)
        }
    }
}

@Composable
private fun DifficultyCard(difficulty: Difficulty, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "Start ${difficulty.label} puzzle. ${difficulty.description}" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(difficulty.label, style = MaterialTheme.typography.titleLarge)
                Text(
                    difficulty.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun StatsCard(state: SudokuLobbyState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp).fillMaxWidth()) {
            if (state.stats.isEmpty()) {
                Text(
                    "Finish a puzzle and your best times will appear here.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@Column
            }
            Text(
                "Puzzles solved: ${state.totalSolved}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            state.stats.forEachIndexed { index, s ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Column(
                    Modifier.clearAndSetSemantics {
                        contentDescription = "${s.difficulty.label}: ${s.solved} solved. " +
                            "Best time ${formatDurationWords(s.bestSeconds)}."
                    },
                ) {
                    Text(s.difficulty.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${s.solved} solved · best ${formatClock(s.bestSeconds)} · average ${formatClock(s.averageSeconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
