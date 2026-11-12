package com.hoagies.games.games.mahjong.engine

import kotlin.random.Random

/** Faces for each position, plus one guaranteed way to clear the board. */
class Deal(val faces: Map<Int, TileFace>, val solution: List<Pair<Int, Int>>)

/**
 * Deals boards that can always be cleared.
 *
 * Trick: play a game *before* the faces are known. Repeatedly take two random free tiles off
 * the (blank) board until it's empty, then give each removed pair matching faces. That removal
 * order is now a valid solution of the resulting board.
 */
object MahjongDealer {

    fun newDeal(layout: BoardLayout, random: Random = Random.Default): Deal {
        val pairs = pickPairs(layout.size / 2, random)
        return checkNotNull(deal(layout, layout.positions.indices.toList(), pairs, random)) {
            "Could not deal layout ${layout.id}"
        }
    }

    /**
     * Assigns [pairs] to the tiles in [onBoard] (everything else counts as already removed)
     * so that the board can be cleared. Returns null if this arrangement of tiles can't be
     * cleared at all (e.g. one tile sitting directly on another).
     */
    fun deal(
        layout: BoardLayout,
        onBoard: List<Int>,
        pairs: List<Pair<TileFace, TileFace>>,
        random: Random,
        attempts: Int = 300,
    ): Deal? {
        require(onBoard.size == pairs.size * 2) { "Need one pair per two tiles" }
        repeat(attempts) {
            val removed = BooleanArray(layout.size) { true }
            onBoard.forEach { removed[it] = false }
            val order = ArrayList<Pair<Int, Int>>(pairs.size)
            while (order.size < pairs.size) {
                val free = onBoard.filter { layout.isFree(it) { i -> removed[i] } }
                if (free.size < 2) break
                val a = free.random(random)
                val b = free.filter { it != a }.random(random)
                removed[a] = true
                removed[b] = true
                order += a to b
            }
            if (order.size == pairs.size) {
                val shuffledPairs = pairs.shuffled(random)
                val faces = HashMap<Int, TileFace>(onBoard.size)
                order.forEachIndexed { k, (a, b) ->
                    faces[a] = shuffledPairs[k].first
                    faces[b] = shuffledPairs[k].second
                }
                return Deal(faces, order)
            }
        }
        return null
    }

    /**
     * Chooses [count] matching pairs, keeping each face's tiles together (all four of a face
     * where possible), like a real set. Larger boards reuse the set if needed.
     */
    fun pickPairs(count: Int, random: Random): List<Pair<TileFace, TileFace>> {
        val groups = buildList {
            TileFace.entries
                .filter { it.suit != Suit.FLOWER && it.suit != Suit.SEASON }
                .forEach { add(listOf(it to it, it to it)) }
            add(listOf(TileFace.FLOWER_1 to TileFace.FLOWER_2, TileFace.FLOWER_3 to TileFace.FLOWER_4))
            add(listOf(TileFace.SEASON_1 to TileFace.SEASON_2, TileFace.SEASON_3 to TileFace.SEASON_4))
        }
        val result = ArrayList<Pair<TileFace, TileFace>>(count)
        while (result.size < count) {
            for (group in groups.shuffled(random)) {
                for (pair in group) if (result.size < count) result += pair
            }
        }
        return result
    }

    /** Splits [faces] into matching pairs. Works for any set of tiles left after matched removals. */
    fun pairUp(faces: List<TileFace>): List<Pair<TileFace, TileFace>>? {
        val pairs = mutableListOf<Pair<TileFace, TileFace>>()
        for ((_, group) in faces.groupBy { it.matchKey }) {
            if (group.size % 2 != 0) return null
            group.chunked(2).forEach { pairs += it[0] to it[1] }
        }
        return pairs
    }
}
