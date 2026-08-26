package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

@Composable
fun SettingsModal(
    fontSizeSp: Float,
    tabSize: Int,
    wordWrap: Boolean,
    showLineNumbers: Boolean,
    onSaveSettings: (fontSize: Float, tabSize: Int, wordWrap: Boolean, showLineNumbers: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var curFontSize by remember { mutableFloatStateOf(fontSizeSp) }
    var curTabSize by remember { mutableIntStateOf(tabSize) }
    var curWordWrap by remember { mutableStateOf(wordWrap) }
    var curShowLineNumbers by remember { mutableStateOf(showLineNumbers) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = VsCodeSidebar,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = VsCodeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings (Ctrl+,)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Font Size Slider
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Editor Font Size", fontSize = 13.sp, color = VsCodeText)
                        Text(
                            text = "${curFontSize.toInt()} sp",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeAccent
                        )
                    }
                    Slider(
                        value = curFontSize,
                        onValueChange = { curFontSize = it },
                        valueRange = 10f..24f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = VsCodeAccent,
                            activeTrackColor = VsCodeAccent
                        ),
                        modifier = Modifier.testTag("settings_font_size_slider")
                    )
                }

                // Show Line Numbers Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(text = "Show Line Numbers", fontSize = 13.sp, color = VsCodeText)
                        Text(text = "Display line gutter in code editor", fontSize = 11.sp, color = VsCodeTextMuted)
                    }
                    Switch(
                        checked = curShowLineNumbers,
                        onCheckedChange = { curShowLineNumbers = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VsCodeAccent),
                        modifier = Modifier.testTag("settings_line_numbers_switch")
                    )
                }

                // Word Wrap Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(text = "Word Wrap", fontSize = 13.sp, color = VsCodeText)
                        Text(text = "Wrap long lines to fit viewport", fontSize = 11.sp, color = VsCodeTextMuted)
                    }
                    Switch(
                        checked = curWordWrap,
                        onCheckedChange = { curWordWrap = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VsCodeAccent),
                        modifier = Modifier.testTag("settings_word_wrap_switch")
                    )
                }

                // Actions
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C3C3C)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSaveSettings(curFontSize, curTabSize, curWordWrap, curShowLineNumbers)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeAccent),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Apply", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
