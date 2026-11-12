package com.hoagies.games.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

@Serializable data object SettingsRoute

@Serializable data object SudokuLobbyRoute

/** [difficulty] = a Difficulty name to start fresh, or null to resume the saved puzzle. */
@Serializable data class SudokuPlayRoute(val difficulty: String? = null)

@Serializable data object MahjongLobbyRoute

/** [layout] = a board id to deal a new game, or null to resume the saved game. */
@Serializable data class MahjongPlayRoute(val layout: String? = null)
