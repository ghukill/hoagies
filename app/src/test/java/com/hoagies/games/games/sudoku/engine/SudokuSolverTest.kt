package com.hoagies.games.games.sudoku.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuSolverTest {

    private fun parse(s: String) = s.map { if (it == '.') 0 else it.digitToInt() }.toIntArray()

    // A well-known easy puzzle (solvable with singles).
    private val easy = parse("003020600900305001001806400008102900700000008006708200002609500800203009005010300")

    @Test fun geometry() {
        assertEquals(27, Grid.UNITS.size)
        Grid.PEERS.forEach { assertEquals(20, it.size) }
        assertTrue(Grid.isPeer(0, 80).not())
        assertTrue(Grid.isPeer(0, 20)) // same box
    }

    @Test fun solvesKnownPuzzleConsistently() {
        val solution = SudokuSolver.solve(easy)
        assertNotNull(solution)
        solution!!
        assertTrue(SudokuSolver.isValidSolution(solution.toList()))
        easy.forEachIndexed { i, v -> if (v != 0) assertEquals(v, solution[i]) }
        assertEquals(1, SudokuSolver.countSolutions(easy))
        assertTrue(SudokuSolver.isSolvableWithSingles(easy))
    }

    @Test fun emptyGridHasManySolutions() {
        assertEquals(2, SudokuSolver.countSolutions(IntArray(81), limit = 2))
    }

    @Test fun contradictoryGridHasNoSolution() {
        val bad = IntArray(81).also { it[0] = 5; it[1] = 5 }
        assertEquals(0, SudokuSolver.countSolutions(bad))
        assertNull(SudokuSolver.solve(bad))
        assertFalse(SudokuSolver.isSolvableWithSingles(bad))
    }

    @Test fun randomGridsAreValid() {
        repeat(20) { seed ->
            val grid = SudokuSolver.randomSolvedGrid(Random(seed))
            assertTrue(SudokuSolver.isValidSolution(grid.toList()))
        }
    }
}
