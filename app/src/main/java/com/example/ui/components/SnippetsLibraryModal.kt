package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

data class CodeSnippetItem(
    val title: String,
    val description: String,
    val category: String,
    val code: String,
    val language: String
)

private val SNIPPET_COLLECTION = listOf(
    // Python
    CodeSnippetItem(
        "Binary Search Algorithm",
        "O(log N) iterative binary search on sorted list",
        "Algorithms",
        """def binary_search(arr, target):
    left, right = 0, len(arr) - 1
    while left <= right:
        mid = (left + right) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    return -1

# Example
nums = [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
print(f"Index of 23: {binary_search(nums, 23)}")""",
        "python"
    ),
    CodeSnippetItem(
        "QuickSort Recursive",
        "Fast divide-and-conquer in-place sorting algorithm",
        "Algorithms",
        """def quicksort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quicksort(left) + middle + quicksort(right)

print(quicksort([3, 6, 8, 10, 1, 2, 1]))""",
        "python"
    ),
    CodeSnippetItem(
        "Stack & Queue Structure",
        "LIFO Stack and FIFO Queue implementation with collections.deque",
        "Data Structures",
        """from collections import deque

class Queue:
    def __init__(self):
        self.items = deque()
    def enqueue(self, val):
        self.items.append(val)
    def dequeue(self):
        return self.items.popleft() if self.items else None
    def is_empty(self):
        return len(self.items) == 0

q = Queue()
q.enqueue("Task 1")
q.enqueue("Task 2")
print("Processed:", q.dequeue())""",
        "python"
    ),
    CodeSnippetItem(
        "SQLite Database Operations",
        "Create table, insert records, and query SQLite database in Python",
        "Database",
        """import sqlite3

conn = sqlite3.connect(':memory:')
cursor = conn.cursor()

cursor.execute('''CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, score INTEGER)''')
cursor.execute("INSERT INTO users (name, score) VALUES ('Alice', 95)")
cursor.execute("INSERT INTO users (name, score) VALUES ('Bob', 88)")

cursor.execute("SELECT * FROM users ORDER BY score DESC")
for row in cursor.fetchall():
    print(f"User: {row[1]}, Score: {row[2]}")""",
        "python"
    ),
    CodeSnippetItem(
        "HTTP Async Fetch (JSON)",
        "Fetch and parse JSON API asynchronously with error handling",
        "Web / API",
        """async function fetchUserData(userId) {
  try {
    const res = await fetch(`https://jsonplaceholder.typicode.com/users/${'$'}{userId}`);
    if (!res.ok) throw new Error(`HTTP error! status: ${'$'}{res.status}`);
    const data = await res.json();
    console.log("Fetched User:", data.name, data.email);
    return data;
  } catch (err) {
    console.error("Fetch failed:", err);
  }
}

fetchUserData(1);""",
        "javascript"
    ),
    CodeSnippetItem(
        "HTML5 Modern Responsive Shell",
        "Mobile-first responsive HTML boilerplate with Tailwind CSS CDN",
        "Web",
        """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Modern App</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-900 text-white min-h-screen flex items-center justify-center p-6">
  <div class="max-w-md w-full bg-gray-800 rounded-2xl shadow-xl p-8 border border-gray-700">
    <h1 class="text-2xl font-bold text-blue-400 mb-2">Hello Mobile IDE</h1>
    <p class="text-gray-400 text-sm">Crafted with modern responsive Tailwind CSS.</p>
  </div>
</body>
</html>""",
        "html"
    ),
    CodeSnippetItem(
        "Rust Struct & Methods (OOP)",
        "Struct definition with impl block, constructor, and instance methods",
        "Rust",
        """struct Rectangle {
    width: u32,
    height: u32,
}

impl Rectangle {
    fn new(width: u32, height: u32) -> Self {
        Self { width, height }
    }
    fn area(&self) -> u32 {
        self.width * self.height
    }
}

fn main() {
    let rect = Rectangle::new(30, 50);
    println!("Rectangle area: {} sq units", rect.area());
}""",
        "rust"
    ),
    CodeSnippetItem(
        "C++ Modern Smart Pointers",
        "std::unique_ptr and std::make_unique RAII resource management",
        "C++",
        """#include <iostream>
#include <memory>
#include <vector>

class Entity {
public:
    Entity(std::string name) : name_(name) {
        std::cout << "Created: " << name_ << "\n";
    }
    ~Entity() {
        std::cout << "Destroyed: " << name_ << "\n";
    }
    void Speak() const { std::cout << "Hello from " << name_ << "\n"; }
private:
    std::string name_;
};

int main() {
    auto e = std::make_unique<Entity>("Player 1");
    e->Speak();
    return 0;
}""",
        "cpp"
    )
)

@Composable
fun SnippetsLibraryModal(
    activeLanguage: String,
    onInsertSnippet: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = remember { listOf("All", "Algorithms", "Data Structures", "Web / API", "Database", "Language Specific") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val filteredSnippets = remember(searchQuery, selectedCategoryIndex, activeLanguage) {
        SNIPPET_COLLECTION.filter { snippet ->
            val matchesSearch = searchQuery.isBlank() ||
                    snippet.title.contains(searchQuery, ignoreCase = true) ||
                    snippet.description.contains(searchQuery, ignoreCase = true) ||
                    snippet.code.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryIndex) {
                0 -> true
                5 -> snippet.language.equals(activeLanguage, ignoreCase = true)
                else -> snippet.category.equals(categories[selectedCategoryIndex], ignoreCase = true)
            }

            matchesSearch && matchesCategory
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VsCodeSidebar),
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, VsCodeBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code Snippets",
                        tint = VsCodeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Code Snippets & Boilerplates",
                        color = VsCodeText,
                        fontSize = 15.sp,
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

                Spacer(Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search snippets, algorithms, templates...", fontSize = 12.sp, color = VsCodeTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VsCodeTextMuted, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedBorderColor = VsCodeAccent,
                        unfocusedBorderColor = VsCodeBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Categories Scrollable Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = Color.Transparent,
                    contentColor = VsCodeAccent,
                    edgePadding = 0.dp,
                    divider = {},
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                ) {
                    categories.forEachIndexed { idx, cat ->
                        Tab(
                            selected = selectedCategoryIndex == idx,
                            onClick = { selectedCategoryIndex = idx },
                            text = {
                                Text(
                                    text = if (idx == 5) "For $activeLanguage" else cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategoryIndex == idx) VsCodeAccent else VsCodeTextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Snippets List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filteredSnippets) { snippet ->
                        SnippetItemCard(
                            snippet = snippet,
                            onInsert = {
                                onInsertSnippet(snippet.code)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SnippetItemCard(
    snippet: CodeSnippetItem,
    onInsert: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(10.dp))
            .clickable { onInsert() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = snippet.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(VsCodeAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = snippet.language.uppercase(),
                        color = VsCodeAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = snippet.description,
                color = VsCodeTextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF141414))
                    .padding(8.dp)
            ) {
                Text(
                    text = snippet.code.lines().take(3).joinToString("\n") + if (snippet.code.lines().size > 3) "\n..." else "",
                    color = Color(0xFF9CDCFE),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3
                )
            }
        }
    }
}
