package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.FileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PyCodeDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Files
    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY isDirectory DESC, name ASC")
    fun getFilesByProject(projectId: Long): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): FileEntity?

    @Query("SELECT * FROM files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("UPDATE files SET content = :content, isModified = :isModified, lastModified = :lastModified WHERE id = :id")
    suspend fun updateFileContent(id: Long, content: String, isModified: Boolean, lastModified: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE projectId = :projectId AND (path = :path OR path LIKE :pathPrefix)")
    suspend fun deleteFileAndChildren(projectId: Long, path: String, pathPrefix: String)

    @Query("DELETE FROM files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: Long)

    // Settings
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)
}

@Database(
    entities = [ProjectEntity::class, FileEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PyCodeDatabase : RoomDatabase() {
    abstract fun pyCodeDao(): PyCodeDao

    fun workspaceDao(): PyCodeDao = pyCodeDao()

    companion object {
        @Volatile
        private var INSTANCE: PyCodeDatabase? = null

        fun getDatabase(context: Context): PyCodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PyCodeDatabase::class.java,
                    "pycode_ide_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
