package com.example.ui.terminal

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConsoleOutput
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeYellow
import com.example.ui.viewmodel.TerminalDockPosition

@Composable
fun RawTerminalView(
    consoleOutputs: List<ConsoleOutput>,
    isRunning: Boolean,
    isWaitingForInput: Boolean,
    isPythonMode: Boolean = false,
    inputPrompt: String = "",
    dockPosition: TerminalDockPosition = TerminalDockPosition.BOTTOM,
    isFocused: Boolean = false,
    availableCompletions: List<String> = emptyList(),
    onSubmitCommand: (String) -> Unit,
    onClearConsole: () -> Unit = {},
    onCloseTerminal: () -> Unit = {},
    onRunAgain: () -> Unit = {},
    onStopExecution: () -> Unit = {},
    onToggleDock: () -> Unit = {},
    onFocusTerminal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val terminalFocusRequester = remember { FocusRequester() }
    var hasInternalFocus by remember { mutableStateOf(false) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    val emulator = remember {
        RawTerminalEmulator(
            onExecuteCommand = { cmd ->
                textFieldValue = TextFieldValue("")
                onSubmitCommand(cmd)
            },
            onInterrupt = {
                textFieldValue = TextFieldValue("")
                onStopExecution()
            },
            onClear = {
                textFieldValue = TextFieldValue("")
                onClearConsole()
            },
            onExit = {
                textFieldValue = TextFieldValue("")
                if (isPythonMode) {
                    onSubmitCommand("exit()")
                } else {
                    onCloseTerminal()
                }
            }
        )
    }

    // Blinking cursor transition
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // Request keyboard focus immediately
    LaunchedEffect(Unit) {
        try {
            terminalFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Auto-scroll on new output or text typing
    LaunchedEffect(consoleOutputs.size, textFieldValue.text) {
        val total = consoleOutputs.size + 1
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    val shellPrompt = when {
        isWaitingForInput -> if (inputPrompt.isNotBlank()) inputPrompt else "input() > "
        isPythonMode -> ">>> "
        else -> "$ "
    }

    val promptColor = when {
        isWaitingForInput -> VsCodeYellow
        isPythonMode -> VsCodeAccent
        else -> VsCodeGreen
    }

    Surface(
        color = Color(0xFF0A0A0C),
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onFocusTerminal()
                try {
                    terminalFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Clean Terminal Stream
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (consoleOutputs.isEmpty()) {
                    item {
                        Text(
                            text = "Linux aarch64 (tty1)\nType commands or run scripts. Type 'help' for available tools.\n",
                            color = Color(0xFF707680),
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                } else {
                    items(consoleOutputs) { output ->
                        val parsedAnnotated = AnsiColorParser.parse(
                            text = output.text,
                            defaultColor = when {
                                output.isError -> VsCodeError
                                output.isSystem -> Color(0xFF4EC9B0)
                                else -> Color(0xFFD4D4D4)
                            }
                        )
                        Text(
                            text = parsedAnnotated,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Interactive Prompt & Live Cursor Line
                item {
                    val currentText = textFieldValue.text

                    val promptLineAnnotated = buildAnnotatedString {
                        withStyle(SpanStyle(color = promptColor, fontWeight = FontWeight.Bold)) {
                            append(shellPrompt)
                        }
                        withStyle(SpanStyle(color = Color(0xFFF0F0F0))) {
                            append(currentText)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = promptLineAnnotated,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )

                        // Blinking cursor block
                        Box(
                            modifier = Modifier
                                .padding(start = 1.dp)
                                .size(width = 8.dp, height = 15.dp)
                                .alpha(if (hasInternalFocus || isFocused) cursorAlpha else 0.5f)
                                .background(if (isWaitingForInput) VsCodeYellow else Color(0xFFCCCCCC))
                        )
                    }
                }
            }

            // Invisible full-window touch & keyboard receiver
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    emulator.setInput(newValue.text, newValue.selection.start)
                },
                singleLine = true,
                cursorBrush = SolidColor(Color.Transparent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Send,
                    autoCorrect = false
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        val commandToRun = textFieldValue.text
                        if (commandToRun.isNotBlank() || isWaitingForInput) {
                            onSubmitCommand(commandToRun)
                            textFieldValue = TextFieldValue("")
                            emulator.clearInput()
                        }
                    }
                ),
                textStyle = TextStyle(
                    color = Color.Transparent,
                    fontSize = 1.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .focusRequester(terminalFocusRequester)
                    .onFocusChanged { hasInternalFocus = it.isFocused }
                    .testTag("raw_terminal_input_receiver")
                    .onPreviewKeyEvent { keyEvent ->
                        val handled = emulator.processKeyEvent(keyEvent, availableCompletions)
                        if (handled) {
                            textFieldValue = TextFieldValue(
                                text = emulator.inputBuffer,
                                selection = TextRange(emulator.cursorPosition)
                            )
                            return@onPreviewKeyEvent true
                        }
                        false
                    }
            )
        }
    }
}
