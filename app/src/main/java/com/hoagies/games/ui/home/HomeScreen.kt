package com.hoagies.games.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoagies.games.games.GameInfo
import com.hoagies.games.games.GameRegistry
import com.hoagies.games.ui.components.HoagiesTopBar
import java.time.LocalTime

@Composable
fun HomeScreen(
    onOpenGame: (route: Any) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val inProgress by viewModel.gamesInProgress.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HoagiesTopBar(title = "Hoagies") {
                TextButton(onClick = onOpenSettings, modifier = Modifier.padding(end = 4.dp)) {
                    Icon(Icons.Rounded.Settings, contentDescription = null)
                    Text("  Settings", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Welcome() }
                items(GameRegistry.games, key = { it.id }) { game ->
                    GameCard(game, inProgress = game.id in inProgress, onClick = { onOpenGame(game.route) })
                }
                item { ComingSoonCard(GameRegistry.comingSoon) }
            }
        }
    }
}

@Composable
private fun Welcome() {
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            else -> "Good evening!"
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(greeting, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(
            "Pick a game to play. Your progress is saved automatically.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SafetyNote()
    }
}

/** One calm sentence rather than a row of chips, so it wraps gracefully at any text size. */
@Composable
private fun SafetyNote() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                "No ads, no purchases, no internet.",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun GameCard(game: GameInfo, inProgress: Boolean, onClick: () -> Unit) {
    val description = buildString {
        append(game.title)
        append(". ")
        append(game.tagline)
        if (inProgress) append(" You have a game in progress.")
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary) {
                Icon(
                    game.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(10.dp).size(36.dp),
                )
            }
            Column(Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)) {
                Text(game.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(game.tagline, style = MaterialTheme.typography.bodyMedium)
                if (inProgress) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "▶  Continue your game",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun ComingSoonCard(titles: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("More games on the way", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                titles.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
