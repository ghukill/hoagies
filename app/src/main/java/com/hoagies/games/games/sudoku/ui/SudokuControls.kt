package com.hoagies.games.games.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoagies.games.games.sudoku.engine.SudokuGameState
import com.hoagies.games.ui.components.BigTouchTarget

/** Undo / Notes / Hint — every tool has a word, not just an icon. */
@Composable
fun SudokuToolbar(
    canUndo: Boolean,
    notesMode: Boolean,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolButton("Undo", Icons.AutoMirrored.Rounded.Undo, onUndo, Modifier.weight(1f), enabled = canUndo)
        ToolButton(
            label = if (notesMode) "Notes: On" else "Notes: Off",
            icon = if (notesMode) Icons.Rounded.Edit else Icons.Rounded.EditOff,
            onClick = onToggleNotes,
            modifier = Modifier.weight(1f),
            active = notesMode,
            state = if (notesMode) "on" else "off",
        )
        ToolButton("Hint", Icons.Rounded.Lightbulb, onHint, Modifier.weight(1f))
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    state: String? = null,
) {
    val container = if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHigh
    val content = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .height(BigTouchTarget + 8.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clearAndSetSemantics {
                contentDescription = label.substringBefore(":")
                if (state != null) stateDescription = state
                role = Role.Button
                if (enabled) onClick { onClick(); true }
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

/** Two rows of big keys: 1–5, then 6–9 and Erase. */
@Composable
fun NumberPad(
    game: SudokuGameState,
    notesMode: Boolean,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (rowDigits in listOf(1..5, 6..9)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (d in rowDigits) {
                    DigitKey(d, game.countPlaced(d) >= 9, notesMode, { onDigit(d) }, Modifier.weight(1f))
                }
                if (rowDigits.last == 9) {
                    PadKey(
                        onClick = onErase,
                        description = "Erase",
                        container = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = null, modifier = Modifier.size(26.dp))
                        Text("Erase", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitKey(digit: Int, allPlaced: Boolean, notesMode: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    PadKey(
        onClick = onClick,
        description = buildString {
            append(digit)
            if (notesMode) append(", note")
            if (allPlaced) append(", all placed")
        },
        container = if (notesMode) scheme.secondaryContainer else scheme.primaryContainer,
        contentColor = if (notesMode) scheme.onSecondaryContainer else scheme.onPrimaryContainer,
        modifier = modifier.alpha(if (allPlaced) 0.4f else 1f),
    ) {
        Text(
            digit.toString(),
            // Fixed size: these keys are already big, and must stay one row wide at any text size.
            fontSize = if (notesMode) 24.sp else 30.sp,
            fontWeight = if (notesMode) FontWeight.Normal else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PadKey(
    onClick: () -> Unit,
    description: String,
    container: Color,
    contentColor: Color,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = container,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .height(BigTouchTarget)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick { onClick(); true }
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { content() }
    }
}
