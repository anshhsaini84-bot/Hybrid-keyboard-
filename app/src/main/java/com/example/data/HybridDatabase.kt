package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.io.File

@Entity(tableName = "clipboard_entries")
data class ClipboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val textFilePath: String? = null,
    val lineCount: Int = 1,
    val charCount: Int = 0,
    val imagePath: String? = null,
    val mimeType: String? = null,
    val isScreenshot: Boolean = false,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isImage: Boolean
        get() = !imagePath.isNullOrBlank()

    val isLargeText: Boolean
        get() = !textFilePath.isNullOrBlank()
}

@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shortcut: String,
    val expansion: String,
    val languageCode: String = "EN"
)

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_entries ORDER BY isPinned DESC, timestamp DESC LIMIT 60")
    fun observeAll(): Flow<List<ClipboardEntryEntity>>

    @Query("SELECT * FROM clipboard_entries WHERE content = :content AND imagePath IS NULL AND textFilePath IS NULL LIMIT 1")
    suspend fun findByText(content: String): ClipboardEntryEntity?

    @Query("SELECT * FROM clipboard_entries WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): ClipboardEntryEntity?

    @Query("SELECT * FROM clipboard_entries WHERE isPinned = 0")
    suspend fun getUnpinned(): List<ClipboardEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClipboardEntryEntity)

    @Query("UPDATE clipboard_entries SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Int, pinned: Boolean)

    @Query("UPDATE clipboard_entries SET timestamp = :timestamp WHERE id = :id")
    suspend fun touchTimestamp(id: Int, timestamp: Long)

    @Query("DELETE FROM clipboard_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM clipboard_entries WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("SELECT COUNT(*) FROM clipboard_entries")
    suspend fun count(): Int
}

@Dao
interface CustomWordDao {
    @Query("SELECT * FROM custom_words ORDER BY shortcut ASC")
    fun observeAll(): Flow<List<CustomWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: CustomWordEntity)

    @Query("DELETE FROM custom_words WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM custom_words")
    suspend fun count(): Int
}

@Database(
    entities = [ClipboardEntryEntity::class, CustomWordEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HybridDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun customWordDao(): CustomWordDao

    companion object {
        @Volatile
        private var INSTANCE: HybridDatabase? = null

        fun getInstance(context: Context): HybridDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HybridDatabase::class.java,
                    "hybrid_keyboard_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class HybridDataRepository(private val db: HybridDatabase) {
    val clipboardEntries: Flow<List<ClipboardEntryEntity>> = db.clipboardDao().observeAll()
    val customWords: Flow<List<CustomWordEntity>> = db.customWordDao().observeAll()

    suspend fun ensureDefaultsSeeded() {
        if (db.clipboardDao().count() == 0) {
            db.clipboardDao().insert(
                ClipboardEntryEntity(
                    content = "Hybrid Keyboard: Supports 50,000+ line code copy/paste & screenshots",
                    lineCount = 1,
                    charCount = 69,
                    isPinned = true
                )
            )
        }
        if (db.customWordDao().count() == 0) {
            val defaults = listOf(
                CustomWordEntity(shortcut = "omw", expansion = "On my way!", languageCode = "EN"),
                CustomWordEntity(shortcut = "ty", expansion = "Thank you so much!", languageCode = "EN"),
                CustomWordEntity(shortcut = "brb", expansion = "Be right back", languageCode = "EN"),
                CustomWordEntity(shortcut = "namaste", expansion = "नमस्ते 🙏", languageCode = "HI-EN"),
                CustomWordEntity(shortcut = "gm", expansion = "Good morning ☀️", languageCode = "EN")
            )
            defaults.forEach { db.customWordDao().insert(it) }
        }
    }

    suspend fun addClipboardText(text: String, pinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val existing = db.clipboardDao().findByText(trimmed)
        if (existing != null) {
            db.clipboardDao().touchTimestamp(existing.id, System.currentTimeMillis())
        } else {
            val lines = trimmed.count { it == '\n' } + 1
            db.clipboardDao().insert(
                ClipboardEntryEntity(
                    content = trimmed,
                    lineCount = lines,
                    charCount = trimmed.length,
                    isPinned = pinned
                )
            )
        }
    }

    suspend fun addLargeClipboardText(
        preview: String,
        textFilePath: String,
        lineCount: Int,
        charCount: Int,
        pinned: Boolean = false
    ) {
        db.clipboardDao().insert(
            ClipboardEntryEntity(
                content = preview,
                textFilePath = textFilePath,
                lineCount = lineCount,
                charCount = charCount,
                isPinned = pinned,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addClipboardImage(
        label: String,
        imagePath: String,
        mimeType: String,
        isScreenshot: Boolean,
        pinned: Boolean = false
    ) {
        db.clipboardDao().insert(
            ClipboardEntryEntity(
                content = label,
                imagePath = imagePath,
                mimeType = mimeType,
                isScreenshot = isScreenshot,
                isPinned = pinned,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun togglePin(id: Int, currentlyPinned: Boolean) {
        db.clipboardDao().setPinned(id, !currentlyPinned)
    }

    suspend fun deleteClipboardEntry(id: Int) {
        val entry = db.clipboardDao().findById(id)
        entry?.imagePath?.let { path -> runCatching { File(path).delete() } }
        entry?.textFilePath?.let { path -> runCatching { File(path).delete() } }
        db.clipboardDao().deleteById(id)
    }

    suspend fun clearUnpinnedClipboard() {
        val unpinned = db.clipboardDao().getUnpinned()
        unpinned.forEach { entry ->
            entry.imagePath?.let { path -> runCatching { File(path).delete() } }
            entry.textFilePath?.let { path -> runCatching { File(path).delete() } }
        }
        db.clipboardDao().clearUnpinned()
    }

    suspend fun addCustomWord(shortcut: String, expansion: String, languageCode: String = "EN") {
        val cleanShortcut = shortcut.trim().lowercase()
        val cleanExpansion = expansion.trim()
        if (cleanShortcut.isNotEmpty() && cleanExpansion.isNotEmpty()) {
            db.customWordDao().insert(
                CustomWordEntity(
                    shortcut = cleanShortcut,
                    expansion = cleanExpansion,
                    languageCode = languageCode
                )
            )
        }
    }

    suspend fun deleteCustomWord(id: Int) {
        db.customWordDao().deleteById(id)
    }
}
