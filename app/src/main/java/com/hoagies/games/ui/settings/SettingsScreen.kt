package com.hoagies.games.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoagies.games.BuildConfig
import com.hoagies.games.data.settings.TextSize
import com.hoagies.games.data.settings.ThemeMode
import com.hoagies.games.ui.components.BigButton
import com.hoagies.games.ui.components.BigTouchTarget
import com.hoagies.games.ui.components.ConfirmDialog
import com.hoagies.games.ui.components.HoagiesTopBar
import com.hoagies.games.ui.components.SectionHeader

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var confirmErase by rememberSaveable { mutableStateOf(false) }
    var erased by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { HoagiesTopBar(title = "Settings", onBack = onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                SectionHeader("Text size")
                Text(
                    "This is how text will look in Hoagies.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ChoiceGroup(
                    options = TextSize.entries,
                    selected = settings.textSize,
                    label = { it.label },
                    onSelect = { size -> viewModel.update { it.copy(textSize = size) } },
                )

                SectionHeader("Appearance")
                ChoiceGroup(
                    options = ThemeMode.entries,
                    selected = settings.themeMode,
                    label = { it.label },
                    onSelect = { mode -> viewModel.update { it.copy(themeMode = mode) } },
                )
                SwitchRow(
                    title = "High contrast",
                    description = "Stronger colours and bolder lines. Easier to see.",
                    checked = settings.highContrast,
                    onChange = { on -> viewModel.update { it.copy(highContrast = on) } },
                )

                SectionHeader("Feel")
                SwitchRow(
                    title = "Vibrate on tap",
                    description = "A tiny buzz when you press a button.",
                    checked = settings.haptics,
                    onChange = { on -> viewModel.update { it.copy(haptics = on) } },
                )

                SectionHeader("Sudoku")
                val sudoku = settings.sudoku
                SwitchRow(
                    title = "Show mistakes right away",
                    description = "Wrong numbers turn red as soon as you enter them.",
                    checked = sudoku.showMistakes,
                    onChange = { on -> viewModel.update { it.copy(sudoku = it.sudoku.copy(showMistakes = on)) } },
                )
                SwitchRow(
                    title = "Highlight matching numbers",
                    description = "Tap a square to see every other square with the same number.",
                    checked = sudoku.highlightSameNumbers,
                    onChange = { on ->
                        viewModel.update { it.copy(sudoku = it.sudoku.copy(highlightSameNumbers = on)) }
                    },
                )
                SwitchRow(
                    title = "Tidy up notes automatically",
                    description = "When you place a number, remove it from notes in the same row, column and box.",
                    checked = sudoku.autoRemoveNotes,
                    onChange = { on -> viewModel.update { it.copy(sudoku = it.sudoku.copy(autoRemoveNotes = on)) } },
                )
                SwitchRow(
                    title = "Show the clock",
                    description = "Off by default — there's never any rush.",
                    checked = sudoku.showTimer,
                    onChange = { on -> viewModel.update { it.copy(sudoku = it.sudoku.copy(showTimer = on)) } },
                )

                SectionHeader("Our promise")
                PromiseCard()

                SectionHeader("Saved games and scores")
                Text(
                    if (erased) "Everything has been erased." else
                        "Games in progress and your best times are kept on this phone only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                BigButton(
                    text = "Erase saved games and scores",
                    icon = Icons.Rounded.DeleteForever,
                    primary = false,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { confirmErase = true },
                )

                Spacer(Modifier.height(24.dp))
                Text(
                    "Hoagies version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (confirmErase) {
        ConfirmDialog(
            title = "Erase everything?",
            message = "This removes every game in progress and all your best times. It can't be undone. Your settings stay as they are.",
            confirmLabel = "Erase",
            onConfirm = {
                viewModel.eraseAllProgress()
                erased = true
                confirmErase = false
            },
            onDismiss = { confirmErase = false },
        )
    }
}

@Composable
private fun <T> ChoiceGroup(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = BigTouchTarget)
                    .selectable(selected = option == selected, role = Role.RadioButton, onClick = { onSelect(option) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = option == selected, onClick = null)
                Text(label(option), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = BigTouchTarget)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun PromiseCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(Modifier.padding(20.dp)) {
            Icon(
                Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp),
            )
            Column(Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "No ads. Ever.",
                    "Nothing to buy. Every game is free and complete.",
                    "No internet. The app isn't allowed to go online, so nothing you do leaves this phone.",
                    "No pop-ups, no countdowns, no pressure.",
                ).forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}
