package com.hoagies.games.games

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.ui.graphics.vector.ImageVector
import com.hoagies.games.games.sudoku.SudokuGame
import com.hoagies.games.ui.navigation.SudokuLobbyRoute

/** What the home screen needs to know about a game. */
data class GameInfo(
    /** Stable id; also the key for this game's saved progress and results. Never change it. */
    val id: String,
    val title: String,
    val tagline: String,
    val icon: ImageVector,
    /** Navigation route of the game's entry screen. */
    val route: Any,
)

/**
 * The one list of games in the app. Adding a game = add its package under `games/`,
 * register its screens in HoagiesNavHost, and add an entry here.
 */
object GameRegistry {
    val games: List<GameInfo> = listOf(
        GameInfo(
            id = SudokuGame.ID,
            title = "Sudoku",
            tagline = "The classic number puzzle.",
            icon = Icons.Rounded.GridOn,
            route = SudokuLobbyRoute,
        ),
    )

    /** Shown on the home screen so people know more is on the way. */
    val comingSoon: List<String> = listOf("Solitaire", "Word Search", "Mahjong", "Crosswords")
}
