package com.hoagies.games.data.memory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * The shared "memory" every game uses: resume where you left off, and remember results.
 *
 * Writes are queued and applied in order on [appScope], so a save started as the player
 * leaves a screen still lands even after that screen's ViewModel is gone.
 */
class GameMemory(
    private val savedGames: SavedGameDao,
    private val results: GameResultDao,
    appScope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        appScope.launch {
            for (write in writes) runCatching { write() }
        }
    }

    suspend fun <T> load(gameId: String, serializer: KSerializer<T>): T? =
        savedGames.get(gameId)?.decode(serializer)

    fun <T> observe(gameId: String, serializer: KSerializer<T>): Flow<T?> =
        savedGames.observe(gameId).map { it?.decode(serializer) }

    /** Ids of games that have a game in progress. */
    fun observeSavedGameIds(): Flow<Set<String>> = savedGames.observeIds().map { it.toSet() }

    fun <T> save(gameId: String, serializer: KSerializer<T>, state: T) {
        val encoded = json.encodeToString(serializer, state)
        val now = clock()
        writes.trySend { savedGames.upsert(SavedGameEntity(gameId, encoded, now)) }
    }

    fun clear(gameId: String) {
        writes.trySend { savedGames.delete(gameId) }
    }

    fun recordResult(
        gameId: String,
        variant: String,
        durationSeconds: Long,
        hintsUsed: Int,
        mistakes: Int,
    ) {
        val result = GameResultEntity(
            gameId = gameId,
            variant = variant,
            durationSeconds = durationSeconds,
            hintsUsed = hintsUsed,
            mistakes = mistakes,
            completedAt = clock(),
        )
        writes.trySend { results.insert(result) }
    }

    fun observeResults(gameId: String): Flow<List<GameResultEntity>> = results.observeForGame(gameId)

    /** Forgets every saved game and every result. Settings are kept. */
    fun eraseEverything() {
        writes.trySend {
            savedGames.deleteAll()
            results.deleteAll()
        }
    }

    /** A save from an older app version that no longer decodes is treated as "no save". */
    private fun <T> SavedGameEntity.decode(serializer: KSerializer<T>): T? =
        runCatching { json.decodeFromString(serializer, stateJson) }.getOrNull()
}
