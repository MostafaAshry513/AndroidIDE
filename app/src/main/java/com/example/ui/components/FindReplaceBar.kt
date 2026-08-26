package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

@Composable
fun FindReplaceBar(
    searchQuery: String,
    replaceQuery: String,
    isReplaceMode: Boolean,
    matchCount: Int,
    currentMatchIndex: Int,
    onSearchQueryChange: (String) -> Unit,
    onReplaceQueryChange: (String) -> Unit,
    onToggleReplaceMode: () -> Unit,
    onNextMatch: () -> Unit,
    onPrevMatch: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeSidebar,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Find input & navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggleReplaceMode,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Toggle Replace (Ctrl+H)",
                        tint = if (isReplaceMode) VsCodeAccent else VsCodeTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Find input box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3C3C3C))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Find in file... (Ctrl+F)",
                            fontSize = 12.sp,
                            color = VsCodeTextMuted
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(color = Color.White, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(VsCodeAccent),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("find_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Match count badge
                Text(
                    text = if (searchQuery.isEmpty()) "No results" else "$matchCount found",
                    fontSize = 11.sp,
                    color = if (matchCount > 0) VsCodeText else VsCodeTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Next match
                IconButton(
                    onClick = onNextMatch,
                    enabled = matchCount > 0,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Next Match (Enter)",
                        tint = if (matchCount > 0) VsCodeText else VsCodeTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Prev match
                IconButton(
                    onClick = onPrevMatch,
                    enabled = matchCount > 0,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Previous Match (Shift+Enter)",
                        tint = if (matchCount > 0) VsCodeText else VsCodeTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Close bar
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Find (Esc)",
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Row 2: Replace controls (if expanded)
            if (isReplaceMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(32.dp))

                    // Replace input box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF3C3C3C))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (replaceQuery.isEmpty()) {
                            Text(
                                text = "Replace with...",
                                fontSize = 12.sp,
                                color = VsCodeTextMuted
                            )
                        }
                        BasicTextField(
                            value = replaceQuery,
                            onValueChange = onReplaceQueryChange,
                            textStyle = TextStyle(color = Color.White, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace),
                            cursorBrush = SolidColor(VsCodeAccent),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("replace_input_field")
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onReplaceCurrent,
                        enabled = matchCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E639C)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Replace", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onReplaceAll,
                        enabled = matchCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E639C)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("All", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
