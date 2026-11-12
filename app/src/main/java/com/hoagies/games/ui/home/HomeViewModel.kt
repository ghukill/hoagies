package com.hoagies.games.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(memory: GameMemory) : ViewModel() {
    /** Ids of games with a game in progress, to show "Continue" badges. */
    val gamesInProgress: StateFlow<Set<String>> = memory.observeSavedGameIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(appContainer.memory) }
        }
    }
}
