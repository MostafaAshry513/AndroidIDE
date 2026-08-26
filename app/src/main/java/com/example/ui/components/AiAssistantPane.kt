package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiChatMessage
import com.example.ai.MessageSender
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextMuted

@Composable
fun AiAssistantPane(
    chatMessages: List<AiChatMessage>,
    isLoading: Boolean,
    activeFileName: String,
    activeLanguage: String,
    onSendMessage: (String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onReplaceFile: (String) -> Unit,
    onRunSnippet: (String) -> Unit,
    onClearChat: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Surface(
        color = VsCodeSidebar,
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF1F1F1F))
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Copilot",
                        tint = VsCodeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "GEMINI COPILOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E0E0),
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2D3748))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "3.5 Flash",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF63B3ED)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Chat",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Copilot",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Quick Prompt Suggestion Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181818))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuickPromptChip(
                    icon = Icons.Default.Psychology,
                    label = "Explain",
                    onClick = { onSendMessage("Explain how this $activeLanguage file works in detail.") }
                )
                QuickPromptChip(
                    icon = Icons.Default.FlashOn,
                    label = "Optimize",
                    onClick = { onSendMessage("Optimize and refactor this $activeLanguage code for maximum performance.") }
                )
                QuickPromptChip(
                    icon = Icons.Default.Science,
                    label = "Unit Tests",
                    onClick = { onSendMessage("Generate comprehensive unit tests for this $activeLanguage code.") }
                )
                QuickPromptChip(
                    icon = Icons.Default.BugReport,
                    label = "Fix Bugs",
                    onClick = { onSendMessage("Find any potential bugs, edge cases, and type safety issues in this code.") }
                )
                QuickPromptChip(
                    icon = Icons.Default.Description,
                    label = "Docs",
                    onClick = { onSendMessage("Add professional docstrings and inline comments.") }
                )
            }

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF23354E))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = VsCodeAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "CodeStudio Copilot Active",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Pair-programming on '$activeFileName' ($activeLanguage). Ask anything or use the quick action chips above.",
                                    fontSize = 10.sp,
                                    color = VsCodeTextMuted,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                items(chatMessages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        onInsertSnippet = onInsertSnippet,
                        onReplaceFile = onReplaceFile,
                        onRunSnippet = onRunSnippet,
                        onCopy = { code -> clipboardManager.setText(AnnotatedString(code)) }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = VsCodeAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Gemini is analyzing & generating code...",
                                fontSize = 10.sp,
                                color = VsCodeAccent
                            )
                        }
                    }
                }
            }

            // Input Bar at Bottom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A2D2E))
                        .border(1.dp, Color(0xFF3C3C3C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Ask Copilot or request code...",
                            color = VsCodeTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(VsCodeAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 20.dp, max = 60.dp)
                            .testTag("copilot_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val prompt = inputText.trim()
                            inputText = ""
                            onSendMessage(prompt)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (inputText.isNotBlank() && !isLoading) VsCodeAccent else Color(0xFF333333))
                        .testTag("copilot_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isLoading) Color.White else VsCodeTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPromptChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF2D2D2D))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = VsCodeAccent,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: AiChatMessage,
    onInsertSnippet: (String) -> Unit,
    onReplaceFile: (String) -> Unit,
    onRunSnippet: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val bubbleBg = if (isUser) Color(0xFF264F78) else Color(0xFF1E1E1E)
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        horizontalAlignment = align,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Sender header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Description else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isUser) Color(0xFF9CDCFE) else VsCodeAccent,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = if (isUser) "You" else "Copilot",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) Color(0xFF9CDCFE) else VsCodeAccent
            )
        }

        // Bubble Content
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bubbleBg)
                .border(1.dp, if (isUser) Color(0xFF3B6E9C) else Color(0xFF333333), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Parse markdown code blocks vs plain text
                val extractedBlocks = remember(message.text) { parseMessageBlocks(message.text) }

                for (block in extractedBlocks) {
                    if (block.isCode) {
                        CodeBlockCard(
                            code = block.content,
                            language = block.language,
                            onInsert = { onInsertSnippet(block.content) },
                            onReplace = { onReplaceFile(block.content) },
                            onRun = { onRunSnippet(block.content) },
                            onCopy = { onCopy(block.content) }
                        )
                    } else {
                        Text(
                            text = block.content,
                            fontSize = 11.sp,
                            color = Color(0xFFE0E0E0),
                            lineHeight = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    code: String,
    language: String,
    onInsert: () -> Unit,
    onReplace: () -> Unit,
    onRun: () -> Unit,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF3C3C3C), RoundedCornerShape(4.dp))
    ) {
        // Code Block Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF202020))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = language.uppercase().ifEmpty { "CODE" },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VsCodeAccent
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Copy Action
                IconButton(
                    onClick = {
                        onCopy()
                        copied = true
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (copied) Color(0xFF4EC9B0) else VsCodeTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // Code Body
        Text(
            text = code,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD4D4D4),
            lineHeight = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ActionMiniButton(
                icon = Icons.Default.Input,
                label = "Insert",
                color = VsCodeAccent,
                onClick = onInsert
            )
            ActionMiniButton(
                icon = Icons.Default.Description,
                label = "Replace File",
                color = Color(0xFFE5C07B),
                onClick = onReplace
            )
            ActionMiniButton(
                icon = Icons.Default.PlayArrow,
                label = "Run",
                color = Color(0xFF4EC9B0),
                onClick = onRun
            )
        }
    }
}

@Composable
private fun ActionMiniButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(9.dp)
        )
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private data class MessageBlock(
    val content: String,
    val isCode: Boolean,
    val language: String = ""
)

private fun parseMessageBlocks(text: String): List<MessageBlock> {
    val blocks = mutableListOf<MessageBlock>()
    val codeBlockRegex = Regex("```(\\w*)\\n([\\s\\S]*?)```")
    var lastIdx = 0

    codeBlockRegex.findAll(text).forEach { match ->
        val before = text.substring(lastIdx, match.range.first).trim()
        if (before.isNotEmpty()) {
            blocks.add(MessageBlock(before, isCode = false))
        }
        val lang = match.groupValues[1]
        val code = match.groupValues[2].trimEnd()
        blocks.add(MessageBlock(code, isCode = true, language = lang))
        lastIdx = match.range.last + 1
    }

    if (lastIdx < text.length) {
        val remaining = text.substring(lastIdx).trim()
        if (remaining.isNotEmpty()) {
            blocks.add(MessageBlock(remaining, isCode = false))
        }
    }

    if (blocks.isEmpty()) {
        blocks.add(MessageBlock(text, isCode = false))
    }
    return blocks
}
