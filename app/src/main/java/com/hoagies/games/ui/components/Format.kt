package com.hoagies.games.ui.components

/** "4:05" or "1:02:03". */
fun formatClock(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** Friendlier wording for screen readers and summaries: "4 minutes 5 seconds". */
fun formatDurationWords(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    fun part(n: Long, unit: String) = "$n $unit${if (n == 1L) "" else "s"}"
    return buildList {
        if (h > 0) add(part(h, "hour"))
        if (m > 0) add(part(m, "minute"))
        if (s > 0 || (h == 0L && m == 0L)) add(part(s, "second"))
    }.joinToString(" ")
}
