package com.hoagies.games.games.mahjong.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.memory.VariantStats
import com.hoagies.games.data.memory.summarizeResults
import com.hoagies.games.games.mahjong.MahjongGame
import com.hoagies.games.games.mahjong.engine.MahjongGameState
import com.hoagies.games.games.mahjong.engine.MahjongLayouts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MahjongLobbyState(
    val inProgress: MahjongGameState? = null,
    val stats: List<VariantStats> = emptyList(),
)

class MahjongLobbyViewModel(memory: GameMemory) : ViewModel() {
    val state: StateFlow<MahjongLobbyState> = combine(
        memory.observe(MahjongGame.ID, MahjongGameState.serializer()),
        memory.observeResults(MahjongGame.ID),
    ) { saved, results ->
        MahjongLobbyState(
            inProgress = saved?.takeIf { it.isPlayable() },
            stats = summarizeResults(results, MahjongLayouts.all.map { it.id }),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MahjongLobbyState())

    companion object {
        val Factory = viewModelFactory {
            initializer { MahjongLobbyViewModel(appContainer.memory) }
        }
    }
}
