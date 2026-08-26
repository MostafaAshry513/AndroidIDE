package com.example.data.repository

import com.example.data.db.PyCodeDao
import com.example.data.model.FileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SettingEntity
import com.example.data.sample.StarterProjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WorkspaceRepository(private val dao: PyCodeDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allSettings: Flow<List<SettingEntity>> = dao.getAllSettings()

    fun getFilesByProject(projectId: Long): Flow<List<FileEntity>> {
        return dao.getFilesByProject(projectId)
    }

    suspend fun ensureDefaultProject(): Long = withContext(Dispatchers.IO) {
        val projects = dao.getAllProjects().first()
        if (projects.isEmpty()) {
            val projectId = dao.insertProject(StarterProjects.defaultProject)
            val files = StarterProjects.getInitialFiles(projectId)
            dao.insertFiles(files)
            projectId
        } else {
            projects.first().id
        }
    }

    suspend fun getFileById(fileId: Long): FileEntity? = withContext(Dispatchers.IO) {
        dao.getFileById(fileId)
    }

    suspend fun createFile(projectId: Long, name: String, path: String, content: String = "", isDirectory: Boolean = false, parentPath: String = ""): Long = withContext(Dispatchers.IO) {
        val ext = if (name.contains(".")) name.substringAfterLast(".").lowercase() else "txt"
        val lang = when (ext) {
            "py" -> "python"
            "md" -> "markdown"
            "json" -> "json"
            else -> "text"
        }
        val file = FileEntity(
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            isDirectory = isDirectory,
            parentPath = parentPath,
            language = lang,
            lastModified = System.currentTimeMillis()
        )
        dao.insertFile(file)
    }

    suspend fun saveFileContent(fileId: Long, content: String) = withContext(Dispatchers.IO) {
        dao.updateFileContent(fileId, content, isModified = false, lastModified = System.currentTimeMillis())
    }

    suspend fun markFileModified(fileId: Long, content: String, isModified: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFileContent(fileId, content, isModified, System.currentTimeMillis())
    }

    suspend fun deleteFile(file: FileEntity) = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            dao.deleteFileAndChildren(file.projectId, file.path, "${file.path}/%")
        } else {
            dao.deleteFile(file)
        }
    }

    suspend fun renameFile(file: FileEntity, newName: String) = withContext(Dispatchers.IO) {
        val newPath = if (file.parentPath.isEmpty()) newName else "${file.parentPath}/$newName"
        val updated = file.copy(
            name = newName,
            path = newPath,
            lastModified = System.currentTimeMillis()
        )
        dao.updateFile(updated)
    }

    suspend fun createProjectWithFiles(name: String, description: String, filesGenerator: (projectId: Long) -> List<FileEntity>): Long = withContext(Dispatchers.IO) {
        val project = ProjectEntity(name = name, description = description)
        val projectId = dao.insertProject(project)
        val files = filesGenerator(projectId)
        dao.insertFiles(files)
        projectId
    }

    suspend fun getSetting(key: String, default: String): String = withContext(Dispatchers.IO) {
        dao.getSetting(key) ?: default
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.setSetting(SettingEntity(key, value))
    }
}
