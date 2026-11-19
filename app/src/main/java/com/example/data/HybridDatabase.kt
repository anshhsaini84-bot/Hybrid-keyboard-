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

@Entity(tableName = "clipboard_entries")
data class ClipboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shortcut: String,
    val expansion: String,
    val languageCode: String = "EN"
)

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_entries ORDER BY isPinned DESC, timestamp DESC LIMIT 40")
    fun observeAll(): Flow<List<ClipboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClipboardEntryEntity)

    @Query("UPDATE clipboard_entries SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Int, pinned: Boolean)

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
    version = 1,
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
                ).build().also { INSTANCE = it }
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
                    content = "Hybrid Keyboard: Samsung layout + Gboard glide + Voice Mic",
                    isPinned = true
                )
            )
            db.clipboardDao().insert(
                ClipboardEntryEntity(
                    content = "Namaste! Ready for smooth typing with Eco battery mode.",
                    isPinned = false
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
        if (trimmed.isNotEmpty()) {
            db.clipboardDao().insert(ClipboardEntryEntity(content = trimmed, isPinned = pinned))
        }
    }

    suspend fun togglePin(id: Int, currentlyPinned: Boolean) {
        db.clipboardDao().setPinned(id, !currentlyPinned)
    }

    suspend fun deleteClipboardEntry(id: Int) {
        db.clipboardDao().deleteById(id)
    }

    suspend fun clearUnpinnedClipboard() {
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
