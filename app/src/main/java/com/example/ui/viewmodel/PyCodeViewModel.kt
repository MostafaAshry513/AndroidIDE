package com.example.ui.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiChatMessage
import com.example.ai.AiCopilotService
import com.example.ai.MessageSender
import com.example.data.model.ConsoleOutput
import com.example.data.model.DiagnosticError
import com.example.data.model.EditorTab
import com.example.data.model.FileEntity
import com.example.data.model.ProjectEntity
import com.example.data.repository.WorkspaceRepository
import com.example.data.sample.ProjectTemplate
import com.example.data.sample.StarterProjects
import com.example.interpreter.PolyglotRuntime
import com.example.interpreter.PythonLinter
import com.example.interpreter.PythonRuntime
import com.example.shortcuts.ShortcutAction
import com.example.shortcuts.ShortcutHUDNotification
import com.example.ui.components.GitCommit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SidebarTab {
    EXPLORER, SEARCH, SOURCE_CONTROL, RUN_DEBUG, EXTENSIONS, AI_COPILOT, SHORTCUTS, SETTINGS
}

enum class TerminalTab {
    OUTPUT, REPL, PROBLEMS
}

enum class TerminalDockPosition {
    RIGHT, BOTTOM
}

enum class TabLayoutStyle {
    HORIZONTAL, VERTICAL
}

enum class ActiveFocusArea {
    EDITOR, EXPLORER, TERMINAL, TABS, SEARCH
}

data class GlobalSearchMatch(
    val fileId: Long,
    val fileName: String,
    val filePath: String,
    val lineNumber: Int,
    val lineText: String,
    val matchStartIndex: Int
)

data class IdeUiState(
    val primaryStage: PrimaryStage = PrimaryStage.EDITOR,
    val currentProject: ProjectEntity? = null,
    val files: List<FileEntity> = emptyList(),
    val openTabs: List<EditorTab> = emptyList(),
    val activeTabFileId: Long? = null,
    val editorValue: TextFieldValue = TextFieldValue(""),
    val activeFileName: String = "Untitled",
    val activeFilePath: String = "",
    val activeFileLanguage: String = "python",
    val isFileModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val diagnostics: List<DiagnosticError> = emptyList(),
    val isRunning: Boolean = false,
    val consoleOutputs: List<ConsoleOutput> = emptyList(),
    val replLines: List<Pair<String, String?>> = emptyList(),
    val replInput: String = "",
    val isWaitingForInput: Boolean = false,
    val inputPrompt: String = "",
    val isSidebarOpen: Boolean = false, // Starts closed to maximize horizontal screen space
    val activeSidebarTab: SidebarTab = SidebarTab.EXPLORER,
    val isTerminalOpen: Boolean = false, // Toggleable via Ctrl+` / Ctrl+J
    val isTerminalInPythonMode: Boolean = false, // When in interactive Python REPL subshell
    val activeTerminalTab: TerminalTab = TerminalTab.OUTPUT,
    val terminalDockPosition: TerminalDockPosition = TerminalDockPosition.RIGHT, // Horizontal side-by-side!
    val tabLayoutStyle: TabLayoutStyle = TabLayoutStyle.HORIZONTAL,
    val activeFocusArea: ActiveFocusArea = ActiveFocusArea.EDITOR,
    val isZenMode: Boolean = false,
    val isMobileAssistBarVisible: Boolean = false,
    val isSmartAssistBarVisible: Boolean = true,
    val explorerSelectedIndex: Int = 0,
    val globalSearchQuery: String = "",
    val globalSearchResults: List<GlobalSearchMatch> = emptyList(),
    val isCommandPaletteOpen: Boolean = false,
    val isQuickOpenOpen: Boolean = false,
    val isGoToLineOpen: Boolean = false,
    val isShortcutsHelpOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isColorPickerOpen: Boolean = false,
    val isSnippetsLibraryOpen: Boolean = false,
    val isNewFileDialogOpen: Boolean = false,
    val isNewFolderDialogOpen: Boolean = false,
    val isRenameDialogOpen: Boolean = false,
    val fileToRename: FileEntity? = null,
    val isFindBarOpen: Boolean = false,
    val isReplaceMode: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val searchMatchCount: Int = 0,
    val currentMatchIndex: Int = 0,
    val latchedCtrl: Boolean = false,
    val latchedAlt: Boolean = false,
    val latchedShift: Boolean = false,
    val activeHudNotification: ShortcutHUDNotification? = null,
    val fontSizeSp: Float = 13.5f,
    val tabSize: Int = 4,
    val wordWrap: Boolean = false,
    val showLineNumbers: Boolean = true,
    val isWebPreviewOpen: Boolean = false,
    val isNewProjectDialogOpen: Boolean = false,
    val isGitDiffModalOpen: Boolean = false,
    val diffOriginalContent: String = "",
    val diffModifiedContent: String = "",
    val diffFileName: String = "",
    val aiChatMessages: List<AiChatMessage> = emptyList(),
    val isAiLoading: Boolean = false,
    val gitBranch: String = "main",
    val gitCommits: List<GitCommit> = listOf(
        GitCommit("9a7f31b", "feat: Initial polyglot workspace scaffolding", "Developer", "15m ago"),
        GitCommit("c4e892d", "feat: Add Web, C++, Rust, and SQL support", "Developer", "5m ago")
    )
)

