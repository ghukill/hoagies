package com.hoagies.games.games.mahjong.ui

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
import com.hoagies.games.games.mahjong.engine.BoardLayout
import com.hoagies.games.games.mahjong.engine.MahjongLayouts
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.ChoiceCard
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.ContinueCard
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.ProgressCard
import com.hoagies.games.ui.components.SectionHeader

/** [onPlay] with null means "continue the saved game"; otherwise deal a new game on that board. */
@Composable
fun MahjongLobbyScreen(
    onBack: () -> Unit,
    onPlay: (BoardLayout?) -> Unit,
    viewModel: MahjongLobbyViewModel = viewModel(factory = MahjongLobbyViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingNew by rememberSaveable { mutableStateOf<String?>(null) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { HoagiesTopBar(title = "Mahjong Solitaire", onBack = onBack) }) { padding ->
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
                    ContinueCard("Your game is waiting", saved.layout.name, saved.progress) { onPlay(null) }
                }

                SectionHeader("Choose a board")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MahjongLayouts.all.forEach { layout ->
                        ChoiceCard(
                            title = layout.name,
                            description = "${layout.level} · ${layout.size} tiles. ${layout.description}",
                            spokenLabel = "Start ${layout.name}",
                        ) {
                            if (state.inProgress != null) pendingNew = layout.id else onPlay(layout)
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
                    completedWord = "cleared",
                    emptyMessage = "Clear a board and your best times will appear here.",
                    label = { id -> MahjongLayouts.find(id)?.name ?: id },
                )
            }
        }
    }

    pendingNew?.let(MahjongLayouts::find)?.let { layout ->
        ConfirmDialog(
            title = "Start a new game?",
            message = "You have a game in progress. Starting ${layout.name} will replace it.",
            confirmLabel = "Start new",
            dismissLabel = "Keep my game",
            onConfirm = {
                pendingNew = null
                onPlay(layout)
            },
            onDismiss = { pendingNew = null },
        )
    }
    if (showHelp) MahjongHowToPlayDialog(onDismiss = { showHelp = false })
}
