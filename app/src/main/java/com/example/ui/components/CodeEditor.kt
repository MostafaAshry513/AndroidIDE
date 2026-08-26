package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticError
import com.example.interpreter.CodeSyntaxHighlighter
import com.example.ui.theme.EditorCodeTextStyle
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeGutter
import com.example.ui.theme.VsCodeLineNumber
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

@Composable
fun CodeEditor(
    editorValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    filePath: String,
    language: String,
    cursorLine: Int,
    diagnostics: List<DiagnosticError>,
    focusRequester: FocusRequester,
    fontSizeSp: Float = 13.5f,
    showLineNumbers: Boolean = true,
    wordWrap: Boolean = false,
    showBreadcrumbs: Boolean = true,
    isFocused: Boolean = false,
    onFontSizeZoom: ((Float) -> Unit)? = null,
    onResetFontSize: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lines = remember(editorValue.text) { editorValue.text.lines() }
    val lineCount = lines.size.coerceAtLeast(1)
    val errorLines = remember(diagnostics) { diagnostics.map { it.line }.toSet() }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Multi-Language Syntax Highlighting transformation
    val visualTransformation = remember(language) {
        VisualTransformation { text ->
            val highlighted = CodeSyntaxHighlighter.highlight(text.text, language)
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    val codeStyle = remember(fontSizeSp) {
        EditorCodeTextStyle.copy(
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.45).sp
        )
    }

    Surface(
        color = VsCodeBg,
        modifier = modifier
            .fillMaxSize()
            .border(
                width = if (isFocused) 1.5.dp else 0.5.dp,
                color = if (isFocused) VsCodeAccent else VsCodeBorder
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f && onFontSizeZoom != null) {
                        onFontSizeZoom(zoom)
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Breadcrumbs bar
            if (showBreadcrumbs && filePath.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(if (isFocused) VsCodeAccent.copy(alpha = 0.08f) else Color(0xFF1E1E1E))
                        .padding(horizontal = 8.dp)
                ) {
                    val parts = filePath.split("/")
                    parts.forEachIndexed { idx, part ->
                        Text(
                            text = part,
                            color = if (idx == parts.lastIndex) (if (isFocused) Color.White else VsCodeText) else VsCodeTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (idx == parts.lastIndex) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (idx < parts.lastIndex) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = VsCodeTextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Font Size Pill Indicator (Tap to reset)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A2D2E), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${fontSizeSp.toInt()}sp",
                            color = VsCodeTextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Language tag badge
                    Text(
                        text = language.uppercase(),
                        color = VsCodeAccent,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Editor Core: Gutter (Line Numbers) + Code Surface
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Line Numbers Gutter
                if (showLineNumbers) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(if (lineCount >= 1000) 40.dp else if (lineCount >= 100) 32.dp else 26.dp)
                            .background(VsCodeGutter)
                            .verticalScroll(verticalScrollState)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            for (i in 1..lineCount) {
                                val isCurrentLine = i == cursorLine
                                val isErrorLine = i in errorLines

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(codeStyle.lineHeight.value.dp)
                                ) {
                                    if (isErrorLine) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(VsCodeError)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = "$i",
                                        color = when {
                                            isErrorLine -> VsCodeError
                                            isCurrentLine -> VsCodeText
                                            else -> VsCodeLineNumber
                                        },
                                        fontSize = (fontSizeSp * 0.85).sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Code Input Area
                val codeModifier = if (wordWrap) {
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(verticalScrollState)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                } else {
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                }

                Box(modifier = codeModifier) {
                    BasicTextField(
                        value = editorValue,
                        onValueChange = onValueChange,
                        textStyle = codeStyle,
                        cursorBrush = SolidColor(VsCodeAccent),
                        visualTransformation = visualTransformation,
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .testTag("code_editor_input")
                    )
                }
            }
        }
    }
}
