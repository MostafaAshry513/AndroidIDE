package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String = "You",
    val timeAgo: String = "just now"
)

@Composable
fun SourceControlPane(
    files: List<FileEntity>,
    activeFile: FileEntity?,
    onFileSelected: (FileEntity) -> Unit,
    onCommit: (message: String) -> Unit,
    commits: List<GitCommit>,
    currentBranch: String = "main",
    onOpenDiff: (FileEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var selectedFileForDiff by remember { mutableStateOf<FileEntity?>(null) }

    val modifiedFiles = remember(files) {
        files.filter { it.isModified || it.id > 3 }
    }

    Surface(
        color = VsCodeSidebarBg,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SOURCE CONTROL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF252526), RoundedCornerShape(4.dp))
                            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⎇ $currentBranch",
                            color = VsCodeAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row {
                    IconButton(onClick = { /* Refresh */ }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Git", tint = VsCodeTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Commit Message Box
            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                placeholder = { Text("Message (Ctrl+Enter to commit on '$currentBranch')", fontSize = 11.sp, color = VsCodeTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VsCodeAccent,
                    unfocusedBorderColor = VsCodeBorder,
                    focusedTextColor = VsCodeText,
                    unfocusedTextColor = VsCodeText,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("git_commit_input")
            )

            Spacer(Modifier.height(8.dp))

            // Commit Button
            Button(
                onClick = {
                    if (commitMessage.isNotBlank()) {
                        onCommit(commitMessage)
                        commitMessage = ""
                    }
                },
                enabled = commitMessage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VsCodeAccent,
                    disabledContainerColor = VsCodeAccent.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .testTag("git_commit_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Commit Changes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Changes Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = "CHANGES (${modifiedFiles.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (modifiedFiles.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                        ) {
                            Text("No local changes to commit", color = VsCodeTextMuted, fontSize = 11.sp)
                        }
                    }
                }

                items(modifiedFiles) { file ->
                    val isSelected = selectedFileForDiff?.id == file.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF094771) else Color.Transparent)
                            .clickable {
                                selectedFileForDiff = file
                                onFileSelected(file)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Difference, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = file.name,
                                color = VsCodeText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onOpenDiff(file) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Difference, contentDescription = "View Diff", tint = VsCodeAccent, modifier = Modifier.size(13.dp))
                            }
                            Spacer(Modifier.width(2.dp))
                            // Status badge: M (Modified) or U (Untracked)
                            Box(
                                modifier = Modifier
                                    .background(if (file.isModified) Color(0xFFE2C08D).copy(alpha = 0.2f) else Color(0xFF89D185).copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (file.isModified) "M" else "U",
                                    color = if (file.isModified) Color(0xFFE2C08D) else Color(0xFF89D185),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Recent Commits section
                item {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = VsCodeTextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "COMMITS TIMELINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeTextMuted
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                items(commits) { commit ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(commit.message, color = VsCodeText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(commit.hash, color = VsCodeAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("by ${commit.author} • ${commit.timeAgo}", color = VsCodeTextMuted, fontSize = 9.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
