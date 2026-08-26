package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shortcuts.ShortcutAction
import com.example.shortcuts.ShortcutMatcher
import com.example.ui.components.AiAssistantPane
import com.example.ui.components.CodeEditor
import com.example.ui.components.ColorPickerModal
import com.example.ui.components.CommandPaletteModal
import com.example.ui.components.EditorTabBar
import com.example.ui.components.FileExplorerPane
import com.example.ui.components.FindReplaceBar
import com.example.ui.components.GitDiffModal
import com.example.ui.components.GlobalSearchPane
import com.example.ui.components.GoToLineModal
import com.example.ui.components.NewFileDialog
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.PrimaryStageNavigationBar
import com.example.ui.components.QuickOpenModal
import com.example.ui.components.RenameFileDialog
import com.example.ui.components.SettingsModal
import com.example.ui.components.ShortcutsCheatSheetModal
import com.example.ui.components.ShortcutsHudToast
import com.example.ui.components.ShortcutsMobileBar
import com.example.ui.components.SmartAutoCompleteBar
import com.example.ui.components.SnippetsLibraryModal
import com.example.ui.components.StatusBar
import com.example.ui.components.WebPreviewPane
import com.example.ui.terminal.RawTerminalView
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTitleBar
import com.example.ui.viewmodel.ActiveFocusArea
import com.example.ui.viewmodel.PrimaryStage
import com.example.ui.viewmodel.PyCodeViewModel
import com.example.ui.viewmodel.TabLayoutStyle

