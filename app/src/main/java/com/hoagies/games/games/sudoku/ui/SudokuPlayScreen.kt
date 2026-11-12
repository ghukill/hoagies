package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoagies.games.data.settings.SudokuSettings
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.formatClock
import com.hoagies.games.ui.components.formatDurationWords
import com.hoagies.games.ui.components.rememberHaptics
import kotlinx.coroutines.flow.StateFlow

private enum class PlayDialog { None, Options, NewGame, Restart, HowToPlay }

@Composable
fun SudokuPlayScreen(
    onBack: () -> Unit,
    viewModel: SudokuViewModel = viewModel(factory = SudokuViewModel.Factory),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var dialog by rememberSaveable { mutableStateOf(PlayDialog.None) }
    val game = ui.game

    // The clock only runs while this screen is actually on display; leaving saves the game.
    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        onPauseOrDispose { viewModel.onPause() }
    }
    LaunchedEffect(ui.justSolved) { if (ui.justSolved) haptics.celebrate() }

    Scaffold(
        topBar = {
            HoagiesTopBar(title = "Sudoku", subtitle = game?.difficulty?.label, onBack = onBack) {
                if (settings.showTimer && game != null) Clock(viewModel.elapsedSeconds)
                TextButton(onClick = { dialog = PlayDialog.Options }, modifier = Modifier.padding(end = 4.dp)) {
                    Icon(Icons.Rounded.Menu, contentDescription = null)
                    Text("  Menu", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        if (ui.isLoading || game == null) {
            Loading(Modifier.padding(padding))
        } else {
            PlayContent(
                ui = ui,
                game = game,
                settings = settings,
                onCellClick = { haptics.tap(); viewModel.select(it) },
                onDigit = { haptics.tap(); viewModel.enterDigit(it) },
                onErase = { haptics.tap(); viewModel.erase() },
                onUndo = { haptics.tap(); viewModel.undo() },
                onToggleNotes = { haptics.tap(); viewModel.toggleNotesMode() },
                onHint = { haptics.tap(); viewModel.hint() },
                onRevealMistakes = viewModel::revealMistakes,
                onPlayAgain = { viewModel.startNewGame(game.difficulty) },
                onBack = onBack,
                modifier = Modifier.padding(padding),
            )
        }
    }

    when (dialog) {
        PlayDialog.None -> Unit
        PlayDialog.Options -> OptionsDialog(
            onHowToPlay = { dialog = PlayDialog.HowToPlay },
            onNewGame = { dialog = PlayDialog.NewGame },
            onRestart = { dialog = PlayDialog.Restart },
            onDismiss = { dialog = PlayDialog.None },
        )
        PlayDialog.NewGame -> NewGameDialog(
            onPick = {
                dialog = PlayDialog.None
                viewModel.startNewGame(it)
            },
            onDismiss = { dialog = PlayDialog.None },
        )
        PlayDialog.Restart -> ConfirmDialog(
            title = "Start this puzzle over?",
            message = "All the numbers you've entered will be cleared. The starting numbers stay.",
            confirmLabel = "Start over",
            onConfirm = {
                dialog = PlayDialog.None
                viewModel.restart()
            },
            onDismiss = { dialog = PlayDialog.None },
        )
        PlayDialog.HowToPlay -> HowToPlayDialog(onDismiss = { dialog = PlayDialog.None })
    }

    if (ui.justSolved && game != null) {
        SolvedDialog(
            game = game,
            elapsedSeconds = viewModel.elapsedSeconds.collectAsStateWithLifecycle().value,
            onPlayAgain = { viewModel.startNewGame(game.difficulty) },
            onBackToMenu = {
                viewModel.dismissSolved()
                onBack()
            },
            onDismiss = viewModel::dismissSolved,
        )
    }
}

@Composable
private fun PlayContent(
    ui: SudokuUiState,
    game: SudokuGameState,
    settings: SudokuSettings,
    onCellClick: (Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onRevealMistakes: () -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val board: @Composable (Modifier) -> Unit = { m ->
        SudokuBoard(game, ui.selected, settings, ui.revealMistakes, onCellClick, m)
    }
    val controls: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusMessage(game, ui, settings, onRevealMistakes)
            if (game.isComplete) {
                BigButton("Play another", onClick = onPlayAgain, icon = Icons.Rounded.Add)
                BigButton("Back to menu", onClick = onBack, primary = false)
            } else {
                SudokuToolbar(ui.canUndo, ui.notesMode, onUndo, onToggleNotes, onHint)
                NumberPad(game, ui.notesMode, onDigit, onErase)
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            // Landscape / tablets: board on the left, controls on the right.
            Row(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                board(Modifier.fillMaxHeight())
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { controls() }
            }
        } else {
            // Leave room below the board for the controls; scroll if text is very large.
            val boardSize = minOf(maxWidth - 24.dp, maxHeight - 260.dp).coerceAtLeast(280.dp)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                board(Modifier.size(boardSize))
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) { controls() }
            }
        }
    }
}

@Composable
private fun StatusMessage(game: SudokuGameState, ui: SudokuUiState, settings: SudokuSettings, onReveal: () -> Unit) {
    val mistakesVisible = settings.showMistakes || ui.revealMistakes
    // Always shown, always at least two lines tall: if this appeared and disappeared,
    // the number pad would jump under the player's finger.
    val selected = ui.selected
    val message = when {
        game.isComplete -> "Solved! Well done."
        game.isFilledWithMistakes && mistakesVisible -> "Almost there! Fix the numbers shown in red to finish."
        game.isFilledWithMistakes -> "Every square is filled, but a few numbers aren't right yet."
        selected == null -> "Tap a square, then tap a number."
        game.isGiven(selected) -> "That's a starting number. Tap an empty square."
        ui.notesMode -> "Notes are on: tap numbers to jot small reminders."
        game.values[selected] != 0 -> "Tap a different number to change it, or Erase to clear it."
        else -> "Now tap a number to fill the square."
    }
    Card(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Invisible two-line spacer fixes the height; the message is centred within it.
                Text(" ", style = MaterialTheme.typography.bodyLarge, minLines = 2, modifier = Modifier.alpha(0f).clearAndSetSemantics {})
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (game.isFilledWithMistakes && !mistakesVisible) {
                Spacer(Modifier.height(8.dp))
                BigButton("Show me which ones", onClick = onReveal, primary = false)
            }
        }
    }
}

