package com.hoagies.games.games.mahjong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.hoagies.games.games.mahjong.engine.MahjongGameState
import com.hoagies.games.games.mahjong.engine.TileFace
import com.hoagies.games.games.mahjong.tiles.TileFaceContent
import com.hoagies.games.ui.theme.LocalAppSettings

private const val TILE_ASPECT = 1.3f // height / width
private const val DEPTH = 0.12f // visible tile edge, as a fraction of tile width

private data class TileColors(
    val face: Color,
    val edge: Color,
    val border: Color,
    val selectedFace: Color,
    val selectedBorder: Color,
    val hintFace: Color,
    val hintBorder: Color,
    val stuckShade: Color,
)

private val StandardTiles = TileColors(
    face = Color(0xFFFFF8E7), edge = Color(0xFFC9B48A), border = Color(0xFFA8916A),
    selectedFace = Color(0xFFFFE08A), selectedBorder = Color(0xFF1D4F91),
    hintFace = Color(0xFFCDEFD0), hintBorder = Color(0xFF2E6B3A),
    stuckShade = Color.Black.copy(alpha = 0.30f),
)

private val HighContrastTiles = TileColors(
    face = Color.White, edge = Color(0xFF8C8C8C), border = Color.Black,
    selectedFace = Color(0xFFFFE500), selectedBorder = Color.Black,
    hintFace = Color(0xFF9CF0A6), hintBorder = Color.Black,
    stuckShade = Color.Black.copy(alpha = 0.45f),
)

/**
 * The stacked tiles, scaled to fit the space available. Higher layers are drawn shifted
 * up-left with a visible edge, so the stack reads as 3D. Each tile is its own accessible,
 * tappable element; upper tiles naturally sit on top for touch as well as drawing.
 */
@Composable
fun MahjongBoard(
    game: MahjongGameState,
    selected: Int?,
    hint: Pair<Int, Int>?,
    dimStuckTiles: Boolean,
    images: Map<TileFace, ImageBitmap>,
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = game.layout
    val highContrast = LocalAppSettings.current.highContrast
    val colors = if (highContrast) HighContrastTiles else StandardTiles
    val drawOrder = remember(layout) {
        layout.positions.indices.sortedWith(
            compareBy({ layout.positions[it].z }, { layout.positions[it].y }, { layout.positions[it].x }),
        )
    }

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val cols = layout.widthUnits / 2f
        val rows = layout.heightUnits / 2f
        val layers = layout.maxZ + 1
        val tileW: Dp = min(maxWidth / (cols + DEPTH * layers), maxHeight / (rows * TILE_ASPECT + DEPTH * layers))
        val tileH = tileW * TILE_ASPECT
        val depth = tileW * DEPTH

        Box(Modifier.size(tileW * cols + depth * layers, tileH * rows + depth * layers)) {
            for (i in drawOrder) {
                if (game.removed[i]) continue
                val p = layout.positions[i]
                val lift = depth * (layout.maxZ - p.z)
                val free = game.isFree(i)
                key(i) {
                    Tile(
                        face = game.faces[i],
                        image = images[game.faces[i]],
                        width = tileW,
                        height = tileH,
                        depth = depth,
                        free = free,
                        isSelected = i == selected,
                        isHinted = hint != null && (i == hint.first || i == hint.second),
                        dim = dimStuckTiles && !free,
                        colors = colors,
                        onClick = { onTileClick(i) },
                        modifier = Modifier.offset(x = tileW * (p.x / 2f) + lift, y = tileH * (p.y / 2f) + lift),
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    face: TileFace,
    image: ImageBitmap?,
    width: Dp,
    height: Dp,
    depth: Dp,
    free: Boolean,
    isSelected: Boolean,
    isHinted: Boolean,
    dim: Boolean,
    colors: TileColors,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val corner = width * 0.12f
    val shape = RoundedCornerShape(corner)
    val faceColor = when {
        isSelected -> colors.selectedFace
        isHinted -> colors.hintFace
        else -> colors.face
    }
    val (borderWidth, borderColor) = when {
        isSelected -> 3.dp to colors.selectedBorder
        isHinted -> 3.dp to colors.hintBorder
        else -> 1.5.dp to colors.border
    }
    val description = buildString {
        append(face.spokenName)
        append(if (free) ", free" else ", stuck")
        if (isSelected) append(", selected")
        if (isHinted) append(", hint")
    }

    Box(
        modifier
            .size(width, height)
            .drawBehind {
                // The tile's thickness, peeking out below and to the right.
                drawRoundRect(
                    color = colors.edge,
                    topLeft = Offset(depth.toPx(), depth.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(corner.toPx()),
                )
            }
            .clip(shape)
            .background(faceColor)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                selected = isSelected
                role = Role.Button
                onClick { onClick(); true }
            },
    ) {
        TileFaceContent(face, image, width)
        if (dim) Box(Modifier.size(width, height).background(colors.stuckShade))
    }
}
