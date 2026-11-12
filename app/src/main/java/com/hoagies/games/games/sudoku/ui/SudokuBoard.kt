package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.hoagies.games.data.settings.SudokuSettings
import com.hoagies.games.games.sudoku.engine.Grid
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import com.hoagies.games.ui.theme.LocalAppSettings

@Immutable
private data class BoardColors(
    val cell: Color,
    val peer: Color,
    val sameNumber: Color,
    val selected: Color,
    val wrongBackground: Color,
    val given: Color,
    val entered: Color,
    val wrongOnBackground: Color,
    val selectionBorder: Color,
    val note: Color,
    val thinLine: Color,
    val thickLine: Color,
)

private fun boardColors(scheme: ColorScheme, highContrast: Boolean): BoardColors {
    val cell = scheme.surfaceContainerLowest
    val isDark = cell.luminance() < 0.5f
    val peerAlpha = when {
        highContrast -> 0.2f
        isDark -> 0.16f
        else -> 0.09f
    }
    return BoardColors(
        cell = cell,
        peer = scheme.primary.copy(alpha = peerAlpha).compositeOver(cell),
        sameNumber = scheme.secondaryContainer,
        selected = scheme.primaryContainer,
        wrongBackground = scheme.errorContainer,
        given = scheme.onSurface,
        entered = scheme.primary,
        wrongOnBackground = scheme.onErrorContainer,
        selectionBorder = scheme.primary,
        note = scheme.onSurfaceVariant,
        thinLine = if (highContrast) scheme.onSurface else scheme.outline,
        thickLine = scheme.onSurface,
    )
}

/**
 * The 9×9 grid. Each square is its own accessible element, so TalkBack users hear
 * e.g. "Row 3, column 5. 7, starting number".
 */
@Composable
fun SudokuBoard(
    game: SudokuGameState,
    selected: Int?,
    settings: SudokuSettings,
    revealMistakes: Boolean,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highContrast = LocalAppSettings.current.highContrast
    val scheme = MaterialTheme.colorScheme
    val colors = remember(scheme, highContrast) { boardColors(scheme, highContrast) }
    val showWrong = settings.showMistakes || revealMistakes
    val selectedValue = selected?.let { game.values[it] }?.takeIf { it != 0 }

    BoxWithConstraints(modifier.aspectRatio(1f)) {
        val cellSize = maxWidth / Grid.SIZE
        // Sized from the square itself (not the font setting), so digits always fit.
        val density = LocalDensity.current
        val digitSize = with(density) { (cellSize * 0.62f).toSp() }
        val noteSize = with(density) { (cellSize * 0.27f).toSp() }

        Column {
            for (r in 0 until Grid.SIZE) {
                Row {
                    for (c in 0 until Grid.SIZE) {
                        val i = Grid.index(r, c)
                        val value = game.values[i]
                        val wrong = showWrong && game.isWrong(i)
                        val background = when {
                            wrong -> colors.wrongBackground
                            i == selected -> colors.selected
                            settings.highlightSameNumbers && selectedValue != null && value == selectedValue ->
                                colors.sameNumber
                            selected != null && Grid.isPeer(i, selected) -> colors.peer
                            else -> colors.cell
                        }
                        Cell(
                            game = game,
                            cell = i,
                            size = cellSize,
                            background = background,
                            textColor = when {
                                game.isGiven(i) -> colors.given
                                wrong -> colors.wrongOnBackground
                                else -> colors.entered
                            },
                            noteColor = colors.note,
                            digitSize = digitSize,
                            noteSize = noteSize,
                            isSelected = i == selected,
                            isWrong = wrong,
                            onClick = { onCellClick(i) },
                        )
                    }
                }
            }
        }

        // Grid lines are drawn on top of the cells; thick lines mark the 3×3 boxes.
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / Grid.SIZE
            val thin = (if (highContrast) 1.5.dp else 1.dp).toPx()
            val thick = (if (highContrast) 4.dp else 3.dp).toPx()
            for (k in 1 until Grid.SIZE) {
                val isBox = k % 3 == 0
                val color = if (isBox) colors.thickLine else colors.thinLine
                val width = if (isBox) thick else thin
                val p = k * step
                drawLine(color, Offset(p, 0f), Offset(p, size.height), width)
                drawLine(color, Offset(0f, p), Offset(size.width, p), width)
            }
            // The selected square always gets a bold outline, whatever its fill colour.
            if (selected != null) {
                val inset = thick / 2
                drawRect(
                    colors.selectionBorder,
                    topLeft = Offset(Grid.col(selected) * step + inset, Grid.row(selected) * step + inset),
                    size = androidx.compose.ui.geometry.Size(step - thick, step - thick),
                    style = Stroke(thick),
                )
            }
            drawRect(
                colors.thickLine,
                topLeft = Offset(thick / 2, thick / 2),
                size = androidx.compose.ui.geometry.Size(size.width - thick, size.height - thick),
                style = Stroke(thick),
            )
        }
    }
}

@Composable
private fun Cell(
    game: SudokuGameState,
    cell: Int,
    size: Dp,
    background: Color,
    textColor: Color,
    noteColor: Color,
    digitSize: TextUnit,
    noteSize: TextUnit,
    isSelected: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
) {
    val value = game.values[cell]
    val description = describeCell(game, cell, isWrong)
    Box(
        Modifier
            .size(size)
            .background(background)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                selected = isSelected
                role = Role.Button
                onClick(label = "Select") { onClick(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = digitSize,
                fontWeight = if (game.isGiven(cell)) FontWeight.Bold else FontWeight.Medium,
            )
        } else if (game.notes[cell] != 0) {
            Column(Modifier.fillMaxSize()) {
                for (nr in 0 until 3) {
                    Row(Modifier.weight(1f)) {
                        for (nc in 0 until 3) {
                            val d = nr * 3 + nc + 1
                            Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (game.hasNote(cell, d)) {
                                    Text(d.toString(), color = noteColor, fontSize = noteSize, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun describeCell(game: SudokuGameState, cell: Int, isWrong: Boolean): String {
    val where = "Row ${Grid.row(cell) + 1}, column ${Grid.col(cell) + 1}"
    val value = game.values[cell]
    val what = when {
        game.isGiven(cell) -> "$value, starting number"
        value != 0 && isWrong -> "$value, not correct"
        value != 0 -> "$value"
        game.notes[cell] != 0 -> "empty, notes ${Grid.digitsIn(game.notes[cell]).joinToString(", ")}"
        else -> "empty"
    }
    return "$where. $what"
}
