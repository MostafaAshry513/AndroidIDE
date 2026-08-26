package com.example.ui.terminal

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Lightweight raw shell character stream & line editor engine.
 * Handles ANSI streams, cursor positioning, command history, and POSIX terminal shortcuts.
 */
class RawTerminalEmulator(
    private val onExecuteCommand: (String) -> Unit,
    private val onInterrupt: () -> Unit,
    private val onClear: () -> Unit,
    private val onExit: () -> Unit = {}
) {
    var inputBuffer: String = ""
        private set

    var cursorPosition: Int = 0
        private set

    val history: MutableList<String> = mutableListOf()
    private var historyIndex: Int = -1
    private var savedCurrentInput: String = ""

    fun setInput(newText: String, newCursor: Int = newText.length) {
        inputBuffer = newText
        cursorPosition = newCursor.coerceIn(0, newText.length)
    }

    fun clearInput() {
        inputBuffer = ""
        cursorPosition = 0
        historyIndex = -1
        savedCurrentInput = ""
    }

    fun insertChar(char: Char) {
        if (char == '\n' || char == '\r') {
            submitCurrentLine()
            return
        }
        val before = inputBuffer.substring(0, cursorPosition)
        val after = inputBuffer.substring(cursorPosition)
        inputBuffer = before + char + after
        cursorPosition++
    }

    fun insertText(text: String) {
        if (text.isEmpty()) return
        if (text.contains('\n') || text.contains('\r')) {
            val lines = text.split(Regex("[\r\n]+"))
            lines.forEachIndexed { index, line ->
                if (index > 0) submitCurrentLine()
                if (line.isNotEmpty()) {
                    val before = inputBuffer.substring(0, cursorPosition)
                    val after = inputBuffer.substring(cursorPosition)
                    inputBuffer = before + line + after
                    cursorPosition += line.length
                }
            }
            return
        }
        val before = inputBuffer.substring(0, cursorPosition)
        val after = inputBuffer.substring(cursorPosition)
        inputBuffer = before + text + after
        cursorPosition += text.length
    }

    fun deleteCharBeforeCursor() {
        if (cursorPosition > 0) {
            val before = inputBuffer.substring(0, cursorPosition - 1)
            val after = inputBuffer.substring(cursorPosition)
            inputBuffer = before + after
            cursorPosition--
        }
    }

    fun deleteCharAtCursor() {
        if (cursorPosition < inputBuffer.length) {
            val before = inputBuffer.substring(0, cursorPosition)
            val after = inputBuffer.substring(cursorPosition + 1)
            inputBuffer = before + after
        }
    }

    fun moveCursorLeft() {
        if (cursorPosition > 0) cursorPosition--
    }

    fun moveCursorRight() {
        if (cursorPosition < inputBuffer.length) cursorPosition++
    }

    fun moveCursorToStart() {
        cursorPosition = 0
    }

    fun moveCursorToEnd() {
        cursorPosition = inputBuffer.length
    }

    fun killLineToStart() {
        inputBuffer = inputBuffer.substring(cursorPosition)
        cursorPosition = 0
    }

    fun killLineToEnd() {
        inputBuffer = inputBuffer.substring(0, cursorPosition)
    }

    fun deleteWordBackwards() {
        if (cursorPosition == 0) return
        val textBefore = inputBuffer.substring(0, cursorPosition).trimEnd()
        val lastSpaceIndex = textBefore.lastIndexOf(' ')
        val newPos = if (lastSpaceIndex == -1) 0 else lastSpaceIndex + 1
        val after = inputBuffer.substring(cursorPosition)
        inputBuffer = inputBuffer.substring(0, newPos) + after
        cursorPosition = newPos
    }

    fun historyUp() {
        if (history.isEmpty()) return
        if (historyIndex == -1) {
            savedCurrentInput = inputBuffer
            historyIndex = history.size - 1
        } else if (historyIndex > 0) {
            historyIndex--
        }
        val item = history[historyIndex]
        inputBuffer = item
        cursorPosition = item.length
    }

    fun historyDown() {
        if (history.isEmpty() || historyIndex == -1) return
        if (historyIndex < history.size - 1) {
            historyIndex++
            val item = history[historyIndex]
            inputBuffer = item
            cursorPosition = item.length
        } else {
            historyIndex = -1
            inputBuffer = savedCurrentInput
            cursorPosition = savedCurrentInput.length
        }
    }

    fun submitCurrentLine() {
        val cmd = inputBuffer
        if (cmd.isNotBlank()) {
            history.add(cmd)
        }
        clearInput()
        onExecuteCommand(cmd)
    }

    fun handleTabCompletion(availableCompletions: List<String>) {
        if (inputBuffer.isEmpty()) return
        val lastWord = inputBuffer.substring(0, cursorPosition).substringAfterLast(' ')
        if (lastWord.isEmpty()) return

        val matches = availableCompletions.filter { it.startsWith(lastWord, ignoreCase = true) }
        if (matches.size == 1) {
            val match = matches.first()
            val beforeWord = inputBuffer.substring(0, cursorPosition - lastWord.length)
            val afterCursor = inputBuffer.substring(cursorPosition)
            val completionSuffix = match + (if (match.endsWith("/")) "" else " ")
            inputBuffer = beforeWord + completionSuffix + afterCursor
            cursorPosition = (beforeWord + completionSuffix).length
        } else if (matches.size > 1) {
            // Find longest common prefix
            var prefix = matches.first()
            for (m in matches.drop(1)) {
                prefix = prefix.commonPrefixWith(m)
            }
            if (prefix.length > lastWord.length) {
                val beforeWord = inputBuffer.substring(0, cursorPosition - lastWord.length)
                val afterCursor = inputBuffer.substring(cursorPosition)
                inputBuffer = beforeWord + prefix + afterCursor
                cursorPosition = (beforeWord + prefix).length
            }
        }
    }

    /**
     * Dispatches raw hardware key events directly to the terminal stream.
     * Returns true if the key event was consumed and processed by the raw terminal emulator.
     */
    fun processKeyEvent(event: KeyEvent, availableCompletions: List<String> = emptyList()): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        val ctrl = event.isCtrlPressed || event.isMetaPressed
        val shift = event.isShiftPressed
        val alt = event.isAltPressed
        val key = event.key

        // Terminal Control Signals (Ctrl+*)
        if (ctrl && !alt && !shift) {
            when (key) {
                Key.C -> {
                    onInterrupt()
                    clearInput()
                    return true
                }
                Key.L -> {
                    onClear()
                    return true
                }
                Key.D -> {
                    if (inputBuffer.isEmpty()) {
                        onExit()
                    } else {
                        deleteCharAtCursor()
                    }
                    return true
                }
                Key.A -> {
                    moveCursorToStart()
                    return true
                }
                Key.E -> {
                    moveCursorToEnd()
                    return true
                }
                Key.U -> {
                    killLineToStart()
                    return true
                }
                Key.K -> {
                    killLineToEnd()
                    return true
                }
                Key.W -> {
                    deleteWordBackwards()
                    return true
                }
            }
        }

        // Navigation & Editing Keys
        when (key) {
            Key.Enter, Key.NumPadEnter -> {
                submitCurrentLine()
                return true
            }
            Key.Backspace -> {
                deleteCharBeforeCursor()
                return true
            }
            Key.Delete -> {
                deleteCharAtCursor()
                return true
            }
            Key.DirectionLeft -> {
                moveCursorLeft()
                return true
            }
            Key.DirectionRight -> {
                moveCursorRight()
                return true
            }
            Key.DirectionUp -> {
                historyUp()
                return true
            }
            Key.DirectionDown -> {
                historyDown()
                return true
            }
            Key.MoveHome -> {
                moveCursorToStart()
                return true
            }
            Key.MoveEnd -> {
                moveCursorToEnd()
                return true
            }
            Key.Tab -> {
                handleTabCompletion(availableCompletions)
                return true
            }
        }

        return false
    }
}
