package com.hoagies.games.games.mahjong.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.hoagies.games.games.mahjong.engine.Suit
import com.hoagies.games.games.mahjong.engine.TileFace

/**
 * Draws the symbol on a tile face: a picture from the chosen tile set if there is one,
 * otherwise the built-in "Simple shapes" design.
 */
@Composable
fun TileFaceContent(face: TileFace, image: ImageBitmap?, tileWidth: Dp) {
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(tileWidth * 0.06f),
        )
    } else {
        SimpleTileFace(face, tileWidth)
    }
}

/**
 * "Simple shapes": a big number plus a suit symbol. Suits differ by **both** colour and shape,
 * so they stay distinguishable for colour-blind players. Tiles are always ivory, whatever the
 * app theme, so these colours are fixed.
 */
@Composable
private fun SimpleTileFace(face: TileFace, tileWidth: Dp) {
    val density = LocalDensity.current
    val style = simpleStyle(face)
    // Sized from the tile, not the font setting, so symbols always fit.
    val numberSize = with(density) { (tileWidth * 0.5f).toSp() }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (face.suit) {
            Suit.DOTS, Suit.BAMBOO, Suit.CHARACTERS, Suit.WIND -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (face.suit == Suit.WIND) "ESWN"[face.rank - 1].toString() else face.rank.toString(),
                    color = style.color,
                    fontSize = numberSize,
                    lineHeight = numberSize,
                    fontWeight = FontWeight.Bold,
                )
                Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(tileWidth * 0.3f))
            }
            Suit.DRAGON, Suit.FLOWER, Suit.SEASON ->
                Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(tileWidth * 0.66f))
        }
    }
}

private data class SimpleStyle(val icon: ImageVector, val color: Color)

private fun simpleStyle(face: TileFace): SimpleStyle = when (face.suit) {
    Suit.DOTS -> SimpleStyle(Icons.Rounded.Circle, Color(0xFF1E56B0))
    Suit.BAMBOO -> SimpleStyle(Icons.Rounded.Grass, Color(0xFF1B7A32))
    Suit.CHARACTERS -> SimpleStyle(Icons.Rounded.Star, Color(0xFFB71C1C))
    Suit.WIND -> SimpleStyle(Icons.Rounded.Air, Color(0xFF37474F))
    Suit.DRAGON -> when (face) {
        TileFace.DRAGON_RED -> SimpleStyle(Icons.Rounded.LocalFireDepartment, Color(0xFFC62828))
        TileFace.DRAGON_GREEN -> SimpleStyle(Icons.Rounded.Eco, Color(0xFF2E7D32))
        else -> SimpleStyle(Icons.Rounded.CropSquare, Color(0xFF455A64))
    }
    Suit.FLOWER -> SimpleStyle(Icons.Rounded.LocalFlorist, Color(0xFFAD1457))
    Suit.SEASON -> SimpleStyle(Icons.Rounded.WbSunny, Color(0xFFE65100))
}
