package com.example.interpreter

import com.example.data.model.DiagnosticError
import com.example.data.model.DiagnosticSeverity
import java.util.Stack

object PythonLinter {

    private val BLOCK_KEYWORDS = setOf(
        "def", "class", "if", "elif", "else", "for", "while",
        "try", "except", "finally", "with", "match", "case"
    )

    fun lint(code: String): List<DiagnosticError> {
        val diagnostics = mutableListOf<DiagnosticError>()
        val lines = code.lines()

        val bracketStack = Stack<Pair<Char, Pair<Int, Int>>>() // char, (line, col)
        var inMultiLineString: Char? = null

        val indentStack = Stack<Int>()
        indentStack.push(0)

        for ((lineIndex, rawLine) in lines.withIndex()) {
            val lineNumber = lineIndex + 1
            val trimmed = rawLine.trim()

            // Skip empty lines
            if (trimmed.isEmpty()) continue

            // 1. Check indentation consistency (spaces vs tabs)
            val leadingWhitespace = rawLine.takeWhile { it.isWhitespace() }
            if (leadingWhitespace.contains("\t") && leadingWhitespace.contains(" ")) {
                diagnostics.add(
                    DiagnosticError(
                        line = lineNumber,
                        column = 1,
                        message = "TabError: Inconsistent use of tabs and spaces in indentation",
                        severity = DiagnosticSeverity.WARNING
                    )
                )
            }

            // Skip comment-only lines
            if (trimmed.startsWith("#")) continue

            // 2. Multi-line string tracking
            var i = 0
            while (i < rawLine.length) {
                if (inMultiLineString == null) {
                    if (i + 2 < rawLine.length && rawLine.substring(i, i + 3) == "\"\"\"") {
                        inMultiLineString = '"'
                        i += 3
                        continue
                    } else if (i + 2 < rawLine.length && rawLine.substring(i, i + 3) == "'''") {
                        inMultiLineString = '\''
                        i += 3
                        continue
                    }
                } else {
                    if (inMultiLineString == '"' && i + 2 < rawLine.length && rawLine.substring(i, i + 3) == "\"\"\"") {
                        inMultiLineString = null
                        i += 3
                        continue
                    } else if (inMultiLineString == '\'' && i + 2 < rawLine.length && rawLine.substring(i, i + 3) == "'''") {
                        inMultiLineString = null
                        i += 3
                        continue
                    }
                    i++
                    continue
                }

                val c = rawLine[i]
                if (c == '#') break // rest of line is comment

                // String literal skips
                if (c == '"' || c == '\'') {
                    val quote = c
                    val startCol = i + 1
                    i++
                    var escaped = false
                    var closed = false
                    while (i < rawLine.length) {
                        if (escaped) {
                            escaped = false
                        } else if (rawLine[i] == '\\') {
                            escaped = true
                        } else if (rawLine[i] == quote) {
                            closed = true
                            break
                        }
                        i++
                    }
                    if (!closed) {
                        diagnostics.add(
                            DiagnosticError(
                                line = lineNumber,
                                column = startCol,
                                message = "SyntaxError: Unclosed string literal (missing $quote)"
                            )
                        )
                    }
                    i++
                    continue
                }

                // Brackets tracking
                when (c) {
                    '(', '[', '{' -> bracketStack.push(c to (lineNumber to i + 1))
                    ')' -> {
                        if (bracketStack.isEmpty() || bracketStack.peek().first != '(') {
                            diagnostics.add(
                                DiagnosticError(
                                    line = lineNumber,
                                    column = i + 1,
                                    message = "SyntaxError: Unmatched closing parenthesis ')'"
                                )
                            )
                        } else {
                            bracketStack.pop()
                        }
                    }
                    ']' -> {
                        if (bracketStack.isEmpty() || bracketStack.peek().first != '[') {
                            diagnostics.add(
                                DiagnosticError(
                                    line = lineNumber,
                                    column = i + 1,
                                    message = "SyntaxError: Unmatched closing bracket ']'"
                                )
                            )
                        } else {
                            bracketStack.pop()
                        }
                    }
                    '}' -> {
                        if (bracketStack.isEmpty() || bracketStack.peek().first != '{') {
                            diagnostics.add(
                                DiagnosticError(
                                    line = lineNumber,
                                    column = i + 1,
                                    message = "SyntaxError: Unmatched closing brace '}'"
                                )
                            )
                        } else {
                            bracketStack.pop()
                        }
                    }
                }
                i++
            }

            // 3. Missing colon check on statement blocks
            if (inMultiLineString == null && bracketStack.isEmpty()) {
                val cleanLine = if (trimmed.contains("#")) trimmed.substringBefore("#").trim() else trimmed
                val firstWord = cleanLine.split(Regex("\\s+|\\("))[0]

                if (firstWord in BLOCK_KEYWORDS) {
                    if (!cleanLine.endsWith(":") && !cleanLine.endsWith("\\")) {
                        diagnostics.add(
                            DiagnosticError(
                                line = lineNumber,
                                column = rawLine.length,
                                message = "SyntaxError: Expected ':' at end of '$firstWord' statement"
                            )
                        )
                    }
                }
            }
        }

        // Check for unclosed brackets
        while (bracketStack.isNotEmpty()) {
            val (openChar, pos) = bracketStack.pop()
            val expected = when (openChar) {
                '(' -> "parenthesis ')'"
                '[' -> "bracket ']'"
                '{' -> "brace '}'"
                else -> "closing token"
            }
            diagnostics.add(
                DiagnosticError(
                    line = pos.first,
                    column = pos.second,
                    message = "SyntaxError: Unclosed $expected"
                )
            )
        }

        if (inMultiLineString != null) {
            diagnostics.add(
                DiagnosticError(
                    line = lines.size,
                    column = 1,
                    message = "SyntaxError: EOF while scanning triple-quoted string literal"
                )
            )
        }

        return diagnostics
    }
}
