package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.ChoiceCard
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.ContinueCard
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.ProgressCard
import com.hoagies.games.ui.components.SectionHeader

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
                    ContinueCard("Your puzzle is waiting", saved.difficulty.label, saved.progress) { onPlay(null) }
                }

                SectionHeader("Start a new puzzle")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Difficulty.entries.forEach { d ->
                        ChoiceCard(d.label, d.description, spokenLabel = "Start ${d.label} puzzle") {
                            if (state.inProgress != null) pendingNew = d else onPlay(d)
                        }
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
                ProgressCard(
                    stats = state.stats,
                    completedWord = "solved",
                    emptyMessage = "Finish a puzzle and your best times will appear here.",
                    label = { variant -> Difficulty.entries.firstOrNull { it.name == variant }?.label ?: variant },
                )
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
    if (showHelp) SudokuHowToPlayDialog(onDismiss = { showHelp = false })
}
