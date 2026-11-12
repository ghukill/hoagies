package com.hoagies.games.games.sudoku.engine

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuGameStateTest {

    private val puzzle = SudokuGenerator(Random(42)).generate(Difficulty.EASY)
    private val game = SudokuGameState.newGame(puzzle)
    private val emptyCell = puzzle.givens.indexOfFirst { it == 0 }
    private val givenCell = puzzle.givens.indexOfFirst { it != 0 }
    private val answer = puzzle.solution[emptyCell]
    private val wrongDigit = (1..9).first { it != answer }

    @Test fun givensCannotBeChanged() {
        assertSame(game, game.enter(givenCell, 1, autoRemoveNotes = true))
        assertSame(game, game.erase(givenCell))
        assertSame(game, game.toggleNote(givenCell, 1))
    }

    @Test fun enteringRightAndWrongNumbers() {
        val right = game.enter(emptyCell, answer, autoRemoveNotes = true)
        assertEquals(answer, right.values[emptyCell])
        assertEquals(0, right.mistakes)
        assertFalse(right.isWrong(emptyCell))

        val wrong = game.enter(emptyCell, wrongDigit, autoRemoveNotes = true)
        assertEquals(1, wrong.mistakes)
        assertTrue(wrong.isWrong(emptyCell))
    }

    @Test fun enteringTheSameNumberAgainClearsIt() {
        val placed = game.enter(emptyCell, answer, autoRemoveNotes = true)
        assertEquals(0, placed.enter(emptyCell, answer, autoRemoveNotes = true).values[emptyCell])
    }

    @Test fun notesToggleAndAreTidiedFromPeers() {
        val peer = Grid.PEERS[emptyCell].first { puzzle.givens[it] == 0 }
        val noted = game.toggleNote(peer, answer)
        assertTrue(noted.hasNote(peer, answer))
        assertFalse(noted.toggleNote(peer, answer).hasNote(peer, answer))

        assertFalse(noted.enter(emptyCell, answer, autoRemoveNotes = true).hasNote(peer, answer))
        assertTrue(noted.enter(emptyCell, answer, autoRemoveNotes = false).hasNote(peer, answer))
    }

    @Test fun hintFillsSelectedCellThenAnyCell() {
        val (hinted, cell) = game.hint(emptyCell)!!
        assertEquals(emptyCell, cell)
        assertEquals(answer, hinted.values[cell])
        assertEquals(1, hinted.hintsUsed)

        // With a given selected, the hint picks some other open cell.
        val (other, otherCell) = game.hint(givenCell)!!
        assertEquals(0, game.values[otherCell])
        assertEquals(puzzle.solution[otherCell], other.values[otherCell])
    }

    @Test fun hintFixesMistakesFirst() {
        val wrong = game.enter(emptyCell, wrongDigit, autoRemoveNotes = true)
        val (_, cell) = wrong.hint(null)!!
        assertEquals(emptyCell, cell)
    }

    @Test fun fillingEverySquareCorrectlyCompletesTheGame() {
        var state = game
        while (true) state = state.hint(null)?.first ?: break
        assertTrue(state.isComplete)
        assertEquals(1f, state.progress)
        assertNull(state.hint(null))
        assertSame(state, state.enter(emptyCell, wrongDigit, autoRemoveNotes = true))
    }

    @Test fun filledButWrongIsDetected() {
        var state = game
        while (true) state = state.hint(null)?.first ?: break
        // Swap in a wrong digit (bypassing the complete-lock via a copy).
        val broken = state.copy(values = state.values.toMutableList().also { it[emptyCell] = wrongDigit })
        assertTrue(broken.isFilledWithMistakes)
        assertFalse(broken.isComplete)
    }

    @Test fun restartKeepsGivensOnly() {
        val played = game.enter(emptyCell, answer, autoRemoveNotes = true).toggleNote(
            puzzle.givens.indexOfLast { it == 0 }, 3,
        )
        val restarted = played.restart()
        assertEquals(puzzle.givens, restarted.values)
        assertTrue(restarted.notes.all { it == 0 })
    }

    @Test fun saveFormatRoundTrips() {
        val played = game.enter(emptyCell, wrongDigit, autoRemoveNotes = true).copy(elapsedSeconds = 99)
        val json = Json.encodeToString(SudokuGameState.serializer(), played)
        assertEquals(played, Json.decodeFromString(SudokuGameState.serializer(), json))
    }
}
