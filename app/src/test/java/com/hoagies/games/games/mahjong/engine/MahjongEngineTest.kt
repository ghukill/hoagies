package com.hoagies.games.games.mahjong.engine

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MahjongEngineTest {

    private val row3 = BoardLayout.build("t", "t", "t", "t") {
        rect(0.0, 0.0, 3, 1, z = 0)
        tile(2.0, 0.0, z = 1) // sits on the right-hand tile
        // Two loose tiles elsewhere keep the count even.
        tile(5.0, 0.0, z = 0)
        tile(7.0, 0.0, z = 0)
    }

    @Test fun freeTileRule() {
        val none = { _: Int -> false }
        assertTrue("left end is open on the left", row3.isFree(0, none))
        assertFalse("middle tile is flanked on both sides", row3.isFree(1, none))
        assertFalse("covered tile is never free", row3.isFree(2, none))
        assertTrue("top tile is free", row3.isFree(3, none))
        // Remove the left end: the middle tile's left side opens up.
        assertTrue(row3.isFree(1) { it == 0 })
    }

    @Test fun halfOffsetTilesCoverTwoBelow() {
        val layout = BoardLayout.build("h", "h", "h", "h") {
            rect(0.0, 0.0, 2, 1, z = 0)
            tile(0.5, 0.0, z = 1)
            tile(4.0, 0.0, z = 0)
        }
        assertFalse(layout.isFree(0) { false })
        assertFalse(layout.isFree(1) { false })
        assertTrue(layout.isFree(0) { it == 2 })
    }

    @Test fun builtInLayoutsAreValid() {
        assertEquals(MahjongLayouts.all.size, MahjongLayouts.all.map { it.id }.toSet().size)
        MahjongLayouts.all.forEach { layout ->
            assertTrue("${layout.id} has ${layout.size} tiles", layout.size % 2 == 0 && layout.size <= 144)
            assertTrue("${layout.id} is at most 7 tiles wide", layout.widthUnits <= 14)
        }
        assertEquals(144, MahjongLayouts.FORTRESS.size)
    }

    @Test fun everyDealCanBeCleared() {
        for (layout in MahjongLayouts.all) {
            repeat(15) { seed ->
                val deal = MahjongDealer.newDeal(layout, Random(seed))
                val faces = List(layout.size) { deal.faces.getValue(it) }
                var state = MahjongGameState(layout.id, faces, faces, List(layout.size) { false })
                for ((a, b) in deal.solution) {
                    assertTrue("${layout.id} seed $seed: move $a,$b must be legal", state.canMatch(a, b))
                    state = state.removePair(a, b)
                }
                assertTrue(state.isComplete)
            }
        }
    }

    @Test fun dealsUseTilesLikeARealSet() {
        val game = MahjongGameState.newGame(MahjongLayouts.FORTRESS, Random(3))
        game.faces.groupingBy { it }.eachCount().forEach { (face, n) ->
            when (face.suit) {
                Suit.FLOWER, Suit.SEASON -> assertEquals(1, n)
                else -> assertEquals("$face", 4, n)
            }
        }
    }

    @Test fun matchingRemovesUndoRestores() {
        val game = MahjongGameState.newGame(MahjongLayouts.MEADOW, Random(1))
        val (a, b) = assertNotNull(game.findMatch()).let { game.findMatch()!! }
        val after = game.removePair(a, b)
        assertEquals(game.tilesLeft - 2, after.tilesLeft)
        assertEquals(listOf(a, b), after.history)
        assertEquals(game.removed, after.undo()!!.removed)
        assertNull(game.undo())
    }

    @Test fun illegalMovesDoNothing() {
        val game = MahjongGameState.newGame(MahjongLayouts.PYRAMID, Random(2))
        val blocked = game.faces.indices.first { !game.isFree(it) }
        val free = game.faces.indices.first { game.isFree(it) }
        assertSame(game, game.removePair(blocked, free))
        assertSame(game, game.removePair(free, free))
        val mismatch = game.faces.indices.firstOrNull { it != free && game.isFree(it) && !game.faces[it].matches(game.faces[free]) }
        if (mismatch != null) assertSame(game, game.removePair(free, mismatch))
    }

    @Test fun flowersMatchAnyFlower() {
        assertTrue(TileFace.FLOWER_1.matches(TileFace.FLOWER_4))
        assertTrue(TileFace.SEASON_2.matches(TileFace.SEASON_3))
        assertFalse(TileFace.FLOWER_1.matches(TileFace.SEASON_1))
        assertFalse(TileFace.DOTS_1.matches(TileFace.DOTS_2))
    }

    @Test fun shuffleKeepsTilesAndStaysWinnable() {
        var game = MahjongGameState.newGame(MahjongLayouts.DIAMOND, Random(5))
        repeat(10) { game.findMatch()?.let { (a, b) -> game = game.removePair(a, b) } }
        val shuffled = assertNotNull(game.shuffled(Random(9))).let { game.shuffled(Random(9))!! }
        assertEquals(game.removed, shuffled.removed)
        assertEquals(
            game.faces.filterIndexed { i, _ -> !game.removed[i] }.sorted(),
            shuffled.faces.filterIndexed { i, _ -> !game.removed[i] }.sorted(),
        )
        assertEquals(1, shuffled.shufflesUsed)
        assertTrue(shuffled.hasMoves)
    }

    @Test fun greedyPlayWithShufflesFinishesEveryBoard() {
        for (layout in MahjongLayouts.all) {
            var game = MahjongGameState.newGame(layout, Random(11))
            var guard = 0
            while (!game.isComplete && guard++ < 500) {
                val match = game.findMatch()
                game = if (match != null) game.removePair(match.first, match.second) else game.shuffled(Random(guard)) ?: break
            }
            assertTrue("${layout.id} finished", game.isComplete)
        }
    }

    @Test fun restartRestoresTheOriginalDeal() {
        var game = MahjongGameState.newGame(MahjongLayouts.MEADOW, Random(4))
        game.findMatch()!!.let { (a, b) -> game = game.removePair(a, b) }
        game = game.shuffled(Random(1))!!
        val restarted = game.restart()
        assertEquals(game.dealtFaces, restarted.faces)
        assertEquals(restarted.faces.size, restarted.tilesLeft)
    }

    @Test fun saveFormatRoundTrips() {
        val game = MahjongGameState.newGame(MahjongLayouts.MEADOW, Random(8)).let { g ->
            g.findMatch()!!.let { (a, b) -> g.removePair(a, b) }
        }
        val json = Json.encodeToString(MahjongGameState.serializer(), game)
        assertEquals(game, Json.decodeFromString(MahjongGameState.serializer(), json))
    }
}
