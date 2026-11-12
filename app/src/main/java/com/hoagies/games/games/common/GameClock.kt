package com.hoagies.games.games.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A pausable seconds counter for a game. Kept apart from the game's UI state so a ticking
 * clock doesn't redraw the whole board every second.
 */
class GameClock(private val scope: CoroutineScope) {
    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed.asStateFlow()
    private var job: Job? = null

    val seconds: Long get() = _elapsed.value

    fun reset(seconds: Long) {
        _elapsed.value = seconds
    }

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                delay(1_000)
                _elapsed.update { it + 1 }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
