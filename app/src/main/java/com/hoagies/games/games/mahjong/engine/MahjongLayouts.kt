package com.hoagies.games.games.mahjong.engine

/**
 * The boards on offer. Designed for a phone held upright: at most 7 tiles across, so tiles
 * stay big enough to see and tap.
 *
 * Layout ids are saved with games and results — never change or reuse one.
 */
object MahjongLayouts {

    val MEADOW = BoardLayout.build(
        id = "meadow", name = "Meadow", level = "Gentle",
        description = "Low and open, with only two layers. Perfect for learning.",
    ) {
        rect(0.0, 0.0, 6, 6, z = 0)
        rect(1.0, 1.0, 4, 4, z = 1)
    }

    val DIAMOND = BoardLayout.build(
        id = "diamond", name = "Diamond", level = "Easy",
        description = "A sparkling diamond, a little taller in the middle.",
    ) {
        centredRows(width = 7, firstRow = 0, lengths = listOf(2, 4, 6, 7, 7, 6, 4, 2), z = 0)
        centredRows(width = 7, firstRow = 1, lengths = listOf(2, 4, 5, 5, 4, 2), z = 1)
        centredRows(width = 7, firstRow = 2, lengths = listOf(2, 3, 3, 2), z = 2)
        centredRows(width = 7, firstRow = 3, lengths = listOf(1, 1), z = 3)
    }

    val PYRAMID = BoardLayout.build(
        id = "pyramid", name = "Pyramid", level = "Medium",
        description = "Four layers stacked high. Plan a few moves ahead.",
    ) {
        rect(0.0, 0.0, 6, 8, z = 0)
        rect(1.0, 1.0, 4, 6, z = 1)
        rect(2.0, 2.0, 2, 4, z = 2)
        rect(2.5, 3.0, 1, 2, z = 3)
    }

    val FORTRESS = BoardLayout.build(
        id = "fortress", name = "Fortress", level = "Hard",
        description = "A full set, five layers high. A real challenge.",
    ) {
        // Whole-tile steps (no brickwork offsets): offsets leave slivers of lower tiles
        // peeking out everywhere, which is visually noisy on a phone.
        rect(0.0, 0.0, 7, 8, z = 0)
        rect(0.0, 1.0, 7, 6, z = 1)
        rect(1.0, 1.0, 5, 6, z = 2)
        rect(2.0, 2.0, 3, 4, z = 3)
        rect(2.5, 3.0, 2, 2, z = 4)
    }

    val all: List<BoardLayout> = listOf(MEADOW, DIAMOND, PYRAMID, FORTRESS)

    private val byId = all.associateBy { it.id }

    fun find(id: String): BoardLayout? = byId[id]
}
