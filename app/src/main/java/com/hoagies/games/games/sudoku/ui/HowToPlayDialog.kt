package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to play Sudoku", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "The goal: fill every empty square with a number from 1 to 9.",
                    "Each row, each column, and each of the nine 3×3 boxes must contain every number from 1 to 9 exactly once.",
                    "Tap a square to choose it, then tap a number below the grid.",
                    "Not sure? Turn Notes on to jot down small possible numbers in a square.",
                    "Made a mistake? Tap Undo or Erase. Stuck? Tap Hint — there's no penalty.",
                    "Your puzzle is saved automatically. Leave whenever you like and come back later.",
                ).forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it", style = MaterialTheme.typography.labelLarge) }
        },
    )
}
