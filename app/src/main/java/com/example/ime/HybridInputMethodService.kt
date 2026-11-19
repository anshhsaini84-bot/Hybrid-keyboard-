package com.example.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ClipboardEntryEntity
import com.example.data.HybridDataRepository
import com.example.data.HybridDatabase
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardSettings
import com.example.data.OneHandedMode
import com.example.data.SettingsRepository
import com.example.data.ShiftState
import com.example.engine.BatteryMonitor
import com.example.engine.DictionaryEngine
import com.example.engine.VoiceDictationManager
import com.example.ui.keyboard.HybridKeyboardActions
import com.example.ui.keyboard.HybridKeyboardView
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Real system-wide Android InputMethodService for Hybrid Keyboard.
 * Supports Samsung-style layout + Gboard glide/voice + Android 17 edge-to-edge IME insets.
 */
class HybridInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var dataRepository: HybridDataRepository
    private lateinit var voiceManager: VoiceDictationManager

    private val currentWordFlow = MutableStateFlow("")
    private val previousWordFlow = MutableStateFlow("")
    private var shiftState by mutableStateOf(ShiftState.SINGLE)
    private var currentSettings = KeyboardSettings()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        settingsRepository = SettingsRepository(applicationContext)
        dataRepository = HybridDataRepository(HybridDatabase.getInstance(applicationContext))
        voiceManager = VoiceDictationManager(applicationContext)

        serviceScope.launch {
            dataRepository.ensureDefaultsSeeded()
        }
    }

    override fun onCreateInputView(): View {
        val decorView = window?.window?.decorView
        decorView?.let { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeViewModelStoreOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
        }

        val suggestionsFlow = combine(
            currentWordFlow,
            previousWordFlow,
            settingsRepository.settingsFlow,
            dataRepository.customWords,
            BatteryMonitor.observe(applicationContext)
        ) { word, prev, settings, customWords, battery ->
            val effectiveEco = settings.ecoBatteryMode ||
                (settings.autoEcoOnLowBattery && !battery.isCharging &&
                    (battery.levelPercent <= 20 || battery.isPowerSaveMode))
            val effectiveSettings = settings.copy(ecoBatteryMode = effectiveEco)
            currentSettings = effectiveSettings
            effectiveSettings to DictionaryEngine.suggest(
                currentWord = word,
                previousWord = prev,
                language = effectiveSettings.language,
                customWords = customWords,
                ecoMode = effectiveEco
            )
        }

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@HybridInputMethodService))
            setViewTreeLifecycleOwner(this@HybridInputMethodService)
            setViewTreeViewModelStoreOwner(this@HybridInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@HybridInputMethodService)

            setContent {
                val pair by suggestionsFlow.collectAsState(initial = KeyboardSettings() to listOf("Hello", "Hybrid", "Thanks"))
                val clipboardEntries by dataRepository.clipboardEntries.collectAsState(initial = emptyList())
                val voiceState by voiceManager.state.collectAsState()
                val rmsLevel by voiceManager.rmsLevel.collectAsState()
                val (settings, suggestions) = pair

                MyApplicationTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        HybridKeyboardView(
                            settings = settings,
                            suggestions = suggestions,
                            clipboardEntries = clipboardEntries,
                            voiceState = voiceState,
                            rmsLevel = rmsLevel,
                            shiftState = shiftState,
                            onShiftStateChange = { shiftState = it },
                            showSettingsIcon = true,
                            actions = object : HybridKeyboardActions {
                                override fun onCommitText(text: String) {
                                    commitToInputConnection(text)
                                }

                                override fun onCommitWordReplacement(word: String) {
                                    replaceCurrentWordInIc(word)
                                }

                                override fun onBackspace(count: Int) {
                                    deleteFromIc(count)
                                }

                                override fun onEnter() {
                                    handleEnterAction()
                                }

                                override fun onMoveCursor(delta: Int) {
                                    moveCursorInIc(delta)
                                }

                                override fun onMicClick() {
                                    voiceManager.startListening(
                                        localeTag = currentSettings.language.speechLocale
                                    ) { spoken ->
                                        commitToInputConnection(spoken)
                                    }
                                }

                                override fun onStopMic() {
                                    voiceManager.stopListening()
                                }

                                override fun onDismissMicError() {
                                    voiceManager.clearError()
                                }

                                override fun onCycleLanguage() {
                                    serviceScope.launch {
                                        settingsRepository.updateSettings { cur ->
                                            val all = KeyboardLanguage.entries
                                            val next = all[(all.indexOf(cur.language) + 1) % all.size]
                                            cur.copy(language = next)
                                        }
                                    }
                                }

                                override fun onToggleOneHanded() {
                                    serviceScope.launch {
                                        settingsRepository.updateSettings { cur ->
                                            val all = OneHandedMode.entries
                                            val next = all[(all.indexOf(cur.oneHandedMode) + 1) % all.size]
                                            cur.copy(oneHandedMode = next)
                                        }
                                    }
                                }

                                override fun onToggleEcoMode() {
                                    serviceScope.launch {
                                        settingsRepository.updateSettings { cur ->
                                            cur.copy(ecoBatteryMode = !cur.ecoBatteryMode)
                                        }
                                    }
                                }

                                override fun onToggleClipboardPin(entry: ClipboardEntryEntity) {
                                    serviceScope.launch {
                                        dataRepository.togglePin(entry.id, entry.isPinned)
                                    }
                                }

                                override fun onDeleteClipboardEntry(id: Int) {
                                    serviceScope.launch {
                                        dataRepository.deleteClipboardEntry(id)
                                    }
                                }

                                override fun onOpenSettings() {
                                    val intent = Intent(this@HybridInputMethodService, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        refreshWordTracking()
        val ic = currentInputConnection
        val before = ic?.getTextBeforeCursor(2, 0)?.toString().orEmpty()
        shiftState = if (currentSettings.autoCapitalize &&
            (before.isEmpty() || before.endsWith(". ") || before.endsWith("! ") || before.endsWith("? ") || before.endsWith("\n"))
        ) {
            ShiftState.SINGLE
        } else {
            ShiftState.OFF
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        voiceManager.stopListening()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        voiceManager.stopListening()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun commitToInputConnection(text: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
        refreshWordTracking()
        if (currentSettings.autoCapitalize && (text.endsWith(". ") || text.endsWith("! ") || text.endsWith("? "))) {
            shiftState = ShiftState.SINGLE
        }
    }

    private fun replaceCurrentWordInIc(word: String) {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(48, 0)?.toString().orEmpty()
        val tokenLen = before.takeLastWhile { !it.isWhitespace() }.length
        if (tokenLen > 0) {
            ic.deleteSurroundingText(tokenLen, 0)
        }
        ic.commitText("$word ", 1)
        refreshWordTracking()
    }

    private fun deleteFromIc(count: Int) {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(count, 0)
        }
        refreshWordTracking()
    }

    private fun handleEnterAction() {
        val ic = currentInputConnection ?: return
        val imeOptions = currentInputEditorInfo?.imeOptions ?: 0
        val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
        if (actionId != EditorInfo.IME_ACTION_NONE &&
            actionId != EditorInfo.IME_ACTION_UNSPECIFIED &&
            (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0
        ) {
            ic.performEditorAction(actionId)
        } else {
            ic.commitText("\n", 1)
            if (currentSettings.autoCapitalize) shiftState = ShiftState.SINGLE
        }
        refreshWordTracking()
    }

    private fun moveCursorInIc(delta: Int) {
        val ic = currentInputConnection ?: return
        val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        repeat(kotlin.math.abs(delta).coerceAtMost(5)) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
        refreshWordTracking()
    }

    private fun refreshWordTracking() {
        val before = currentInputConnection?.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        val currentToken = before.takeLastWhile { !it.isWhitespace() }
        val prior = before.dropLast(currentToken.length).trimEnd().takeLastWhile { !it.isWhitespace() }
        currentWordFlow.value = currentToken
        previousWordFlow.value = prior
    }
}
