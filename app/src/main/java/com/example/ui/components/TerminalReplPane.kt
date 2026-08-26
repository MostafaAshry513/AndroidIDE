package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.ConsoleOutput
import com.example.ui.terminal.RawTerminalView
import com.example.ui.viewmodel.TerminalDockPosition

/**
 * Terminal & Raw Shell Interface Pane.
 * Uses the lightweight RawTerminalView emulator with direct keyboard stream mapping.
 */
@Composable
fun TerminalReplPane(
    consoleOutputs: List<ConsoleOutput>,
    isRunning: Boolean,
    isWaitingForInput: Boolean,
    isPythonMode: Boolean = false,
    inputPrompt: String = "",
    dockPosition: TerminalDockPosition,
    isFocused: Boolean = false,
    availableCompletions: List<String> = emptyList(),
    onSubmitCommand: (String) -> Unit,
    onClearConsole: () -> Unit,
    onCloseTerminal: () -> Unit,
    onRunAgain: () -> Unit,
    onStopExecution: () -> Unit,
    onToggleDock: () -> Unit,
    onFocusTerminal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    RawTerminalView(
        consoleOutputs = consoleOutputs,
        isRunning = isRunning,
        isWaitingForInput = isWaitingForInput,
        isPythonMode = isPythonMode,
        inputPrompt = inputPrompt,
        dockPosition = dockPosition,
        isFocused = isFocused,
        availableCompletions = availableCompletions,
        onSubmitCommand = onSubmitCommand,
        onClearConsole = onClearConsole,
        onCloseTerminal = onCloseTerminal,
        onRunAgain = onRunAgain,
        onStopExecution = onStopExecution,
        onToggleDock = onToggleDock,
        onFocusTerminal = onFocusTerminal,
        modifier = modifier
    )
}
