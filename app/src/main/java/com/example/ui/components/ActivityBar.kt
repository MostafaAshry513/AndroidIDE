package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.viewmodel.SidebarTab

@Composable
fun ActivityBar(
    activeTab: SidebarTab,
    isSidebarOpen: Boolean,
    onTabSelected: (SidebarTab) -> Unit,
    onToggleSidebar: () -> Unit,
    onOpenPalette: () -> Unit,
    onRunScript: () -> Unit,
    onOpenWebPreview: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeActivityBar,
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp)
        ) {
            // Top action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Command Palette
                ActivityBarItem(
                    icon = Icons.Default.Code,
                    label = "Command Palette",
                    shortcut = "Ctrl+Shift+P",
                    isActive = false,
                    badgeColor = VsCodeAccent,
                    onClick = onOpenPalette,
                    testTag = "activity_palette_button"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF454545))
                )

                // 1. Explorer (Ctrl+Shift+E / Ctrl+B)
                ActivityBarItem(
                    icon = Icons.Default.Folder,
                    label = "Explorer",
                    shortcut = "Ctrl+Shift+E",
                    isActive = isSidebarOpen && activeTab == SidebarTab.EXPLORER,
                    onClick = {
                        if (isSidebarOpen && activeTab == SidebarTab.EXPLORER) {
                            onToggleSidebar()
                        } else {
                            onTabSelected(SidebarTab.EXPLORER)
                        }
                    },
                    testTag = "activity_explorer_button"
                )

                // 2. Search (Ctrl+Shift+F)
                ActivityBarItem(
                    icon = Icons.Default.Search,
                    label = "Search",
                    shortcut = "Ctrl+Shift+F",
                    isActive = isSidebarOpen && activeTab == SidebarTab.SEARCH,
                    onClick = { onTabSelected(SidebarTab.SEARCH) },
                    testTag = "activity_search_button"
                )

                // 3. Source Control / Git (Ctrl+Shift+G)
                ActivityBarItem(
                    icon = Icons.Default.Commit,
                    label = "Source Control",
                    shortcut = "Ctrl+Shift+G",
                    isActive = isSidebarOpen && activeTab == SidebarTab.SOURCE_CONTROL,
                    onClick = { onTabSelected(SidebarTab.SOURCE_CONTROL) },
                    testTag = "activity_git_button"
                )

                // 4. Run & Debug (Ctrl+Shift+D)
                ActivityBarItem(
                    icon = Icons.Default.PlayArrow,
                    label = "Run & Debug",
                    shortcut = "Ctrl+Shift+D",
                    isActive = isSidebarOpen && activeTab == SidebarTab.RUN_DEBUG,
                    badgeColor = if (isRunning) Color(0xFF4EC9B0) else null,
                    onClick = { onTabSelected(SidebarTab.RUN_DEBUG) },
                    testTag = "activity_run_button"
                )

                // 5. Extensions & Frameworks (Ctrl+Shift+X)
                ActivityBarItem(
                    icon = Icons.Default.Extension,
                    label = "Extensions",
                    shortcut = "Ctrl+Shift+X",
                    isActive = isSidebarOpen && activeTab == SidebarTab.EXTENSIONS,
                    onClick = { onTabSelected(SidebarTab.EXTENSIONS) },
                    testTag = "activity_extensions_button"
                )

                // 6. Gemini AI Copilot (Ctrl+Shift+I)
                ActivityBarItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "AI Copilot",
                    shortcut = "Ctrl+Shift+I",
                    isActive = isSidebarOpen && activeTab == SidebarTab.AI_COPILOT,
                    badgeColor = Color(0xFF63B3ED),
                    onClick = { onTabSelected(SidebarTab.AI_COPILOT) },
                    testTag = "activity_ai_copilot_button"
                )

                // 7. Live Web Browser Canvas (Ctrl+Shift+V)
                ActivityBarItem(
                    icon = Icons.Default.Language,
                    label = "Live Web Canvas",
                    shortcut = "Ctrl+Shift+V",
                    isActive = false,
                    badgeColor = Color(0xFF4EC9B0),
                    onClick = onOpenWebPreview,
                    testTag = "activity_web_preview_button"
                )
            }

            // Bottom action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Shortcuts Cheat Sheet (F2)
                ActivityBarItem(
                    icon = Icons.Default.Keyboard,
                    label = "Shortcuts",
                    shortcut = "F2",
                    isActive = isSidebarOpen && activeTab == SidebarTab.SHORTCUTS,
                    badgeColor = Color(0xFFE5C07B),
                    onClick = { onTabSelected(SidebarTab.SHORTCUTS) },
                    testTag = "activity_shortcuts_button"
                )

                // Settings (Ctrl+,)
                ActivityBarItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    shortcut = "Ctrl+,",
                    isActive = isSidebarOpen && activeTab == SidebarTab.SETTINGS,
                    onClick = { onTabSelected(SidebarTab.SETTINGS) },
                    testTag = "activity_settings_button"
                )
            }
        }
    }
}

@Composable
private fun ActivityBarItem(
    icon: ImageVector,
    label: String,
    shortcut: String,
    isActive: Boolean,
    badgeColor: Color? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Color(0xFF252526) else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        // Left active indicator strip
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .height(22.dp)
                    .background(VsCodeAccent)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = "$label ($shortcut)",
            tint = badgeColor ?: if (isActive) Color.White else VsCodeTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
