package com.hoagies.games.games.sudoku.engine

/**
 * Geometry helpers for a classic 9×9 Sudoku grid.
 *
 * Cells are indexed 0..80 in row-major order. Digit sets are stored as bitmasks where
 * digit `d` (1..9) is bit `1 shl d`; bit 0 is unused.
 */
object Grid {
    const val SIZE = 9
    const val CELLS = 81
    const val ALL_DIGITS = 0x3FE

    fun row(cell: Int) = cell / SIZE
    fun col(cell: Int) = cell % SIZE
    fun box(cell: Int) = (row(cell) / 3) * 3 + col(cell) / 3
    fun index(row: Int, col: Int) = row * SIZE + col

    fun bit(digit: Int) = 1 shl digit
    fun digitsIn(mask: Int): List<Int> = (1..9).filter { mask and bit(it) != 0 }

    /** The 27 units every digit must appear in exactly once: 9 rows, 9 columns, 9 boxes. */
    val UNITS: List<IntArray> = buildList {
        for (r in 0 until SIZE) add(IntArray(SIZE) { c -> index(r, c) })
        for (c in 0 until SIZE) add(IntArray(SIZE) { r -> index(r, c) })
        for (b in 0 until SIZE) add(IntArray(SIZE) { k -> index((b / 3) * 3 + k / 3, (b % 3) * 3 + k % 3) })
    }

    /** For each cell, the 20 other cells that share its row, column or box. */
    val PEERS: Array<IntArray> = Array(CELLS) { cell ->
        (0 until CELLS).filter { other ->
            other != cell &&
                (row(other) == row(cell) || col(other) == col(cell) || box(other) == box(cell))
        }.toIntArray()
    }

    fun isPeer(a: Int, b: Int) = a != b && (row(a) == row(b) || col(a) == col(b) || box(a) == box(b))

    /** Digits that could legally go in [cell], considering only the values of its peers (0 = empty). */
    fun candidates(values: List<Int>, cell: Int): Int {
        var used = 0
        for (p in PEERS[cell]) {
            val v = values[p]
            if (v != 0) used = used or bit(v)
        }
        return ALL_DIGITS and used.inv()
    }
}
