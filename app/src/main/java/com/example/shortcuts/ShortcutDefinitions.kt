package com.example.shortcuts

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

enum class ShortcutCategory(val displayName: String) {
    GENERAL("General"),
    EDITOR("Editor"),
    NAVIGATION("Navigation"),
    FOCUS("Focus"),
    RUN_DEBUG("Run & Debug"),
    TERMINAL("Terminal"),
    LAYOUT("Layout")
}

enum class ShortcutAction(
    val title: String,
    val description: String,
    val defaultKeyDisplay: String,
    val category: ShortcutCategory
) {
    // RUN / DEBUG
    RUN_SCRIPT(
        "Run Active Script",
        "Executes current file in the embedded runner",
        "F5 / Ctrl+Enter",
        ShortcutCategory.RUN_DEBUG
    ),
    RUN_REPL_LINE(
        "Send Line to REPL",
        "Evaluates the current line or selection in the interactive shell",
        "Ctrl+Shift+Enter",
        ShortcutCategory.RUN_DEBUG
    ),

    // GENERAL
    COMMAND_PALETTE(
        "Command Palette",
        "Universal searchable list of all IDE commands",
        "Ctrl+Shift+P / F1",
        ShortcutCategory.GENERAL
    ),
    QUICK_OPEN_FILE(
        "Quick Open File",
        "Fuzzy search and jump to any project file",
        "Ctrl+P",
        ShortcutCategory.NAVIGATION
    ),
    GLOBAL_SEARCH(
        "Search in Project",
        "Find text occurrences across all project files",
        "Ctrl+Shift+F",
        ShortcutCategory.NAVIGATION
    ),
    TOGGLE_SOURCE_CONTROL(
        "Source Control (Git)",
        "View file diffs, branches, and commit code",
        "Ctrl+Shift+G",
        ShortcutCategory.NAVIGATION
    ),
    TOGGLE_RUN_DEBUG(
        "Run & Debug View",
        "Multi-language execution control and diagnostics",
        "Ctrl+Shift+D",
        ShortcutCategory.RUN_DEBUG
    ),
    TOGGLE_EXTENSIONS(
        "Extensions & Snippets",
        "Framework templates, toolkits, and language snippets",
        "Ctrl+Shift+X",
        ShortcutCategory.GENERAL
    ),
    NEW_PROJECT_TEMPLATE(
        "New Project from Template",
        "Create React, Rust, Go, Python, C++, or Fullstack projects",
        "Ctrl+Alt+N",
        ShortcutCategory.GENERAL
    ),
    SHORTCUTS_HELP(
        "Shortcuts Cheat Sheet",
        "Open keyboard shortcuts map and key tester",
        "F2 / Ctrl+K Ctrl+S",
        ShortcutCategory.GENERAL
    ),
    OPEN_SETTINGS(
        "Editor Settings",
        "Configure font size, tab spacing, line numbers, word wrap",
        "Ctrl+,",
        ShortcutCategory.GENERAL
    ),
    ESCAPE(
        "Dismiss / Return to Editor",
        "Close modals, find bar, or return focus to editor",
        "Esc",
        ShortcutCategory.GENERAL
    ),

    // FOCUS SWITCHING (100% Zero-Touch)
    FOCUS_EDITOR(
        "Focus Editor",
        "Move keyboard focus straight to the code editor canvas",
        "Ctrl+1",
        ShortcutCategory.FOCUS
    ),
    FOCUS_EXPLORER(
        "Focus File Explorer",
        "Move keyboard focus to the file tree for arrow navigation",
        "Ctrl+2 / Ctrl+0",
        ShortcutCategory.FOCUS
    ),
    FOCUS_TERMINAL(
        "Focus Terminal / REPL",
        "Move keyboard focus directly to the terminal input",
        "Ctrl+3",
        ShortcutCategory.FOCUS
    ),
    FOCUS_TABS(
        "Focus Tab Bar",
        "Move keyboard focus to cycle open file tabs",
        "Ctrl+4",
        ShortcutCategory.FOCUS
    ),
    CYCLE_FOCUS_NEXT(
        "Cycle Focus Forward",
        "Switch keyboard focus to the next pane (Explorer -> Editor -> Terminal)",
        "F6 / Ctrl+Alt+Right",
        ShortcutCategory.FOCUS
    ),
    CYCLE_FOCUS_PREV(
        "Cycle Focus Backward",
        "Switch keyboard focus to the previous pane (Terminal -> Editor -> Explorer)",
        "Shift+F6 / Ctrl+Alt+Left",
        ShortcutCategory.FOCUS
    ),

    // LAYOUT & SCREEN SPACE
    TOGGLE_ZEN_MODE(
        "Toggle Zen Mode (Fullscreen)",
        "Hide all bars and panels to maximize 100% screen space for code",
        "F11 / Ctrl+M",
        ShortcutCategory.LAYOUT
    ),
    TOGGLE_TERMINAL_DOCK(
        "Toggle Terminal Dock (Side / Bottom)",
        "Switch terminal between side-by-side right pane and bottom dock",
        "Ctrl+Alt+S",
        ShortcutCategory.LAYOUT
    ),
    TOGGLE_TAB_LAYOUT(
        "Toggle Tab Layout (Horizontal / Vertical)",
        "Switch tabs between top bar and zero-vertical-height side rail",
        "Ctrl+Alt+T",
        ShortcutCategory.LAYOUT
    ),
    TOGGLE_MOBILE_ASSIST_BAR(
        "Toggle Touch Assist Bar",
        "Show or hide the bottom mobile touch modifier bar",
        "F12 / Ctrl+Shift+U",
        ShortcutCategory.LAYOUT
    ),

    // NAVIGATION & SIDEBARS
    TOGGLE_EXPLORER(
        "Toggle File Explorer",
        "Show or hide the project files tree sidebar",
        "Ctrl+B",
        ShortcutCategory.NAVIGATION
    ),
    TOGGLE_TERMINAL(
        "Toggle Terminal Panel",
        "Show or hide the integrated output and REPL panel",
        "Ctrl+` / Ctrl+J",
        ShortcutCategory.TERMINAL
    ),
    GOTO_LINE(
        "Go to Line",
        "Jump directly to line number",
        "Ctrl+G",
        ShortcutCategory.NAVIGATION
    ),
    CLOSE_TAB(
        "Close Active Tab",
        "Closes the currently active editor tab",
        "Ctrl+W",
        ShortcutCategory.NAVIGATION
    ),
    REOPEN_CLOSED_TAB(
        "Reopen Closed Tab",
        "Reopens the most recently closed tab",
        "Ctrl+Shift+T",
        ShortcutCategory.NAVIGATION
    ),
    NEXT_TAB(
        "Next Editor Tab",
        "Switch to the next tab on the right",
        "Ctrl+Tab / Ctrl+PageDown",
        ShortcutCategory.NAVIGATION
    ),
    PREV_TAB(
        "Previous Editor Tab",
        "Switch to the previous tab on the left",
        "Ctrl+Shift+Tab / Ctrl+PageUp",
        ShortcutCategory.NAVIGATION
    ),

    // TAB DIRECT SWITCH (Alt+1 .. Alt+9)
    TAB_1("Switch to Tab 1", "Jump to the first open tab", "Alt+1", ShortcutCategory.NAVIGATION),
    TAB_2("Switch to Tab 2", "Jump to the second open tab", "Alt+2", ShortcutCategory.NAVIGATION),
    TAB_3("Switch to Tab 3", "Jump to the third open tab", "Alt+3", ShortcutCategory.NAVIGATION),
    TAB_4("Switch to Tab 4", "Jump to the fourth open tab", "Alt+4", ShortcutCategory.NAVIGATION),
    TAB_5("Switch to Tab 5", "Jump to the fifth open tab", "Alt+5", ShortcutCategory.NAVIGATION),
    TAB_6("Switch to Tab 6", "Jump to the sixth open tab", "Alt+6", ShortcutCategory.NAVIGATION),
    TAB_7("Switch to Tab 7", "Jump to the seventh open tab", "Alt+7", ShortcutCategory.NAVIGATION),
    TAB_8("Switch to Tab 8", "Jump to the eighth open tab", "Alt+8", ShortcutCategory.NAVIGATION),
    TAB_9("Switch to Tab 9", "Jump to the last open tab", "Alt+9", ShortcutCategory.NAVIGATION),

    // EDITOR ACTIONS
    SAVE_FILE(
        "Save Active Buffer",
        "Persists editor changes to project storage",
        "Ctrl+S",
        ShortcutCategory.EDITOR
    ),
    FIND_IN_FILE(
        "Find in File",
        "Open inline search bar",
        "Ctrl+F",
        ShortcutCategory.EDITOR
    ),
    REPLACE_IN_FILE(
        "Find and Replace",
        "Open inline search & replace bar",
        "Ctrl+H",
        ShortcutCategory.EDITOR
    ),
    TOGGLE_COMMENT(
        "Toggle Line Comment",
        "Comment / uncomment active line",
        "Ctrl+/",
        ShortcutCategory.EDITOR
    ),
    INDENT(
        "Indent (4 spaces)",
        "Insert 4 spaces at cursor or indent selection",
        "Tab",
        ShortcutCategory.EDITOR
    ),
    OUTDENT(
        "Outdent / Dedent",
        "Remove leading indentation",
        "Shift+Tab",
        ShortcutCategory.EDITOR
    ),
    DUPLICATE_LINE(
        "Duplicate Line",
        "Duplicate current line downwards",
        "Ctrl+D",
        ShortcutCategory.EDITOR
    ),
    DELETE_LINE(
        "Delete Line",
        "Delete current active line completely",
        "Ctrl+Shift+K",
        ShortcutCategory.EDITOR
    ),
    MOVE_LINE_UP(
        "Move Line Up",
        "Swap active line with the line above",
        "Alt+Up",
        ShortcutCategory.EDITOR
    ),
    MOVE_LINE_DOWN(
        "Move Line Down",
        "Swap active line with the line below",
        "Alt+Down",
        ShortcutCategory.EDITOR
    ),
    UNDO(
        "Undo",
        "Revert last text change",
        "Ctrl+Z",
        ShortcutCategory.EDITOR
    ),
    REDO(
        "Redo",
        "Reapply reverted text change",
        "Ctrl+Y / Ctrl+Shift+Z",
        ShortcutCategory.EDITOR
    ),
    FORMAT_DOCUMENT(
        "Format Document",
        "Clean indentation and spacing",
        "Shift+Alt+F",
        ShortcutCategory.EDITOR
    ),

    // FILE MANAGEMENT
    NEW_FILE(
        "New File",
        "Create a new file in project",
        "Ctrl+N",
        ShortcutCategory.GENERAL
    ),
    NEW_FOLDER(
        "New Folder",
        "Create a new directory in project",
        "Ctrl+Shift+N",
        ShortcutCategory.GENERAL
    ),
    RENAME_FILE(
        "Rename File",
        "Rename the active or selected file",
        "F2 / Ctrl+R",
        ShortcutCategory.GENERAL
    ),

    // TERMINAL
    CLEAR_CONSOLE(
        "Clear Terminal Output",
        "Clear all console and REPL history",
        "Ctrl+L",
        ShortcutCategory.TERMINAL
    ),
    TERMINAL_INTERRUPT(
        "Interrupt Execution",
        "Stop the currently running script",
        "Ctrl+C",
        ShortcutCategory.TERMINAL
    )
}

