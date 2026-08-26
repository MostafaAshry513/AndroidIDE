package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val path: String, // e.g. "src/main.py"
    val content: String,
    val isDirectory: Boolean = false,
    val parentPath: String = "", // e.g. "src" or ""
    val language: String = "python",
    val isModified: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

data class EditorTab(
    val fileId: Long,
    val name: String,
    val path: String,
    val isModified: Boolean = false
)

data class DiagnosticError(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR
)

enum class DiagnosticSeverity {
    ERROR, WARNING, INFO
}

data class ConsoleOutput(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false,
    val isSystem: Boolean = false,
    val isInputPrompt: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
