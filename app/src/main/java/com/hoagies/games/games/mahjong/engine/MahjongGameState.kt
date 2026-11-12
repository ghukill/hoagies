package com.hoagies.games.games.mahjong.engine

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * A Mahjong Solitaire game in progress. Immutable and serializable (this is what gets saved).
 *
 * Tile `i` sits at `layout.positions[i]`. [history] lists removed tiles two at a time, in order,
 * which is what Undo walks back through.
 */
@Serializable
data class MahjongGameState(
    val layoutId: String,
    val faces: List<TileFace>,
    /** The faces as originally dealt, so "Start over" gives back the same (winnable) board. */
    val dealtFaces: List<TileFace>,
    val removed: List<Boolean>,
    val history: List<Int> = emptyList(),
    val elapsedSeconds: Long = 0,
    val hintsUsed: Int = 0,
    val shufflesUsed: Int = 0,
) {
    val layout: BoardLayout get() = checkNotNull(MahjongLayouts.find(layoutId)) { "Unknown layout $layoutId" }

    val tilesLeft: Int get() = removed.count { !it }
    val isComplete: Boolean get() = removed.all { it }
    val progress: Float get() = 1f - tilesLeft.toFloat() / faces.size

    fun isFree(index: Int): Boolean = layout.isFree(index) { removed[it] }

    fun canMatch(a: Int, b: Int): Boolean =
        a != b && isFree(a) && isFree(b) && faces[a].matches(faces[b])

    fun removePair(a: Int, b: Int): MahjongGameState {
        if (!canMatch(a, b)) return this
        return copy(
            removed = removed.toMutableList().also { it[a] = true; it[b] = true },
            history = history + listOf(a, b),
        )
    }

    /** Puts the last removed pair back. Null if there's nothing to undo. */
    fun undo(): MahjongGameState? {
        if (history.size < 2) return null
        val a = history[history.size - 2]
        val b = history.last()
        return copy(
            removed = removed.toMutableList().also { it[a] = false; it[b] = false },
            history = history.dropLast(2),
        )
    }

    /** Any pair of free, matching tiles, or null if the player is stuck. */
    fun findMatch(): Pair<Int, Int>? {
        val free = faces.indices.filter { isFree(it) }
        for ((_, group) in free.groupBy { faces[it].matchKey }) {
            if (group.size >= 2) return group[0] to group[1]
        }
        return null
    }

    val hasMoves: Boolean get() = findMatch() != null

    /** Counts a hint and returns the pair to show, or null if there are no moves. */
    fun hint(): Pair<MahjongGameState, Pair<Int, Int>>? {
        val pair = findMatch() ?: return null
        return copy(hintsUsed = hintsUsed + 1) to pair
    }

    /**
     * Mixes the faces of the tiles still on the board into a new arrangement that can be cleared.
     * Null if no arrangement could work (e.g. the last tiles are stacked on each other).
     */
    fun shuffled(random: Random = Random.Default): MahjongGameState? {
        val onBoard = faces.indices.filter { !removed[it] }
        val pairs = MahjongDealer.pairUp(onBoard.map { faces[it] }) ?: return null
        val deal = MahjongDealer.deal(layout, onBoard, pairs, random) ?: return null
        val newFaces = faces.toMutableList()
        deal.faces.forEach { (i, face) -> newFaces[i] = face }
        return copy(faces = newFaces, shufflesUsed = shufflesUsed + 1)
    }

    /** Back to the original deal with every tile in place. Clock and counters keep going. */
    fun restart(): MahjongGameState =
        copy(faces = dealtFaces, removed = List(faces.size) { false }, history = emptyList())

    companion object {
        fun newGame(layout: BoardLayout, random: Random = Random.Default): MahjongGameState {
            val deal = MahjongDealer.newDeal(layout, random)
            val faces = List(layout.size) { deal.faces.getValue(it) }
            return MahjongGameState(
                layoutId = layout.id,
                faces = faces,
                dealtFaces = faces,
                removed = List(layout.size) { false },
            )
        }
    }
}