@Composable
private fun Clock(elapsed: StateFlow<Long>) {
    val seconds by elapsed.collectAsStateWithLifecycle()
    Text(
        formatClock(seconds),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clearAndSetSemantics { contentDescription = "Time: ${formatDurationWords(seconds)}" },
    )
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("Making a fresh puzzle…", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OptionsDialog(onHowToPlay: () -> Unit, onNewGame: () -> Unit, onRestart: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton("New puzzle", onClick = onNewGame, icon = Icons.Rounded.Add)
                BigButton("Start this puzzle over", onClick = onRestart, icon = Icons.Rounded.Refresh, primary = false)
                BigButton("How to play", onClick = onHowToPlay, icon = Icons.AutoMirrored.Rounded.HelpOutline, primary = false)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", style = MaterialTheme.typography.labelLarge) }
        },
    )
}

@Composable
private fun NewGameDialog(onPick: (Difficulty) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New puzzle", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Your current puzzle will be replaced. Pick a level:",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Difficulty.entries.forEach { d ->
                    BigButton(d.label, onClick = { onPick(d) }, primary = false)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
        },
    )
}

@Composable
private fun SolvedDialog(
    game: SudokuGameState,
    elapsedSeconds: Long,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(56.dp),
            )
        },
        title = { Text("You solved it!", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wonderful work on a ${game.difficulty.label} puzzle.", style = MaterialTheme.typography.bodyLarge)
                Text("Time: ${formatDurationWords(elapsedSeconds)}", style = MaterialTheme.typography.bodyLarge)
                if (game.hintsUsed > 0) {
                    Text("Hints used: ${game.hintsUsed}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onPlayAgain) { Text("Play another", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onBackToMenu) { Text("Back to menu", style = MaterialTheme.typography.labelLarge) }
        },
    )
}
