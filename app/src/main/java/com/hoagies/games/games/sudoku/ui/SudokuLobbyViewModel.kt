package com.hoagies.games.games.sudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.memory.VariantStats
import com.hoagies.games.data.memory.summarizeResults
import com.hoagies.games.games.sudoku.SudokuGame
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SudokuLobbyState(
    val isLoading: Boolean = true,
    val inProgress: SudokuGameState? = null,
    val stats: List<VariantStats> = emptyList(),
)

class SudokuLobbyViewModel(memory: GameMemory) : ViewModel() {

    val state: StateFlow<SudokuLobbyState> = combine(
        memory.observe(SudokuGame.ID, SudokuGameState.serializer()),
        memory.observeResults(SudokuGame.ID),
    ) { saved, results ->
        SudokuLobbyState(
            isLoading = false,
            inProgress = saved?.takeIf { !it.isComplete },
            stats = summarizeResults(results, Difficulty.entries.map { it.name }),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SudokuLobbyState())

    companion object {
        val Factory = viewModelFactory {
            initializer { SudokuLobbyViewModel(appContainer.memory) }
        }
    }
}
