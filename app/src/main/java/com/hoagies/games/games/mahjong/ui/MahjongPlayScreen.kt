package com.hoagies.games.games.mahjong.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.hoagies.games.games.mahjong.engine.BoardLayout
import com.hoagies.games.games.mahjong.engine.MahjongGameState
import com.hoagies.games.games.mahjong.engine.MahjongLayouts
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.BigTouchTarget
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.formatClock
import com.hoagies.games.ui.components.formatDurationWords
import com.hoagies.games.ui.components.rememberHaptics
import kotlinx.coroutines.flow.StateFlow

private enum class PlayDialog { None, Options, NewGame, Restart, HowToPlay }

@Composable
fun MahjongPlayScreen(
    onBack: () -> Unit,
    viewModel: MahjongViewModel = viewModel(factory = MahjongViewModel.Factory),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val images by viewModel.tileImages.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var dialog by rememberSaveable { mutableStateOf(PlayDialog.None) }
    val game = ui.game

    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        onPauseOrDispose { viewModel.onPause() }
    }
    LaunchedEffect(ui.justWon) { if (ui.justWon) haptics.celebrate() }

    Scaffold(
        topBar = {
            HoagiesTopBar(
                title = "Mahjong",
                subtitle = game?.let { "${it.layout.name} · ${it.tilesLeft} tiles left" },
                onBack = onBack,
            ) {
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
            val board: @Composable (Modifier) -> Unit = { m ->
                MahjongBoard(
                    game = game,
                    selected = ui.selected,
                    hint = ui.hint,
                    dimStuckTiles = settings.dimBlockedTiles,
                    images = images,
                    onTileClick = { haptics.tap(); viewModel.tapTile(it) },
                    modifier = m,
                )
            }
            val controls: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusMessage(game, ui, onShuffle = viewModel::shuffle)
                    if (game.isComplete) {
                        BigButton("Play again", onClick = { viewModel.startNewGame(game.layout) }, icon = Icons.Rounded.Add)
                        BigButton("Back to menu", onClick = onBack, primary = false)
                    } else {
                        Toolbar(
                            canUndo = game.history.isNotEmpty(),
                            suggestShuffle = ui.notice == MahjongNotice.NO_MOVES,
                            onUndo = { haptics.tap(); viewModel.undo() },
                            onHint = { haptics.tap(); viewModel.hint() },
                            onShuffle = { haptics.tap(); viewModel.shuffle() },
                        )
                    }
                }
            }

            BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                if (maxWidth > maxHeight) {
                    Row(
                        Modifier.fillMaxSize().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        board(Modifier.weight(1f).fillMaxHeight())
                        Box(Modifier.width(320.dp)) { controls() }
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        board(Modifier.weight(1f).fillMaxWidth())
                        Box(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 4.dp)) { controls() }
                    }
                }
            }
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
            title = "Start this board over?",
            message = "Every tile goes back where it started, in the same arrangement.",
            confirmLabel = "Start over",
            onConfirm = {
                dialog = PlayDialog.None
                viewModel.restart()
            },
            onDismiss = { dialog = PlayDialog.None },
        )
        PlayDialog.HowToPlay -> MahjongHowToPlayDialog(onDismiss = { dialog = PlayDialog.None })
    }

    if (ui.justWon && game != null) {
        WonDialog(
            game = game,
            elapsedSeconds = viewModel.elapsedSeconds.collectAsStateWithLifecycle().value,
            onPlayAgain = { viewModel.startNewGame(game.layout) },
            onBackToMenu = {
                viewModel.dismissWin()
                onBack()
            },
            onDismiss = viewModel::dismissWin,
        )
    }
}

@Composable
private fun StatusMessage(game: MahjongGameState, ui: MahjongUiState, onShuffle: () -> Unit) {
    // Always present and at least two lines tall, so the buttons below never jump.
    val message = when {
        game.isComplete -> "Board cleared! Well done."
        ui.notice == MahjongNotice.NO_MOVES -> "No more matching pairs. Tap Shuffle to mix the tiles — no penalty."
        ui.notice == MahjongNotice.CANNOT_SHUFFLE ->
            "These last tiles can't be matched. Tap Undo to go back a few pairs, or start over from the Menu."
        ui.notice == MahjongNotice.BLOCKED -> "That tile is stuck. Pick one with nothing on top and an open side."
        ui.notice == MahjongNotice.NO_MATCH -> "Those two don't match. Now tap a tile with the same picture."
        ui.hint != null -> "The two green tiles are a match. Tap them both."
        ui.selected != null -> "Now tap a matching tile."
        else -> "Tap two matching tiles to remove them."
    }
    Card(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(" ", style = MaterialTheme.typography.bodyLarge, minLines = 2, modifier = Modifier.alpha(0f).clearAndSetSemantics {})
            Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Toolbar(
    canUndo: Boolean,
    suggestShuffle: Boolean,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onShuffle: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolButton("Undo", Icons.AutoMirrored.Rounded.Undo, onUndo, Modifier.weight(1f), enabled = canUndo)
        ToolButton("Hint", Icons.Rounded.Lightbulb, onHint, Modifier.weight(1f))
        ToolButton("Shuffle", Icons.Rounded.Shuffle, onShuffle, Modifier.weight(1f), highlighted = suggestShuffle)
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    highlighted: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (highlighted) scheme.secondary else scheme.surfaceContainerHigh,
        contentColor = if (highlighted) scheme.onSecondary else scheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.height(BigTouchTarget + 8.dp).alpha(if (enabled) 1f else 0.45f),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("Setting out the tiles…", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OptionsDialog(onHowToPlay: () -> Unit, onNewGame: () -> Unit, onRestart: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton("New game", onClick = onNewGame, icon = Icons.Rounded.Add)
                BigButton("Start this board over", onClick = onRestart, icon = Icons.Rounded.Refresh, primary = false)
                BigButton("How to play", onClick = onHowToPlay, icon = Icons.AutoMirrored.Rounded.HelpOutline, primary = false)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", style = MaterialTheme.typography.labelLarge) } },
    )
}

@Composable
private fun NewGameDialog(onPick: (BoardLayout) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New game", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your current game will be replaced. Pick a board:", style = MaterialTheme.typography.bodyLarge)
                MahjongLayouts.all.forEach { layout ->
                    BigButton("${layout.name} (${layout.level})", onClick = { onPick(layout) }, primary = false)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", style = MaterialTheme.typography.labelLarge) } },
    )
}

@Composable
private fun WonDialog(
    game: MahjongGameState,
    elapsedSeconds: Long,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(56.dp))
        },
        title = { Text("Board cleared!", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wonderful work on ${game.layout.name}.", style = MaterialTheme.typography.bodyLarge)
                Text("Time: ${formatDurationWords(elapsedSeconds)}", style = MaterialTheme.typography.bodyLarge)
                if (game.hintsUsed > 0) Text("Hints used: ${game.hintsUsed}", style = MaterialTheme.typography.bodyLarge)
                if (game.shufflesUsed > 0) Text("Shuffles: ${game.shufflesUsed}", style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = { TextButton(onClick = onPlayAgain) { Text("Play again", style = MaterialTheme.typography.labelLarge) } },
        dismissButton = { TextButton(onClick = onBackToMenu) { Text("Back to menu", style = MaterialTheme.typography.labelLarge) } },
    )
}