@Composable
fun IdeMainScreen(
    viewModel: PyCodeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val editorFocusRequester = remember { FocusRequester() }
    val rootFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    LaunchedEffect(uiState.activeFocusArea) {
        if (uiState.activeFocusArea == ActiveFocusArea.EDITOR) {
            editorFocusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                val hasModalOpen = uiState.isCommandPaletteOpen ||
                        uiState.isQuickOpenOpen ||
                        uiState.isGoToLineOpen ||
                        uiState.isShortcutsHelpOpen ||
                        uiState.isSettingsOpen ||
                        uiState.isColorPickerOpen ||
                        uiState.isSnippetsLibraryOpen ||
                        uiState.isNewFileDialogOpen ||
                        uiState.isNewFolderDialogOpen ||
                        uiState.isRenameDialogOpen ||
                        uiState.isNewProjectDialogOpen

                if (hasModalOpen) {
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                        viewModel.handleShortcutAction(ShortcutAction.ESCAPE)
                        return@onPreviewKeyEvent true
                    }
                    return@onPreviewKeyEvent false
                }

                if (uiState.activeFocusArea == ActiveFocusArea.TERMINAL) {
                    val action = ShortcutMatcher.match(
                        event = keyEvent,
                        latchedCtrl = uiState.latchedCtrl,
                        latchedAlt = uiState.latchedAlt,
                        latchedShift = uiState.latchedShift
                    )
                    if (action != null) {
                        when (action) {
                            ShortcutAction.FOCUS_EDITOR,
                            ShortcutAction.FOCUS_EXPLORER,
                            ShortcutAction.FOCUS_TERMINAL,
                            ShortcutAction.FOCUS_TABS,
                            ShortcutAction.CYCLE_FOCUS_NEXT,
                            ShortcutAction.CYCLE_FOCUS_PREV,
                            ShortcutAction.TOGGLE_TERMINAL,
                            ShortcutAction.TOGGLE_ZEN_MODE,
                            ShortcutAction.COMMAND_PALETTE,
                            ShortcutAction.SHORTCUTS_HELP,
                            ShortcutAction.ESCAPE -> {
                                viewModel.handleShortcutAction(action)
                                return@onPreviewKeyEvent true
                            }
                            else -> return@onPreviewKeyEvent false
                        }
                    }
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                        viewModel.focusEditor()
                        return@onPreviewKeyEvent true
                    }
                    return@onPreviewKeyEvent false
                }

                val action = ShortcutMatcher.match(
                    event = keyEvent,
                    latchedCtrl = uiState.latchedCtrl,
                    latchedAlt = uiState.latchedAlt,
                    latchedShift = uiState.latchedShift
                )
                if (action != null) {
                    if (action == ShortcutAction.INDENT && uiState.activeFocusArea != ActiveFocusArea.EDITOR) {
                        return@onPreviewKeyEvent false
                    }
                    viewModel.handleShortcutAction(action)
                    return@onPreviewKeyEvent true
                }

                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (uiState.activeFocusArea == ActiveFocusArea.EXPLORER) {
                        when (keyEvent.key) {
                            Key.DirectionDown -> {
                                viewModel.moveExplorerSelection(1)
                                return@onPreviewKeyEvent true
                            }
                            Key.DirectionUp -> {
                                viewModel.moveExplorerSelection(-1)
                                return@onPreviewKeyEvent true
                            }
                            Key.Enter, Key.Spacebar, Key.DirectionRight -> {
                                viewModel.openExplorerSelectedFile()
                                return@onPreviewKeyEvent true
                            }
                            Key.Delete, Key.Backspace -> {
                                val file = uiState.files.getOrNull(uiState.explorerSelectedIndex)
                                if (file != null) viewModel.deleteFile(file)
                                return@onPreviewKeyEvent true
                            }
                            Key.F2 -> {
                                val file = uiState.files.getOrNull(uiState.explorerSelectedIndex)
                                if (file != null) viewModel.openRenameDialog(file)
                                return@onPreviewKeyEvent true
                            }
                            Key.A -> {
                                if (keyEvent.nativeKeyEvent.isShiftPressed) {
                                    viewModel.setNewFolderDialogOpen(true)
                                } else {
                                    viewModel.setNewFileDialogOpen(true)
                                }
                                return@onPreviewKeyEvent true
                            }
                            Key.MoveHome -> {
                                viewModel.moveExplorerSelection(-uiState.files.size)
                                return@onPreviewKeyEvent true
                            }
                            Key.MoveEnd -> {
                                viewModel.moveExplorerSelection(uiState.files.size)
                                return@onPreviewKeyEvent true
                            }
                            Key.Escape -> {
                                viewModel.focusEditor()
                                return@onPreviewKeyEvent true
                            }
                        }
                    }

                    if (uiState.activeFocusArea == ActiveFocusArea.TABS) {
                        when (keyEvent.key) {
                            Key.DirectionRight, Key.Tab -> {
                                viewModel.nextTab()
                                return@onPreviewKeyEvent true
                            }
                            Key.DirectionLeft -> {
                                viewModel.prevTab()
                                return@onPreviewKeyEvent true
                            }
                            Key.Enter, Key.Spacebar -> {
                                viewModel.focusEditor()
                                return@onPreviewKeyEvent true
                            }
                            Key.Delete, Key.Backspace, Key.W -> {
                                viewModel.handleShortcutAction(ShortcutAction.CLOSE_TAB)
                                return@onPreviewKeyEvent true
                            }
                            Key.Escape -> {
                                viewModel.focusEditor()
                                return@onPreviewKeyEvent true
                            }
                        }
                    }
                }

                false
            },
        topBar = {
            if (!uiState.isZenMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(VsCodeTitleBar)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.setQuickOpenOpen(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Project",
                            tint = VsCodeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${uiState.currentProject?.name ?: "Workspace"} / ${uiState.activeFileName}",
                            color = VsCodeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isRunning) {
                            IconButton(
                                onClick = { viewModel.stopExecution() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = VsCodeError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.runCurrentScript() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run Script (F5)",
                                    tint = VsCodeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setSnippetsLibraryOpen(true) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Snippets",
                                tint = VsCodeTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setColorPickerOpen(true) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Color Picker",
                                tint = VsCodeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setCommandPaletteOpen(true) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Command Palette",
                                tint = VsCodeTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isZenMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.primaryStage == PrimaryStage.EDITOR && uiState.isSmartAssistBarVisible) {
                        SmartAutoCompleteBar(
                            language = uiState.activeFileLanguage,
                            onInsertText = { snippet -> viewModel.insertTextAtCursor(snippet) },
                            onIndent = { viewModel.indentCurrentLine() },
                            onOutdent = { viewModel.outdentCurrentLine() },
                            onMoveCursorLeft = { viewModel.moveCursorLeft() },
                            onMoveCursorRight = { viewModel.moveCursorRight() },
                            onTriggerAiCopilot = {
                                viewModel.selectPrimaryStage(PrimaryStage.AI_COPILOT)
                            }
                        )
                    }

                    if (uiState.isMobileAssistBarVisible) {
                        ShortcutsMobileBar(
                            latchedCtrl = uiState.latchedCtrl,
                            latchedAlt = uiState.latchedAlt,
                            latchedShift = uiState.latchedShift,
                            onToggleCtrl = { viewModel.toggleLatchCtrl() },
                            onToggleAlt = { viewModel.toggleLatchAlt() },
                            onToggleShift = { viewModel.toggleLatchShift() },
                            onShortcutAction = { a -> viewModel.handleShortcutAction(a) },
                            onInsertSymbol = { snippet -> viewModel.insertTextAtCursor(snippet) }
                        )
                    }

                    PrimaryStageNavigationBar(
                        activeStage = uiState.primaryStage,
                        isRunning = uiState.isRunning,
                        errorCount = uiState.diagnostics.size,
                        isWebFileActive = uiState.activeFileName.endsWith(".html") || uiState.activeFileName.endsWith(".js") || uiState.activeFileName.endsWith(".css"),
                        onSelectStage = { stage -> viewModel.selectPrimaryStage(stage) }
                    )

                    StatusBar(
                        cursorLine = uiState.cursorLine,
                        cursorColumn = uiState.cursorColumn,
                        language = uiState.activeFileLanguage,
                        errorCount = uiState.diagnostics.size,
                        isRunning = uiState.isRunning,
                        terminalDockPosition = uiState.terminalDockPosition,
                        isZenMode = uiState.isZenMode,
                        activeFocusArea = uiState.activeFocusArea,
                        onToggleZenMode = { viewModel.toggleZenMode() },
                        onToggleDock = { viewModel.toggleTerminalDockPosition() },
                        onOpenShortcutsHelp = { viewModel.setShortcutsHelpOpen(true) },
                        onOpenSettings = { viewModel.setSettingsOpen(true) },
                        onCycleFocus = { viewModel.cycleFocus(forward = true) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VsCodeBg)
        ) {
            when (uiState.primaryStage) {
                PrimaryStage.EDITOR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!uiState.isZenMode && uiState.tabLayoutStyle == TabLayoutStyle.HORIZONTAL) {
                            EditorTabBar(
                                tabs = uiState.openTabs,
                                activeTabFileId = uiState.activeTabFileId,
                                isFocused = uiState.activeFocusArea == ActiveFocusArea.TABS,
                                onTabClick = { tab ->
                                    val file = uiState.files.find { it.id == tab.fileId }
                                    if (file != null) viewModel.openFile(file)
                                },
                                onTabClose = { tab -> viewModel.closeTab(tab) },
                                onNewTabClick = { viewModel.setNewFileDialogOpen(true) }
                            )
                        }

                        if (uiState.isFindBarOpen) {
                            FindReplaceBar(
                                searchQuery = uiState.searchQuery,
                                replaceQuery = uiState.replaceQuery,
                                isReplaceMode = uiState.isReplaceMode,
                                matchCount = uiState.searchMatchCount,
                                currentMatchIndex = uiState.currentMatchIndex,
                                onSearchQueryChange = { viewModel.performSearch(it) },
                                onReplaceQueryChange = { /* handled in state */ },
                                onToggleReplaceMode = {
                                    viewModel.handleShortcutAction(ShortcutAction.REPLACE_IN_FILE)
                                },
                                onNextMatch = { /* cycle next */ },
                                onPrevMatch = { /* cycle prev */ },
                                onReplaceCurrent = { viewModel.replaceCurrentMatch(uiState.replaceQuery) },
                                onReplaceAll = { viewModel.replaceAllMatches(uiState.replaceQuery) },
                                onClose = { viewModel.setFindBarOpen(false) }
                            )
                        }

                        CodeEditor(
                            editorValue = uiState.editorValue,
                            onValueChange = { viewModel.onEditorValueChange(it) },
                            filePath = uiState.activeFilePath,
                            language = uiState.activeFileLanguage,
                            cursorLine = uiState.cursorLine,
                            diagnostics = uiState.diagnostics,
                            focusRequester = editorFocusRequester,
                            fontSizeSp = uiState.fontSizeSp,
                            showLineNumbers = uiState.showLineNumbers,
                            wordWrap = uiState.wordWrap,
                            showBreadcrumbs = !uiState.isZenMode,
                            isFocused = uiState.activeFocusArea == ActiveFocusArea.EDITOR,
                            onFontSizeZoom = { zoom -> viewModel.onFontSizeZoom(zoom) },
                            onResetFontSize = { viewModel.resetFontSize() },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }

                PrimaryStage.TERMINAL -> {
                    val terminalCompletions = remember(uiState.files) {
                        uiState.files.map { it.name } + listOf(
                            "python", "python3", "node", "cargo", "gcc", "run", "ls", "dir", "ll", "cat",
                            "touch", "mkdir", "rm", "echo", "pwd", "whoami", "uname",
                            "date", "clear", "help", "problems", "zen", "dock", "tabs", "exit"
                        )
                    }
                    RawTerminalView(
                        consoleOutputs = uiState.consoleOutputs,
                        isRunning = uiState.isRunning,
                        isWaitingForInput = uiState.isWaitingForInput,
                        isPythonMode = uiState.isTerminalInPythonMode,
                        inputPrompt = uiState.inputPrompt,
                        dockPosition = uiState.terminalDockPosition,
                        isFocused = true,
                        availableCompletions = terminalCompletions,
                        onSubmitCommand = { cmd -> viewModel.submitTerminalCommand(cmd) },
                        onClearConsole = { viewModel.clearConsole() },
                        onCloseTerminal = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) },
                        onRunAgain = { viewModel.runCurrentScript() },
                        onStopExecution = { viewModel.stopExecution() },
                        onToggleDock = { viewModel.toggleTerminalDockPosition() },
                        onFocusTerminal = { viewModel.focusTerminal() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PrimaryStage.WEB_PREVIEW -> {
                    val activeContent = uiState.editorValue.text
                    val htmlFile = uiState.files.find { it.name.endsWith(".html") }?.content ?: activeContent
                    val cssFile = uiState.files.find { it.name.endsWith(".css") }?.content ?: ""
                    val jsFile = uiState.files.find { it.name.endsWith(".js") || it.name.endsWith(".ts") }?.content ?: ""

                    WebPreviewPane(
                        htmlContent = htmlFile,
                        cssContent = cssFile,
                        jsContent = jsFile,
                        onClose = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PrimaryStage.EXPLORER -> {
                    FileExplorerPane(
                        projectName = uiState.currentProject?.name ?: "Workspace",
                        files = uiState.files,
                        activeFileId = uiState.activeTabFileId,
                        selectedIndex = uiState.explorerSelectedIndex,
                        isFocused = true,
                        onFileClick = { file -> viewModel.openFile(file) },
                        onNewFileClick = { viewModel.setNewFileDialogOpen(true) },
                        onNewFolderClick = { viewModel.setNewFolderDialogOpen(true) },
                        onRenameFile = { file -> viewModel.openRenameDialog(file) },
                        onDeleteFile = { file -> viewModel.deleteFile(file) },
                        onRunFile = { file ->
                            viewModel.openFile(file)
                            viewModel.runCurrentScript()
                        },
                        onCloseSidebar = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PrimaryStage.GLOBAL_SEARCH -> {
                    GlobalSearchPane(
                        query = uiState.globalSearchQuery,
                        results = uiState.globalSearchResults,
                        onQueryChange = { viewModel.performGlobalSearch(it) },
                        onMatchClick = { match ->
                            val file = uiState.files.find { it.id == match.fileId }
                            if (file != null) {
                                viewModel.openFile(file)
                                viewModel.goToLine(match.lineNumber)
                            }
                        },
                        onClose = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PrimaryStage.AI_COPILOT -> {
                    AiAssistantPane(
                        chatMessages = uiState.aiChatMessages,
                        isLoading = uiState.isAiLoading,
                        activeFileName = uiState.activeFileName,
                        activeLanguage = uiState.activeFileLanguage,
                        onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) },
                        onInsertSnippet = { snippet ->
                            viewModel.insertAiSnippet(snippet)
                            viewModel.selectPrimaryStage(PrimaryStage.EDITOR)
                        },
                        onReplaceFile = { code ->
                            viewModel.replaceFileWithAiCode(code)
                            viewModel.selectPrimaryStage(PrimaryStage.EDITOR)
                        },
                        onRunSnippet = { snippet ->
                            viewModel.runSnippetDirectly(snippet)
                        },
                        onClearChat = { viewModel.clearAiChat() },
                        onClose = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PrimaryStage.SETTINGS -> {
                    SettingsModal(
                        fontSizeSp = uiState.fontSizeSp,
                        tabSize = uiState.tabSize,
                        wordWrap = uiState.wordWrap,
                        showLineNumbers = uiState.showLineNumbers,
                        onSaveSettings = { fs, ts, ww, ln -> viewModel.updateSettings(fs, ts, ww, ln) },
                        onDismiss = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) }
                    )
                }

                PrimaryStage.GIT_DIFF -> {
                    GitDiffModal(
                        fileName = uiState.diffFileName,
                        originalContent = uiState.diffOriginalContent,
                        modifiedContent = uiState.diffModifiedContent,
                        onStageAndCommit = {
                            viewModel.commitCurrentChanges("Update ${uiState.diffFileName}")
                            viewModel.selectPrimaryStage(PrimaryStage.EDITOR)
                        },
                        onDismiss = { viewModel.selectPrimaryStage(PrimaryStage.EDITOR) }
                    )
                }
            }

            ShortcutsHudToast(
                notification = uiState.activeHudNotification,
                onDismiss = { viewModel.dismissHudNotification() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )

            if (uiState.isColorPickerOpen) {
                ColorPickerModal(
                    onDismiss = { viewModel.setColorPickerOpen(false) },
                    onColorSelected = { hex ->
                        viewModel.insertTextAtCursor(hex)
                        viewModel.setColorPickerOpen(false)
                    }
                )
            }

            if (uiState.isSnippetsLibraryOpen) {
                SnippetsLibraryModal(
                    activeLanguage = uiState.activeFileLanguage,
                    onDismiss = { viewModel.setSnippetsLibraryOpen(false) },
                    onInsertSnippet = { snippet ->
                        viewModel.insertSnippet(snippet)
                        viewModel.setSnippetsLibraryOpen(false)
                    }
                )
            }

            if (uiState.isCommandPaletteOpen) {
                CommandPaletteModal(
                    onDismiss = { viewModel.setCommandPaletteOpen(false) },
                    onActionSelected = { a -> viewModel.handleShortcutAction(a) }
                )
            }

            if (uiState.isQuickOpenOpen) {
                QuickOpenModal(
                    files = uiState.files,
                    onFileSelected = { file -> viewModel.openFile(file) },
                    onDismiss = { viewModel.setQuickOpenOpen(false) }
                )
            }

            if (uiState.isGoToLineOpen) {
                GoToLineModal(
                    totalLines = uiState.editorValue.text.lines().size,
                    currentLine = uiState.cursorLine,
                    onGoToLine = { line -> viewModel.goToLine(line) },
                    onDismiss = { viewModel.setGoToLineOpen(false) }
                )
            }

            if (uiState.isShortcutsHelpOpen) {
                ShortcutsCheatSheetModal(
                    onDismiss = { viewModel.setShortcutsHelpOpen(false) },
                    onExecuteAction = { a -> viewModel.handleShortcutAction(a) }
                )
            }

            if (uiState.isSettingsOpen) {
                SettingsModal(
                    fontSizeSp = uiState.fontSizeSp,
                    tabSize = uiState.tabSize,
                    wordWrap = uiState.wordWrap,
                    showLineNumbers = uiState.showLineNumbers,
                    onSaveSettings = { fs, ts, ww, ln -> viewModel.updateSettings(fs, ts, ww, ln) },
                    onDismiss = { viewModel.setSettingsOpen(false) }
                )
            }

            if (uiState.isNewFileDialogOpen) {
                NewFileDialog(
                    isFolder = false,
                    onConfirm = { name -> viewModel.createNewFile(name) },
                    onDismiss = { viewModel.setNewFileDialogOpen(false) }
                )
            }

            if (uiState.isNewFolderDialogOpen) {
                NewFileDialog(
                    isFolder = true,
                    onConfirm = { name -> viewModel.createNewFolder(name) },
                    onDismiss = { viewModel.setNewFolderDialogOpen(false) }
                )
            }

            if (uiState.isRenameDialogOpen && uiState.fileToRename != null) {
                RenameFileDialog(
                    currentName = uiState.fileToRename?.name ?: "",
                    onConfirm = { newName -> viewModel.renameFile(newName) },
                    onDismiss = { viewModel.setRenameDialogOpen(false) }
                )
            }

            if (uiState.isNewProjectDialogOpen) {
                NewProjectDialog(
                    onSelectTemplate = { template -> viewModel.createProjectFromTemplate(template) },
                    onDismiss = { viewModel.setNewProjectDialogOpen(false) }
                )
            }

            if (uiState.isGitDiffModalOpen) {
                GitDiffModal(
                    fileName = uiState.diffFileName,
                    originalContent = uiState.diffOriginalContent,
                    modifiedContent = uiState.diffModifiedContent,
                    onStageAndCommit = {
                        viewModel.commitCurrentChanges("Update ${uiState.diffFileName}")
                    },
                    onDismiss = { viewModel.closeGitDiffModal() }
                )
            }
        }
    }
}
