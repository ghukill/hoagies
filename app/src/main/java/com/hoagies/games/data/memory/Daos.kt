package com.hoagies.games.data.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedGameDao {
    @Query("SELECT * FROM saved_games WHERE game_id = :gameId")
    suspend fun get(gameId: String): SavedGameEntity?

    @Query("SELECT * FROM saved_games WHERE game_id = :gameId")
    fun observe(gameId: String): Flow<SavedGameEntity?>

    @Query("SELECT game_id FROM saved_games")
    fun observeIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(entity: SavedGameEntity)

    @Query("DELETE FROM saved_games WHERE game_id = :gameId")
    suspend fun delete(gameId: String)

    @Query("DELETE FROM saved_games")
    suspend fun deleteAll()
}

@Dao
interface GameResultDao {
    @Insert
    suspend fun insert(result: GameResultEntity)

    @Query("SELECT * FROM game_results WHERE game_id = :gameId ORDER BY completed_at DESC")
    fun observeForGame(gameId: String): Flow<List<GameResultEntity>>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}
