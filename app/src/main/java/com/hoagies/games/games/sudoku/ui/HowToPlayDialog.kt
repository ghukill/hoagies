package com.hoagies.games.games.sudoku.ui

import androidx.compose.runtime.Composable
import com.hoagies.games.ui.components.HowToPlayDialog

private val steps = listOf(
    "The goal: fill every empty square with a number from 1 to 9.",
    "Each row, each column, and each of the nine 3×3 boxes must contain every number from 1 to 9 exactly once.",
    "Tap a square to choose it, then tap a number below the grid.",
    "Not sure? Turn Notes on to jot down small possible numbers in a square.",
    "Made a mistake? Tap Undo or Erase. Stuck? Tap Hint — there's no penalty.",
    "Your puzzle is saved automatically. Leave whenever you like and come back later.",
)

@Composable
fun SudokuHowToPlayDialog(onDismiss: () -> Unit) = HowToPlayDialog("How to play Sudoku", steps, onDismiss)