class PyCodeViewModel(private val repository: WorkspaceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    private var inputDeferred: CompletableDeferred<String>? = null
    private var runJob: Job? = null

    // Undo / Redo stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val closedTabHistory = mutableListOf<EditorTab>()

    private val polyglotRuntime: PolyglotRuntime = PolyglotRuntime(
        onPrint = { text ->
            appendConsoleOutput(text, isError = false)
        },
        onError = { error ->
            appendConsoleOutput(error, isError = true)
        },
        onInputRequest = { prompt ->
            _uiState.update { it.copy(isWaitingForInput = true, inputPrompt = prompt) }
            val deferred = CompletableDeferred<String>()
            inputDeferred = deferred
            val result = deferred.await()
            _uiState.update { it.copy(isWaitingForInput = false, inputPrompt = "") }
            result
        },
        onOpenWebPreview = {
            openWebPreview()
        }
    )
    private val runtime get() = polyglotRuntime.pythonRuntime
    private val aiCopilotService = AiCopilotService()

    init {
        loadWorkspace()
    }

    private fun loadWorkspace() {
        viewModelScope.launch {
            val projectId = repository.ensureDefaultProject()
            
            repository.allProjects.collect { projects ->
                val project = projects.firstOrNull { it.id == projectId } ?: projects.firstOrNull()
                _uiState.update { it.copy(currentProject = project) }
            }
        }

        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                val project = projects.firstOrNull() ?: return@collect
                repository.getFilesByProject(project.id).collect { files ->
                    _uiState.update { state ->
                        state.copy(files = files)
                    }

                    // Open main.py if no tabs open
                    if (_uiState.value.openTabs.isEmpty() && files.isNotEmpty()) {
                        val mainFile = files.firstOrNull { it.name == "main.py" } ?: files.first()
                        openFile(mainFile)
                    }
                }
            }
        }
    }

    private fun detectLanguage(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "py", "pyw", "python" -> "python"
            "js", "mjs", "cjs" -> "javascript"
            "ts", "tsx" -> "typescript"
            "html", "htm", "xml" -> "html"
            "css", "scss", "sass", "less" -> "css"
            "json" -> "json"
            "md", "markdown" -> "markdown"
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "c", "h" -> "c"
            "cpp", "cc", "cxx", "hpp" -> "cpp"
            "rs", "rust" -> "rust"
            "go" -> "go"
            "sql" -> "sql"
            "sh", "bash", "zsh" -> "shell"
            "yaml", "yml" -> "yaml"
            "dockerfile" -> "dockerfile"
            else -> "text"
        }
    }

    fun openFile(file: FileEntity) {
        if (file.isDirectory) return

        val currentTabs = _uiState.value.openTabs.toMutableList()
        val existingTab = currentTabs.find { it.fileId == file.id }

        if (existingTab == null) {
            currentTabs.add(EditorTab(file.id, file.name, file.path, file.isModified))
        }

        undoStack.clear()
        redoStack.clear()

        val textVal = TextFieldValue(file.content, TextRange(0))
        val lang = detectLanguage(file.name)
        val diagnostics = if (lang == "python") PythonLinter.lint(file.content) else emptyList()

        _uiState.update {
            it.copy(
                openTabs = currentTabs,
                activeTabFileId = file.id,
                editorValue = textVal,
                activeFileName = file.name,
                activeFilePath = file.path,
                activeFileLanguage = lang,
                isFileModified = file.isModified,
                diagnostics = diagnostics,
                cursorLine = 1,
                cursorColumn = 1,
                activeFocusArea = ActiveFocusArea.EDITOR,
                primaryStage = PrimaryStage.EDITOR,
                isTerminalOpen = false,
                isSidebarOpen = false
            )
        }
    }

    fun closeTab(tab: EditorTab) {
        val currentTabs = _uiState.value.openTabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.fileId == tab.fileId }
        if (index == -1) return

        closedTabHistory.add(tab)
        currentTabs.removeAt(index)

        if (tab.fileId == _uiState.value.activeTabFileId) {
            if (currentTabs.isNotEmpty()) {
                val nextTab = if (index < currentTabs.size) currentTabs[index] else currentTabs.last()
                val nextFile = _uiState.value.files.find { it.id == nextTab.fileId }
                if (nextFile != null) {
                    openFile(nextFile)
                    return
                }
            } else {
                _uiState.update {
                    it.copy(
                        openTabs = emptyList(),
                        activeTabFileId = null,
                        editorValue = TextFieldValue(""),
                        activeFileName = "No Open File",
                        activeFilePath = "",
                        activeFileLanguage = "text",
                        isFileModified = false,
                        diagnostics = emptyList()
                    )
                }
                return
            }
        }

        _uiState.update { it.copy(openTabs = currentTabs) }
    }

    fun reopenClosedTab() {
        if (closedTabHistory.isEmpty()) return
        val lastClosed = closedTabHistory.removeAt(closedTabHistory.lastIndex)
        val file = _uiState.value.files.find { it.id == lastClosed.fileId }
        if (file != null) {
            openFile(file)
            showShortcutNotification(ShortcutAction.REOPEN_CLOSED_TAB, "[Ctrl+Shift+T] Reopened ${file.name}")
        }
    }

    fun selectTabByIndex(zeroBasedIndex: Int) {
        val tabs = _uiState.value.openTabs
        if (tabs.isEmpty()) return
        val targetIdx = zeroBasedIndex.coerceIn(0, tabs.size - 1)
        val targetTab = tabs[targetIdx]
        val file = _uiState.value.files.find { it.id == targetTab.fileId }
        if (file != null) {
            openFile(file)
            showShortcutNotification(ShortcutAction.TAB_1, "[Alt+${targetIdx + 1}] Switched to ${file.name}")
        }
    }

    fun onEditorValueChange(newValue: TextFieldValue) {
        val oldValue = _uiState.value.editorValue.text
        if (oldValue != newValue.text) {
            undoStack.add(oldValue)
            if (undoStack.size > 100) undoStack.removeAt(0)
            redoStack.clear()
        }

        val text = newValue.text
        val cursor = newValue.selection.start
        val textBefore = text.take(cursor.coerceIn(0, text.length))
        val line = textBefore.count { it == '\n' } + 1
        val col = cursor - textBefore.lastIndexOf('\n')

        val diagnostics = if (_uiState.value.activeFileLanguage == "python") PythonLinter.lint(text) else emptyList()

        _uiState.update {
            it.copy(
                editorValue = newValue,
                isFileModified = true,
                cursorLine = line,
                cursorColumn = col,
                diagnostics = diagnostics
            )
        }

        val activeId = _uiState.value.activeTabFileId ?: return
        viewModelScope.launch {
            repository.markFileModified(activeId, text, isModified = true)
        }
    }

    fun saveActiveFile() {
        val activeId = _uiState.value.activeTabFileId ?: return
        val content = _uiState.value.editorValue.text

        viewModelScope.launch {
            repository.saveFileContent(activeId, content)
            _uiState.update { state ->
                val updatedTabs = state.openTabs.map {
                    if (it.fileId == activeId) it.copy(isModified = false) else it
                }
                state.copy(isFileModified = false, openTabs = updatedTabs)
            }
            showShortcutNotification(ShortcutAction.SAVE_FILE, "[Ctrl+S] Saved ${stateFileName()}")
        }
    }

    fun runCurrentScript() {
        saveActiveFile()
        val code = _uiState.value.editorValue.text
        val fileName = _uiState.value.activeFileName
        val lang = _uiState.value.activeFileLanguage

        if (lang in listOf("html", "htm") || fileName.endsWith(".html")) {
            openWebPreview()
            showShortcutNotification(ShortcutAction.RUN_SCRIPT, "[F5] Live Web Preview Opened")
            return
        }

        _uiState.update {
            it.copy(
                isRunning = true,
                isTerminalOpen = true,
                primaryStage = PrimaryStage.TERMINAL,
                activeTerminalTab = TerminalTab.OUTPUT
            )
        }

        appendConsoleOutput("\n--- [Running $fileName ($lang)] ---", isSystem = true)
        showShortcutNotification(ShortcutAction.RUN_SCRIPT, "[F5] Running $fileName ($lang)...")

        runJob?.cancel()
        runJob = viewModelScope.launch {
            val summary = polyglotRuntime.execute(code, lang, fileName)
            
            val statusMsg = if (summary.success) {
                "Process finished with exit code ${summary.exitCode}"
            } else {
                "Process terminated with error: ${summary.error ?: "exit code ${summary.exitCode}"}"
            }
            appendConsoleOutput("\n--- [$statusMsg in ${(summary.durationMs / 1000.0).toString().take(5)}s] ---\n", isSystem = true)
            
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun stopExecution() {
        if (_uiState.value.isRunning) {
            runJob?.cancel()
            inputDeferred?.cancel()
            inputDeferred = null
            _uiState.update { it.copy(isRunning = false, isWaitingForInput = false) }
            appendConsoleOutput("\n🛑 [Execution interrupted by user]\n", isError = true)
            showShortcutNotification(ShortcutAction.TERMINAL_INTERRUPT, "[Ctrl+C] Execution Stopped")
        }
    }

    fun submitUserInput(input: String) {
        appendConsoleOutput(input + "\n", isError = false)
        inputDeferred?.complete(input)
        inputDeferred = null
        _uiState.update { it.copy(isWaitingForInput = false) }
    }

    fun submitTerminalCommand(command: String) {
        if (_uiState.value.isWaitingForInput) {
            submitUserInput(command)
            return
        }
        val trimmed = command.trim()
        if (trimmed.isEmpty()) {
            val prompt = if (_uiState.value.isTerminalInPythonMode) ">>> " else "pycode@android:~/workspace$ "
            appendConsoleOutput("$prompt\n", isSystem = false)
            return
        }

        val inPythonMode = _uiState.value.isTerminalInPythonMode
        val prompt = if (inPythonMode) ">>> " else "pycode@android:~/workspace$ "
        appendConsoleOutput("$prompt$trimmed\n", isSystem = false)

        if (inPythonMode) {
            if (trimmed == "exit()" || trimmed == "quit()" || trimmed == "exit" || trimmed == "quit") {
                _uiState.update { it.copy(isTerminalInPythonMode = false) }
                appendConsoleOutput("Exiting Python interactive subshell.\n\n", isSystem = true)
                return
            }
            viewModelScope.launch {
                val output = runtime.executeReplLine(trimmed)
                if (output != null) {
                    appendConsoleOutput("$output\n", isError = false)
                }
            }
            return
        }

        val parts = trimmed.split(Regex("\\s+"))
        val cmd = parts[0].lowercase()
        val args = parts.drop(1)

        when (cmd) {
            "help" -> {
                appendConsoleOutput(
                    """
                    PyCode Raw Shell v3.12 (Android arm64)
                    Shell Builtins:
                      python / python3           - Start interactive Python REPL subshell
                      python <file> / run [file] - Execute Python script file
                      ls / dir / ll              - List files in current directory
                      cat <file>                 - Display content of a file
                      touch <file>               - Create new file and open in editor
                      mkdir <dir>                - Create new directory
                      rm <file> / del <file>     - Delete file from workspace
                      echo [text...]             - Echo string arguments to stdout
                      pwd                        - Print current working directory
                      whoami                     - Print current username
                      uname [-a]                 - Print system & OS architecture
                      date                       - Print current date & time
                      clear / cls                - Clear terminal screen (Ctrl+L)
                      problems / lint            - Display active syntax/lint issues
                      zen                        - Toggle distraction-free Zen mode
                      dock                       - Toggle terminal side / bottom dock
                      tabs                       - Toggle tab bar style
                      <expression>               - Direct Python code execution
                    """.trimIndent() + "\n\n",
                    isSystem = true
                )
            }
            "clear", "cls" -> {
                clearConsole()
            }
            "python", "python3" -> {
                if (args.isEmpty()) {
                    _uiState.update { it.copy(isTerminalInPythonMode = true) }
                    appendConsoleOutput(
                        "Python 3.12.0 (PyCode Mobile Engine, Aug 2026)\nType \"help\", \"copyright\", \"credits\" or \"license\" for more information. Type \"exit()\" to leave.\n\n",
                        isSystem = true
                    )
                } else {
                    val targetName = args[0]
                    val file = _uiState.value.files.find { it.name.equals(targetName, ignoreCase = true) }
                    if (file != null) {
                        openFile(file)
                        runCurrentScript()
                    } else {
                        appendConsoleOutput("python: can't open file '$targetName': [Errno 2] No such file or directory\n", isError = true)
                    }
                }
            }
            "run" -> {
                if (args.isNotEmpty()) {
                    val targetName = args[0]
                    val file = _uiState.value.files.find { it.name.equals(targetName, ignoreCase = true) }
                    if (file != null) {
                        openFile(file)
                        runCurrentScript()
                    } else {
                        appendConsoleOutput("Error: File '$targetName' not found.\n", isError = true)
                    }
                } else {
                    runCurrentScript()
                }
            }
            "ls", "dir", "ll" -> {
                val fileList = _uiState.value.files.joinToString("\n") { f ->
                    val type = if (f.isDirectory) "drwxr-xr-x" else "-rw-r--r--"
                    val size = "${f.content.length}".padStart(6) + " B"
                    val activeMarker = if (f.id == _uiState.value.activeTabFileId) " *" else ""
                    "  $type  pycode  $size  ${f.name}$activeMarker"
                }
                appendConsoleOutput("total ${_uiState.value.files.size} items\n$fileList\n\n", isSystem = true)
            }
            "cat" -> {
                if (args.isNotEmpty()) {
                    val targetName = args[0]
                    val file = _uiState.value.files.find { it.name.equals(targetName, ignoreCase = true) }
                    if (file != null) {
                        appendConsoleOutput("${file.content}\n\n", isSystem = false)
                    } else {
                        appendConsoleOutput("cat: $targetName: No such file or directory\n", isError = true)
                    }
                } else {
                    appendConsoleOutput("cat: missing operand\n", isError = true)
                }
            }
            "touch" -> {
                if (args.isNotEmpty()) {
                    val fileName = args[0]
                    createNewFile(fileName)
                    appendConsoleOutput("Created file '$fileName'\n", isSystem = true)
                } else {
                    appendConsoleOutput("touch: missing file operand\n", isError = true)
                }
            }
            "mkdir" -> {
                if (args.isNotEmpty()) {
                    val dirName = args[0]
                    createNewFolder(dirName)
                    appendConsoleOutput("Created directory '$dirName'\n", isSystem = true)
                } else {
                    appendConsoleOutput("mkdir: missing operand\n", isError = true)
                }
            }
            "rm", "del" -> {
                if (args.isNotEmpty()) {
                    val targetName = args[0]
                    val file = _uiState.value.files.find { it.name.equals(targetName, ignoreCase = true) }
                    if (file != null) {
                        deleteFile(file)
                        appendConsoleOutput("Removed '$targetName'\n", isSystem = true)
                    } else {
                        appendConsoleOutput("rm: cannot remove '$targetName': No such file\n", isError = true)
                    }
                } else {
                    appendConsoleOutput("rm: missing operand\n", isError = true)
                }
            }
            "pwd" -> {
                appendConsoleOutput("/workspace\n", isSystem = true)
            }
            "whoami" -> {
                appendConsoleOutput("pycode\n", isSystem = true)
            }
            "uname" -> {
                appendConsoleOutput("Linux pycode-android 5.15.0-arm64 #1 SMP Android aarch64 GNU/Linux\n", isSystem = true)
            }
            "date" -> {
                appendConsoleOutput("${java.util.Date()}\n", isSystem = true)
            }
            "echo" -> {
                appendConsoleOutput("${args.joinToString(" ")}\n", isSystem = false)
            }
            "problems", "lint" -> {
                val diags = _uiState.value.diagnostics
                if (diags.isEmpty()) {
                    appendConsoleOutput("✅ No lint or syntax errors detected in active file.\n\n", isSystem = true)
                } else {
                    val report = diags.joinToString("\n") { "  Line ${it.line}: [${it.severity}] ${it.message}" }
                    appendConsoleOutput("Problems in ${_uiState.value.activeFileName}:\n$report\n\n", isError = true)
                }
            }
            "zen" -> {
                toggleZenMode()
            }
            "dock" -> {
                toggleTerminalDockPosition()
            }
            "tabs" -> {
                toggleTabLayoutStyle()
            }
            "pip" -> {
                if (args.firstOrNull() == "install" && args.size > 1) {
                    val pkg = args[1]
                    appendConsoleOutput("Collecting $pkg...\n  Downloading $pkg-2.4.0-py3-none-any.whl (1.2 MB)\n  Installing collected packages: $pkg\nSuccessfully installed $pkg-2.4.0\n", isSystem = true)
                } else if (args.firstOrNull() == "list") {
                    appendConsoleOutput("Package    Version\n---------- -------\nrequests   2.31.0\nnumpy      1.26.4\nrich       13.7.1\nfastapi    0.110.0\npydantic   2.6.4\n", isSystem = true)
                } else {
                    appendConsoleOutput("Usage: pip install <package> | pip list\n", isSystem = true)
                }
            }
            "npm", "pnpm", "yarn" -> {
                if (args.firstOrNull() in listOf("install", "i", "add") && args.size > 1) {
                    val pkg = args[1]
                    appendConsoleOutput("added 24 packages in 0.42s\n+ $pkg@latest\n", isSystem = true)
                } else {
                    appendConsoleOutput("Usage: npm install <package>\n", isSystem = true)
                }
            }
            "cargo" -> {
                if (args.firstOrNull() == "add" && args.size > 1) {
                    val crate = args[1]
                    appendConsoleOutput("    Updating crates.io index\n      Adding $crate v1.0.0 to dependencies\n", isSystem = true)
                } else {
                    appendConsoleOutput("Usage: cargo add <crate>\n", isSystem = true)
                }
            }
            "git" -> {
                val sub = args.firstOrNull()
                when (sub) {
                    "status" -> {
                        val modifiedCount = _uiState.value.openTabs.count { it.isModified }
                        appendConsoleOutput("On branch ${_uiState.value.gitBranch}\nChanges not staged for commit:\n  (use \"git commit -m <msg>\" to commit)\n  modified: ${_uiState.value.activeFileName} ($modifiedCount modified files)\n\n", isSystem = true)
                    }
                    "commit" -> {
                        val msg = args.drop(1).joinToString(" ").removePrefix("-m").trim().removeSurrounding("\"")
                        if (msg.isNotBlank()) {
                            commitCurrentChanges(msg)
                            appendConsoleOutput("[${_uiState.value.gitBranch} ${_uiState.value.gitCommits.firstOrNull()?.hash}] $msg\n 1 file changed\n", isSystem = true)
                        } else {
                            appendConsoleOutput("error: commit message required (-m \"message\")\n", isError = true)
                        }
                    }
                    "log" -> {
                        val logText = _uiState.value.gitCommits.joinToString("\n") {
                            "* \u001b[33m${it.hash}\u001b[0m - ${it.message} (\u001b[36m${it.timeAgo}\u001b[0m) <${it.author}>"
                        }
                        appendConsoleOutput("$logText\n\n", isSystem = true)
                    }
                    "diff" -> {
                        openGitDiffModal()
                    }
                    "branch" -> {
                        appendConsoleOutput("* \u001b[32m${_uiState.value.gitBranch}\u001b[0m\n  feature/polyglot-ide\n  release/v1.0\n", isSystem = true)
                    }
                    else -> {
                        appendConsoleOutput("git version 2.44.0\nCommands: status, commit -m \"msg\", log, diff, branch\n", isSystem = true)
                    }
                }
            }
            "ai", "copilot", "ask" -> {
                val query = args.joinToString(" ")
                if (query.isNotBlank()) {
                    sendAiMessage(query)
                    appendConsoleOutput("🤖 Asking Copilot: \"$query\"... (opening Copilot pane)\n", isSystem = true)
                } else {
                    appendConsoleOutput("Usage: ai <your question or code request>\n", isSystem = true)
                }
            }
            else -> {
                // Direct interactive Python expression
                viewModelScope.launch {
                    val output = runtime.executeReplLine(trimmed)
                    if (output != null) {
                        appendConsoleOutput("$output\n", isError = false)
                    }
                }
            }
        }
    }

    fun submitReplLine(line: String) {
        if (line.isBlank()) return
        viewModelScope.launch {
            val output = runtime.executeReplLine(line)
            _uiState.update { state ->
                state.copy(
                    replLines = state.replLines + (line to output),
                    replInput = ""
                )
            }
        }
    }

    fun clearConsole() {
        _uiState.update {
            it.copy(
                consoleOutputs = emptyList(),
                replLines = emptyList()
            )
        }
        showShortcutNotification(ShortcutAction.CLEAR_CONSOLE, "[Ctrl+L] Terminal Cleared")
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.editorValue.text
        val prev = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(current)
        _uiState.update {
            it.copy(editorValue = TextFieldValue(prev, TextRange(prev.length.coerceAtMost(it.editorValue.selection.start))))
        }
        showShortcutNotification(ShortcutAction.UNDO, "[Ctrl+Z] Undo")
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.editorValue.text
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(current)
        _uiState.update {
            it.copy(editorValue = TextFieldValue(next, TextRange(next.length.coerceAtMost(it.editorValue.selection.start))))
        }
        showShortcutNotification(ShortcutAction.REDO, "[Ctrl+Y] Redo")
    }

    fun indentActiveLine() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.take(cursor).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + "    " + text.substring(lineStart)
        onEditorValueChange(TextFieldValue(newText, TextRange(cursor + 4)))
        showShortcutNotification(ShortcutAction.INDENT, "[Tab] Indented 4 spaces")
    }

    fun outdentActiveLine() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start
        val lineStart = text.take(cursor).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }

        val lineText = text.substring(lineStart)
        val spacesToRemove = when {
            lineText.startsWith("    ") -> 4
            lineText.startsWith("   ") -> 3
            lineText.startsWith("  ") -> 2
            lineText.startsWith(" ") -> 1
            lineText.startsWith("\t") -> 1
            else -> 0
        }

        if (spacesToRemove > 0) {
            val newText = text.substring(0, lineStart) + text.substring(lineStart + spacesToRemove)
            val newCursor = (cursor - spacesToRemove).coerceAtLeast(lineStart)
            onEditorValueChange(TextFieldValue(newText, TextRange(newCursor)))
            showShortcutNotification(ShortcutAction.OUTDENT, "[Shift+Tab] Outdented")
        }
    }

    fun toggleLineComment() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start
        val lang = _uiState.value.activeFileLanguage

        val commentPrefix = when (lang) {
            "python", "shell", "yaml" -> "# "
            "html" -> "<!-- "
            else -> "// "
        }

        val lineStart = text.take(cursor).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val trimmed = line.trimStart()
        val leadingSpaces = line.takeWhile { it == ' ' || it == '\t' }

        val newLine = if (trimmed.startsWith(commentPrefix)) {
            leadingSpaces + trimmed.substring(commentPrefix.length)
        } else if (trimmed.startsWith(commentPrefix.trim())) {
            leadingSpaces + trimmed.substring(commentPrefix.trim().length)
        } else {
            leadingSpaces + commentPrefix + trimmed
        }

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val diff = newLine.length - line.length
        onEditorValueChange(TextFieldValue(newText, TextRange((cursor + diff).coerceIn(0, newText.length))))
        showShortcutNotification(ShortcutAction.TOGGLE_COMMENT, "[Ctrl+/] Toggle Comment")
    }

    fun duplicateLine() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.take(cursor).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val newText = text.substring(0, lineEnd) + "\n" + line + text.substring(lineEnd)
        onEditorValueChange(TextFieldValue(newText, TextRange(lineEnd + 1 + line.length)))
        showShortcutNotification(ShortcutAction.DUPLICATE_LINE, "[Ctrl+D] Duplicated Line")
    }

    fun deleteLine() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.take(cursor).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it + 1 }

        val newText = text.substring(0, lineStart) + text.substring(lineEnd.coerceAtMost(text.length))
        onEditorValueChange(TextFieldValue(newText, TextRange(lineStart.coerceIn(0, newText.length))))
        showShortcutNotification(ShortcutAction.DELETE_LINE, "[Ctrl+Shift+K] Deleted Line")
    }

    fun moveLineUp() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start

        val lines = text.lines().toMutableList()
        val currentLineIdx = text.take(cursor).count { it == '\n' }

        if (currentLineIdx > 0 && lines.size > 1) {
            val temp = lines[currentLineIdx]
            lines[currentLineIdx] = lines[currentLineIdx - 1]
            lines[currentLineIdx - 1] = temp

            val newText = lines.joinToString("\n")
            onEditorValueChange(TextFieldValue(newText, TextRange(cursor.coerceIn(0, newText.length))))
            showShortcutNotification(ShortcutAction.MOVE_LINE_UP, "[Alt+Up] Moved Line Up")
        }
    }

    fun moveLineDown() {
        val current = _uiState.value.editorValue
        val text = current.text
        val cursor = current.selection.start

        val lines = text.lines().toMutableList()
        val currentLineIdx = text.take(cursor).count { it == '\n' }

        if (currentLineIdx < lines.size - 1) {
            val temp = lines[currentLineIdx]
            lines[currentLineIdx] = lines[currentLineIdx + 1]
            lines[currentLineIdx + 1] = temp

            val newText = lines.joinToString("\n")
            onEditorValueChange(TextFieldValue(newText, TextRange(cursor.coerceIn(0, newText.length))))
            showShortcutNotification(ShortcutAction.MOVE_LINE_DOWN, "[Alt+Down] Moved Line Down")
        }
    }

    fun formatDocument() {
        val current = _uiState.value.editorValue.text
        val lines = current.lines()
        val formatted = StringBuilder()
        var indentLevel = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                formatted.append("\n")
                continue
            }

            if (trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]") ||
                trimmed.startsWith("elif") || trimmed.startsWith("else:") || trimmed.startsWith("except") || trimmed.startsWith("finally:")
            ) {
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
            }

            val indentSpaces = "    ".repeat(indentLevel)
            formatted.append(indentSpaces).append(trimmed).append("\n")

            if (trimmed.endsWith(":") || trimmed.endsWith("{") || trimmed.endsWith("(")) {
                indentLevel++
            }
        }

        val result = formatted.toString().trimEnd()
        onEditorValueChange(TextFieldValue(result, TextRange(0)))
        showShortcutNotification(ShortcutAction.FORMAT_DOCUMENT, "[Shift+Alt+F] Document Formatted")
    }

    fun insertTextAtCursor(snippet: String) {
        val current = _uiState.value.editorValue
        val text = current.text
        val sel = current.selection

        val newText = text.substring(0, sel.start) + snippet + text.substring(sel.end)
        val newPos = sel.start + snippet.length
        onEditorValueChange(TextFieldValue(newText, TextRange(newPos)))
    }

    fun goToLine(lineNumber: Int) {
        val lines = _uiState.value.editorValue.text.lines()
        val target = (lineNumber - 1).coerceIn(0, (lines.size - 1).coerceAtLeast(0))
        var charOffset = 0
        for (i in 0 until target) {
            charOffset += lines[i].length + 1
        }

        _uiState.update {
            it.copy(
                editorValue = it.editorValue.copy(selection = TextRange(charOffset)),
                isGoToLineOpen = false,
                cursorLine = target + 1,
                cursorColumn = 1,
                activeFocusArea = ActiveFocusArea.EDITOR
            )
        }
        showShortcutNotification(ShortcutAction.GOTO_LINE, "[Ctrl+G] Jumped to line ${target + 1}")
    }

    fun performSearch(query: String) {
        val text = _uiState.value.editorValue.text
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchQuery = "", searchMatchCount = 0) }
            return
        }

        val matches = mutableListOf<IntRange>()
        var index = text.indexOf(query, 0, ignoreCase = true)
        while (index >= 0) {
            matches.add(index until (index + query.length))
            index = text.indexOf(query, index + 1, ignoreCase = true)
        }

        _uiState.update {
            it.copy(
                searchQuery = query,
                searchMatchCount = matches.size,
                currentMatchIndex = if (matches.isNotEmpty()) 1 else 0
            )
        }

        if (matches.isNotEmpty()) {
            val first = matches[0]
            _uiState.update {
                it.copy(editorValue = it.editorValue.copy(selection = TextRange(first.first, first.last + 1)))
            }
        }
    }

    fun replaceCurrentMatch(replacement: String) {
        val query = _uiState.value.searchQuery
        if (query.isEmpty()) return

        val current = _uiState.value.editorValue
        val text = current.text
        val sel = current.selection

        if (sel.length > 0 && text.substring(sel.start, sel.end).equals(query, ignoreCase = true)) {
            val newText = text.substring(0, sel.start) + replacement + text.substring(sel.end)
            onEditorValueChange(TextFieldValue(newText, TextRange(sel.start + replacement.length)))
            performSearch(query)
        }
    }

    fun replaceAllMatches(replacement: String) {
        val query = _uiState.value.searchQuery
        if (query.isEmpty()) return

        val current = _uiState.value.editorValue
        val text = current.text
        val newText = text.replace(query, replacement, ignoreCase = true)
        onEditorValueChange(TextFieldValue(newText, TextRange(0)))
        performSearch(query)
    }

    fun performGlobalSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(globalSearchQuery = "", globalSearchResults = emptyList()) }
            return
        }

        val matches = mutableListOf<GlobalSearchMatch>()
        for (file in _uiState.value.files) {
            if (file.isDirectory) continue
            file.content.lines().forEachIndexed { lineIdx, lineText ->
                var pos = lineText.indexOf(query, 0, ignoreCase = true)
                while (pos >= 0) {
                    matches.add(
                        GlobalSearchMatch(
                            fileId = file.id,
                            fileName = file.name,
                            filePath = file.path,
                            lineNumber = lineIdx + 1,
                            lineText = lineText.trim(),
                            matchStartIndex = pos
                        )
                    )
                    pos = lineText.indexOf(query, pos + 1, ignoreCase = true)
                }
            }
        }

        _uiState.update {
            it.copy(
                globalSearchQuery = query,
                globalSearchResults = matches
            )
        }
    }

    fun createNewFile(name: String) {
        val project = _uiState.value.currentProject ?: return
        val finalName = if (name.contains(".")) name else "$name.py"
        val lang = detectLanguage(finalName)
        
        viewModelScope.launch {
            val boilerplate = when (lang) {
                "python" -> "# $finalName\ndef main():\n    print('Hello from $finalName!')\n\nif __name__ == '__main__':\n    main()\n"
                "javascript" -> "// $finalName\nconsole.log('Hello from $finalName');\n"
                "html" -> "<!DOCTYPE html>\n<html>\n<head><title>$finalName</title></head>\n<body>\n    <h1>Hello</h1>\n</body>\n</html>\n"
                "json" -> "{\n  \"name\": \"$finalName\"\n}\n"
                "markdown" -> "# $finalName\n\nDocument details here.\n"
                else -> "// $finalName\n"
            }

            val id = repository.createFile(
                projectId = project.id,
                name = finalName,
                path = finalName,
                content = boilerplate,
                isDirectory = false
            )
            val newFile = repository.getFileById(id)
            if (newFile != null) {
                openFile(newFile)
            }
            _uiState.update { it.copy(isNewFileDialogOpen = false, activeFocusArea = ActiveFocusArea.EDITOR) }
            showShortcutNotification(ShortcutAction.NEW_FILE, "[Ctrl+N] Created $finalName")
        }
    }

    fun createNewFolder(name: String) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.createFile(
                projectId = project.id,
                name = name,
                path = name,
                content = "",
                isDirectory = true
            )
            _uiState.update { it.copy(isNewFolderDialogOpen = false) }
            showShortcutNotification(ShortcutAction.NEW_FOLDER, "[Ctrl+Shift+N] Created Folder $name")
        }
    }

    fun openRenameDialog(file: FileEntity) {
        _uiState.update { it.copy(isRenameDialogOpen = true, fileToRename = file) }
    }

    fun renameFile(newName: String) {
        val file = _uiState.value.fileToRename ?: return
        if (newName.isBlank() || newName == file.name) {
            _uiState.update { it.copy(isRenameDialogOpen = false, fileToRename = null) }
            return
        }

        viewModelScope.launch {
            val updated = file.copy(name = newName, path = newName, language = detectLanguage(newName))
            repository.renameFile(file, newName)
            
            // If active file was renamed
            if (_uiState.value.activeTabFileId == file.id) {
                val updatedTabs = _uiState.value.openTabs.map {
                    if (it.fileId == file.id) it.copy(name = newName, path = newName) else it
                }
                _uiState.update {
                    it.copy(
                        openTabs = updatedTabs,
                        activeFileName = newName,
                        activeFilePath = newName,
                        activeFileLanguage = updated.language
                    )
                }
            }
            _uiState.update { it.copy(isRenameDialogOpen = false, fileToRename = null) }
            showShortcutNotification(ShortcutAction.RENAME_FILE, "[F2] Renamed to $newName")
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            val tab = _uiState.value.openTabs.find { it.fileId == file.id }
            if (tab != null) {
                closeTab(tab)
            }
        }
    }

    // Keyboard Explorer Traversal
    fun moveExplorerSelection(delta: Int) {
        val files = _uiState.value.files
        if (files.isEmpty()) return
        val newIndex = (_uiState.value.explorerSelectedIndex + delta).coerceIn(0, files.size - 1)
        _uiState.update { it.copy(explorerSelectedIndex = newIndex) }
    }

    fun openExplorerSelectedFile() {
        val files = _uiState.value.files
        val idx = _uiState.value.explorerSelectedIndex
        val file = files.getOrNull(idx) ?: return
        if (!file.isDirectory) {
            openFile(file)
            _uiState.update { it.copy(activeFocusArea = ActiveFocusArea.EDITOR) }
        }
    }

    fun nextTab() {
        val tabs = _uiState.value.openTabs
        if (tabs.size <= 1) return
        val currentIdx = tabs.indexOfFirst { it.fileId == _uiState.value.activeTabFileId }
        val nextIdx = (currentIdx + 1) % tabs.size
        val file = _uiState.value.files.find { it.id == tabs[nextIdx].fileId }
        if (file != null) openFile(file)
        showShortcutNotification(ShortcutAction.NEXT_TAB, "[Ctrl+Tab] Next Tab")
    }

    fun prevTab() {
        val tabs = _uiState.value.openTabs
        if (tabs.size <= 1) return
        val currentIdx = tabs.indexOfFirst { it.fileId == _uiState.value.activeTabFileId }
        val prevIdx = if (currentIdx <= 0) tabs.size - 1 else currentIdx - 1
        val file = _uiState.value.files.find { it.id == tabs[prevIdx].fileId }
        if (file != null) openFile(file)
        showShortcutNotification(ShortcutAction.PREV_TAB, "[Ctrl+Shift+Tab] Previous Tab")
    }

    fun toggleZenMode() {
        val newZen = !_uiState.value.isZenMode
        _uiState.update {
            it.copy(
                isZenMode = newZen,
                isSidebarOpen = if (newZen) false else it.isSidebarOpen,
                isTerminalOpen = if (newZen) false else it.isTerminalOpen
            )
        }
        val msg = if (newZen) "[F11] Entered Zen Mode (Fullscreen)" else "[F11] Exited Zen Mode"
        showShortcutNotification(ShortcutAction.TOGGLE_ZEN_MODE, msg)
    }

    fun toggleTerminalDockPosition() {
        val next = if (_uiState.value.terminalDockPosition == TerminalDockPosition.RIGHT) TerminalDockPosition.BOTTOM else TerminalDockPosition.RIGHT
        _uiState.update { it.copy(terminalDockPosition = next, isTerminalOpen = true) }
        val label = if (next == TerminalDockPosition.RIGHT) "Side-by-Side Right" else "Bottom Dock"
        showShortcutNotification(ShortcutAction.TOGGLE_TERMINAL_DOCK, "[Ctrl+Alt+S] Terminal: $label")
    }

    fun toggleTabLayoutStyle() {
        val next = if (_uiState.value.tabLayoutStyle == TabLayoutStyle.HORIZONTAL) TabLayoutStyle.VERTICAL else TabLayoutStyle.HORIZONTAL
        _uiState.update { it.copy(tabLayoutStyle = next) }
        val label = if (next == TabLayoutStyle.VERTICAL) "Vertical Rail (Zero Vertical Space)" else "Horizontal Ribbon"
        showShortcutNotification(ShortcutAction.TOGGLE_TAB_LAYOUT, "[Ctrl+Alt+T] Tabs: $label")
    }

    fun toggleMobileAssistBar() {
        val next = !_uiState.value.isMobileAssistBarVisible
        _uiState.update { it.copy(isMobileAssistBarVisible = next) }
        val label = if (next) "Shown" else "Hidden (100% Code Canvas)"
        showShortcutNotification(ShortcutAction.TOGGLE_MOBILE_ASSIST_BAR, "[F12] Touch Bar $label")
    }

    fun setActiveFocus(focusArea: ActiveFocusArea) {
        _uiState.update { it.copy(activeFocusArea = focusArea) }
    }

    fun focusTerminal() {
        _uiState.update { it.copy(activeFocusArea = ActiveFocusArea.TERMINAL, isTerminalOpen = true) }
        showShortcutNotification(ShortcutAction.FOCUS_TERMINAL, "[Ctrl+3] Terminal Focused")
    }

    fun focusEditor() {
        _uiState.update { it.copy(activeFocusArea = ActiveFocusArea.EDITOR) }
        showShortcutNotification(ShortcutAction.FOCUS_EDITOR, "[Ctrl+1] Editor Focused")
    }

    fun focusExplorer() {
        _uiState.update {
            it.copy(
                activeFocusArea = ActiveFocusArea.EXPLORER,
                isSidebarOpen = true,
                activeSidebarTab = SidebarTab.EXPLORER
            )
        }
        showShortcutNotification(ShortcutAction.FOCUS_EXPLORER, "[Ctrl+2] Explorer Focused")
    }

    fun focusTabs() {
        _uiState.update { it.copy(activeFocusArea = ActiveFocusArea.TABS) }
        showShortcutNotification(ShortcutAction.FOCUS_TABS, "[Ctrl+4] Tabs Focused")
    }

    fun cycleFocus(forward: Boolean = true) {
        val current = _uiState.value.activeFocusArea
        val isZenMode = _uiState.value.isZenMode

        if (isZenMode) {
            _uiState.update { it.copy(activeFocusArea = ActiveFocusArea.EDITOR) }
            return
        }

        // Ordered cycle loop: EXPLORER -> EDITOR -> TERMINAL -> EXPLORER
        val cycleList = listOf(
            ActiveFocusArea.EXPLORER,
            ActiveFocusArea.EDITOR,
            ActiveFocusArea.TERMINAL
        )

        val currentIndex = cycleList.indexOf(current).let { if (it == -1) 1 else it }
        val nextIndex = if (forward) {
            (currentIndex + 1) % cycleList.size
        } else {
            (currentIndex - 1 + cycleList.size) % cycleList.size
        }

        when (cycleList[nextIndex]) {
            ActiveFocusArea.EXPLORER -> focusExplorer()
            ActiveFocusArea.EDITOR -> focusEditor()
            ActiveFocusArea.TERMINAL -> focusTerminal()
            else -> focusEditor()
        }
    }

    // Master Shortcut Dispatcher
    fun handleShortcutAction(action: ShortcutAction) {
        when (action) {
            ShortcutAction.RUN_SCRIPT -> runCurrentScript()
            ShortcutAction.RUN_REPL_LINE -> {
                val currentLine = _uiState.value.editorValue.text.lines().getOrNull(_uiState.value.cursorLine - 1) ?: ""
                _uiState.update { it.copy(isTerminalOpen = true, activeTerminalTab = TerminalTab.REPL) }
                submitReplLine(currentLine)
                showShortcutNotification(ShortcutAction.RUN_REPL_LINE, "[Ctrl+Shift+Enter] Sent line to REPL")
            }
            ShortcutAction.COMMAND_PALETTE -> {
                _uiState.update { it.copy(isCommandPaletteOpen = !it.isCommandPaletteOpen, isQuickOpenOpen = false) }
                showShortcutNotification(ShortcutAction.COMMAND_PALETTE, "[Ctrl+Shift+P] Command Palette")
            }
            ShortcutAction.QUICK_OPEN_FILE -> {
                _uiState.update { it.copy(isQuickOpenOpen = !it.isQuickOpenOpen, isCommandPaletteOpen = false) }
                showShortcutNotification(ShortcutAction.QUICK_OPEN_FILE, "[Ctrl+P] Quick Open")
            }
            ShortcutAction.SAVE_FILE -> saveActiveFile()
            ShortcutAction.TOGGLE_ZEN_MODE -> toggleZenMode()
            ShortcutAction.TOGGLE_TERMINAL_DOCK -> toggleTerminalDockPosition()
            ShortcutAction.TOGGLE_TAB_LAYOUT -> toggleTabLayoutStyle()
            ShortcutAction.TOGGLE_MOBILE_ASSIST_BAR -> toggleMobileAssistBar()

            ShortcutAction.FOCUS_EDITOR -> focusEditor()
            ShortcutAction.FOCUS_EXPLORER -> focusExplorer()
            ShortcutAction.FOCUS_TERMINAL -> focusTerminal()
            ShortcutAction.FOCUS_TABS -> focusTabs()
            ShortcutAction.CYCLE_FOCUS_NEXT -> cycleFocus(forward = true)
            ShortcutAction.CYCLE_FOCUS_PREV -> cycleFocus(forward = false)

            ShortcutAction.TAB_1 -> selectTabByIndex(0)
            ShortcutAction.TAB_2 -> selectTabByIndex(1)
            ShortcutAction.TAB_3 -> selectTabByIndex(2)
            ShortcutAction.TAB_4 -> selectTabByIndex(3)
            ShortcutAction.TAB_5 -> selectTabByIndex(4)
            ShortcutAction.TAB_6 -> selectTabByIndex(5)
            ShortcutAction.TAB_7 -> selectTabByIndex(6)
            ShortcutAction.TAB_8 -> selectTabByIndex(7)
            ShortcutAction.TAB_9 -> selectTabByIndex(_uiState.value.openTabs.size - 1)

            ShortcutAction.TOGGLE_EXPLORER -> {
                val next = !_uiState.value.isSidebarOpen
                _uiState.update { it.copy(isSidebarOpen = next, activeSidebarTab = SidebarTab.EXPLORER) }
                showShortcutNotification(ShortcutAction.TOGGLE_EXPLORER, "[Ctrl+B] Explorer ${if (next) "Opened" else "Closed"}")
            }
            ShortcutAction.TOGGLE_TERMINAL -> {
                val next = !_uiState.value.isTerminalOpen
                _uiState.update { it.copy(isTerminalOpen = next) }
                showShortcutNotification(ShortcutAction.TOGGLE_TERMINAL, "[Ctrl+`] Terminal ${if (next) "Opened" else "Closed"}")
            }
            ShortcutAction.FIND_IN_FILE -> {
                _uiState.update { it.copy(isFindBarOpen = true, isReplaceMode = false) }
                showShortcutNotification(ShortcutAction.FIND_IN_FILE, "[Ctrl+F] Find Bar")
            }
            ShortcutAction.REPLACE_IN_FILE -> {
                _uiState.update { it.copy(isFindBarOpen = true, isReplaceMode = true) }
                showShortcutNotification(ShortcutAction.REPLACE_IN_FILE, "[Ctrl+H] Replace Bar")
            }
            ShortcutAction.GOTO_LINE -> {
                _uiState.update { it.copy(isGoToLineOpen = true) }
                showShortcutNotification(ShortcutAction.GOTO_LINE, "[Ctrl+G] Go to Line")
            }
            ShortcutAction.TOGGLE_COMMENT -> toggleLineComment()
            ShortcutAction.INDENT -> indentActiveLine()
            ShortcutAction.OUTDENT -> outdentActiveLine()
            ShortcutAction.DUPLICATE_LINE -> duplicateLine()
            ShortcutAction.DELETE_LINE -> deleteLine()
            ShortcutAction.MOVE_LINE_UP -> moveLineUp()
            ShortcutAction.MOVE_LINE_DOWN -> moveLineDown()
            ShortcutAction.UNDO -> undo()
            ShortcutAction.REDO -> redo()
            ShortcutAction.FORMAT_DOCUMENT -> formatDocument()
            ShortcutAction.CLOSE_TAB -> {
                val activeTab = _uiState.value.openTabs.find { it.fileId == _uiState.value.activeTabFileId }
                if (activeTab != null) closeTab(activeTab)
                showShortcutNotification(ShortcutAction.CLOSE_TAB, "[Ctrl+W] Tab Closed")
            }
            ShortcutAction.REOPEN_CLOSED_TAB -> reopenClosedTab()
            ShortcutAction.NEXT_TAB -> nextTab()
            ShortcutAction.PREV_TAB -> prevTab()
            ShortcutAction.NEW_FILE -> {
                _uiState.update { it.copy(isNewFileDialogOpen = true) }
                showShortcutNotification(ShortcutAction.NEW_FILE, "[Ctrl+N] New File Dialog")
            }
            ShortcutAction.NEW_FOLDER -> {
                _uiState.update { it.copy(isNewFolderDialogOpen = true) }
                showShortcutNotification(ShortcutAction.NEW_FOLDER, "[Ctrl+Shift+N] New Folder Dialog")
            }
            ShortcutAction.RENAME_FILE -> {
                val activeFile = _uiState.value.files.find { it.id == _uiState.value.activeTabFileId }
                if (activeFile != null) {
                    openRenameDialog(activeFile)
                }
            }
            ShortcutAction.CLEAR_CONSOLE -> clearConsole()
            ShortcutAction.TERMINAL_INTERRUPT -> stopExecution()
            ShortcutAction.SHORTCUTS_HELP -> {
                _uiState.update { it.copy(isShortcutsHelpOpen = true) }
                showShortcutNotification(ShortcutAction.SHORTCUTS_HELP, "[F2] Shortcuts Map")
            }
            ShortcutAction.OPEN_SETTINGS -> {
                _uiState.update { it.copy(isSettingsOpen = true) }
                showShortcutNotification(ShortcutAction.OPEN_SETTINGS, "[Ctrl+,] Settings")
            }
            ShortcutAction.GLOBAL_SEARCH -> {
                _uiState.update { it.copy(isSidebarOpen = true, activeSidebarTab = SidebarTab.SEARCH, activeFocusArea = ActiveFocusArea.SEARCH) }
                showShortcutNotification(ShortcutAction.GLOBAL_SEARCH, "[Ctrl+Shift+F] Search in Project")
            }
            ShortcutAction.TOGGLE_SOURCE_CONTROL -> {
                _uiState.update { it.copy(isSidebarOpen = true, activeSidebarTab = SidebarTab.SOURCE_CONTROL) }
                showShortcutNotification(ShortcutAction.TOGGLE_SOURCE_CONTROL, "[Ctrl+Shift+G] Source Control")
            }
            ShortcutAction.TOGGLE_RUN_DEBUG -> {
                _uiState.update { it.copy(isSidebarOpen = true, activeSidebarTab = SidebarTab.RUN_DEBUG) }
                showShortcutNotification(ShortcutAction.TOGGLE_RUN_DEBUG, "[Ctrl+Shift+D] Run & Debug")
            }
            ShortcutAction.TOGGLE_EXTENSIONS -> {
                _uiState.update { it.copy(isSidebarOpen = true, activeSidebarTab = SidebarTab.EXTENSIONS) }
                showShortcutNotification(ShortcutAction.TOGGLE_EXTENSIONS, "[Ctrl+Shift+X] Extensions")
            }
            ShortcutAction.NEW_PROJECT_TEMPLATE -> {
                _uiState.update { it.copy(isNewProjectDialogOpen = true) }
                showShortcutNotification(ShortcutAction.NEW_PROJECT_TEMPLATE, "New Project Template Dialog")
            }
            ShortcutAction.ESCAPE -> {
                _uiState.update {
                    it.copy(
                        isCommandPaletteOpen = false,
                        isQuickOpenOpen = false,
                        isGoToLineOpen = false,
                        isShortcutsHelpOpen = false,
                        isSettingsOpen = false,
                        isNewFileDialogOpen = false,
                        isNewFolderDialogOpen = false,
                        isRenameDialogOpen = false,
                        isFindBarOpen = false,
                        activeFocusArea = ActiveFocusArea.EDITOR
                    )
                }
            }
        }
    }

    // Toggle Latches for touch/mobile assist bar
    fun toggleLatchCtrl() { _uiState.update { it.copy(latchedCtrl = !it.latchedCtrl) } }
    fun toggleLatchAlt() { _uiState.update { it.copy(latchedAlt = !it.latchedAlt) } }
    fun toggleLatchShift() { _uiState.update { it.copy(latchedShift = !it.latchedShift) } }

    fun setSidebarTab(tab: SidebarTab) {
        _uiState.update { it.copy(activeSidebarTab = tab, isSidebarOpen = true) }
    }

    fun setTerminalTab(tab: TerminalTab) {
        _uiState.update { it.copy(activeTerminalTab = tab, isTerminalOpen = true) }
    }

    fun setSidebarOpen(open: Boolean) { _uiState.update { it.copy(isSidebarOpen = open) } }
    fun setTerminalOpen(open: Boolean) { _uiState.update { it.copy(isTerminalOpen = open) } }
    fun setCommandPaletteOpen(open: Boolean) { _uiState.update { it.copy(isCommandPaletteOpen = open) } }
    fun setQuickOpenOpen(open: Boolean) { _uiState.update { it.copy(isQuickOpenOpen = open) } }
    fun setGoToLineOpen(open: Boolean) { _uiState.update { it.copy(isGoToLineOpen = open) } }
    fun setShortcutsHelpOpen(open: Boolean) { _uiState.update { it.copy(isShortcutsHelpOpen = open) } }
    fun setSettingsOpen(open: Boolean) { _uiState.update { it.copy(isSettingsOpen = open) } }
    fun setNewFileDialogOpen(open: Boolean) { _uiState.update { it.copy(isNewFileDialogOpen = open) } }
    fun setNewFolderDialogOpen(open: Boolean) { _uiState.update { it.copy(isNewFolderDialogOpen = open) } }
    fun setFindBarOpen(open: Boolean) { _uiState.update { it.copy(isFindBarOpen = open) } }
    fun setRenameDialogOpen(open: Boolean) { _uiState.update { it.copy(isRenameDialogOpen = open) } }

    fun setLanguageMode(language: String) {
        _uiState.update { it.copy(activeFileLanguage = language) }
        showShortcutNotification(ShortcutAction.OPEN_SETTINGS, "Language mode: $language")
    }

    fun updateSettings(fontSize: Float, tabSize: Int, wordWrap: Boolean, showLineNumbers: Boolean) {
        _uiState.update {
            it.copy(
                fontSizeSp = fontSize,
                tabSize = tabSize,
                wordWrap = wordWrap,
                showLineNumbers = showLineNumbers
            )
        }
    }

    fun selectPrimaryStage(stage: PrimaryStage) {
        _uiState.update {
            it.copy(
                primaryStage = stage,
                isTerminalOpen = stage == PrimaryStage.TERMINAL,
                isWebPreviewOpen = stage == PrimaryStage.WEB_PREVIEW,
                isSidebarOpen = stage == PrimaryStage.EXPLORER || stage == PrimaryStage.AI_COPILOT || stage == PrimaryStage.GLOBAL_SEARCH,
                activeSidebarTab = when (stage) {
                    PrimaryStage.EXPLORER -> SidebarTab.EXPLORER
                    PrimaryStage.AI_COPILOT -> SidebarTab.AI_COPILOT
                    PrimaryStage.GLOBAL_SEARCH -> SidebarTab.SEARCH
                    else -> it.activeSidebarTab
                },
                activeFocusArea = when (stage) {
                    PrimaryStage.EDITOR -> ActiveFocusArea.EDITOR
                    PrimaryStage.TERMINAL -> ActiveFocusArea.TERMINAL
                    PrimaryStage.EXPLORER -> ActiveFocusArea.EXPLORER
                    PrimaryStage.GLOBAL_SEARCH -> ActiveFocusArea.SEARCH
                    else -> it.activeFocusArea
                }
            )
        }
    }

    fun setColorPickerOpen(open: Boolean) {
        _uiState.update { it.copy(isColorPickerOpen = open) }
    }

    fun setSnippetsLibraryOpen(open: Boolean) {
        _uiState.update { it.copy(isSnippetsLibraryOpen = open) }
    }

    fun onFontSizeZoom(zoomRatio: Float) {
        val currentSize = _uiState.value.fontSizeSp
        val newSize = (currentSize * zoomRatio).coerceIn(10f, 32f)
        _uiState.update { it.copy(fontSizeSp = newSize) }
    }

    fun resetFontSize() {
        _uiState.update { it.copy(fontSizeSp = 13.5f) }
        showShortcutNotification(ShortcutAction.OPEN_SETTINGS, "Font size reset to 13.5sp")
    }

    fun openWebPreview() {
        _uiState.update { it.copy(isWebPreviewOpen = true, primaryStage = PrimaryStage.WEB_PREVIEW) }
    }

    fun closeWebPreview() {
        _uiState.update { it.copy(isWebPreviewOpen = false, primaryStage = PrimaryStage.EDITOR) }
    }

    fun toggleWebPreview() {
        val next = !_uiState.value.isWebPreviewOpen
        _uiState.update {
            it.copy(
                isWebPreviewOpen = next,
                primaryStage = if (next) PrimaryStage.WEB_PREVIEW else PrimaryStage.EDITOR
            )
        }
    }

    fun setNewProjectDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isNewProjectDialogOpen = open) }
    }

    fun createProjectFromTemplate(template: ProjectTemplate) {
        viewModelScope.launch {
            val projectId = repository.createProjectWithFiles(
                name = template.name,
                description = template.description,
                filesGenerator = template.initialFiles
            )
            val files = repository.getFilesByProject(projectId)
            _uiState.update { it.copy(isNewProjectDialogOpen = false, openTabs = emptyList(), activeTabFileId = null) }
            showShortcutNotification(ShortcutAction.OPEN_SETTINGS, "Created ${template.name}")
        }
    }

    fun commitCurrentChanges(message: String) {
        val newCommit = GitCommit(
            hash = java.util.UUID.randomUUID().toString().take(7),
            message = message,
            author = "Developer",
            timeAgo = "just now"
        )
        _uiState.update { state ->
            val updatedTabs = state.openTabs.map { it.copy(isModified = false) }
            state.copy(
                gitCommits = listOf(newCommit) + state.gitCommits,
                isFileModified = false,
                openTabs = updatedTabs
            )
        }
        showShortcutNotification(ShortcutAction.SAVE_FILE, "[Git] Committed: \"$message\"")
    }

    fun insertSnippet(snippet: String) {
        val currentText = _uiState.value.editorValue.text
        val cursor = _uiState.value.editorValue.selection.start
        val newText = currentText.substring(0, cursor) + "\n" + snippet + "\n" + currentText.substring(cursor)
        val newCursor = cursor + snippet.length + 2
        onEditorValueChange(TextFieldValue(newText, TextRange(newCursor)))
        showShortcutNotification(ShortcutAction.FORMAT_DOCUMENT, "Inserted Snippet")
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(sender = MessageSender.USER, text = prompt)
        _uiState.update { state ->
            state.copy(
                aiChatMessages = state.aiChatMessages + userMsg,
                isAiLoading = true,
                isSidebarOpen = true,
                primaryStage = PrimaryStage.AI_COPILOT,
                activeSidebarTab = SidebarTab.AI_COPILOT
            )
        }

        viewModelScope.launch {
            val response = aiCopilotService.chatWithCopilot(
                userPrompt = prompt,
                activeCode = _uiState.value.editorValue.text,
                language = _uiState.value.activeFileLanguage,
                fileName = _uiState.value.activeFileName,
                history = _uiState.value.aiChatMessages
            )
            val aiMsg = AiChatMessage(sender = MessageSender.AI, text = response)
            _uiState.update { state ->
                state.copy(
                    aiChatMessages = state.aiChatMessages + aiMsg,
                    isAiLoading = false
                )
            }
        }
    }

    fun clearAiChat() {
        _uiState.update { it.copy(aiChatMessages = emptyList()) }
    }

    fun insertAiSnippet(snippet: String) {
        insertTextAtCursor(snippet)
        showShortcutNotification(ShortcutAction.FORMAT_DOCUMENT, "AI Snippet Inserted")
    }

    fun replaceFileWithAiCode(code: String) {
        undoStack.add(_uiState.value.editorValue.text)
        redoStack.clear()
        onEditorValueChange(TextFieldValue(code, TextRange(code.length)))
        showShortcutNotification(ShortcutAction.SAVE_FILE, "File replaced with AI Code")
    }

    fun runSnippetDirectly(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTerminalOpen = true, activeTerminalTab = TerminalTab.OUTPUT) }
            polyglotRuntime.execute(code, _uiState.value.activeFileLanguage, "snippet")
        }
    }

    fun openGitDiffModal(file: FileEntity? = null) {
        val targetFile = file ?: _uiState.value.files.find { it.id == _uiState.value.activeTabFileId }
        val original = targetFile?.content ?: ""
        val modified = _uiState.value.editorValue.text
        _uiState.update {
            it.copy(
                isGitDiffModalOpen = true,
                diffOriginalContent = original,
                diffModifiedContent = modified,
                diffFileName = targetFile?.name ?: "active_file"
            )
        }
    }

    fun closeGitDiffModal() {
        _uiState.update { it.copy(isGitDiffModalOpen = false) }
    }

    fun moveCursorLeft() {
        val cursor = (_uiState.value.editorValue.selection.start - 1).coerceAtLeast(0)
        _uiState.update { it.copy(editorValue = it.editorValue.copy(selection = TextRange(cursor))) }
    }

    fun moveCursorRight() {
        val maxLen = _uiState.value.editorValue.text.length
        val cursor = (_uiState.value.editorValue.selection.start + 1).coerceAtMost(maxLen)
        _uiState.update { it.copy(editorValue = it.editorValue.copy(selection = TextRange(cursor))) }
    }

    fun indentCurrentLine() {
        insertTextAtCursor("    ")
    }

    fun outdentCurrentLine() {
        val currentText = _uiState.value.editorValue.text
        val cursor = _uiState.value.editorValue.selection.start
        if (cursor >= 4 && currentText.substring(cursor - 4, cursor) == "    ") {
            val newText = currentText.substring(0, cursor - 4) + currentText.substring(cursor)
            onEditorValueChange(TextFieldValue(newText, TextRange(cursor - 4)))
        } else if (cursor > 0 && currentText[cursor - 1] == ' ') {
            val newText = currentText.substring(0, cursor - 1) + currentText.substring(cursor)
            onEditorValueChange(TextFieldValue(newText, TextRange(cursor - 1)))
        }
    }

    fun toggleSmartAssistBar() {
        _uiState.update { it.copy(isSmartAssistBarVisible = !it.isSmartAssistBarVisible) }
    }

    private fun appendConsoleOutput(text: String, isError: Boolean = false, isSystem: Boolean = false) {
        val out = ConsoleOutput(text = text, isError = isError, isSystem = isSystem)
        _uiState.update {
            it.copy(consoleOutputs = it.consoleOutputs + out)
        }
    }

    private fun showShortcutNotification(action: ShortcutAction, msg: String) {
        val notif = ShortcutHUDNotification(action, action.defaultKeyDisplay, msg)
        _uiState.update { it.copy(activeHudNotification = notif) }
    }

    fun dismissHudNotification() {
        _uiState.update { it.copy(activeHudNotification = null) }
    }

    private fun stateFileName(): String = _uiState.value.activeFileName
}

class PyCodeViewModelFactory(private val repository: WorkspaceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PyCodeViewModel(repository) as T
    }
}
