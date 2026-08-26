package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shortcuts.ShortcutAction
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeYellow

@Composable
fun ShortcutsMobileBar(
    latchedCtrl: Boolean,
    latchedAlt: Boolean,
    latchedShift: Boolean,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onToggleShift: () -> Unit,
    onShortcutAction: (ShortcutAction) -> Unit,
    onInsertSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1B1B1E),
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp) // Ultra-compact single row
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp)
        ) {
            // Modifiers
            ModifierKeyButton("CTRL", latchedCtrl, onToggleCtrl, "bar_ctrl_key")
            Spacer(Modifier.width(3.dp))
            ModifierKeyButton("ALT", latchedAlt, onToggleAlt, "bar_alt_key")
            Spacer(Modifier.width(3.dp))
            ModifierKeyButton("SHIFT", latchedShift, onToggleShift, "bar_shift_key")

            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(1.dp).height(16.dp).background(Color(0xFF3E3E42)))
            Spacer(Modifier.width(6.dp))

            // Zero-touch focus quick jumpers
            ShortcutKeyButton("Focus ⇥ (F6)", VsCodeAccent, Color(0xFF0D3349)) { onShortcutAction(ShortcutAction.CYCLE_FOCUS_NEXT) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Editor (Ctrl+1)") { onShortcutAction(ShortcutAction.FOCUS_EDITOR) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Files (Ctrl+2)") { onShortcutAction(ShortcutAction.FOCUS_EXPLORER) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Term (Ctrl+3)") { onShortcutAction(ShortcutAction.FOCUS_TERMINAL) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("▶ Run (F5)", VsCodeGreen, Color(0xFF0F3A2E)) { onShortcutAction(ShortcutAction.RUN_SCRIPT) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Zen (F11)", VsCodeAccent, Color(0xFF0D3349)) { onShortcutAction(ShortcutAction.TOGGLE_ZEN_MODE) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Dock (C+A+S)") { onShortcutAction(ShortcutAction.TOGGLE_TERMINAL_DOCK) }
            Spacer(Modifier.width(3.dp))
            ShortcutKeyButton("Tabs (C+A+T)") { onShortcutAction(ShortcutAction.TOGGLE_TAB_LAYOUT) }

            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(1.dp).height(16.dp).background(Color(0xFF3E3E42)))
            Spacer(Modifier.width(6.dp))

            // Fast code symbols
            listOf(":", "(", ")", "[", "]", "{", "}", "=", "\"", "'", "#", "->", "==", "!=", "def", "import").forEach { sym ->
                SymbolKeyButton(sym) { onInsertSymbol(sym) }
                Spacer(Modifier.width(3.dp))
            }
        }
    }
}

@Composable
private fun ModifierKeyButton(
    label: String,
    isLatched: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (isLatched) VsCodeAccent else Color(0xFF2A2D2E)
    val textCol = if (isLatched) Color.White else VsCodeText
    val borderCol = if (isLatched) Color.White else Color(0xFF3E3E42)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .border(0.5.dp, borderCol, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
private fun ShortcutKeyButton(
    label: String,
    textColor: Color = VsCodeText,
    bgColor: Color = Color(0xFF252526),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .border(0.5.dp, Color(0xFF3E3E42), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun SymbolKeyButton(
    symbol: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF2A2D2E))
            .border(0.5.dp, Color(0xFF383838), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = VsCodeYellow
        )
    }
}
