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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeTextMuted

data class DiffLine(
    val type: DiffType,
    val oldLineNum: Int?,
    val newLineNum: Int?,
    val content: String
)

enum class DiffType {
    ADDED, DELETED, UNCHANGED
}

@Composable
fun GitDiffModal(
    fileName: String,
    originalContent: String,
    modifiedContent: String,
    onStageAndCommit: () -> Unit,
    onDismiss: () -> Unit
) {
    val diffLines = remember(originalContent, modifiedContent) {
        computeLineDiff(originalContent, modifiedContent)
    }

    val additions = diffLines.count { it.type == DiffType.ADDED }
    val deletions = diffLines.count { it.type == DiffType.DELETED }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss)
                .padding(16.dp)
        ) {
            Surface(
                color = VsCodeBg,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF454545)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {}
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(Color(0xFF252526))
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = VsCodeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Working Tree Diff: $fileName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.3f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("+$additions", fontSize = 10.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFC62828).copy(alpha = 0.3f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("-$deletions", fontSize = 10.sp, color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = VsCodeTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Diff Table
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF181818))
                    ) {
                        items(diffLines) { line ->
                            val lineBg = when (line.type) {
                                DiffType.ADDED -> Color(0xFF1E3A1E)
                                DiffType.DELETED -> Color(0xFF3E1E1E)
                                DiffType.UNCHANGED -> Color.Transparent
                            }
                            val textColor = when (line.type) {
                                DiffType.ADDED -> Color(0xFFB5CEA8)
                                DiffType.DELETED -> Color(0xFFCE9178)
                                DiffType.UNCHANGED -> Color(0xFFCCCCCC)
                            }
                            val prefix = when (line.type) {
                                DiffType.ADDED -> "+"
                                DiffType.DELETED -> "-"
                                DiffType.UNCHANGED -> " "
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(lineBg)
                                    .padding(vertical = 1.dp)
                            ) {
                                // Old line number
                                Text(
                                    text = line.oldLineNum?.toString() ?: "",
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF6E7681),
                                    modifier = Modifier.width(36.dp).padding(start = 6.dp)
                                )
                                // New line number
                                Text(
                                    text = line.newLineNum?.toString() ?: "",
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF6E7681),
                                    modifier = Modifier.width(36.dp)
                                )
                                // Prefix
                                Text(
                                    text = prefix,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (line.type == DiffType.ADDED) Color(0xFF81C784) else if (line.type == DiffType.DELETED) Color(0xFFE57373) else Color(0xFF6E7681),
                                    modifier = Modifier.width(16.dp)
                                )
                                // Content
                                Text(
                                    text = line.content,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Action Footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF252526))
                            .padding(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Dismiss", fontSize = 11.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onStageAndCommit()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VsCodeAccent),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Commit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stage & Commit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun computeLineDiff(oldText: String, newText: String): List<DiffLine> {
    val oldLines = oldText.lines()
    val newLines = newText.lines()
    val result = mutableListOf<DiffLine>()

    var i = 0
    var j = 0
    var oldNum = 1
    var newNum = 1

    while (i < oldLines.size || j < newLines.size) {
        if (i < oldLines.size && j < newLines.size) {
            if (oldLines[i] == newLines[j]) {
                result.add(DiffLine(DiffType.UNCHANGED, oldNum++, newNum++, oldLines[i]))
                i++
                j++
            } else {
                // Modified line: Show old deleted, then new added
                result.add(DiffLine(DiffType.DELETED, oldNum++, null, oldLines[i]))
                result.add(DiffLine(DiffType.ADDED, null, newNum++, newLines[j]))
                i++
                j++
            }
        } else if (i < oldLines.size) {
            result.add(DiffLine(DiffType.DELETED, oldNum++, null, oldLines[i]))
            i++
        } else if (j < newLines.size) {
            result.add(DiffLine(DiffType.ADDED, null, newNum++, newLines[j]))
            j++
        }
    }
    return result
}
