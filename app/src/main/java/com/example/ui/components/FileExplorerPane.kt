package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.model.FileEntity
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeYellow

@Composable
fun FileExplorerPane(
    projectName: String,
    files: List<FileEntity>,
    activeFileId: Long?,
    selectedIndex: Int,
    isFocused: Boolean,
    onFileClick: (FileEntity) -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onRenameFile: (FileEntity) -> Unit,
    onDeleteFile: (FileEntity) -> Unit,
    onRunFile: (FileEntity) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeSidebar,
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight()
            .border(
                width = if (isFocused) 2.dp else 0.5.dp,
                color = if (isFocused) VsCodeAccent else VsCodeBorder
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(VsCodeSidebar)
        ) {
            // Explorer Title Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(if (isFocused) VsCodeAccent.copy(alpha = 0.12f) else VsCodeSidebar)
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EXPLORER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isFocused) VsCodeAccent else VsCodeTextMuted
                    )
                    if (isFocused) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(VsCodeAccent, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Ctrl+2",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewFileClick,
                        modifier = Modifier.size(24.dp).testTag("explorer_new_file")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New File (Ctrl+N)",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    IconButton(
                        onClick = onNewFolderClick,
                        modifier = Modifier.size(24.dp).testTag("explorer_new_folder")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder (Ctrl+Shift+N)",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    IconButton(
                        onClick = onCloseSidebar,
                        modifier = Modifier.size(24.dp).testTag("explorer_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Explorer (Ctrl+B)",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Project Root Folder Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color(0xFF222225))
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = VsCodeYellow,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = projectName.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Files Tree List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(files) { index, file ->
                    val isActive = file.id == activeFileId
                    val isKeyboardSelected = isFocused && index == selectedIndex

                    val rowBg = when {
                        isKeyboardSelected -> Color(0xFF04395E)
                        isActive -> Color(0xFF37373D)
                        else -> Color.Transparent
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(rowBg)
                            .clickable { onFileClick(file) }
                            .padding(end = 8.dp)
                            .testTag("file_item_${file.name}")
                    ) {
                        // Left active indicator strip for keyboard focus
                        if (isKeyboardSelected) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(VsCodeAccent)
                            )
                        } else {
                            Spacer(Modifier.width(3.dp))
                        }
                        Spacer(Modifier.width(5.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (file.isDirectory) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = VsCodeYellow,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else {
                                val iconColor = when {
                                    file.name.endsWith(".py") -> VsCodeYellow
                                    file.name.endsWith(".js") -> Color(0xFFF7DF1E)
                                    file.name.endsWith(".html") -> Color(0xFFE34F26)
                                    file.name.endsWith(".json") -> Color(0xFFCBCB41)
                                    file.name.endsWith(".md") -> Color(0xFF42A5F5)
                                    else -> VsCodeAccent
                                }
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = file.name,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isKeyboardSelected) Color.White else if (isActive) VsCodeText else VsCodeTextMuted,
                                fontWeight = if (isKeyboardSelected || isActive) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Action icons for file
                        if (!file.isDirectory) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename (F2)",
                                    tint = VsCodeTextMuted,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { onRenameFile(file) }
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run (F5)",
                                    tint = VsCodeGreen,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRunFile(file) }
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = VsCodeTextMuted,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { onDeleteFile(file) }
                                )
                            }
                        }
                    }
                }
            }

            // Keyboard hints footer in Explorer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFocused) VsCodeAccent.copy(alpha = 0.15f) else Color(0xFF1E1E20))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isFocused) "↑↓ Move • ⏎ Open • F2 Rename • ⌫ Del • Esc Editor" else "Ctrl+2 Focus • ↑↓ Navigate",
                    color = if (isFocused) VsCodeAccent else VsCodeTextMuted,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
