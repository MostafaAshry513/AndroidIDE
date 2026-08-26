package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shortcuts.ShortcutAction
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeKeyBadgeBg
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

@Composable
fun CommandPaletteModal(
    onDismiss: () -> Unit,
    onActionSelected: (ShortcutAction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val allActions = remember { ShortcutAction.values().toList() }
    val filteredActions = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allActions
        } else {
            val q = searchQuery.trim().lowercase()
            allActions.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.defaultKeyDisplay.lowercase().contains(q) ||
                it.category.name.lowercase().contains(q)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = VsCodeSidebar,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header Search Input (VS Code style > prompt)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3C3C3C))
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = ">",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Type a command or search shortcuts (e.g. Run, Save, Tab)...",
                                fontSize = 12.5.sp,
                                color = VsCodeTextMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            cursorBrush = SolidColor(VsCodeAccent),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .testTag("command_palette_search_field")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    itemsIndexed(filteredActions) { index, action ->
                        CommandPaletteItem(
                            action = action,
                            onClick = {
                                onActionSelected(action)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandPaletteItem(
    action: ShortcutAction,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("palette_item_${action.name}")
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = VsCodeText
            )
            Text(
                text = "${action.category.name} • ${action.description}",
                fontSize = 11.sp,
                color = VsCodeTextMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Shortcut Key Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(VsCodeKeyBadgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = action.defaultKeyDisplay,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = VsCodeGreen
            )
        }
    }
}
