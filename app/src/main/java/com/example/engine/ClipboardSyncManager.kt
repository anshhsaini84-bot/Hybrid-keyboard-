package com.example.engine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.core.content.FileProvider
import com.example.data.ClipboardEntryEntity
import com.example.data.HybridDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Synchronizes Android's system ClipboardManager (text + copied screenshots/images)
 * into Hybrid Keyboard's persistent Room Clipboard Vault.
 *
 * Supports massive 50,000+ line code/text clips via disk-backed UTF-8 files +
 * chunked streaming so any Android device (including low-RAM devices) can copy,
 * store, and paste huge code blocks without Binder TransactionTooLargeException
 * or SQLite CursorWindow crashes.
 */
class ClipboardSyncManager(
    private val context: Context,
    private val repository: HybridDataRepository,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val clipboardManager =
        appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var isListening = false
    private var autoCaptureEnabled = true
    private var lastCapturedSignature: String? = null

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!autoCaptureEnabled) return@OnPrimaryClipChangedListener
        captureCurrentSystemClip()
    }

    fun setAutoCaptureEnabled(enabled: Boolean) {
        autoCaptureEnabled = enabled
    }

    fun startObserving() {
        if (isListening) return
        try {
            clipboardManager.addPrimaryClipChangedListener(clipListener)
            isListening = true
        } catch (_: Throwable) {
        }
    }

    fun stopObserving() {
        if (!isListening) return
        try {
            clipboardManager.removePrimaryClipChangedListener(clipListener)
        } catch (_: Throwable) {
        }
        isListening = false
    }

    /**
     * Reads the current system clipboard (text, large code, or screenshot/image URI)
     * and persists it to Hybrid Clipboard without risking SQLite or Binder overflows.
     */
    fun captureCurrentSystemClip() {
        val clip = runCatching { clipboardManager.primaryClip }.getOrNull() ?: return
        if (clip.itemCount == 0) return

        val description = clip.description
        val item = clip.getItemAt(0)
        val uri = item.uri

        if (uri != null) {
            val resolvedMime = runCatching {
                appContext.contentResolver.getType(uri)
            }.getOrNull() ?: (0 until description.mimeTypeCount)
                .map { description.getMimeType(it) }
                .firstOrNull { it.startsWith("image/") }

            if (resolvedMime?.startsWith("image/") == true) {
                val sig = "uri:$uri"
                if (sig == lastCapturedSignature) return
                lastCapturedSignature = sig
                scope.launch {
                    importImageFromUri(
                        uri = uri,
                        mimeHint = resolvedMime,
                        isScreenshotHint = detectIfScreenshot(uri, description.label?.toString())
                    )
                }
                return
            }
        }

        val rawText = runCatching { item.coerceToText(appContext)?.toString() }.getOrNull().orEmpty()
        if (rawText.isNotBlank()) {
            val sig = buildTextSignature(rawText)
            if (sig == lastCapturedSignature) return
            lastCapturedSignature = sig
            scope.launch(Dispatchers.IO) {
                saveTextToClipboard(rawText)
            }
        }
    }

    /**
     * Stores normal clips directly in Room, and automatically offloads large code/text clips
     * (> 4,000 chars, up to 50,000+ lines) to a private UTF-8 file on disk so Room stays fast
     * and memory-light on every phone.
     */
    suspend fun saveTextToClipboard(text: String, pinned: Boolean = false) = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext
        if (text.length <= LARGE_TEXT_THRESHOLD) {
            repository.addClipboardText(text, pinned)
            return@withContext
        }

        val dir = File(appContext.filesDir, "clipboard_texts").apply {
            if (!exists()) mkdirs()
        }
        val timestamp = System.currentTimeMillis()
        val outFile = File(dir, "clip_$timestamp.txt")
        outFile.writeText(text, Charsets.UTF_8)

        val lineCount = countLines(text)
        val preview = buildPreview(text, lineCount)
        repository.addLargeClipboardText(
            preview = preview,
            textFilePath = outFile.absolutePath,
            lineCount = lineCount,
            charCount = text.length,
            pinned = pinned
        )
    }

    /**
     * Streams any `.txt` or source-code file chosen via Android's zero-permission Storage Access
     * Framework (`OpenDocument`) directly into Hybrid Clipboard's file-backed vault — handles
     * 50,000+ lines with minimal RAM overhead.
     */
    suspend fun importTextFileFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val dir = File(appContext.filesDir, "clipboard_texts").apply {
                if (!exists()) mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            val outFile = File(dir, "clip_$timestamp.txt")
            val displayName = queryDisplayName(uri)

            var lineCount = 0
            var charCount = 0
            val previewBuilder = StringBuilder(PREVIEW_MAX_CHARS + 32)

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8), 16 * 1024).use { reader ->
                    outFile.bufferedWriter(Charsets.UTF_8, 16 * 1024).use { writer ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            if (lineCount > 0) {
                                writer.newLine()
                                charCount += 1
                            }
                            writer.write(line)
                            charCount += line.length
                            lineCount++
                            if (previewBuilder.length < PREVIEW_MAX_CHARS) {
                                if (previewBuilder.isNotEmpty()) previewBuilder.append('\n')
                                val remaining = PREVIEW_MAX_CHARS - previewBuilder.length
                                previewBuilder.append(line.take(remaining))
                            }
                            line = reader.readLine()
                        }
                    }
                }
            } ?: return@withContext false

            if (charCount == 0) {
                outFile.delete()
                return@withContext false
            }

            val header = if (!displayName.isNullOrBlank()) "$displayName\n" else ""
            val preview = (header + previewBuilder.toString().trim()).take(PREVIEW_MAX_CHARS)
            repository.addLargeClipboardText(
                preview = preview,
                textFilePath = outFile.absolutePath,
                lineCount = lineCount.coerceAtLeast(1),
                charCount = charCount,
                pinned = false
            )
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Streams a clipboard entry's text in safe fixed-size chunks (default 4,000 chars)
     * so `InputConnection.commitText()` can paste 50,000+ lines of code smoothly without
     * triggering Android's 1 MB Binder TransactionTooLargeException.
     */
    suspend fun streamTextChunks(
        entry: ClipboardEntryEntity,
        chunkSize: Int = DEFAULT_PASTE_CHUNK_CHARS,
        onChunk: suspend (String) -> Unit
    ) {
        if (!entry.isLargeText) {
            val text = entry.content
            if (text.length <= chunkSize) {
                onChunk(text)
            } else {
                var offset = 0
                while (offset < text.length) {
                    val end = (offset + chunkSize).coerceAtMost(text.length)
                    onChunk(text.substring(offset, end))
                    offset = end
                }
            }
            return
        }

        val path = entry.textFilePath ?: return
        val file = File(path)
        if (!file.exists()) {
            onChunk(entry.content)
            return
        }

        val reader = withContext(Dispatchers.IO) {
            InputStreamReader(FileInputStream(file), Charsets.UTF_8)
        }
        try {
            val buffer = CharArray(chunkSize)
            while (true) {
                val read = withContext(Dispatchers.IO) { reader.read(buffer) }
                if (read <= 0) break
                onChunk(String(buffer, 0, read))
            }
        } finally {
            withContext(Dispatchers.IO) { runCatching { reader.close() } }
        }
    }

    suspend fun readFullText(entry: ClipboardEntryEntity, maxChars: Int = Int.MAX_VALUE): String =
        withContext(Dispatchers.IO) {
            if (!entry.isLargeText) return@withContext entry.content.take(maxChars)
            val path = entry.textFilePath ?: return@withContext entry.content.take(maxChars)
            val file = File(path)
            if (!file.exists()) return@withContext entry.content.take(maxChars)
            InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(8192)
                while (sb.length < maxChars) {
                    val toRead = (maxChars - sb.length).coerceAtMost(buf.size)
                    val read = reader.read(buf, 0, toRead)
                    if (read <= 0) break
                    sb.append(buf, 0, read)
                }
                sb.toString()
            }
        }

    /**
     * Copies an image or screenshot from any content:// URI (system clipboard or Android Photo Picker)
     * into private app storage so it remains valid and pasteable anytime.
     */
    suspend fun importImageFromUri(
        uri: Uri,
        mimeHint: String? = null,
        isScreenshotHint: Boolean? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val mimeType = mimeHint
                ?: appContext.contentResolver.getType(uri)
                ?: "image/png"
            if (!mimeType.startsWith("image/")) return@withContext false

            val ext = when {
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("gif") -> "gif"
                else -> "png"
            }

            val dir = File(appContext.filesDir, "clipboard_images").apply {
                if (!exists()) mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            val outFile = File(dir, "clip_${timestamp}.$ext")

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            if (outFile.length() == 0L) {
                outFile.delete()
                return@withContext false
            }

            val displayName = queryDisplayName(uri)
            val isScreenshot = isScreenshotHint ?: detectIfScreenshot(uri, displayName)
            val timeLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
            val label = when {
                isScreenshot -> "Screenshot • $timeLabel"
                !displayName.isNullOrBlank() -> displayName
                else -> "Copied Image • $timeLabel"
            }

            repository.addClipboardImage(
                label = label,
                imagePath = outFile.absolutePath,
                mimeType = mimeType,
                isScreenshot = isScreenshot
            )
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Puts a saved clipboard image back onto the Android system clipboard with a FileProvider URI.
     */
    fun copyImageEntryToSystemClipboard(entry: ClipboardEntryEntity): Uri? {
        val uri = getShareableUri(appContext, entry) ?: return null
        lastCapturedSignature = "uri:$uri"
        val clip = ClipData.newUri(appContext.contentResolver, entry.content, uri)
        clipboardManager.setPrimaryClip(clip)
        return uri
    }

    /**
     * Safely puts a text entry back onto the Android system clipboard.
     * For massive 50,000+ line clips exceeding Android's Binder IPC limit, it safely caps the
     * system clipboard copy while Hybrid Keyboard's direct paste streams 100% of the file!
     */
    suspend fun copyTextEntryToSystemClipboard(entry: ClipboardEntryEntity): Boolean {
        val text = readFullText(entry, maxChars = SYSTEM_CLIPBOARD_SAFE_CHARS)
        return copyTextToSystemClipboard(text)
    }

    fun copyTextToSystemClipboard(text: String): Boolean {
        val safeText = if (text.length > SYSTEM_CLIPBOARD_SAFE_CHARS) {
            text.substring(0, SYSTEM_CLIPBOARD_SAFE_CHARS)
        } else {
            text
        }
        lastCapturedSignature = buildTextSignature(safeText)
        return runCatching {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Hybrid Keyboard", safeText))
            true
        }.getOrDefault(false)
    }

    private fun buildTextSignature(text: String): String {
        val len = text.length
        return if (len <= 128) "txt:$text" else "txt:$len:${text.take(64)}:${text.takeLast(64)}"
    }

    private fun detectIfScreenshot(uri: Uri, labelOrName: String?): Boolean {
        val raw = "${uri} ${labelOrName.orEmpty()}".lowercase(Locale.ROOT)
        return raw.contains("screenshot") || raw.contains("screen_shot") || raw.contains("capture")
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            appContext.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    companion object {
        const val LARGE_TEXT_THRESHOLD = 4_000
        const val DEFAULT_PASTE_CHUNK_CHARS = 4_000
        private const val SYSTEM_CLIPBOARD_SAFE_CHARS = 350_000
        private const val PREVIEW_MAX_CHARS = 240

        private val thumbnailCache = object : LruCache<String, Bitmap>(24) {}

        fun countLines(text: String): Int {
            if (text.isEmpty()) return 0
            var lines = 1
            for (ch in text) {
                if (ch == '\n') lines++
            }
            return lines
        }

        fun buildPreview(text: String, lineCount: Int): String {
            val snippet = text.trimStart().take(PREVIEW_MAX_CHARS).replace("\r\n", "\n")
            return if (snippet.isNotBlank()) snippet else "Code Block ($lineCount lines)"
        }

        fun formatCharSize(chars: Int): String = when {
            chars >= 1_000_000 -> String.format(Locale.US, "%.1fM chars", chars / 1_000_000f)
            chars >= 1_000 -> String.format(Locale.US, "%.1fK chars", chars / 1_000f)
            else -> "$chars chars"
        }

        fun getShareableUri(context: Context, entry: ClipboardEntryEntity): Uri? {
            val path = entry.imagePath ?: return null
            val file = File(path)
            if (!file.exists()) return null
            return runCatching {
                FileProvider.getUriForFile(
                    context.applicationContext,
                    "${context.packageName}.fileprovider",
                    file
                )
            }.getOrNull()
        }

        /**
         * Fast, memory-safe downsampled bitmap loader for smooth 60fps keyboard clipboard previews.
         */
        fun loadThumbnail(imagePath: String, maxSizePx: Int = 240): Bitmap? {
            thumbnailCache.get(imagePath)?.let { return it }
            val file = File(imagePath)
            if (!file.exists()) return null
            return runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(imagePath, bounds)
                var sample = 1
                while (bounds.outWidth / sample > maxSizePx || bounds.outHeight / sample > maxSizePx) {
                    sample *= 2
                }
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = sample.coerceAtLeast(1)
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeFile(imagePath, opts)?.also { bmp ->
                    thumbnailCache.put(imagePath, bmp)
                }
            }.getOrNull()
        }
    }
}
