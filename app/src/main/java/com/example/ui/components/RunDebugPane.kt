package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

data class RunConfiguration(
    val id: String,
    val name: String,
    val runtime: String,
    val iconEmoji: String,
    val command: String
)

@Composable
fun RunDebugPane(
    activeLanguage: String,
    isRunning: Boolean,
    onRunScript: () -> Unit,
    onStopScript: () -> Unit,
    onOpenWebPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runConfigurations = remember {
        listOf(
            RunConfiguration("auto", "Auto Detect Runtime (Active File)", "Active Buffer", "⚡", "run active"),
            RunConfiguration("node", "Node.js v20 (V8 Engine)", "node index.js", "🟢", "node --harmony"),
            RunConfiguration("python", "Python 3.12 (CPython AST)", "python3 main.py", "🐍", "python -u"),
            RunConfiguration("web", "Live Web Browser Canvas", "Chrome / Webkit", "🌐", "serve -s public"),
            RunConfiguration("sql", "SQLite 3.42 In-Memory DB", "sqlite3 memory.db", "🗄️", "sqlite3"),
            RunConfiguration("rust", "Rust / Cargo (rustc 1.77)", "cargo run --release", "🦀", "cargo run"),
            RunConfiguration("cpp", "GCC / Clang 18 (C++20)", "g++ -std=c++20 main.cpp", "⚡", "g++ -O3"),
            RunConfiguration("jvm", "JVM 21 / Kotlin 2.0 Runner", "kotlin Main.kt", "🚀", "kotlinc-jvm")
        )
    }

    var selectedConfig by remember { mutableStateOf(runConfigurations[0]) }

    Surface(
        color = VsCodeSidebarBg,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "RUN & DEBUG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted,
                    letterSpacing = 0.5.sp
                )
            }

            // Primary Run / Stop Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (selectedConfig.id == "web") {
                            onOpenWebPreview()
                        } else {
                            onRunScript()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388A34)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("debug_run_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (selectedConfig.id == "web") "Launch Web Canvas" else "Start Debugging", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isRunning) {
                    Button(
                        onClick = onStopScript,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC72E2E)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp).testTag("debug_stop_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Configuration Selector
            Text(
                text = "EXECUTION RUNTIME CONFIGURATIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VsCodeTextMuted
            )

            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(runConfigurations) { config ->
                    val isSelected = selectedConfig.id == config.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF094771) else Color(0xFF1E1E1E))
                            .border(0.5.dp, if (isSelected) VsCodeAccent else VsCodeBorder, RoundedCornerShape(4.dp))
                            .clickable { selectedConfig = config }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(config.iconEmoji, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = config.name,
                                    color = if (isSelected) Color.White else VsCodeText,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = config.runtime,
                                    color = VsCodeTextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .background(VsCodeAccent, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Active", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Variable Watcher / Call Stack section
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("CALL STACK & WATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VsCodeTextMuted)
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text("• [Thread 1] main (pid 4028) - RUNNING", color = VsCodeText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("  locals: scope = { 12 identifiers bound }", color = VsCodeTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("  memory: 4.8MB heap allocated", color = VsCodeTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
