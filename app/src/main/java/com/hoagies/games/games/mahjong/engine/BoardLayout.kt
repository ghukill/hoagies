package com.hoagies.games.games.mahjong.engine

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Where a tile sits. [x] and [y] are in **half-tile** units so tiles can straddle two below
 * (a tile covers x..x+2 by y..y+2); [z] is the layer, 0 = table.
 */
data class TilePos(val x: Int, val y: Int, val z: Int)

/**
 * A board shape: the positions tiles are dealt into. Precomputes which tiles cover or flank
 * each other so the "is this tile free?" check is cheap.
 */
class BoardLayout(
    val id: String,
    val name: String,
    val level: String,
    val description: String,
    val positions: List<TilePos>,
) {
    val size: Int get() = positions.size

    /** Board extent in half-tile units. */
    val widthUnits: Int = positions.maxOf { it.x } + 2
    val heightUnits: Int = positions.maxOf { it.y } + 2
    val maxZ: Int = positions.maxOf { it.z }

    private val above: Array<IntArray>
    private val left: Array<IntArray>
    private val right: Array<IntArray>

    init {
        require(size > 0 && size % 2 == 0) { "Layout $id needs an even number of tiles, has $size" }
        for (i in positions.indices) {
            for (j in i + 1 until size) {
                val a = positions[i]
                val b = positions[j]
                require(!(a.z == b.z && overlaps(a, b))) { "Layout $id: tiles $a and $b overlap" }
            }
        }
        above = Array(size) { i ->
            val p = positions[i]
            positions.indices.filter { j -> positions[j].z > p.z && overlaps(p, positions[j]) }.toIntArray()
        }
        left = Array(size) { i -> sideNeighbours(i, -2) }
        right = Array(size) { i -> sideNeighbours(i, +2) }
    }

    private fun sideNeighbours(i: Int, dx: Int): IntArray {
        val p = positions[i]
        return positions.indices.filter { j ->
            val q = positions[j]
            q.z == p.z && q.x == p.x + dx && abs(q.y - p.y) < 2
        }.toIntArray()
    }

    /**
     * A tile is free when nothing rests on top of it and its left or right side is open.
     * [isRemoved] says whether a given tile index has already been taken off the board.
     */
    inline fun isFree(index: Int, isRemoved: (Int) -> Boolean): Boolean =
        !isRemoved(index) &&
            coveredBy(index).all(isRemoved) &&
            (leftOf(index).all(isRemoved) || rightOf(index).all(isRemoved))

    fun coveredBy(index: Int): IntArray = above[index]
    fun leftOf(index: Int): IntArray = left[index]
    fun rightOf(index: Int): IntArray = right[index]

    private fun overlaps(a: TilePos, b: TilePos) = abs(a.x - b.x) < 2 && abs(a.y - b.y) < 2

    /** Small DSL for describing layouts in whole-tile units (halves allowed: 0.5, 1.5, …). */
    class Builder {
        internal val positions = mutableListOf<TilePos>()

        fun tile(col: Double, row: Double, z: Int) {
            positions += TilePos((col * 2).roundToInt(), (row * 2).roundToInt(), z)
        }

        fun rect(col: Double, row: Double, cols: Int, rows: Int, z: Int) {
            for (r in 0 until rows) for (c in 0 until cols) tile(col + c, row + r, z)
        }

        /** One row per entry of [lengths], each centred within a board [width] tiles wide. */
        fun centredRows(width: Int, firstRow: Int, lengths: List<Int>, z: Int) {
            lengths.forEachIndexed { k, length ->
                for (c in 0 until length) tile((width - length) / 2.0 + c, (firstRow + k).toDouble(), z)
            }
        }
    }

    companion object {
        fun build(id: String, name: String, level: String, description: String, block: Builder.() -> Unit) =
            BoardLayout(id, name, level, description, Builder().apply(block).positions.toList())
    }
}
