package com.hoagies.games.data.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * On-device memory for every game. Players' progress matters: when the schema changes,
 * bump [version] and add a real Migration — never fall back to destructive migration.
 */
@Database(
    entities = [SavedGameEntity::class, GameResultEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class HoagiesDatabase : RoomDatabase() {
    abstract fun savedGames(): SavedGameDao
    abstract fun results(): GameResultDao

    companion object {
        fun build(context: Context): HoagiesDatabase =
            Room.databaseBuilder(context, HoagiesDatabase::class.java, "hoagies.db").build()
    }
}
