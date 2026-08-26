package com.example.data.sample

import com.example.data.model.FileEntity
import com.example.data.model.ProjectEntity

data class ProjectTemplate(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val primaryLanguage: String,
    val category: String,
    val initialFiles: (projectId: Long) -> List<FileEntity>
)

object StarterProjects {

    val defaultProject = ProjectEntity(
        id = 1,
        name = "CodeStudio Workspace",
        description = "Universal polyglot mobile IDE for Web, Python, JS/TS, C++, Rust, Kotlin, Java, and SQL."
    )

    val templates: List<ProjectTemplate> = listOf(
        ProjectTemplate(
            id = "web-fullstack",
            name = "🌐 Full-Stack Web App",
            description = "HTML5, CSS3, Modern JavaScript & Interactive DOM canvas with live preview",
            iconName = "Language",
            primaryLanguage = "javascript",
            category = "Web & Mobile",
            initialFiles = { projectId -> getWebFiles(projectId) }
        ),
        ProjectTemplate(
            id = "python-datascience",
            name = "🐍 Python Science & Algorithms",
            description = "Python 3.12 data analysis, algorithms, mathematics, and benchmark suite",
            iconName = "Code",
            primaryLanguage = "python",
            category = "Data Science & AI",
            initialFiles = { projectId -> getPythonFiles(projectId) }
        ),
        ProjectTemplate(
            id = "react-typescript",
            name = "⚛️ React & TypeScript App",
            description = "Modern React components, state management, TSX typing, and responsive styling",
            iconName = "Layers",
            primaryLanguage = "typescript",
            category = "Frontend Frameworks",
            initialFiles = { projectId -> getReactFiles(projectId) }
        ),
        ProjectTemplate(
            id = "cpp-systems",
            name = "⚡ C++ High Performance",
            description = "C++20 algorithms, data structures, memory management, and benchmark suite",
            iconName = "Memory",
            primaryLanguage = "cpp",
            category = "Systems Programming",
            initialFiles = { projectId -> getCppFiles(projectId) }
        ),
        ProjectTemplate(
            id = "rust-engine",
            name = "🦀 Rust Systems Engine",
            description = "Memory-safe Rust structs, pattern matching, concurrency, and Cargo layout",
            iconName = "Shield",
            primaryLanguage = "rust",
            category = "Systems Programming",
            initialFiles = { projectId -> getRustFiles(projectId) }
        ),
        ProjectTemplate(
            id = "kotlin-java",
            name = "🚀 Kotlin & Java OOP Engine",
            description = "Kotlin 2.0 Coroutines, data classes, modern OOP patterns, and clean architecture",
            iconName = "Smartphone",
            primaryLanguage = "kotlin",
            category = "Backend & Mobile",
            initialFiles = { projectId -> getKotlinJavaFiles(projectId) }
        ),
        ProjectTemplate(
            id = "sql-database",
            name = "🗄️ SQL Database Studio",
            description = "Relational database schema, indexing, complex joins, and analytical queries",
            iconName = "Storage",
            primaryLanguage = "sql",
            category = "Databases & Backend",
            initialFiles = { projectId -> getSqlFiles(projectId) }
        ),
        ProjectTemplate(
            id = "cloud-devops",
            name = "☁️ Cloud, Docker & Bash",
            description = "Containerization Dockerfile, automated bash CI/CD deploy scripts, and configs",
            iconName = "Cloud",
            primaryLanguage = "shell",
            category = "DevOps & Cloud",
            initialFiles = { projectId -> getCloudFiles(projectId) }
        )
    )

