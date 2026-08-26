package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import com.example.ui.theme.VsCodeStatusBar
import com.example.ui.viewmodel.ActiveFocusArea
import com.example.ui.viewmodel.TerminalDockPosition

@Composable
fun StatusBar(
    cursorLine: Int,
    cursorColumn: Int,
    language: String,
    errorCount: Int,
    isRunning: Boolean,
    terminalDockPosition: TerminalDockPosition,
    isZenMode: Boolean,
    activeFocusArea: ActiveFocusArea = ActiveFocusArea.EDITOR,
    onToggleZenMode: () -> Unit,
    onToggleDock: () -> Unit,
    onOpenShortcutsHelp: () -> Unit,
    onOpenSettings: () -> Unit,
    onCycleFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeStatusBar,
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp) // Ultra-compact 20dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            // Left Status Items
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Engine status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isRunning) "Running $language (F5)..." else "CodeStudio Polyglot",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Error / Warning counter
                if (errorCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Errors",
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$errorCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Zen Mode shortcut tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable(onClick = onToggleZenMode)
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Zen Mode (F11)",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = if (isZenMode) "Zen [F11]" else "F11 Zen",
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }

                // Active Focus Area pill indicator (F6 cycle)
                val focusLabel = when (activeFocusArea) {
                    ActiveFocusArea.EDITOR -> "Editor [Ctrl+1]"
                    ActiveFocusArea.EXPLORER -> "Explorer [Ctrl+2]"
                    ActiveFocusArea.TERMINAL -> "Terminal [Ctrl+3]"
                    ActiveFocusArea.TABS -> "Tabs [Ctrl+4]"
                    ActiveFocusArea.SEARCH -> "Search [Ctrl+Shift+F]"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable(onClick = onCycleFocus)
                        .padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4EC9B0))
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = focusLabel,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Dock Position shortcut tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable(onClick = onToggleDock)
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dock,
                        contentDescription = "Dock (Ctrl+Alt+S)",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = if (terminalDockPosition == TerminalDockPosition.RIGHT) "Right Dock" else "Bottom Dock",
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }
            }

            // Right Status Items
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Line & Column indicator
                Text(
                    text = "Ln $cursorLine, Col $cursorColumn",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    modifier = Modifier.padding(end = 6.dp)
                )

                // Language Mode
                Text(
                    text = language.uppercase(),
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable(onClick = onOpenSettings)
                        .padding(horizontal = 4.dp)
                )

                // Shortcuts quick link (F2)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable(onClick = onOpenShortcutsHelp)
                        .padding(horizontal = 4.dp)
                        .testTag("status_shortcuts_help")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Shortcuts Help (F2)",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "F2 Map",
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
