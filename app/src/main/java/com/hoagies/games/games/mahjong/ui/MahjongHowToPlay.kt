package com.hoagies.games.games.mahjong.ui

import androidx.compose.runtime.Composable
import com.hoagies.games.ui.components.HowToPlayDialog

private val steps = listOf(
    "The goal: clear every tile from the board by removing matching pairs.",
    "Tap a tile, then tap another tile with the same picture. If they match, both disappear.",
    "You can only use free tiles: nothing on top, and an open left or right side. Stuck tiles look darker.",
    "Any flower matches any other flower, and any season (sun) matches any other season.",
    "Stuck? Hint shows you a pair, and Shuffle mixes the tiles. Undo takes back your last pair. None of these cost anything.",
    "Your game is saved automatically. Leave whenever you like and come back later.",
)

@Composable
fun MahjongHowToPlayDialog(onDismiss: () -> Unit) = HowToPlayDialog("How to play Mahjong Solitaire", steps, onDismiss)
