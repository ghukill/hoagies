package com.hoagies.games.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test fun clock() {
        assertEquals("0:07", formatClock(7))
        assertEquals("4:05", formatClock(245))
        assertEquals("1:02:03", formatClock(3723))
    }

    @Test fun words() {
        assertEquals("0 seconds", formatDurationWords(0))
        assertEquals("1 minute 5 seconds", formatDurationWords(65))
        assertEquals("1 hour 1 second", formatDurationWords(3601))
    }
}
