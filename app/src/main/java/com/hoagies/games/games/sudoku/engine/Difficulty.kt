package com.hoagies.games.games.sudoku.engine

import kotlinx.serialization.Serializable

/** The hardest kind of reasoning a puzzle is allowed to require. */
enum class Technique {
    /** Every step is "only one number fits in this square". */
    NAKED_SINGLES,

    /** Adds "this number only fits in one square of this row/column/box". */
    SINGLES,

    /** Anything goes, as long as there is exactly one solution. */
    ANY,
}

@Serializable
enum class Difficulty(
    val label: String,
    val description: String,
    /** The generator stops removing numbers once it reaches this many clues. */
    val targetClues: Int,
    val technique: Technique,
) {
    GENTLE("Gentle", "A relaxed start. Great for learning.", 45, Technique.NAKED_SINGLES),
    EASY("Easy", "Comfortable and steady.", 38, Technique.SINGLES),
    MEDIUM("Medium", "Fewer clues. Takes a little more thought.", 30, Technique.SINGLES),
    HARD("Hard", "A real brain-teaser.", 25, Technique.ANY),
}
