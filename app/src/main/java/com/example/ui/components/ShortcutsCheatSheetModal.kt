package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.shortcuts.ShortcutCategory
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeKeyBadgeBg
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeYellow

@Composable
fun ShortcutsCheatSheetModal(
    onDismiss: () -> Unit,
    onExecuteAction: (ShortcutAction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ShortcutCategory?>(null) }

    val allActions = remember { ShortcutAction.values().toList() }
    val filteredActions = remember(searchQuery, selectedCategory) {
        allActions.filter { action ->
            val matchCategory = selectedCategory == null || action.category == selectedCategory
            val matchSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                action.title.lowercase().contains(q) ||
                action.description.lowercase().contains(q) ||
                action.defaultKeyDisplay.lowercase().contains(q)
            }
            matchCategory && matchSearch
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = VsCodeSidebar,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 300.dp) // Optimized for landscape screen height!
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Modal Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = VsCodeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Keyboard Shortcuts Reference (Zero-Touch)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close (Esc)",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Search Filter & Category Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .background(Color(0xFF3C3C3C), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = VsCodeTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                cursorBrush = SolidColor(VsCodeAccent),
                                textStyle = TextStyle(
                                    color = VsCodeText,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Filter shortcuts...",
                                            color = VsCodeTextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Category Filter Chips
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CategoryFilterChip(
                            label = "All",
                            isSelected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                        ShortcutCategory.values().forEach { cat ->
                            CategoryFilterChip(
                                label = cat.displayName,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Shortcuts List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(filteredActions) { action ->
                        ShortcutModalRow(
                            action = action,
                            onClick = {
                                onExecuteAction(action)
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
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) VsCodeAccent else Color(0xFF2A2D2E))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else VsCodeTextMuted
        )
    }
}

@Composable
private fun ShortcutModalRow(
    action: ShortcutAction,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF252526))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("shortcut_row_${action.name}")
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = VsCodeText
            )
            Text(
                text = action.description,
                fontSize = 9.5.sp,
                color = VsCodeTextMuted
            )
        }

        Spacer(Modifier.width(8.dp))

        // Shortcut Key Badge
        Box(
            modifier = Modifier
                .background(VsCodeKeyBadgeBg, RoundedCornerShape(3.dp))
                .border(0.5.dp, VsCodeBorder, RoundedCornerShape(3.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = action.defaultKeyDisplay,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = VsCodeYellow
            )
        }
    }
}
