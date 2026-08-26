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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorTab
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeTabActive
import com.example.ui.theme.VsCodeTabInactive
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTitleBar
import com.example.ui.theme.VsCodeYellow

@Composable
fun EditorTabBar(
    tabs: List<EditorTab>,
    activeTabFileId: Long?,
    onTabClick: (EditorTab) -> Unit,
    onTabClose: (EditorTab) -> Unit,
    onNewTabClick: () -> Unit,
    isFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeTitleBar,
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp) // Ultra-compact 28dp to save vertical screen space in landscape
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) VsCodeAccent else Color.Transparent
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 2.dp)
                        .background(VsCodeAccent, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Ctrl+4",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            tabs.forEachIndexed { index, tab ->
                val isActive = tab.fileId == activeTabFileId
                EditorTabItem(
                    tab = tab,
                    tabIndex = index + 1,
                    isActive = isActive,
                    isFocused = isFocused && isActive,
                    onClick = { onTabClick(tab) },
                    onClose = { onTabClose(tab) }
                )
            }

            // New Tab (+) button
            IconButton(
                onClick = onNewTabClick,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("tab_new_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New File (Ctrl+N)",
                    tint = VsCodeTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorTabItem(
    tab: EditorTab,
    tabIndex: Int,
    isActive: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val bgColor = if (isActive) VsCodeTabActive else VsCodeTabInactive
    val textColor = if (isActive) VsCodeText else VsCodeTextMuted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxHeight()
            .background(bgColor)
            .border(
                width = if (isFocused) 1.dp else 0.5.dp,
                color = if (isFocused) VsCodeAccent else VsCodeBorder
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .testTag("tab_item_${tab.fileId}")
    ) {
        // Alt+Number direct switch indicator (e.g. "1")
        if (tabIndex <= 9) {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(Color(0xFF2A2D2E), RoundedCornerShape(2.dp))
                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "$tabIndex",
                    color = if (isActive) VsCodeAccent else Color(0xFF858585),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // File icon
        val iconColor = when {
            tab.name.endsWith(".py") -> VsCodeYellow
            tab.name.endsWith(".js") -> Color(0xFFF7DF1E)
            tab.name.endsWith(".html") -> Color(0xFFE34F26)
            tab.name.endsWith(".json") -> Color(0xFFCBCB41)
            tab.name.endsWith(".md") -> Color(0xFF42A5F5)
            else -> VsCodeAccent
        }

        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(12.dp)
        )

        Spacer(Modifier.width(4.dp))

        // File Name
        Text(
            text = tab.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Modified indicator / Close button
        Spacer(Modifier.width(4.dp))
        if (tab.isModified) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(VsCodeAccent)
            )
            Spacer(Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Tab",
                tint = VsCodeTextMuted,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

// Vertical Side Tab Rail (Saves 100% of vertical pixels!)
@Composable
fun VerticalTabRail(
    tabs: List<EditorTab>,
    activeTabFileId: Long?,
    onTabClick: (EditorTab) -> Unit,
    onTabClose: (EditorTab) -> Unit,
    onNewTabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeTitleBar,
        modifier = modifier
            .width(140.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OPEN EDITORS",
                    color = VsCodeTextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onNewTabClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New File",
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            tabs.forEachIndexed { index, tab ->
                val isActive = tab.fileId == activeTabFileId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isActive) VsCodeTabActive else Color.Transparent)
                        .clickable { onTabClick(tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    if (index < 9) {
                        Text(
                            text = "${index + 1}",
                            color = if (isActive) VsCodeAccent else Color(0xFF6E6E6E),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(12.dp)
                        )
                    }
                    Text(
                        text = tab.name,
                        color = if (isActive) VsCodeText else VsCodeTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (tab.isModified) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(VsCodeAccent)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = VsCodeTextMuted,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { onTabClose(tab) }
                    )
                }
            }
        }
    }
}
