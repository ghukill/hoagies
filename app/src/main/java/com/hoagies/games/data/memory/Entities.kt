package com.hoagies.games.data.memory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One in-progress game per game type, stored as that game's own JSON. */
@Entity(tableName = "saved_games")
data class SavedGameEntity(
    @PrimaryKey @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "state_json") val stateJson: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** A finished game. `variant` is game-specific, e.g. a Sudoku difficulty. */
@Entity(tableName = "game_results", indices = [Index("game_id")])
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_id") val gameId: String,
    val variant: String,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    @ColumnInfo(name = "hints_used") val hintsUsed: Int,
    val mistakes: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long,
)
