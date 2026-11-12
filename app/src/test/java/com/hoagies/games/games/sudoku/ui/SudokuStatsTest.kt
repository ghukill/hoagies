package com.hoagies.games.games.sudoku.ui

import com.hoagies.games.data.memory.GameResultEntity
import com.hoagies.games.games.sudoku.engine.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuStatsTest {
    private fun result(variant: Difficulty, seconds: Long) =
        GameResultEntity(gameId = "sudoku", variant = variant.name, durationSeconds = seconds, hintsUsed = 0, mistakes = 0, completedAt = 0)

    @Test fun statsPerDifficulty() {
        val stats = SudokuLobbyViewModel.statsFrom(
            listOf(result(Difficulty.EASY, 300), result(Difficulty.EASY, 100), result(Difficulty.HARD, 900)),
        )
        assertEquals(listOf(Difficulty.EASY, Difficulty.HARD), stats.map { it.difficulty })
        assertEquals(2, stats[0].solved)
        assertEquals(100, stats[0].bestSeconds)
        assertEquals(200, stats[0].averageSeconds)
    }
}
