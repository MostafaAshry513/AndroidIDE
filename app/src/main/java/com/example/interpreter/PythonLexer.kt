package com.example.interpreter

import androidx.compose.ui.text.AnnotatedString

object PythonLexer {
    fun highlightPython(code: String, colors: EditorColorScheme = EditorColorScheme()): AnnotatedString {
        return CodeSyntaxHighlighter.highlight(code, "python", colors)
    }
}