data class ShortcutHUDNotification(
    val action: ShortcutAction,
    val keyCombo: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object ShortcutMatcher {

    fun match(
        event: KeyEvent,
        latchedCtrl: Boolean = false,
        latchedAlt: Boolean = false,
        latchedShift: Boolean = false
    ): ShortcutAction? = matchKeyEvent(event, latchedCtrl, latchedAlt, latchedShift)

    fun matchKeyEvent(
        event: KeyEvent,
        latchedCtrl: Boolean = false,
        latchedAlt: Boolean = false,
        latchedShift: Boolean = false
    ): ShortcutAction? {
        if (event.type != KeyEventType.KeyDown) return null

        val ctrl = event.isCtrlPressed || event.isMetaPressed || latchedCtrl
        val alt = event.isAltPressed || latchedAlt
        val shift = event.isShiftPressed || latchedShift
        val key = event.key

        // Function Keys
        if (key == Key.F1) return ShortcutAction.COMMAND_PALETTE
        if (key == Key.F2) return ShortcutAction.SHORTCUTS_HELP
        if (key == Key.F5) return ShortcutAction.RUN_SCRIPT
        if (key == Key.F6) return if (shift) ShortcutAction.CYCLE_FOCUS_PREV else ShortcutAction.CYCLE_FOCUS_NEXT
        if (key == Key.F11) return ShortcutAction.TOGGLE_ZEN_MODE
        if (key == Key.F12) return ShortcutAction.TOGGLE_MOBILE_ASSIST_BAR
        if (key == Key.Escape) return ShortcutAction.ESCAPE

        // Ctrl + Alt combinations
        if (ctrl && alt) {
            when (key) {
                Key.S -> return ShortcutAction.TOGGLE_TERMINAL_DOCK
                Key.T -> return ShortcutAction.TOGGLE_TAB_LAYOUT
                Key.DirectionRight -> return ShortcutAction.CYCLE_FOCUS_NEXT
                Key.DirectionLeft -> return ShortcutAction.CYCLE_FOCUS_PREV
            }
        }

        // Shift + Alt combinations
        if (shift && alt) {
            when (key) {
                Key.F -> return ShortcutAction.FORMAT_DOCUMENT
            }
        }

        // Ctrl + Shift combinations
        if (ctrl && shift) {
            when (key) {
                Key.P -> return ShortcutAction.COMMAND_PALETTE
                Key.N -> return ShortcutAction.NEW_FOLDER
                Key.F -> return ShortcutAction.GLOBAL_SEARCH
                Key.G -> return ShortcutAction.TOGGLE_SOURCE_CONTROL
                Key.D -> return ShortcutAction.TOGGLE_RUN_DEBUG
                Key.X -> return ShortcutAction.TOGGLE_EXTENSIONS
                Key.K -> return ShortcutAction.DELETE_LINE
                Key.Z -> return ShortcutAction.REDO
                Key.T -> return ShortcutAction.REOPEN_CLOSED_TAB
                Key.U -> return ShortcutAction.TOGGLE_MOBILE_ASSIST_BAR
                Key.Enter -> return ShortcutAction.RUN_REPL_LINE
                Key.Tab -> return ShortcutAction.PREV_TAB
                Key.PageUp -> return ShortcutAction.PREV_TAB
                Key.PageDown -> return ShortcutAction.NEXT_TAB
                Key.E -> return ShortcutAction.FOCUS_EXPLORER
            }
        }

        // Ctrl / Meta combinations
        if (ctrl && !alt && !shift) {
            when (key) {
                // Focus targets
                Key.One -> return ShortcutAction.FOCUS_EDITOR
                Key.Two, Key.Zero -> return ShortcutAction.FOCUS_EXPLORER
                Key.Three -> return ShortcutAction.FOCUS_TERMINAL
                Key.Four -> return ShortcutAction.FOCUS_TABS

                // Zen Mode
                Key.M -> return ShortcutAction.TOGGLE_ZEN_MODE

                // Standard Shortcuts
                Key.S -> return ShortcutAction.SAVE_FILE
                Key.P -> return ShortcutAction.QUICK_OPEN_FILE
                Key.B -> return ShortcutAction.TOGGLE_EXPLORER
                Key.J, Key.Grave -> return ShortcutAction.TOGGLE_TERMINAL
                Key.F -> return ShortcutAction.FIND_IN_FILE
                Key.H -> return ShortcutAction.REPLACE_IN_FILE
                Key.G -> return ShortcutAction.GOTO_LINE
                Key.Slash -> return ShortcutAction.TOGGLE_COMMENT
                Key.D -> return ShortcutAction.DUPLICATE_LINE
                Key.W -> return ShortcutAction.CLOSE_TAB
                Key.N -> return ShortcutAction.NEW_FILE
                Key.L -> return ShortcutAction.CLEAR_CONSOLE
                Key.Z -> return ShortcutAction.UNDO
                Key.Y -> return ShortcutAction.REDO
                Key.Enter -> return ShortcutAction.RUN_SCRIPT
                Key.Comma -> return ShortcutAction.OPEN_SETTINGS
                Key.Tab -> return ShortcutAction.NEXT_TAB
                Key.PageDown -> return ShortcutAction.NEXT_TAB
                Key.PageUp -> return ShortcutAction.PREV_TAB
                Key.C -> return ShortcutAction.TERMINAL_INTERRUPT
            }
        }

        // Alt + Number (Direct Tab Switching: Alt+1 .. Alt+9)
        if (alt && !ctrl && !shift) {
            when (key) {
                Key.One -> return ShortcutAction.TAB_1
                Key.Two -> return ShortcutAction.TAB_2
                Key.Three -> return ShortcutAction.TAB_3
                Key.Four -> return ShortcutAction.TAB_4
                Key.Five -> return ShortcutAction.TAB_5
                Key.Six -> return ShortcutAction.TAB_6
                Key.Seven -> return ShortcutAction.TAB_7
                Key.Eight -> return ShortcutAction.TAB_8
                Key.Nine -> return ShortcutAction.TAB_9
                Key.DirectionUp -> return ShortcutAction.MOVE_LINE_UP
                Key.DirectionDown -> return ShortcutAction.MOVE_LINE_DOWN
            }
        }

        // Shift combinations
        if (shift && !ctrl && !alt) {
            if (key == Key.Tab) return ShortcutAction.OUTDENT
        }

        // Plain Tab
        if (!ctrl && !alt && !shift && key == Key.Tab) {
            return ShortcutAction.INDENT
        }

        return null
    }
}
