package com.hoagies.games

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.hoagies.games.data.memory.GameMemory
import com.hoagies.games.data.memory.HoagiesDatabase
import com.hoagies.games.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Hand-rolled dependency container; one per process. */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database = HoagiesDatabase.build(context)

    val settings = SettingsRepository(context.settingsDataStore)
    val memory = GameMemory(database.savedGames(), database.results(), appScope)
}
