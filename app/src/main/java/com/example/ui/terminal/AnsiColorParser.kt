package com.example.ui.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Lightweight ANSI escape sequence parser for raw terminal character streams.
 * Parses standard 16-color ANSI formatting, styles (bold, underline), and reset codes.
 */
object AnsiColorParser {

    private val ANSI_REGEX = Regex("\u001B\\[([0-9;]*)m")

    // Standard ANSI 16-Color Palette
    private val COLOR_BLACK = Color(0xFF1E1E22)
    private val COLOR_RED = Color(0xFFF14C4C)
    private val COLOR_GREEN = Color(0xFF4EC9B0)
    private val COLOR_YELLOW = Color(0xFFDCDCAA)
    private val COLOR_BLUE = Color(0xFF569CD6)
    private val COLOR_MAGENTA = Color(0xFFC586C0)
    private val COLOR_CYAN = Color(0xFF4FC1FF)
    private val COLOR_WHITE = Color(0xFFD4D4D4)

    private val COLOR_BRIGHT_BLACK = Color(0xFF6E6E78)
    private val COLOR_BRIGHT_RED = Color(0xFFF87171)
    private val COLOR_BRIGHT_GREEN = Color(0xFF6EE7B7)
    private val COLOR_BRIGHT_YELLOW = Color(0xFFFDE047)
    private val COLOR_BRIGHT_BLUE = Color(0xFF93C5FD)
    private val COLOR_BRIGHT_MAGENTA = Color(0xFFF472B6)
    private val COLOR_BRIGHT_CYAN = Color(0xFF7DD3FC)
    private val COLOR_BRIGHT_WHITE = Color(0xFFFFFFFF)

    fun parse(text: String, defaultColor: Color = COLOR_WHITE): AnnotatedString {
        if (!text.contains("\u001B[")) {
            return AnnotatedString(text)
        }

        return buildAnnotatedString {
            var currentIndex = 0
            var currentFg: Color? = null
            var currentBg: Color? = null
            var isBold = false
            var isItalic = false
            var isUnderline = false

            fun currentSpanStyle(): SpanStyle {
                return SpanStyle(
                    color = currentFg ?: defaultColor,
                    background = currentBg ?: Color.Transparent,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
                )
            }

            for (match in ANSI_REGEX.findAll(text)) {
                val matchRange = match.range
                if (matchRange.first > currentIndex) {
                    val rawSubstring = text.substring(currentIndex, matchRange.first)
                    pushStyle(currentSpanStyle())
                    append(rawSubstring)
                    pop()
                }

                // Process ANSI codes
                val codesString = match.groupValues[1]
                val codes = if (codesString.isEmpty()) {
                    listOf(0)
                } else {
                    codesString.split(";").mapNotNull { it.toIntOrNull() }
                }

                for (code in codes) {
                    when (code) {
                        0 -> {
                            currentFg = null
                            currentBg = null
                            isBold = false
                            isItalic = false
                            isUnderline = false
                        }
                        1 -> isBold = true
                        3 -> isItalic = true
                        4 -> isUnderline = true
                        22 -> isBold = false
                        23 -> isItalic = false
                        24 -> isUnderline = false
                        // Standard Foregrounds
                        30 -> currentFg = COLOR_BLACK
                        31 -> currentFg = COLOR_RED
                        32 -> currentFg = COLOR_GREEN
                        33 -> currentFg = COLOR_YELLOW
                        34 -> currentFg = COLOR_BLUE
                        35 -> currentFg = COLOR_MAGENTA
                        36 -> currentFg = COLOR_CYAN
                        37 -> currentFg = COLOR_WHITE
                        39 -> currentFg = null
                        // Standard Backgrounds
                        40 -> currentBg = COLOR_BLACK
                        41 -> currentBg = COLOR_RED
                        42 -> currentBg = COLOR_GREEN
                        43 -> currentBg = COLOR_YELLOW
                        44 -> currentBg = COLOR_BLUE
                        45 -> currentBg = COLOR_MAGENTA
                        46 -> currentBg = COLOR_CYAN
                        47 -> currentBg = COLOR_WHITE
                        49 -> currentBg = null
                        // Bright Foregrounds
                        90 -> currentFg = COLOR_BRIGHT_BLACK
                        91 -> currentFg = COLOR_BRIGHT_RED
                        92 -> currentFg = COLOR_BRIGHT_GREEN
                        93 -> currentFg = COLOR_BRIGHT_YELLOW
                        94 -> currentFg = COLOR_BRIGHT_BLUE
                        95 -> currentFg = COLOR_BRIGHT_MAGENTA
                        96 -> currentFg = COLOR_BRIGHT_CYAN
                        97 -> currentFg = COLOR_BRIGHT_WHITE
                        // Bright Backgrounds
                        100 -> currentBg = COLOR_BRIGHT_BLACK
                        101 -> currentBg = COLOR_BRIGHT_RED
                        102 -> currentBg = COLOR_BRIGHT_GREEN
                        103 -> currentBg = COLOR_BRIGHT_YELLOW
                        104 -> currentBg = COLOR_BRIGHT_BLUE
                        105 -> currentBg = COLOR_BRIGHT_MAGENTA
                        106 -> currentBg = COLOR_BRIGHT_CYAN
                        107 -> currentBg = COLOR_BRIGHT_WHITE
                    }
                }

                currentIndex = matchRange.last + 1
            }

            if (currentIndex < text.length) {
                pushStyle(currentSpanStyle())
                append(text.substring(currentIndex))
                pop()
            }
        }
    }
}
