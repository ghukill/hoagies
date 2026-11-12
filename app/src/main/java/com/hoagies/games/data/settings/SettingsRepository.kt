package com.hoagies.games.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Persists [AppSettings] with Jetpack DataStore. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private object Keys {
        val textSize = stringPreferencesKey("text_size")
        val themeMode = stringPreferencesKey("theme_mode")
        val highContrast = booleanPreferencesKey("high_contrast")
        val haptics = booleanPreferencesKey("haptics")
        val sudokuShowTimer = booleanPreferencesKey("sudoku_show_timer")
        val sudokuHighlightSame = booleanPreferencesKey("sudoku_highlight_same")
        val sudokuShowMistakes = booleanPreferencesKey("sudoku_show_mistakes")
        val sudokuAutoRemoveNotes = booleanPreferencesKey("sudoku_auto_remove_notes")
        val mahjongTileSet = stringPreferencesKey("mahjong_tile_set")
        val mahjongDimBlocked = booleanPreferencesKey("mahjong_dim_blocked")
        val mahjongShowTimer = booleanPreferencesKey("mahjong_show_timer")
    }

    private fun Preferences.toSettings(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            textSize = enumOr(this[Keys.textSize], d.textSize),
            themeMode = enumOr(this[Keys.themeMode], d.themeMode),
            highContrast = this[Keys.highContrast] ?: d.highContrast,
            haptics = this[Keys.haptics] ?: d.haptics,
            sudoku = SudokuSettings(
                showTimer = this[Keys.sudokuShowTimer] ?: d.sudoku.showTimer,
                highlightSameNumbers = this[Keys.sudokuHighlightSame] ?: d.sudoku.highlightSameNumbers,
                showMistakes = this[Keys.sudokuShowMistakes] ?: d.sudoku.showMistakes,
                autoRemoveNotes = this[Keys.sudokuAutoRemoveNotes] ?: d.sudoku.autoRemoveNotes,
            ),
            mahjong = MahjongSettings(
                tileSet = this[Keys.mahjongTileSet] ?: d.mahjong.tileSet,
                dimBlockedTiles = this[Keys.mahjongDimBlocked] ?: d.mahjong.dimBlockedTiles,
                showTimer = this[Keys.mahjongShowTimer] ?: d.mahjong.showTimer,
            ),
        )
    }

    private fun MutablePreferences.write(s: AppSettings) {
        this[Keys.textSize] = s.textSize.name
        this[Keys.themeMode] = s.themeMode.name
        this[Keys.highContrast] = s.highContrast
        this[Keys.haptics] = s.haptics
        this[Keys.sudokuShowTimer] = s.sudoku.showTimer
        this[Keys.sudokuHighlightSame] = s.sudoku.highlightSameNumbers
        this[Keys.sudokuShowMistakes] = s.sudoku.showMistakes
        this[Keys.sudokuAutoRemoveNotes] = s.sudoku.autoRemoveNotes
        this[Keys.mahjongTileSet] = s.mahjong.tileSet
        this[Keys.mahjongDimBlocked] = s.mahjong.dimBlockedTiles
        this[Keys.mahjongShowTimer] = s.mahjong.showTimer
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        name?.let { runCatching { enumValueOf<E>(it) }.getOrNull() } ?: default
}
