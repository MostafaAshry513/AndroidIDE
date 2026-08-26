package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

private val PRESET_COLORS = listOf(
    // Modern Material / Tailwind palette
    "#EF4444", "#F97316", "#F59E0B", "#10B981", "#06B6D4", "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899",
    "#F43F5E", "#E11D48", "#D97706", "#059669", "#0891B2", "#2563EB", "#4F46E5", "#7C3AED", "#DB2777",
    "#FFFFFF", "#F3F4F6", "#E5E7EB", "#9CA3AF", "#4B5563", "#1F2937", "#111827", "#0F172A", "#000000",
    "#00E676", "#00B0FF", "#651FFF", "#FF1744", "#FFEA00", "#FF9100", "#76FF03", "#1DE9B6", "#FF4081"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerModal(
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHex by remember { mutableStateOf("#3B82F6") }
    var red by remember { mutableFloatStateOf(59f) }
    var green by remember { mutableFloatStateOf(130f) }
    var blue by remember { mutableFloatStateOf(246f) }

    fun updateFromRgb(r: Float, g: Float, b: Float) {
        red = r
        green = g
        blue = b
        val rInt = r.toInt().coerceIn(0, 255)
        val gInt = g.toInt().coerceIn(0, 255)
        val bInt = b.toInt().coerceIn(0, 255)
        selectedHex = String.format("#%02X%02X%02X", rInt, gInt, bInt)
    }

    fun parseHex(hex: String) {
        try {
            val cleanHex = hex.removePrefix("#")
            if (cleanHex.length == 6) {
                val r = cleanHex.substring(0, 2).toInt(16).toFloat()
                val g = cleanHex.substring(2, 4).toInt(16).toFloat()
                val b = cleanHex.substring(4, 6).toInt(16).toFloat()
                red = r
                green = g
                blue = b
                selectedHex = hex
            }
        } catch (_: Exception) {}
    }

    val currentColor = remember(red, green, blue) {
        Color(red.toInt().coerceIn(0, 255), green.toInt().coerceIn(0, 255), blue.toInt().coerceIn(0, 255))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VsCodeSidebar),
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, VsCodeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Color Picker",
                        tint = VsCodeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Color Palette & Picker",
                        color = VsCodeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Color Preview Card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(currentColor)
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = selectedHex,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "rgb(${red.toInt()}, ${green.toInt()}, ${blue.toInt()})",
                            color = VsCodeTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Preset Grid
                Text(
                    text = "Quick Presets",
                    color = VsCodeTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    maxItemsInEachRow = 9,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PRESET_COLORS.forEach { hex ->
                        val isSelected = selectedHex.equals(hex, ignoreCase = true)
                        val color = try {
                            val cleanHex = hex.removePrefix("#")
                            Color(
                                cleanHex.substring(0, 2).toInt(16),
                                cleanHex.substring(2, 4).toInt(16),
                                cleanHex.substring(4, 6).toInt(16)
                            )
                        } catch (_: Exception) { Color.Gray }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { parseHex(hex) }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (red + green + blue > 380) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // RGB Sliders
                Column(modifier = Modifier.fillMaxWidth()) {
                    RgbSliderRow(label = "R", value = red, color = Color(0xFFEF4444)) { updateFromRgb(it, green, blue) }
                    RgbSliderRow(label = "G", value = green, color = Color(0xFF10B981)) { updateFromRgb(red, it, blue) }
                    RgbSliderRow(label = "B", value = blue, color = Color(0xFF3B82F6)) { updateFromRgb(red, green, it) }
                }

                Spacer(Modifier.height(16.dp))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = VsCodeTextMuted, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onColorSelected(selectedHex)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Insert $selectedHex", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RgbSliderRow(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(18.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF2A2D2E)
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${value.toInt()}",
            color = VsCodeText,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(28.dp)
        )
    }
}
