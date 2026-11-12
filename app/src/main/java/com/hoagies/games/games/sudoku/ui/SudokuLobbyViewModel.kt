package com.hoagies.games.games.sudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.memory.GameResultEntity
import com.hoagies.games.games.sudoku.SudokuGame
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DifficultyStats(
    val difficulty: Difficulty,
    val solved: Int,
    val bestSeconds: Long,
    val averageSeconds: Long,
)

data class SudokuLobbyState(
    val isLoading: Boolean = true,
    val inProgress: SudokuGameState? = null,
    val stats: List<DifficultyStats> = emptyList(),
) {
    val totalSolved: Int get() = stats.sumOf { it.solved }
}

class SudokuLobbyViewModel(memory: GameMemory) : ViewModel() {

    val state: StateFlow<SudokuLobbyState> = combine(
        memory.observe(SudokuGame.ID, SudokuGameState.serializer()),
        memory.observeResults(SudokuGame.ID),
    ) { saved, results ->
        SudokuLobbyState(
            isLoading = false,
            inProgress = saved?.takeIf { !it.isComplete },
            stats = statsFrom(results),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SudokuLobbyState())

    companion object {
        fun statsFrom(results: List<GameResultEntity>): List<DifficultyStats> =
            Difficulty.entries.mapNotNull { difficulty ->
                val times = results.filter { it.variant == difficulty.name }.map { it.durationSeconds }
                if (times.isEmpty()) {
                    null
                } else {
                    DifficultyStats(difficulty, times.size, times.min(), times.sum() / times.size)
                }
            }

        val Factory = viewModelFactory {
            initializer { SudokuLobbyViewModel(appContainer.memory) }
        }
    }
}