    fun getInitialFiles(projectId: Long): List<FileEntity> {
        // Universal default project contains a multi-language showcase
        return listOf(
            FileEntity(
                id = 1,
                projectId = projectId,
                name = "main.py",
                path = "main.py",
                language = "python",
                content = """# =======================================================
# 🌐 CodeStudio - Universal Mobile IDE (VS Code for Phones)
# =======================================================
# Polyglot Multi-Language Development:
#   • Python 3.12 (Data structures, Math, REPL)
#   • JavaScript & TypeScript (Node.js & Web)
#   • HTML5 / CSS3 (Live interactive web preview)
#   • C++20 / Rust / Go / Kotlin / Java
#   • SQL In-Memory Relational Engine
#   • Git Source Control & VS Code Extensions Marketplace
# =======================================================

import sys
import math
import time

def welcome(developer: str, languages: list[str]) -> None:
    print(f"✨ Welcome to CodeStudio, {developer}!")
    print(f"🚀 Active Polyglot Suite: {', '.join(languages)}")
    print("-" * 55)

def benchmark_algorithms(n: int = 1000):
    # Calculate primes up to N
    primes = [x for x in range(2, n) if all(x % d != 0 for d in range(2, int(math.isqrt(x)) + 1))]
    return primes

if __name__ == "__main__":
    welcome("Developer", ["Python", "JS/TS", "HTML/CSS", "C++", "Rust", "Kotlin", "SQL"])
    
    start = time.time()
    found_primes = benchmark_algorithms(200)
    elapsed = time.time() - start
    
    print(f"🔹 Prime numbers generated (2..200): {len(found_primes)} primes found")
    print(f"🔹 First 10: {found_primes[:10]}")
    print(f"✅ Execution finished in {elapsed:.4f}s")
"""
            ),
            FileEntity(
                id = 2,
                projectId = projectId,
                name = "app.ts",
                path = "src/app.ts",
                language = "typescript",
                content = """// TypeScript & Node.js Engine
// Press [F5] or Run Button to execute!

interface ProjectStats {
    totalFiles: number;
    languages: string[];
    isProductionReady: boolean;
}

class CodeStudioEngine {
    private version: string = "3.0.0";

    constructor(public readonly workspaceName: string) {}

    public getWorkspaceSummary(): ProjectStats {
        return {
            totalFiles: 8,
            languages: ["TypeScript", "Python", "Rust", "HTML", "CSS", "SQL"],
            isProductionReady: true
        };
    }

    public runHealthCheck(): void {
        console.log("[CodeStudio v" + this.version + "] Initializing workspace: " + this.workspaceName);
        const stats = this.getWorkspaceSummary();
        console.log("Languages active:", stats.languages.join(", "));
        console.log("Ready for deployment:", stats.isProductionReady);
    }
}

const engine = new CodeStudioEngine("Universal Mobile IDE");
engine.runHealthCheck();
"""
            ),
            FileEntity(
                id = 3,
                projectId = projectId,
                name = "index.html",
                path = "public/index.html",
                language = "html",
                content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CodeStudio Web App</title>
    <link rel="stylesheet" href="styles.css">
    <style>
        body {
            background: #181824;
            color: #f1f1f1;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            margin: 0;
            padding: 24px;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            background: #232334;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.4);
            border: 1px solid #383854;
        }
        h1 { color: #007acc; margin-top: 0; }
        .badge {
            display: inline-block;
            background: #007acc;
            color: #fff;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }
        button {
            background: #0e639c;
            color: white;
            border: none;
            padding: 10px 18px;
            border-radius: 6px;
            font-size: 14px;
            cursor: pointer;
            margin-top: 12px;
        }
        button:hover { background: #1177bb; }
    </style>
</head>
<body>
    <div class="container">
        <span class="badge">Live Web Canvas</span>
        <h1>Welcome to CodeStudio</h1>
        <p>A full-fledged polyglot IDE built for phones and tablets.</p>
        <button onclick="alert('Hello from CodeStudio interactive web runtime!')">Tap Interactive Button</button>
    </div>
</body>
</html>
"""
            ),
            FileEntity(
                id = 4,
                projectId = projectId,
                name = "main.rs",
                path = "src/main.rs",
                language = "rust",
                content = """// Rust Systems Programming Engine
// Memory safety without garbage collection

#[derive(Debug)]
struct Developer {
    name: String,
    skills: Vec<String>,
    experience_years: u32,
}

fn calculate_throughput(requests: u64, duration_secs: f64) -> f64 {
    (requests as f64) / duration_secs
}

fn main() {
    let dev = Developer {
        name: String::from("Mobile Engineer"),
        skills: vec!["Rust".into(), "Kotlin".into(), "TypeScript".into()],
        experience_years: 5,
    };

    println!("🦀 Rust Systems Initialized for: {}", dev.name);
    println!("📦 Skills: {:?}", dev.skills);

    let rps = calculate_throughput(150_000, 2.5);
    println!("⚡ Engine Throughput: {:.2} req/sec", rps);
}
"""
            ),
            FileEntity(
                id = 5,
                projectId = projectId,
                name = "database.sql",
                path = "sql/database.sql",
                language = "sql",
                content = """-- CodeStudio In-Memory Relational Database
-- Press Run (F5) to execute SQL schema and queries

CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    role TEXT NOT NULL,
    reputation INTEGER DEFAULT 100
);

INSERT INTO users (id, username, role, reputation) VALUES 
(1, 'ada_lovelace', 'Admin', 999),
(2, 'linus_torvalds', 'Maintainer', 850),
(3, 'alan_turing', 'Architect', 920);

-- Query all active architects and admins
SELECT id, username, role, reputation 
FROM users 
WHERE reputation >= 900 
ORDER BY reputation DESC;
"""
            ),
            FileEntity(
                id = 6,
                projectId = projectId,
                name = "README.md",
                path = "README.md",
                language = "markdown",
                content = """# 🚀 CodeStudio - VS Code for Phones

A universal, polyglot mobile IDE designed for programmers across every language and framework.

---

## 🌟 Polyglot Language Matrix
| Language | Runtime / Engine | Live Preview / Debug |
|---|---|---|
| 🐍 **Python** | Python 3.12 AST & REPL | ✅ Interactive Shell & Inputs |
| ⚡ **JavaScript / TypeScript** | Node.js v20 / V8 Engine | ✅ console.log & Objects |
| 🌐 **HTML5 / CSS3** | Web Browser Canvas | ✅ Live Interactive DOM |
| 🦀 **Rust** | rustc / Cargo Engine | ✅ Memory-Safe Execution |
| ⚡ **C++ / C** | GCC / Clang 18 (C++20) | ✅ High Performance Run |
| 🚀 **Kotlin & Java** | JVM 21 / Coroutines | ✅ Modern OOP Runner |
| 🗄️ **SQL** | SQLite 3.42 Engine | ✅ Relational Tables & Joins |
| 🐚 **Shell / Bash** | Bash 5.2 POSIX | ✅ Terminal Commands & Scripts |

---

## ⌨️ Pro Shortcuts
- `Ctrl+Shift+P` / `F1` : **Command Palette**
- `F5` / `Ctrl+Enter` : **Run Current File / Polyglot Runner**
- `Ctrl+Shift+G` : **Git Source Control (Diff & Commit)**
- `Ctrl+Shift+X` : **Extensions & Snippet Marketplace**
- `Ctrl+Shift+F` : **Search in Workspace**
- `Ctrl+P` : **Quick File Switcher**
- `F11` / `Ctrl+M` : **Zen Fullscreen Mode**
- `F6` : **Cycle Focus Panels**
"""
            )
        )
    }

    private fun getWebFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 101,
                projectId = projectId,
                name = "index.html",
                path = "index.html",
                language = "html",
                content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modern Web App</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div id="app">
        <header>
            <h1>🌐 Web Application</h1>
            <p id="clock">Loading timer...</p>
        </header>
        <main>
            <input type="text" id="taskInput" placeholder="Add a new task..." />
            <button id="addBtn">Add Task</button>
            <ul id="taskList"></ul>
        </main>
    </div>
    <script src="app.js"></script>
</body>
</html>"""
            ),
            FileEntity(
                id = 102,
                projectId = projectId,
                name = "styles.css",
                path = "styles.css",
                language = "css",
                content = """body {
    background: #0f172a;
    color: #e2e8f0;
    font-family: system-ui, -apple-system, sans-serif;
    display: flex;
    justify-content: center;
    padding: 30px;
}
#app {
    background: #1e293b;
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    max-width: 480px;
    border: 1px solid #334155;
    box-shadow: 0 10px 25px rgba(0,0,0,0.3);
}
h1 { color: #38bdf8; margin: 0 0 8px 0; }
input {
    width: 70%;
    padding: 8px 12px;
    background: #0f172a;
    border: 1px solid #475569;
    color: white;
    border-radius: 6px;
}
button {
    padding: 8px 14px;
    background: #38bdf8;
    color: #0f172a;
    font-weight: bold;
    border: none;
    border-radius: 6px;
    cursor: pointer;
}"""
            ),
            FileEntity(
                id = 103,
                projectId = projectId,
                name = "app.js",
                path = "app.js",
                language = "javascript",
                content = """// Web Application Controller
document.addEventListener('DOMContentLoaded', () => {
    const clock = document.getElementById('clock');
    const input = document.getElementById('taskInput');
    const addBtn = document.getElementById('addBtn');
    const list = document.getElementById('taskList');

    setInterval(() => {
        clock.textContent = new Date().toLocaleTimeString();
    }, 1000);

    addBtn.addEventListener('click', () => {
        if (!input.value) return;
        const li = document.createElement('li');
        li.textContent = input.value;
        list.appendChild(li);
        input.value = '';
    });
});"""
            )
        )
    }

    private fun getPythonFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 201,
                projectId = projectId,
                name = "main.py",
                path = "main.py",
                language = "python",
                content = """import math
import statistics

class DataAnalyzer:
    def __init__(self, data: list[float]):
        self.data = data

    def summarize(self) -> dict:
        return {
            "count": len(self.data),
            "mean": statistics.mean(self.data) if hasattr(statistics, 'mean') else sum(self.data)/len(self.data),
            "min": min(self.data),
            "max": max(self.data)
        }

if __name__ == "__main__":
    sample = [24.5, 30.1, 18.9, 45.2, 33.0, 29.8, 50.0]
    analyzer = DataAnalyzer(sample)
    print("📊 Analysis summary:", analyzer.summarize())"""
            )
        )
    }

    private fun getReactFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 301,
                projectId = projectId,
                name = "App.tsx",
                path = "src/App.tsx",
                language = "typescript",
                content = """import React, { useState } from 'react';

interface CounterProps {
    initialValue?: number;
}

export const App: React.FC<CounterProps> = ({ initialValue = 0 }) => {
    const [count, setCount] = useState<number>(initialValue);

    return (
        <div className="p-6 bg-slate-900 text-white rounded-xl shadow-lg">
            <h1 className="text-2xl font-bold text-cyan-400">React + TypeScript App</h1>
            <p className="mt-2 text-slate-300">Current count: <span className="font-mono text-xl">{count}</span></p>
            <div className="mt-4 space-x-2">
                <button onClick={() => setCount(c => c + 1)} className="px-4 py-2 bg-cyan-600 rounded">Increment</button>
                <button onClick={() => setCount(0)} className="px-4 py-2 bg-slate-700 rounded">Reset</button>
            </div>
        </div>
    );
};"""
            )
        )
    }

    private fun getCppFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 401,
                projectId = projectId,
                name = "main.cpp",
                path = "main.cpp",
                language = "cpp",
                content = """#include <iostream>
#include <vector>
#include <numeric>
#include <algorithm>

int main() {
    std::vector<int> numbers = {10, 25, 3, 44, 95, 12, 67};
    
    std::cout << "⚡ C++20 Systems Algorithm Runner" << std::endl;
    std::sort(numbers.begin(), numbers.end());
    
    std::cout << "Sorted elements: ";
    for (int n : numbers) {
        std::cout << n << " ";
    }
    std::cout << std::endl;
    
    int sum = std::accumulate(numbers.begin(), numbers.end(), 0);
    std::cout << "Sum: " << sum << std::endl;
    return 0;
}"""
            )
        )
    }

    private fun getRustFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 501,
                projectId = projectId,
                name = "main.rs",
                path = "src/main.rs",
                language = "rust",
                content = """fn fibonacci(n: u32) -> u64 {
    match n {
        0 => 0,
        1 => 1,
        _ => {
            let mut a = 0;
            let mut b = 1;
            for _ in 2..=n {
                let temp = a + b;
                a = b;
                b = temp;
            }
            b
        }
    }
}

fn main() {
    println!("🦀 Rust Fibonacci Performance Benchmark");
    for i in 1..=15 {
        println!("F({}) = {}", i, fibonacci(i));
    }
}"""
            )
        )
    }

    private fun getKotlinJavaFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 601,
                projectId = projectId,
                name = "Main.kt",
                path = "src/Main.kt",
                language = "kotlin",
                content = """data class Developer(val name: String, val language: String, val level: String)

fun main() {
    val team = listOf(
        Developer("Alex", "Kotlin", "Senior"),
        Developer("Sam", "Rust", "Staff"),
        Developer("Taylor", "TypeScript", "Lead")
    )

    println("🚀 Kotlin 2.0 Multiplatform Workspace")
    team.groupBy { it.language }.forEach { (groupLang, groupDevs) ->
        println("• Language: " + groupLang + " -> " + groupDevs.joinToString { d -> d.name })
    }
}"""
            )
        )
    }

    private fun getSqlFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 701,
                projectId = projectId,
                name = "queries.sql",
                path = "queries.sql",
                language = "sql",
                content = """CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    customer TEXT NOT NULL,
    amount DECIMAL(10,2),
    status TEXT
);

INSERT INTO orders VALUES (101, 'Acme Corp', 4500.00, 'COMPLETED');
INSERT INTO orders VALUES (102, 'Beta LLC', 1250.50, 'PENDING');
INSERT INTO orders VALUES (103, 'Gamma Inc', 8900.00, 'COMPLETED');

SELECT customer, SUM(amount) AS total_spent 
FROM orders 
WHERE status = 'COMPLETED' 
GROUP BY customer;"""
            )
        )
    }

    private fun getCloudFiles(projectId: Long): List<FileEntity> {
        return listOf(
            FileEntity(
                id = 801,
                projectId = projectId,
                name = "Dockerfile",
                path = "Dockerfile",
                language = "dockerfile",
                content = """FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]"""
            ),
            FileEntity(
                id = 802,
                projectId = projectId,
                name = "deploy.sh",
                path = "deploy.sh",
                language = "shell",
                content = """#!/usr/bin/env bash
set -e

echo "🚀 Starting Automated Deployment Pipeline..."
echo "📦 Building container image: codestudio-app:latest"
docker build -t codestudio-app:latest .

echo "☁️ Pushing to Cloud Container Registry..."
echo "✅ Deployment successful to cluster 'production-k8s'!"
"""
            )
        )
    }
}
