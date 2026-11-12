package com.hoagies.games.data.memory

/** Summary of finished games for one variant (a Sudoku difficulty, a Mahjong board, …). */
data class VariantStats(
    val variant: String,
    val completed: Int,
    val bestSeconds: Long,
    val averageSeconds: Long,
)

/** Per-variant stats in the order of [variants]; variants never finished are left out. */
fun summarizeResults(results: List<GameResultEntity>, variants: List<String>): List<VariantStats> =
    variants.mapNotNull { variant ->
        val times = results.filter { it.variant == variant }.map { it.durationSeconds }
        if (times.isEmpty()) null else VariantStats(variant, times.size, times.min(), times.sum() / times.size)
    }
