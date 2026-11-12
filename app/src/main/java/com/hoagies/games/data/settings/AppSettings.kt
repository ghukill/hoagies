package com.hoagies.games.data.settings

/** Multiplies on top of the phone's own font size setting, so both are respected. */
enum class TextSize(val label: String, val scale: Float) {
    STANDARD("Standard", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra large", 1.3f),
}

enum class ThemeMode(val label: String) {
    SYSTEM("Match my phone"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class SudokuSettings(
    val showTimer: Boolean = false,
    val highlightSameNumbers: Boolean = true,
    val showMistakes: Boolean = true,
    val autoRemoveNotes: Boolean = true,
)

data class MahjongSettings(
    /** Id of the chosen tile set; see TileSetRepository. */
    val tileSet: String = "simple",
    val dimBlockedTiles: Boolean = true,
    val showTimer: Boolean = false,
)

data class AppSettings(
    val textSize: TextSize = TextSize.STANDARD,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highContrast: Boolean = false,
    val haptics: Boolean = true,
    val sudoku: SudokuSettings = SudokuSettings(),
    val mahjong: MahjongSettings = MahjongSettings(),
)
