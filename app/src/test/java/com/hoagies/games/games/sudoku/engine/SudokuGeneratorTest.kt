package com.hoagies.games.games.sudoku.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuGeneratorTest {

    @Test fun everyDifficultyProducesFairPuzzles() {
        for (difficulty in Difficulty.entries) {
            repeat(8) { seed ->
                val puzzle = SudokuGenerator(Random(seed * 31 + difficulty.ordinal)).generate(difficulty)
                val givens = puzzle.givens.toIntArray()

                assertTrue("solution valid", SudokuSolver.isValidSolution(puzzle.solution))
                puzzle.givens.forEachIndexed { i, v -> if (v != 0) assertEquals(puzzle.solution[i], v) }
                assertEquals("$difficulty seed $seed must be unique", 1, SudokuSolver.countSolutions(givens))
                assertTrue("$difficulty has ${puzzle.clueCount} clues", puzzle.clueCount >= difficulty.targetClues)

                when (difficulty.technique) {
                    Technique.NAKED_SINGLES ->
                        assertTrue(SudokuSolver.isSolvableWithSingles(givens, allowHiddenSingles = false))
                    Technique.SINGLES -> assertTrue(SudokuSolver.isSolvableWithSingles(givens))
                    Technique.ANY -> Unit
                }
            }
        }
    }

    @Test fun puzzlesAreSymmetric() {
        val puzzle = SudokuGenerator(Random(7)).generate(Difficulty.MEDIUM)
        for (i in 0 until 81) {
            assertEquals(puzzle.givens[i] == 0, puzzle.givens[80 - i] == 0)
        }
    }

    @Test fun harderLevelsHaveFewerCluesOnAverage() {
        fun avg(d: Difficulty) = (0 until 6).map { SudokuGenerator(Random(it)).generate(d).clueCount }.average()
        assertTrue(avg(Difficulty.GENTLE) > avg(Difficulty.EASY))
        assertTrue(avg(Difficulty.EASY) > avg(Difficulty.HARD))
    }
}
