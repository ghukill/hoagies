package com.hoagies.games.games.sudoku.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoagies.games.appContainer
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.settings.SettingsRepository
import com.hoagies.games.data.settings.SudokuSettings
import com.hoagies.games.games.sudoku.SudokuGame
import com.hoagies.games.games.sudoku.engine.Difficulty
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import com.hoagies.games.games.sudoku.engine.SudokuGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SudokuUiState(
    val isLoading: Boolean = true,
    val game: SudokuGameState? = null,
    val selected: Int? = null,
    val notesMode: Boolean = false,
    val canUndo: Boolean = false,
    /** Temporarily show wrong numbers even if "show mistakes" is off (after the board fills up). */
    val revealMistakes: Boolean = false,
    val justSolved: Boolean = false,
)

class SudokuViewModel(
    private val memory: GameMemory,
    settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val generator: SudokuGenerator = SudokuGenerator(),
) : ViewModel() {

    private val _ui = MutableStateFlow(SudokuUiState())
    val ui: StateFlow<SudokuUiState> = _ui.asStateFlow()

    val settings: StateFlow<SudokuSettings> = settingsRepository.settings
        .map { it.sudoku }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SudokuSettings())

    /**
     * The clock lives apart from [ui] so the board doesn't redraw every second.
     * It is folded back into the game state whenever the game is saved.
     */
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val undoStack = ArrayDeque<SudokuGameState>()
    private var timerJob: Job? = null
    private var isResumed = false

    init {
        viewModelScope.launch {
            val requested = savedStateHandle.get<String>(ARG_DIFFICULTY)
                ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
            val saved = if (requested == null) memory.load(SudokuGame.ID, SudokuGameState.serializer()) else null
            if (saved != null && !saved.isComplete) {
                show(saved)
            } else {
                startNewGame(requested ?: Difficulty.EASY)
            }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        stopTimer()
        _ui.update { it.copy(isLoading = true, justSolved = false) }
        viewModelScope.launch {
            val puzzle = withContext(Dispatchers.Default) { generator.generate(difficulty) }
            // From now on, re-creating this screen (e.g. after Android reclaims memory)
            // should resume this puzzle rather than generate yet another one.
            savedStateHandle.remove<String>(ARG_DIFFICULTY)
            show(SudokuGameState.newGame(puzzle))
            persist()
        }
    }

    private fun show(game: SudokuGameState) {
        undoStack.clear()
        _elapsedSeconds.value = game.elapsedSeconds
        _ui.value = SudokuUiState(isLoading = false, game = game)
        maybeStartTimer()
    }

    fun select(cell: Int) = _ui.update { it.copy(selected = cell) }

    fun toggleNotesMode() = _ui.update { it.copy(notesMode = !it.notesMode) }

    fun enterDigit(digit: Int) {
        val state = _ui.value
        val game = state.game ?: return
        val cell = state.selected ?: return
        val next = if (state.notesMode) {
            game.toggleNote(cell, digit)
        } else {
            game.enter(cell, digit, autoRemoveNotes = settings.value.autoRemoveNotes)
        }
        commit(game, next)
    }

    fun erase() {
        val state = _ui.value
        val game = state.game ?: return
        val cell = state.selected ?: return
        commit(game, game.erase(cell))
    }

    fun hint() {
        val state = _ui.value
        val game = state.game ?: return
        val (next, cell) = game.hint(state.selected) ?: return
        _ui.update { it.copy(selected = cell) }
        commit(game, next)
    }

    fun undo() {
        val current = _ui.value.game ?: return
        if (current.isComplete) return
        val previous = undoStack.removeLastOrNull() ?: return
        // Undo puts numbers back, but never rewinds the hint/mistake counters.
        val restored = previous.copy(
            mistakes = current.mistakes,
            hintsUsed = current.hintsUsed,
        )
        _ui.update { it.copy(game = restored, canUndo = undoStack.isNotEmpty(), revealMistakes = false) }
        persist()
    }

    fun restart() {
        val game = _ui.value.game ?: return
        commit(game, game.restart())
    }

    fun revealMistakes() = _ui.update { it.copy(revealMistakes = true) }

    fun dismissSolved() = _ui.update { it.copy(justSolved = false) }

    /** Called by the screen as it becomes visible / hidden. The clock only runs while visible. */
    fun onResume() {
        isResumed = true
        maybeStartTimer()
    }

    fun onPause() {
        isResumed = false
        stopTimer()
        persist()
    }

    private fun commit(previous: SudokuGameState, next: SudokuGameState) {
        if (next == previous) return
        undoStack.addLast(previous)
        if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
        _ui.update { it.copy(game = next, canUndo = true, revealMistakes = false) }

        if (next.isComplete && !previous.isComplete) {
            onSolved(next)
        } else {
            persist()
        }
    }

    private fun onSolved(game: SudokuGameState) {
        stopTimer()
        undoStack.clear()
        memory.clear(SudokuGame.ID)
        memory.recordResult(
            gameId = SudokuGame.ID,
            variant = game.difficulty.name,
            durationSeconds = _elapsedSeconds.value,
            hintsUsed = game.hintsUsed,
            mistakes = game.mistakes,
        )
        _ui.update { it.copy(justSolved = true, canUndo = false, selected = null) }
    }

    private fun persist() {
        val game = _ui.value.game ?: return
        if (!game.isComplete) {
            memory.save(SudokuGame.ID, SudokuGameState.serializer(), game.copy(elapsedSeconds = _elapsedSeconds.value))
        }
    }

    private fun maybeStartTimer() {
        if (!isResumed || timerJob?.isActive == true) return
        val game = _ui.value.game ?: return
        if (game.isComplete) return
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                if (_ui.value.game?.isComplete == false) _elapsedSeconds.update { it + 1 }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        /** Must match the property name in SudokuPlayRoute. */
        const val ARG_DIFFICULTY = "difficulty"
        private const val MAX_UNDO = 500

        val Factory = viewModelFactory {
            initializer {
                SudokuViewModel(appContainer.memory, appContainer.settings, createSavedStateHandle())
            }
        }
    }
}
