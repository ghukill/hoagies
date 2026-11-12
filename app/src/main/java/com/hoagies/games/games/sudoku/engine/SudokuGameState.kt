package com.hoagies.games.games.sudoku.engine

import kotlinx.serialization.Serializable

/**
 * Everything about a Sudoku in progress. Immutable, serializable (this is what gets saved),
 * and free of Android dependencies so the rules can be unit-tested.
 *
 * Grids are 81-element lists, row-major, 0 = empty. [notes] holds a digit bitmask per cell.
 */
@Serializable
data class SudokuGameState(
    val difficulty: Difficulty,
    val givens: List<Int>,
    val solution: List<Int>,
    val values: List<Int>,
    val notes: List<Int>,
    val elapsedSeconds: Long = 0,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
) {
    val isComplete: Boolean get() = values == solution

    /** Every square has a number, but some are wrong. */
    val isFilledWithMistakes: Boolean get() = values.none { it == 0 } && !isComplete

    /** Fraction of the originally-empty squares that are now filled (0.0..1.0). */
    val progress: Float
        get() {
            val open = givens.count { it == 0 }
            if (open == 0) return 1f
            return values.indices.count { givens[it] == 0 && values[it] != 0 }.toFloat() / open
        }

    fun isGiven(cell: Int) = givens[cell] != 0
    fun isWrong(cell: Int) = values[cell] != 0 && values[cell] != solution[cell]
    fun hasNote(cell: Int, digit: Int) = notes[cell] and Grid.bit(digit) != 0
    fun countPlaced(digit: Int) = values.count { it == digit }

    /** Places [digit] in [cell]. Entering the same digit again clears the square. */
    fun enter(cell: Int, digit: Int, autoRemoveNotes: Boolean): SudokuGameState {
        if (isComplete || isGiven(cell)) return this
        if (values[cell] == digit) return copy(values = values.with(cell, 0))
        var newNotes = notes.with(cell, 0)
        if (autoRemoveNotes) newNotes = newNotes.withoutNoteInPeers(cell, digit)
        return copy(
            values = values.with(cell, digit),
            notes = newNotes,
            mistakes = if (digit != solution[cell]) mistakes + 1 else mistakes,
        )
    }

    /** Adds or removes a pencil mark. Only allowed on empty squares. */
    fun toggleNote(cell: Int, digit: Int): SudokuGameState {
        if (isComplete || isGiven(cell) || values[cell] != 0) return this
        return copy(notes = notes.with(cell, notes[cell] xor Grid.bit(digit)))
    }

    fun erase(cell: Int): SudokuGameState {
        if (isComplete || isGiven(cell)) return this
        return copy(values = values.with(cell, 0), notes = notes.with(cell, 0))
    }

    /** Clears every square the player filled. The clock and counters keep going. */
    fun restart(): SudokuGameState = copy(values = givens, notes = List(Grid.CELLS) { 0 })

    /**
     * Fills in one correct number. Prefers [preferred] (the selected square) if it needs help,
     * then fixes a wrong number, then the empty square with the fewest possibilities.
     * Returns the new state and the cell that was filled, or null if there is nothing to hint.
     */
    fun hint(preferred: Int?): Pair<SudokuGameState, Int>? {
        if (isComplete) return null
        val target = preferred?.takeIf { !isGiven(it) && values[it] != solution[it] }
            ?: values.indices.firstOrNull { isWrong(it) }
            ?: values.indices
                .filter { values[it] == 0 }
                .minByOrNull { Integer.bitCount(Grid.candidates(values, it)) }
            ?: return null
        val digit = solution[target]
        val next = copy(
            values = values.with(target, digit),
            notes = notes.with(target, 0).withoutNoteInPeers(target, digit),
            hintsUsed = hintsUsed + 1,
        )
        return next to target
    }

    private fun List<Int>.withoutNoteInPeers(cell: Int, digit: Int): List<Int> {
        val mask = Grid.bit(digit).inv()
        return mapIndexed { i, n -> if (Grid.isPeer(i, cell)) n and mask else n }
    }

    companion object {
        fun newGame(puzzle: SudokuPuzzle) = SudokuGameState(
            difficulty = puzzle.difficulty,
            givens = puzzle.givens,
            solution = puzzle.solution,
            values = puzzle.givens,
            notes = List(Grid.CELLS) { 0 },
        )
    }
}

private fun List<Int>.with(index: Int, value: Int): List<Int> =
    toMutableList().also { it[index] = value }
