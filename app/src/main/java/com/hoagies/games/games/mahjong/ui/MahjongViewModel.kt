package com.hoagies.games.games.mahjong.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.settings.MahjongSettings
import com.hoagies.games.data.settings.SettingsRepository
import com.hoagies.games.games.common.GameClock
import com.hoagies.games.games.mahjong.MahjongGame
import com.hoagies.games.games.mahjong.engine.BoardLayout
import com.hoagies.games.games.mahjong.engine.MahjongGameState
import com.hoagies.games.games.mahjong.engine.MahjongLayouts
import com.hoagies.games.games.mahjong.engine.TileFace
import com.hoagies.games.games.mahjong.tiles.TileSetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Short explanations shown in the status line after something didn't work. */
enum class MahjongNotice { BLOCKED, NO_MATCH, NO_MOVES, CANNOT_SHUFFLE }

data class MahjongUiState(
    val isLoading: Boolean = true,
    val game: MahjongGameState? = null,
    val selected: Int? = null,
    val hint: Pair<Int, Int>? = null,
    val notice: MahjongNotice? = null,
    val justWon: Boolean = false,
)

class MahjongViewModel(
    private val memory: GameMemory,
    settingsRepository: SettingsRepository,
    tileSets: TileSetRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _ui = MutableStateFlow(MahjongUiState())
    val ui: StateFlow<MahjongUiState> = _ui.asStateFlow()

    val settings: StateFlow<MahjongSettings> = settingsRepository.settings
        .map { it.mahjong }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MahjongSettings())

    /** Pictures for the chosen tile set; empty means "draw the simple shapes". */
    val tileImages: StateFlow<Map<TileFace, ImageBitmap>> = settings
        .map { it.tileSet }
        .distinctUntilChanged()
        .map { tileSets.images(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val clock = GameClock(viewModelScope)
    val elapsedSeconds: StateFlow<Long> = clock.elapsed
    private var isResumed = false

    init {
        viewModelScope.launch {
            val requested = savedStateHandle.get<String>(ARG_LAYOUT)?.let(MahjongLayouts::find)
            val saved = if (requested == null) memory.load(MahjongGame.ID, MahjongGameState.serializer()) else null
            if (saved != null && saved.isPlayable()) {
                show(saved)
            } else {
                startNewGame(requested ?: MahjongLayouts.MEADOW)
            }
        }
    }

    fun startNewGame(layout: BoardLayout) {
        clock.stop()
        _ui.update { it.copy(isLoading = true, justWon = false) }
        viewModelScope.launch {
            val game = withContext(Dispatchers.Default) { MahjongGameState.newGame(layout) }
            // Re-creating this screen later should resume this game, not deal another.
            savedStateHandle.remove<String>(ARG_LAYOUT)
            show(game)
            persist()
        }
    }

    private fun show(game: MahjongGameState) {
        clock.reset(game.elapsedSeconds)
        _ui.value = MahjongUiState(isLoading = false, game = game)
        maybeStartClock()
    }

    fun tapTile(index: Int) {
        val state = _ui.value
        val game = state.game ?: return
        if (game.isComplete || game.removed[index]) return
        val selected = state.selected
        when {
            !game.isFree(index) -> _ui.update { it.copy(notice = MahjongNotice.BLOCKED) }
            selected == null -> _ui.update { it.copy(selected = index, notice = null) }
            selected == index -> _ui.update { it.copy(selected = null, notice = null) }
            game.canMatch(selected, index) -> commit(game.removePair(selected, index))
            else -> _ui.update { it.copy(selected = index, notice = MahjongNotice.NO_MATCH) }
        }
    }

    fun undo() {
        val previous = _ui.value.game?.undo() ?: return
        commit(previous)
    }

    fun hint() {
        val game = _ui.value.game ?: return
        val (next, pair) = game.hint() ?: run {
            _ui.update { it.copy(notice = MahjongNotice.NO_MOVES) }
            return
        }
        _ui.update { it.copy(game = next, hint = pair, selected = null, notice = null) }
        persist()
    }

    fun shuffle() {
        val game = _ui.value.game ?: return
        if (game.isComplete) return
        val next = game.shuffled() ?: run {
            _ui.update { it.copy(notice = MahjongNotice.CANNOT_SHUFFLE) }
            return
        }
        commit(next)
    }

    fun restart() {
        val game = _ui.value.game ?: return
        commit(game.restart())
        maybeStartClock()
    }

    fun dismissWin() = _ui.update { it.copy(justWon = false) }

    fun onResume() {
        isResumed = true
        maybeStartClock()
    }

    fun onPause() {
        isResumed = false
        clock.stop()
        persist()
    }

    private fun commit(next: MahjongGameState) {
        val wasComplete = _ui.value.game?.isComplete == true
        _ui.update {
            it.copy(
                game = next,
                selected = null,
                hint = null,
                notice = if (!next.isComplete && !next.hasMoves) MahjongNotice.NO_MOVES else null,
            )
        }
        if (next.isComplete && !wasComplete) onWon(next) else persist()
    }

    private fun onWon(game: MahjongGameState) {
        clock.stop()
        memory.clear(MahjongGame.ID)
        memory.recordResult(
            gameId = MahjongGame.ID,
            variant = game.layoutId,
            durationSeconds = clock.seconds,
            hintsUsed = game.hintsUsed,
            mistakes = 0, // Mahjong has no wrong moves; shuffles aren't tracked in results yet.
        )
        _ui.update { it.copy(justWon = true) }
    }

    private fun persist() {
        val game = _ui.value.game ?: return
        if (!game.isComplete) {
            memory.save(MahjongGame.ID, MahjongGameState.serializer(), game.copy(elapsedSeconds = clock.seconds))
        }
    }

    private fun maybeStartClock() {
        val game = _ui.value.game ?: return
        if (isResumed && !game.isComplete) clock.start()
    }

    companion object {
        /** Must match the property name in MahjongPlayRoute. */
        const val ARG_LAYOUT = "layout"

        val Factory = viewModelFactory {
            initializer {
                MahjongViewModel(
                    appContainer.memory,
                    appContainer.settings,
                    appContainer.mahjongTileSets,
                    createSavedStateHandle(),
                )
            }
        }
    }
}

/** A save is only usable if its board still exists and matches (layouts could change in updates). */
internal fun MahjongGameState.isPlayable(): Boolean {
    val layout = MahjongLayouts.find(layoutId) ?: return false
    return faces.size == layout.size && removed.size == layout.size && !isComplete
}
