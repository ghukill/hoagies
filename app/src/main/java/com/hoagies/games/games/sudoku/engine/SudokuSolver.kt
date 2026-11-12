package com.hoagies.games.games.sudoku.engine

import kotlin.random.Random

/**
 * Fast bitmask backtracking solver (most-constrained-cell first), plus a "human" solver
 * that only uses simple techniques — used to grade how friendly a puzzle is.
 */
object SudokuSolver {

    /** Counts solutions of [grid] (0 = empty), stopping early once [limit] is reached. */
    fun countSolutions(grid: IntArray, limit: Int = 2): Int {
        val state = State.from(grid) ?: return 0
        var count = 0
        state.search(random = null) {
            count++
            count >= limit
        }
        return count
    }

    /** Returns one solution of [grid], or null if it has none. */
    fun solve(grid: IntArray): IntArray? {
        val state = State.from(grid) ?: return null
        var result: IntArray? = null
        state.search(random = null) {
            result = it.copyOf()
            true
        }
        return result
    }

    /** A complete, valid, randomly-shuffled grid. */
    fun randomSolvedGrid(random: Random): IntArray {
        val state = State.from(IntArray(Grid.CELLS))!!
        var result: IntArray? = null
        state.search(random) {
            result = it.copyOf()
            true
        }
        return checkNotNull(result)
    }

    /**
     * True if a person could solve [grid] using only "singles":
     * - naked single: a square where only one number fits;
     * - hidden single (if [allowHiddenSingles]): a number that fits in only one square of a row/column/box.
     *
     * A grid solvable this way is guaranteed to have exactly one solution.
     */
    fun isSolvableWithSingles(grid: IntArray, allowHiddenSingles: Boolean = true): Boolean {
        val state = State.from(grid) ?: return false
        val cells = state.cells
        while (true) {
            var progressed = false
            for (i in 0 until Grid.CELLS) {
                if (cells[i] != 0) continue
                val mask = state.candidatesOf(i)
                when (Integer.bitCount(mask)) {
                    0 -> return false
                    1 -> {
                        state.place(i, Integer.numberOfTrailingZeros(mask))
                        progressed = true
                    }
                }
            }
            if (!progressed && allowHiddenSingles) {
                for (unit in Grid.UNITS) {
                    for (d in 1..9) {
                        var spot = -1
                        var count = 0
                        var present = false
                        for (i in unit) {
                            if (cells[i] == d) {
                                present = true
                                break
                            }
                            if (cells[i] == 0 && state.candidatesOf(i) and Grid.bit(d) != 0) {
                                count++
                                spot = i
                            }
                        }
                        if (present) continue
                        if (count == 0) return false
                        if (count == 1) {
                            state.place(spot, d)
                            progressed = true
                        }
                    }
                }
            }
            if (!progressed) break
        }
        return cells.all { it != 0 }
    }

    /** True if [values] is a completely filled, rule-abiding grid. */
    fun isValidSolution(values: List<Int>): Boolean =
        values.size == Grid.CELLS &&
            Grid.UNITS.all { unit -> unit.map { values[it] }.toSet() == (1..9).toSet() }

    private class State(
        val cells: IntArray,
        private val rows: IntArray,
        private val cols: IntArray,
        private val boxes: IntArray,
    ) {
        fun candidatesOf(i: Int): Int =
            Grid.ALL_DIGITS and (rows[Grid.row(i)] or cols[Grid.col(i)] or boxes[Grid.box(i)]).inv()

        fun place(i: Int, d: Int) {
            val b = Grid.bit(d)
            cells[i] = d
            rows[Grid.row(i)] = rows[Grid.row(i)] or b
            cols[Grid.col(i)] = cols[Grid.col(i)] or b
            boxes[Grid.box(i)] = boxes[Grid.box(i)] or b
        }

        fun unplace(i: Int, d: Int) {
            val b = Grid.bit(d).inv()
            cells[i] = 0
            rows[Grid.row(i)] = rows[Grid.row(i)] and b
            cols[Grid.col(i)] = cols[Grid.col(i)] and b
            boxes[Grid.box(i)] = boxes[Grid.box(i)] and b
        }

        /** Depth-first search. [onSolution] returns true to stop; returns true if stopped. */
        fun search(random: Random?, onSolution: (IntArray) -> Boolean): Boolean {
            var best = -1
            var bestMask = 0
            var bestCount = 10
            for (i in 0 until Grid.CELLS) {
                if (cells[i] != 0) continue
                val mask = candidatesOf(i)
                val n = Integer.bitCount(mask)
                if (n == 0) return false
                if (n < bestCount) {
                    best = i
                    bestMask = mask
                    bestCount = n
                    if (n == 1) break
                }
            }
            if (best == -1) return onSolution(cells)

            val digits = Grid.digitsIn(bestMask).let { if (random != null) it.shuffled(random) else it }
            for (d in digits) {
                place(best, d)
                val stop = search(random, onSolution)
                unplace(best, d)
                if (stop) return true
            }
            return false
        }

        companion object {
            /** Builds a search state, or null if [grid] already breaks the rules. */
            fun from(grid: IntArray): State? {
                require(grid.size == Grid.CELLS) { "A Sudoku grid has ${Grid.CELLS} cells" }
                val state = State(IntArray(Grid.CELLS), IntArray(9), IntArray(9), IntArray(9))
                for (i in 0 until Grid.CELLS) {
                    val d = grid[i]
                    if (d == 0) continue
                    require(d in 1..9) { "Invalid digit $d at cell $i" }
                    if (state.candidatesOf(i) and Grid.bit(d) == 0) return null
                    state.place(i, d)
                }
                return state
            }
        }
    }
}
