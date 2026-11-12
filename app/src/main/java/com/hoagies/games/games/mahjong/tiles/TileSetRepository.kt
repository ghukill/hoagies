package com.hoagies.games.games.mahjong.tiles

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.hoagies.games.games.mahjong.engine.TileFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class TileSetInfo(val id: String, val name: String)

/**
 * Finds the tile sets the player can choose from.
 *
 * "Simple shapes" is always available and drawn in code. Picture sets are discovered from
 * `assets/mahjong/tilesets/<id>/`, one image per face named after [TileFace.fileKey]
 * (e.g. `dots_3.png`, `wind_east.webp`). See the README in that folder. A face with no image
 * falls back to the simple design, so a partial set still works.
 */
class TileSetRepository(private val assets: AssetManager) {

    private val mutex = Mutex()
    private var discovered: List<TileSetInfo>? = null
    private val loaded = HashMap<String, Map<TileFace, ImageBitmap>>()

    suspend fun available(): List<TileSetInfo> = listOf(SIMPLE) + discover()

    /** Images for [id], keyed by face. Empty for the simple set or an unknown id. */
    suspend fun images(id: String): Map<TileFace, ImageBitmap> {
        if (id == SIMPLE.id) return emptyMap()
        return mutex.withLock {
            loaded.getOrPut(id) { withContext(Dispatchers.IO) { loadImages(id) } }
        }
    }

    private suspend fun discover(): List<TileSetInfo> = mutex.withLock {
        discovered ?: withContext(Dispatchers.IO) {
            val dirs = assets.list(ROOT).orEmpty().filter { assets.list("$ROOT/$it").orEmpty().isNotEmpty() }
            dirs.sorted().map { id -> TileSetInfo(id, readName(id)) }
        }.also { discovered = it }
    }

    private fun readName(id: String): String =
        runCatching { assets.open("$ROOT/$id/name.txt").bufferedReader().use { it.readText().trim() } }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: id.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }

    private fun loadImages(id: String): Map<TileFace, ImageBitmap> {
        val files = assets.list("$ROOT/$id").orEmpty()
        val result = HashMap<TileFace, ImageBitmap>()
        for (face in TileFace.entries) {
            val file = EXTENSIONS.map { "${face.fileKey}.$it" }.firstOrNull { it in files } ?: continue
            runCatching {
                assets.open("$ROOT/$id/$file").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            }.getOrNull()?.let { result[face] = it }
        }
        return result
    }

    companion object {
        val SIMPLE = TileSetInfo("simple", "Simple shapes")
        private const val ROOT = "mahjong/tilesets"
        private val EXTENSIONS = listOf("png", "webp", "jpg", "jpeg")
    }
}
