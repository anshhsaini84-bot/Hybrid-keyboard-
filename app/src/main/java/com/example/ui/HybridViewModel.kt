package com.example.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BatteryTelemetry
import com.example.data.ClipboardEntryEntity
import com.example.data.CustomWordEntity
import com.example.data.DEFAULT_PINNED_TOOLS
import com.example.data.HybridDataRepository
import com.example.data.HybridDatabase
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardSettings
import com.example.data.OneHandedMode
import com.example.data.SettingsRepository
import com.example.data.ShiftState
import com.example.data.TextEditCommand
import com.example.data.ToolbarTool
import com.example.engine.BatteryMonitor
import com.example.engine.ClipboardSyncManager
import com.example.engine.DictionaryEngine
import com.example.engine.VoiceDictationManager
import com.example.engine.VoiceDictationState
import com.example.ime.HybridInputMethodService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImeActivationStatus(
    val isEnabledInSystem: Boolean = false,
    val isSelectedAsDefault: Boolean = false
)

data class HybridStudioUiState(
    val settings: KeyboardSettings = KeyboardSettings(),
    val effectiveEcoActive: Boolean = false,
    val battery: BatteryTelemetry = BatteryTelemetry(),
    val suggestions: List<String> = listOf("Hello", "Hybrid", "Thanks"),
    val clipboardEntries: List<ClipboardEntryEntity> = emptyList(),
    val customWords: List<CustomWordEntity> = emptyList(),
    val sandboxText: TextFieldValue = TextFieldValue(
        text = "Welcome to Hybrid Keyboard! ",
        selection = TextRange(28)
    ),
    val attachedSandboxImage: ClipboardEntryEntity? = null,
    val shiftState: ShiftState = ShiftState.SINGLE,
    val voiceState: VoiceDictationState = VoiceDictationState.Idle,
    val rmsLevel: Float = 0f,
    val imeStatus: ImeActivationStatus = ImeActivationStatus()
)

class HybridViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val settingsRepo = SettingsRepository(appContext)
    private val dataRepo = HybridDataRepository(HybridDatabase.getInstance(appContext))
    private val voiceManager = VoiceDictationManager(appContext)
    private val clipboardSync = ClipboardSyncManager(appContext, dataRepo, viewModelScope)

    private val undoStack = ArrayDeque<TextFieldValue>()
    private val _sandboxText = MutableStateFlow(
        TextFieldValue(
            text = "Welcome to Hybrid Keyboard! ",
            selection = TextRange(28)
        )
    )
    private val _attachedSandboxImage = MutableStateFlow<ClipboardEntryEntity?>(null)
    private val _shiftState = MutableStateFlow(ShiftState.SINGLE)
    private val _imeStatus = MutableStateFlow(readImeStatus())

    val uiState: StateFlow<HybridStudioUiState> = combine(
        settingsRepo.settingsFlow,
        BatteryMonitor.observe(appContext),
        dataRepo.clipboardEntries,
        dataRepo.customWords,
        _sandboxText,
        _attachedSandboxImage,
        _shiftState,
        voiceManager.state,
        voiceManager.rmsLevel,
        _imeStatus
    ) { values ->
        val settings = values[0] as KeyboardSettings
        val battery = values[1] as BatteryTelemetry
        @Suppress("UNCHECKED_CAST")
        val clipboard = values[2] as List<ClipboardEntryEntity>
        @Suppress("UNCHECKED_CAST")
        val customWords = values[3] as List<CustomWordEntity>
        val sandbox = values[4] as TextFieldValue
        val attachedImg = values[5] as ClipboardEntryEntity?
        val shift = values[6] as ShiftState
        val voice = values[7] as VoiceDictationState
        val rms = values[8] as Float
        val imeStatus = values[9] as ImeActivationStatus

        clipboardSync.setAutoCaptureEnabled(settings.autoCaptureClipboard)

        val autoEcoTriggered = settings.autoEcoOnLowBattery &&
            !battery.isCharging &&
            (battery.levelPercent <= 20 || battery.isPowerSaveMode)
        val effectiveEco = settings.ecoBatteryMode || autoEcoTriggered
        val effectiveSettings = settings.copy(ecoBatteryMode = effectiveEco)

        val (currentWord, previousWord) = extractWordsAtCursor(sandbox)
        val suggestions = DictionaryEngine.suggest(
            currentWord = currentWord,
            previousWord = previousWord,
            language = effectiveSettings.language,
            customWords = customWords,
            ecoMode = effectiveEco
        )

        HybridStudioUiState(
            settings = effectiveSettings,
            effectiveEcoActive = effectiveEco,
            battery = battery,
            suggestions = suggestions,
            clipboardEntries = clipboard,
            customWords = customWords,
            sandboxText = sandbox,
            attachedSandboxImage = attachedImg,
            shiftState = shift,
            voiceState = voice,
            rmsLevel = rms,
            imeStatus = imeStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HybridStudioUiState()
    )

    init {
        clipboardSync.startObserving()
        viewModelScope.launch {
            dataRepo.ensureDefaultsSeeded()
        }
    }

    fun refreshImeStatus() {
        _imeStatus.value = readImeStatus()
        if (uiState.value.settings.autoCaptureClipboard) {
            clipboardSync.captureCurrentSystemClip()
        }
    }

    fun syncSystemClipboardNow() {
        clipboardSync.captureCurrentSystemClip()
    }

    fun importImageToClipboard(uri: Uri, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = clipboardSync.importImageFromUri(uri)
            onResult(ok)
        }
    }

    fun importTextFileToClipboard(uri: Uri, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = clipboardSync.importTextFileFromUri(uri)
            onResult(ok)
        }
    }

    fun copyImageEntryToSystemClipboard(entry: ClipboardEntryEntity): Boolean {
        return clipboardSync.copyImageEntryToSystemClipboard(entry) != null
    }

    fun copyTextEntryToSystemClipboard(entry: ClipboardEntryEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = clipboardSync.copyTextEntryToSystemClipboard(entry)
            onResult(ok)
        }
    }

    fun commitClipboardEntryToSandbox(
        entry: ClipboardEntryEntity,
        onLargePasteInfo: (String) -> Unit = {}
    ) {
        if (!entry.isLargeText) {
            commitTextToSandbox(entry.content)
            return
        }
        viewModelScope.launch {
            val previewChunk = clipboardSync.readFullText(entry, maxChars = 2_400)
            val suffix = "\n… [${entry.lineCount} lines • ${ClipboardSyncManager.formatCharSize(entry.charCount)} stored cleanly for full chunked paste in any app]"
            commitTextToSandbox(previewChunk + suffix)
            onLargePasteInfo("Loaded preview of ${entry.lineCount} lines (streams 100% in any app)")
        }
    }

    fun attachImageToSandbox(entry: ClipboardEntryEntity) {
        _attachedSandboxImage.value = entry
        clipboardSync.copyImageEntryToSystemClipboard(entry)
    }

    fun clearAttachedSandboxImage() {
        _attachedSandboxImage.value = null
    }

    private fun readImeStatus(): ImeActivationStatus {
        return try {
            val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val myComponent = ComponentName(appContext, HybridInputMethodService::class.java)
            val myId = myComponent.flattenToShortString()
            val enabled = imm.enabledInputMethodList.any {
                it.component == myComponent || it.id == myId
            }
            val defaultIme = Settings.Secure.getString(
                appContext.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ).orEmpty()
            val selected = defaultIme == myId || defaultIme == myComponent.flattenToString()
            ImeActivationStatus(isEnabledInSystem = enabled, isSelectedAsDefault = selected)
        } catch (_: Throwable) {
            ImeActivationStatus()
        }
    }

    private fun pushUndoSnapshot() {
        if (undoStack.size >= 25) undoStack.removeFirst()
        undoStack.addLast(_sandboxText.value)
    }

    fun updateSandboxValue(value: TextFieldValue) {
        _sandboxText.value = value
    }

    fun setShiftState(shift: ShiftState) {
        _shiftState.value = shift
    }

    fun commitTextToSandbox(text: String) {
        pushUndoSnapshot()
        val current = _sandboxText.value
        val start = current.selection.min.coerceIn(0, current.text.length)
        val end = current.selection.max.coerceIn(start, current.text.length)
        val newText = buildString {
            append(current.text.substring(0, start))
            append(text)
            append(current.text.substring(end))
        }
        val newCursor = start + text.length
        _sandboxText.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
        if (uiState.value.settings.autoCapitalize &&
            (text.endsWith(". ") || text.endsWith("! ") || text.endsWith("? ") || text.endsWith("\n"))
        ) {
            _shiftState.value = ShiftState.SINGLE
        }
    }

    fun doubleSpacePeriodInSandbox() {
        val current = _sandboxText.value
        val cursor = current.selection.min.coerceIn(0, current.text.length)
        val before = current.text.substring(0, cursor)
        if (before.endsWith(" ") && before.length >= 2 && before[before.length - 2].isLetterOrDigit()) {
            pushUndoSnapshot()
            val newText = before.dropLast(1) + ". " + current.text.substring(cursor)
            _sandboxText.value = TextFieldValue(newText, TextRange(cursor + 1))
        } else {
            commitTextToSandbox(" ")
        }
    }

    fun deletePreviousWordInSandbox() {
        val current = _sandboxText.value
        val cursor = current.selection.min.coerceIn(0, current.text.length)
        if (cursor == 0) return
        val before = current.text.substring(0, cursor)
        var idx = before.length
        while (idx > 0 && before[idx - 1].isWhitespace()) idx--
        while (idx > 0 && !before[idx - 1].isWhitespace()) idx--
        if (idx < cursor) {
            pushUndoSnapshot()
            val newText = current.text.removeRange(idx, cursor)
            _sandboxText.value = TextFieldValue(newText, TextRange(idx))
        }
    }

    fun replaceWordInSandbox(replacement: String) {
        pushUndoSnapshot()
        val current = _sandboxText.value
        val cursor = current.selection.min.coerceIn(0, current.text.length)
        val before = current.text.substring(0, cursor)
        val after = current.text.substring(current.selection.max.coerceIn(cursor, current.text.length))
        val tokenLen = before.takeLastWhile { !it.isWhitespace() }.length
        val prefix = before.dropLast(tokenLen)
        val newText = "$prefix$replacement $after"
        val newCursor = prefix.length + replacement.length + 1
        _sandboxText.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    fun backspaceInSandbox(count: Int = 1) {
        val current = _sandboxText.value
        val start = current.selection.min.coerceIn(0, current.text.length)
        val end = current.selection.max.coerceIn(start, current.text.length)
        if (start != end) {
            pushUndoSnapshot()
            val newText = current.text.removeRange(start, end)
            _sandboxText.value = TextFieldValue(newText, TextRange(start))
        } else if (start > 0) {
            pushUndoSnapshot()
            val deleteFrom = (start - count).coerceAtLeast(0)
            val newText = current.text.removeRange(deleteFrom, start)
            _sandboxText.value = TextFieldValue(newText, TextRange(deleteFrom))
            if (newText.isEmpty() && uiState.value.settings.autoCapitalize) {
                _shiftState.value = ShiftState.SINGLE
            }
        }
    }

    fun moveCursorInSandbox(delta: Int) {
        val current = _sandboxText.value
        val next = (current.selection.start + delta).coerceIn(0, current.text.length)
        _sandboxText.value = current.copy(selection = TextRange(next))
    }

    fun performTextEditInSandbox(command: TextEditCommand) {
        val current = _sandboxText.value
        when (command) {
            TextEditCommand.MOVE_LEFT -> moveCursorInSandbox(-1)
            TextEditCommand.MOVE_RIGHT -> moveCursorInSandbox(1)
            TextEditCommand.MOVE_UP -> moveCursorInSandbox(-18)
            TextEditCommand.MOVE_DOWN -> moveCursorInSandbox(18)
            TextEditCommand.JUMP_START -> _sandboxText.value = current.copy(selection = TextRange(0))
            TextEditCommand.JUMP_END -> _sandboxText.value = current.copy(selection = TextRange(current.text.length))
            TextEditCommand.SELECT_ALL -> _sandboxText.value = current.copy(selection = TextRange(0, current.text.length))
            TextEditCommand.COPY -> {
                val selected = if (!current.selection.collapsed) {
                    current.text.substring(current.selection.min, current.selection.max)
                } else {
                    current.text
                }
                if (selected.isNotBlank()) {
                    clipboardSync.copyTextToSystemClipboard(selected)
                    viewModelScope.launch { clipboardSync.saveTextToClipboard(selected) }
                }
            }
            TextEditCommand.CUT -> {
                val start = current.selection.min.coerceIn(0, current.text.length)
                val end = current.selection.max.coerceIn(start, current.text.length)
                val cutText = if (start != end) current.text.substring(start, end) else current.text
                if (cutText.isNotBlank()) {
                    pushUndoSnapshot()
                    clipboardSync.copyTextToSystemClipboard(cutText)
                    viewModelScope.launch { clipboardSync.saveTextToClipboard(cutText) }
                    val remaining = if (start != end) current.text.removeRange(start, end) else ""
                    _sandboxText.value = TextFieldValue(remaining, TextRange(if (start != end) start else 0))
                }
            }
            TextEditCommand.PASTE -> {
                val latestEntry = uiState.value.clipboardEntries.firstOrNull { !it.isImage }
                if (latestEntry != null) {
                    commitClipboardEntryToSandbox(latestEntry)
                }
            }
            TextEditCommand.UNDO -> {
                if (undoStack.isNotEmpty()) {
                    _sandboxText.value = undoStack.removeLast()
                }
            }
        }
    }

    fun clearSandbox() {
        pushUndoSnapshot()
        _sandboxText.value = TextFieldValue("", TextRange(0))
        _attachedSandboxImage.value = null
        if (uiState.value.settings.autoCapitalize) {
            _shiftState.value = ShiftState.SINGLE
        }
    }

    fun saveSandboxToClipboard() {
        val text = _sandboxText.value.text.trim()
        if (text.isNotEmpty()) {
            clipboardSync.copyTextToSystemClipboard(text)
            viewModelScope.launch {
                clipboardSync.saveTextToClipboard(text, pinned = false)
            }
        }
    }

    fun startVoiceDictation() {
        val locale = uiState.value.settings.language.speechLocale
        voiceManager.startListening(localeTag = locale) { spoken ->
            commitTextToSandbox(spoken)
        }
    }

    fun stopVoiceDictation() {
        voiceManager.stopListening()
    }

    fun dismissVoiceError() {
        voiceManager.clearError()
    }

    fun hasMicPermission(): Boolean = voiceManager.hasMicPermission()

    fun setThemeStyle(style: HybridThemeStyle) {
        viewModelScope.launch {
            settingsRepo.updateSettings {
                it.copy(
                    themeStyle = style,
                    ecoBatteryMode = style == HybridThemeStyle.AMOLED_ECO
                )
            }
        }
    }

    fun toggleNumberRow(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(showNumberRow = enabled) } }
    }

    fun toggleGlideTyping(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(glideTypingEnabled = enabled) } }
    }

    fun toggleSpacebarCursor(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(spacebarCursorEnabled = enabled) } }
    }

    fun toggleDoubleSpacePeriod(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(doubleSpacePeriod = enabled) } }
    }

    fun toggleSwipeBackspaceWord(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(swipeBackspaceDeleteWord = enabled) } }
    }

    fun toggleFloatingMode(enabled: Boolean? = null) {
        viewModelScope.launch {
            settingsRepo.updateSettings { cur ->
                cur.copy(floatingMode = enabled ?: !cur.floatingMode)
            }
        }
    }

    fun toggleAutoCaptureClipboard(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(autoCaptureClipboard = enabled) } }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(hapticFeedbackEnabled = enabled) } }
    }

    fun setHapticStrength(ms: Int) {
        viewModelScope.launch {
            settingsRepo.updateSettings { it.copy(hapticStrengthMs = ms.coerceIn(5, 30)) }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(soundOnKeypress = enabled) } }
    }

    fun toggleSoftMicVisible(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(softMicAlwaysVisible = enabled) } }
    }

    fun toggleAutoCapitalize(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(autoCapitalize = enabled) } }
    }

    fun setOneHandedMode(mode: OneHandedMode) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(oneHandedMode = mode) } }
    }

    fun cycleOneHandedMode() {
        viewModelScope.launch {
            settingsRepo.updateSettings { cur ->
                val all = OneHandedMode.entries
                cur.copy(oneHandedMode = all[(all.indexOf(cur.oneHandedMode) + 1) % all.size])
            }
        }
    }

    fun setLanguage(language: KeyboardLanguage) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(language = language) } }
    }

    fun cycleLanguage() {
        viewModelScope.launch {
            settingsRepo.updateSettings { cur ->
                val all = KeyboardLanguage.entries
                cur.copy(language = all[(all.indexOf(cur.language) + 1) % all.size])
            }
        }
    }

    fun setHeightScale(scale: Float) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(heightScale = scale.coerceIn(0.72f, 1.35f)) } }
    }

    fun updatePinnedToolbarTools(tools: List<ToolbarTool>) {
        val clean = tools.distinct().take(5).ifEmpty { DEFAULT_PINNED_TOOLS }
        viewModelScope.launch {
            settingsRepo.updateSettings { it.copy(pinnedToolbarTools = clean) }
        }
    }

    fun toggleEcoBatteryMode(enabled: Boolean? = null) {
        viewModelScope.launch {
            settingsRepo.updateSettings { cur ->
                cur.copy(ecoBatteryMode = enabled ?: !cur.ecoBatteryMode)
            }
        }
    }

    fun toggleAutoEcoOnLowBattery(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSettings { it.copy(autoEcoOnLowBattery = enabled) } }
    }

    fun addClipboardItem(text: String) {
        viewModelScope.launch { clipboardSync.saveTextToClipboard(text) }
    }

    fun toggleClipboardPin(entry: ClipboardEntryEntity) {
        viewModelScope.launch { dataRepo.togglePin(entry.id, entry.isPinned) }
    }

    fun deleteClipboardEntry(id: Int) {
        viewModelScope.launch { dataRepo.deleteClipboardEntry(id) }
    }

    fun clearUnpinnedClipboard() {
        viewModelScope.launch { dataRepo.clearUnpinnedClipboard() }
    }

    fun addCustomWord(shortcut: String, expansion: String, langCode: String) {
        viewModelScope.launch { dataRepo.addCustomWord(shortcut, expansion, langCode) }
    }

    fun deleteCustomWord(id: Int) {
        viewModelScope.launch { dataRepo.deleteCustomWord(id) }
    }

    private fun extractWordsAtCursor(tfv: TextFieldValue): Pair<String, String> {
        val cursor = tfv.selection.min.coerceIn(0, tfv.text.length)
        val before = tfv.text.substring(0, cursor)
        val currentWord = before.takeLastWhile { !it.isWhitespace() }
        val prevWord = before.dropLast(currentWord.length).trimEnd().takeLastWhile { !it.isWhitespace() }
        return currentWord to prevWord
    }

    override fun onCleared() {
        clipboardSync.stopObserving()
        voiceManager.stopListening()
        super.onCleared()
    }
}
