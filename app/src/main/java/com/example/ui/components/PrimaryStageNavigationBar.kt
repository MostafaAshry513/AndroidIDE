package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.viewmodel.PrimaryStage

@Composable
fun PrimaryStageNavigationBar(
    activeStage: PrimaryStage,
    isRunning: Boolean,
    errorCount: Int,
    isWebFileActive: Boolean,
    onSelectStage: (PrimaryStage) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF141414),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(
                width = 1.dp,
                color = VsCodeBorder,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {
            // 1. Code Editor Stage
            StageNavItem(
                label = "Code",
                icon = Icons.Default.Code,
                isSelected = activeStage == PrimaryStage.EDITOR,
                badgeCount = if (errorCount > 0) errorCount else null,
                badgeColor = VsCodeError,
                onClick = { onSelectStage(PrimaryStage.EDITOR) }
            )

            // 2. Terminal Stage
            StageNavItem(
                label = "Terminal",
                icon = Icons.Default.Terminal,
                isSelected = activeStage == PrimaryStage.TERMINAL,
                isPulseRunning = isRunning,
                onClick = { onSelectStage(PrimaryStage.TERMINAL) }
            )

            // 3. Web Preview (Live browser canvas)
            StageNavItem(
                label = "Preview",
                icon = Icons.Default.Language,
                isSelected = activeStage == PrimaryStage.WEB_PREVIEW,
                isHighlighted = isWebFileActive,
                onClick = { onSelectStage(PrimaryStage.WEB_PREVIEW) }
            )

            // 4. Explorer (Files & Projects)
            StageNavItem(
                label = "Files",
                icon = Icons.Default.Folder,
                isSelected = activeStage == PrimaryStage.EXPLORER,
                onClick = { onSelectStage(PrimaryStage.EXPLORER) }
            )

            // 5. AI Copilot
            StageNavItem(
                label = "Copilot",
                icon = Icons.Default.AutoAwesome,
                isSelected = activeStage == PrimaryStage.AI_COPILOT,
                isAccentGradient = true,
                onClick = { onSelectStage(PrimaryStage.AI_COPILOT) }
            )

            // 6. Settings & Config
            StageNavItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                isSelected = activeStage == PrimaryStage.SETTINGS,
                onClick = { onSelectStage(PrimaryStage.SETTINGS) }
            )
        }
    }
}

@Composable
private fun StageNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    isPulseRunning: Boolean = false,
    isHighlighted: Boolean = false,
    isAccentGradient: Boolean = false,
    badgeCount: Int? = null,
    badgeColor: Color = VsCodeError,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val itemBg by animateColorAsState(
        targetValue = when {
            isSelected -> VsCodeAccent.copy(alpha = 0.20f)
            else -> Color.Transparent
        },
        label = "itemBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> VsCodeAccent
            isPulseRunning -> Color(0xFF10B981)
            isHighlighted -> Color(0xFF60A5FA)
            isAccentGradient -> Color(0xFFA78BFA)
            else -> VsCodeTextMuted
        },
        label = "contentColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(itemBg)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )

                if (isPulseRunning) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(6.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }

                if (badgeCount != null && badgeCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = label,
                color = if (isSelected) VsCodeText else VsCodeTextMuted,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
