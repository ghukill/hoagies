package com.hoagies.games.data.memory

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {
    private fun result(variant: String, seconds: Long) =
        GameResultEntity(gameId = "g", variant = variant, durationSeconds = seconds, hintsUsed = 0, mistakes = 0, completedAt = 0)

    @Test fun statsPerVariantInGivenOrder() {
        val stats = summarizeResults(
            listOf(result("EASY", 300), result("HARD", 900), result("EASY", 100)),
            variants = listOf("GENTLE", "EASY", "MEDIUM", "HARD"),
        )
        assertEquals(listOf("EASY", "HARD"), stats.map { it.variant })
        assertEquals(2, stats[0].completed)
        assertEquals(100, stats[0].bestSeconds)
        assertEquals(200, stats[0].averageSeconds)
    }
}
