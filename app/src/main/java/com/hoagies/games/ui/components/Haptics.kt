package com.hoagies.games.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.hoagies.games.ui.theme.LocalAppSettings

/** Gentle vibration feedback that respects the user's "Vibrate on tap" setting. */
class Haptics(private val feedback: HapticFeedback, private val enabled: Boolean) {
    fun tap() {
        if (enabled) feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun celebrate() {
        if (enabled) feedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val feedback = LocalHapticFeedback.current
    val enabled = LocalAppSettings.current.haptics
    return remember(feedback, enabled) { Haptics(feedback, enabled) }
}
