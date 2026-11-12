package com.hoagies.games.games.sudoku.engine

import kotlin.random.Random

/** A puzzle as handed to a player: the starting clues plus the (unique) answer. */
data class SudokuPuzzle(
    val givens: List<Int>,
    val solution: List<Int>,
    val difficulty: Difficulty,
) {
    val clueCount: Int get() = givens.count { it != 0 }
}

/**
 * Generates puzzles with exactly one solution.
 *
 * Starts from a random full grid and removes numbers in symmetric pairs (a cell and its
 * 180° rotation — the classic newspaper look) as long as the puzzle stays solvable using the
 * difficulty's allowed [Technique].
 */
class SudokuGenerator(private val random: Random = Random.Default) {

    fun generate(difficulty: Difficulty): SudokuPuzzle {
        val solution = SudokuSolver.randomSolvedGrid(random)
        val puzzle = solution.copyOf()
        var clues = Grid.CELLS

        for (cell in (0..Grid.CELLS / 2).shuffled(random)) {
            if (clues <= difficulty.targetClues) break
            val mirror = Grid.CELLS - 1 - cell
            val pair = if (cell == mirror) intArrayOf(cell) else intArrayOf(cell, mirror)
            if (clues - pair.size < difficulty.targetClues) continue

            pair.forEach { puzzle[it] = 0 }
            if (isAcceptable(puzzle, difficulty.technique)) {
                clues -= pair.size
            } else {
                pair.forEach { puzzle[it] = solution[it] }
            }
        }
        return SudokuPuzzle(puzzle.toList(), solution.toList(), difficulty)
    }

    private fun isAcceptable(puzzle: IntArray, technique: Technique): Boolean = when (technique) {
        Technique.NAKED_SINGLES -> SudokuSolver.isSolvableWithSingles(puzzle, allowHiddenSingles = false)
        Technique.SINGLES -> SudokuSolver.isSolvableWithSingles(puzzle, allowHiddenSingles = true)
        Technique.ANY -> SudokuSolver.countSolutions(puzzle, limit = 2) == 1
    }
}
