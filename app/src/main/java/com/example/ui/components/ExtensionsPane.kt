package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

data class IdeExtension(
    val id: String,
    val name: String,
    val publisher: String,
    val description: String,
    val version: String,
    val downloads: String,
    val iconEmoji: String,
    val isInstalled: Boolean = true,
    val snippetSnippet: String? = null
)

@Composable
fun ExtensionsPane(
    onInsertSnippet: (snippet: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val allExtensions = remember {
        listOf(
            IdeExtension(
                id = "python-pack",
                name = "Python Polyglot Tools",
                publisher = "Microsoft",
                description = "IntelliSense, Linting, Debugging, Code Navigation, and NumPy/Pandas helpers",
                version = "2024.4.1",
                downloads = "104M",
                iconEmoji = "🐍",
                snippetSnippet = "import numpy as np\nimport pandas as pd\n\ndf = pd.DataFrame({'a': [1, 2, 3], 'b': [4, 5, 6]})\nprint(df.describe())"
            ),
            IdeExtension(
                id = "react-snippets",
                name = "ES7+ React/Redux/React-Native",
                publisher = "dsznajder",
                description = "Extensions and TypeScript snippets for React, Redux, and React Native",
                version = "4.4.3",
                downloads = "18M",
                iconEmoji = "⚛️",
                snippetSnippet = "import React, { useState, useEffect } from 'react';\n\nexport const Component: React.FC = () => {\n  const [data, setData] = useState<string>('');\n  return <div className=\"container\">{data}</div>;\n};"
            ),
            IdeExtension(
                id = "tailwind-kit",
                name = "Tailwind CSS IntelliSense",
                publisher = "Tailwind Labs",
                description = "Intelligent Tailwind CSS tooling for responsive web layouts and CSS utility classes",
                version = "0.9.11",
                downloads = "12M",
                iconEmoji = "🎨",
                snippetSnippet = "<div className=\"flex items-center justify-between p-4 bg-slate-800 text-white rounded-lg shadow-md\">\n  <span className=\"font-semibold\">Card Title</span>\n</div>"
            ),
            IdeExtension(
                id = "rust-analyzer",
                name = "rust-analyzer",
                publisher = "rust-lang",
                description = "Rust language support with real-time compilation, memory layout and Cargo commands",
                version = "0.3.1892",
                downloads = "5.2M",
                iconEmoji = "🦀",
                snippetSnippet = "#[derive(Debug, Clone)]\npub struct ServiceConfig {\n    pub host: String,\n    pub port: u16,\n}"
            ),
            IdeExtension(
                id = "sql-tools",
                name = "SQLTools & SQLite Database Studio",
                publisher = "Matheus Teixeira",
                description = "Database management, query runner, schema explorer, and query bookmarks",
                version = "0.28.1",
                downloads = "4.8M",
                iconEmoji = "🗄️",
                snippetSnippet = "SELECT \n    u.id, u.username, COUNT(o.id) as order_count\nFROM users u\nLEFT JOIN orders o ON u.id = o.user_id\nGROUP BY u.id;"
            ),
            IdeExtension(
                id = "prettier-formatter",
                name = "Prettier - Code Formatter",
                publisher = "Prettier",
                description = "Universal opinionated code formatter for JS, TS, HTML, CSS, JSON, and Markdown",
                version = "10.4.0",
                downloads = "42M",
                iconEmoji = "✨"
            ),
            IdeExtension(
                id = "docker-cloud",
                name = "Docker & Cloud Deploy Tools",
                publisher = "Microsoft",
                description = "Build, manage, and deploy containerized applications from phone workspace",
                version = "1.29.0",
                downloads = "31M",
                iconEmoji = "🐳",
                snippetSnippet = "FROM node:20-alpine\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install\nCOPY . .\nEXPOSE 3000\nCMD [\"npm\", \"start\"]"
            )
        )
    }

    val filteredExtensions = remember(searchQuery) {
        if (searchQuery.isBlank()) allExtensions
        else allExtensions.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.publisher.contains(searchQuery, ignoreCase = true)
        }
    }

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
                Icon(Icons.Default.Extension, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "EXTENSIONS MARKETPLACE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted,
                    letterSpacing = 0.5.sp
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Extensions in Marketplace", fontSize = 11.sp, color = VsCodeTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VsCodeTextMuted, modifier = Modifier.size(16.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VsCodeAccent,
                    unfocusedBorderColor = VsCodeBorder,
                    focusedTextColor = VsCodeText,
                    unfocusedTextColor = VsCodeText,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("extensions_search_input")
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "INSTALLED & POPULAR (${filteredExtensions.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VsCodeTextMuted
            )

            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExtensions) { ext ->
                    ExtensionCard(
                        extension = ext,
                        onInsertSnippet = { ext.snippetSnippet?.let { onInsertSnippet(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtensionCard(
    extension: IdeExtension,
    onInsertSnippet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E1E1E))
            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2D2D2D), RoundedCornerShape(6.dp))
                ) {
                    Text(extension.iconEmoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = extension.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${extension.publisher} • v${extension.version} • ⬇ ${extension.downloads}",
                        color = VsCodeTextMuted,
                        fontSize = 9.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(VsCodeAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Installed", color = VsCodeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = extension.description,
            color = VsCodeText,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )

        if (extension.snippetSnippet != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onInsertSnippet,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2D2E)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Insert ${extension.name.substringBefore(" ")} Snippet", color = Color.White, fontSize = 9.5.sp)
                }
            }
        }
    }
}
