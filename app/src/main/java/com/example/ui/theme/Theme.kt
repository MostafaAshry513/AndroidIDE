package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PyCodeColorScheme = darkColorScheme(
    primary = VsCodeAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF094771),
    onPrimaryContainer = Color.White,
    secondary = VsCodeGreen,
    onSecondary = Color.Black,
    tertiary = VsCodeYellow,
    background = VsCodeBg,
    onBackground = VsCodeText,
    surface = VsCodeSidebar,
    onSurface = VsCodeText,
    surfaceVariant = VsCodeTitleBar,
    onSurfaceVariant = VsCodeTextMuted,
    outline = VsCodeBorder,
    error = VsCodeError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PyCodeColorScheme,
        typography = Typography,
        content = content
    )
}
