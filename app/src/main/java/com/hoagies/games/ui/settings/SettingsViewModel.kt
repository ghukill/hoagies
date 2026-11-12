package com.hoagies.games.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.settings.AppSettings
import com.hoagies.games.data.settings.SettingsRepository
import com.hoagies.games.games.mahjong.tiles.TileSetInfo
import com.hoagies.games.games.mahjong.tiles.TileSetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val memory: GameMemory,
    tileSetRepository: TileSetRepository,
) : ViewModel() {

    val mahjongTileSets: StateFlow<List<TileSetInfo>> = flow { emit(tileSetRepository.available()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf(TileSetRepository.SIMPLE))

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repository.update(transform) }
    }

    fun eraseAllProgress() = memory.eraseEverything()

    companion object {
        val Factory = viewModelFactory {
            initializer { SettingsViewModel(appContainer.settings, appContainer.memory, appContainer.mahjongTileSets) }
        }
    }
}
