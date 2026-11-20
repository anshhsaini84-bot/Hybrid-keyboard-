package com.example.ui.keyboard

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.data.ClipboardEntryEntity
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardPanel
import com.example.data.KeyboardSettings
import com.example.data.OneHandedMode
import com.example.data.ShiftState
import com.example.data.TextEditCommand
import com.example.engine.DictionaryEngine
import com.example.engine.KeySpec
import com.example.engine.KeyType
import com.example.engine.KeyboardLayouts
import com.example.engine.VoiceDictationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

interface HybridKeyboardActions {
    fun onCommitText(text: String)
    fun onCommitWordReplacement(word: String)
    fun onCommitImageEntry(entry: ClipboardEntryEntity)
    fun onBackspace(count: Int = 1)
    fun onDeletePreviousWord()
    fun onDoubleSpacePeriod()
    fun onEnter()
    fun onMoveCursor(delta: Int)
    fun onTextEditCommand(command: TextEditCommand)
    fun onSyncSystemClipboard()
    fun onMicClick()
    fun onStopMic()
    fun onDismissMicError()
    fun onCycleLanguage()
    fun onToggleOneHanded()
    fun onToggleFloatingMode()
    fun onToggleEcoMode()
    fun onToggleClipboardPin(entry: ClipboardEntryEntity)
    fun onDeleteClipboardEntry(id: Int)
    fun onOpenSettings()
}

@Composable
fun HybridKeyboardView(
    settings: KeyboardSettings,
    suggestions: List<String>,
    clipboardEntries: List<ClipboardEntryEntity>,
    voiceState: VoiceDictationState,
    rmsLevel: Float,
    shiftState: ShiftState,
    onShiftStateChange: (ShiftState) -> Unit,
    actions: HybridKeyboardActions,
    modifier: Modifier = Modifier,
    showSettingsIcon: Boolean = true
) {
    val context = LocalContext.current
    val theme = settings.effectiveTheme
    var activePanel by remember { mutableStateOf(KeyboardPanel.ALPHA) }
    var lastShiftTapMs by remember { mutableLongStateOf(0L) }
    var lastSpaceTapMs by remember { mutableLongStateOf(0L) }
    var pressedKeyPreview by remember { mutableStateOf<Pair<KeySpec, Offset>?>(null) }
    var alternatePickerKey by remember { mutableStateOf<KeySpec?>(null) }

    // Glide typing state
    val glidePoints = remember { mutableStateListOf<Offset>() }
    val glidedKeyLabels = remember { mutableStateListOf<String>() }
    val keyBounds = remember { mutableStateMapOf<String, Rect>() }
    var isGliding by remember { mutableStateOf(false) }

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    fun triggerFeedback(isSpecial: Boolean = false) {
        if (settings.hapticFeedbackEnabled && vibrator?.hasVibrator() == true) {
            val ms = if (settings.ecoBatteryMode) 8L else if (isSpecial) 18L else 12L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        }
        if (settings.soundOnKeypress && !settings.ecoBatteryMode) {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.35f)
        }
    }

    fun handleKeyPress(key: KeySpec) {
        triggerFeedback(key.type != KeyType.CHARACTER)
        when (key.type) {
            KeyType.CHARACTER -> {
                lastSpaceTapMs = 0L
                val out = if (shiftState != ShiftState.OFF && settings.language != KeyboardLanguage.HINDI_DEVANAGARI) {
                    key.primary.uppercase()
                } else {
                    key.primary
                }
                actions.onCommitText(out)
                if (shiftState == ShiftState.SINGLE) {
                    onShiftStateChange(ShiftState.OFF)
                }
            }
            KeyType.COMMA, KeyType.PERIOD -> {
                lastSpaceTapMs = 0L
                actions.onCommitText(key.primary)
            }
            KeyType.SPACE -> {
                val now = System.currentTimeMillis()
                if (settings.doubleSpacePeriod && (now - lastSpaceTapMs) in 40L..380L) {
                    lastSpaceTapMs = 0L
                    actions.onDoubleSpacePeriod()
                    if (settings.autoCapitalize) {
                        onShiftStateChange(ShiftState.SINGLE)
                    }
                } else {
                    lastSpaceTapMs = now
                    actions.onCommitText(" ")
                }
            }
            KeyType.BACKSPACE -> {
                lastSpaceTapMs = 0L
                actions.onBackspace(1)
            }
            KeyType.ENTER -> {
                lastSpaceTapMs = 0L
                actions.onEnter()
            }
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                val next = when {
                    shiftState == ShiftState.CAPS_LOCK -> ShiftState.OFF
                    shiftState == ShiftState.SINGLE && (now - lastShiftTapMs) < 350L -> ShiftState.CAPS_LOCK
                    shiftState == ShiftState.SINGLE -> ShiftState.OFF
                    else -> ShiftState.SINGLE
                }
                lastShiftTapMs = now
                onShiftStateChange(next)
            }
            KeyType.SYMBOLS_SWITCH -> {
                activePanel = if (activePanel == KeyboardPanel.ALPHA) {
                    KeyboardPanel.SYMBOLS_1
                } else {
                    KeyboardPanel.ALPHA
                }
            }
            KeyType.SYMBOLS_PAGE_SWITCH -> {
                activePanel = if (activePanel == KeyboardPanel.SYMBOLS_1) {
                    KeyboardPanel.SYMBOLS_2
                } else {
                    KeyboardPanel.SYMBOLS_1
                }
            }
            KeyType.EMOJI_TRIGGER -> {
                activePanel = if (activePanel == KeyboardPanel.EMOJI) {
                    KeyboardPanel.ALPHA
                } else {
                    KeyboardPanel.EMOJI
                }
            }
        }
    }

    val containerModifier = if (settings.floatingMode) {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, theme.accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .background(theme.backgroundColor)
            .padding(bottom = 6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .background(theme.backgroundColor)
            .padding(bottom = 4.dp)
    }

    Column(modifier = containerModifier) {
        if (settings.floatingMode) {
            // Samsung/Gboard Floating Keyboard Drag Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.utilityBarColor)
                    .padding(top = 6.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(theme.hintTextColor.copy(alpha = 0.6f))
                )
            }
        }

        KeyboardTopStrip(
            theme = theme,
            settings = settings,
            activePanel = activePanel,
            suggestions = suggestions,
            showSuggestionsMode = activePanel == KeyboardPanel.ALPHA,
            voiceState = voiceState,
            hasScreenshotInClipboard = clipboardEntries.any { it.isImage },
            onSuggestionClick = { suggestion ->
                triggerFeedback(true)
                actions.onCommitWordReplacement(suggestion)
            },
            onPanelToggle = { panel ->
                triggerFeedback(true)
                if (panel == KeyboardPanel.CLIPBOARD) {
                    actions.onSyncSystemClipboard()
                }
                activePanel = panel
            },
            onMicClick = {
                triggerFeedback(true)
                actions.onMicClick()
            },
            onCycleLanguage = {
                triggerFeedback(true)
                actions.onCycleLanguage()
            },
            onToggleOneHanded = {
                triggerFeedback(true)
                actions.onToggleOneHanded()
            },
            onToggleFloating = {
                triggerFeedback(true)
                actions.onToggleFloatingMode()
            },
            onToggleEcoMode = {
                triggerFeedback(true)
                actions.onToggleEcoMode()
            },
            onOpenSettings = if (showSettingsIcon) ({ actions.onOpenSettings() }) else null
        )

        VoiceSoftMicBanner(
            theme = theme,
            voiceState = voiceState,
            rmsLevel = rmsLevel,
            language = settings.language,
            ecoMode = settings.ecoBatteryMode,
            onStopMic = actions::onStopMic,
            onDismissError = actions::onDismissMicError
        )

        // Long-press alternate key chooser bar
        if (alternatePickerKey != null && alternatePickerKey!!.alternates.isNotEmpty()) {
            val alts = alternatePickerKey!!.alternates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.specialKeyColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                alts.forEach { ch ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.keyColor,
                        modifier = Modifier
                            .size(width = 36.dp, height = 34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, theme.accentColor, RoundedCornerShape(8.dp))
                            .clickable {
                                triggerFeedback()
                                actions.onCommitText(ch)
                                alternatePickerKey = null
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ch,
                                color = theme.keyTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        when (activePanel) {
            KeyboardPanel.EMOJI -> {
                EmojiKeyboardPanel(
                    theme = theme,
                    onEmojiClick = { emoji ->
                        triggerFeedback()
                        actions.onCommitText(emoji)
                    },
                    onBackspace = {
                        triggerFeedback(true)
                        actions.onBackspace(1)
                    },
                    onBackToAlpha = { activePanel = KeyboardPanel.ALPHA }
                )
            }
            KeyboardPanel.CLIPBOARD -> {
                ClipboardKeyboardPanel(
                    theme = theme,
                    entries = clipboardEntries,
                    onPasteTextEntry = { text ->
                        triggerFeedback(true)
                        actions.onCommitText(text)
                        activePanel = KeyboardPanel.ALPHA
                    },
                    onPasteImageEntry = { entry ->
                        triggerFeedback(true)
                        actions.onCommitImageEntry(entry)
                        activePanel = KeyboardPanel.ALPHA
                    },
                    onSyncSystemClipboard = {
                        triggerFeedback(true)
                        actions.onSyncSystemClipboard()
                    },
                    onTogglePin = actions::onToggleClipboardPin,
                    onDeleteEntry = actions::onDeleteClipboardEntry,
                    onBackToAlpha = { activePanel = KeyboardPanel.ALPHA }
                )
            }
            KeyboardPanel.TEXT_EDIT -> {
                TextEditKeyboardPanel(
                    theme = theme,
                    onCommand = { cmd ->
                        triggerFeedback(true)
                        actions.onTextEditCommand(cmd)
                    },
                    onBackToAlpha = { activePanel = KeyboardPanel.ALPHA }
                )
            }
            KeyboardPanel.TRANSLATE -> {
                TranslateKeyboardPanel(
                    theme = theme,
                    onInsertTranslation = { translated ->
                        triggerFeedback(true)
                        actions.onCommitText(translated)
                        activePanel = KeyboardPanel.ALPHA
                    },
                    onBackToAlpha = { activePanel = KeyboardPanel.ALPHA }
                )
            }
            KeyboardPanel.ALPHA, KeyboardPanel.SYMBOLS_1, KeyboardPanel.SYMBOLS_2 -> {
                val scale = if (settings.floatingMode) settings.heightScale * 0.90f else settings.heightScale
                val baseKeyHeight = (46f * scale).dp
                val rows = when (activePanel) {
                    KeyboardPanel.ALPHA -> KeyboardLayouts.rowsForAlpha(
                        language = settings.language,
                        isHindiShifted = shiftState != ShiftState.OFF
                    )
                    KeyboardPanel.SYMBOLS_1 -> KeyboardLayouts.symbolsPage1
                    KeyboardPanel.SYMBOLS_2 -> KeyboardLayouts.symbolsPage2
                    else -> KeyboardLayouts.qwertyRows
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.oneHandedMode == OneHandedMode.RIGHT) {
                        OneHandedSideRail(
                            theme = theme,
                            isLeftDock = true,
                            onExpand = actions::onToggleOneHanded,
                            onSwitchSide = actions::onToggleOneHanded
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .then(
                                if (settings.glideTypingEnabled && activePanel == KeyboardPanel.ALPHA &&
                                    settings.language != KeyboardLanguage.HINDI_DEVANAGARI
                                ) {
                                    Modifier.pointerInput(settings.language, shiftState, settings.ecoBatteryMode) {
                                        detectDragGestures(
                                            onDragStart = { startOffset ->
                                                val hit = keyBounds.entries.firstOrNull {
                                                    it.value.contains(startOffset) && it.key.length == 1
                                                }
                                                if (hit != null) {
                                                    isGliding = true
                                                    glidePoints.clear()
                                                    glidedKeyLabels.clear()
                                                    glidePoints.add(startOffset)
                                                    glidedKeyLabels.add(hit.key)
                                                }
                                            },
                                            onDrag = { change, _ ->
                                                if (isGliding) {
                                                    val pos = change.position
                                                    glidePoints.add(pos)
                                                    if (glidePoints.size > 48) {
                                                        glidePoints.removeAt(0)
                                                    }
                                                    val hit = keyBounds.entries.firstOrNull {
                                                        it.value.contains(pos) && it.key.length == 1
                                                    }
                                                    if (hit != null && glidedKeyLabels.lastOrNull() != hit.key) {
                                                        glidedKeyLabels.add(hit.key)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                if (isGliding && glidedKeyLabels.size >= 2) {
                                                    val decoded = DictionaryEngine.decodeGlideSequence(
                                                        visitedKeys = glidedKeyLabels.toList(),
                                                        language = settings.language
                                                    )
                                                    if (decoded != null) {
                                                        triggerFeedback(true)
                                                        val formatted = if (shiftState != ShiftState.OFF) {
                                                            decoded.replaceFirstChar { it.uppercase() }
                                                        } else {
                                                            decoded
                                                        }
                                                        actions.onCommitText("$formatted ")
                                                        if (shiftState == ShiftState.SINGLE) {
                                                            onShiftStateChange(ShiftState.OFF)
                                                        }
                                                    }
                                                }
                                                isGliding = false
                                                glidePoints.clear()
                                                glidedKeyLabels.clear()
                                            },
                                            onDragCancel = {
                                                isGliding = false
                                                glidePoints.clear()
                                                glidedKeyLabels.clear()
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Dedicated Samsung Number Row
                            if (settings.showNumberRow && activePanel == KeyboardPanel.ALPHA) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    KeyboardLayouts.numberRow.forEach { numKey ->
                                        KeyboardKeyCap(
                                            keySpec = numKey,
                                            theme = theme,
                                            shiftState = ShiftState.OFF,
                                            language = settings.language,
                                            ecoMode = settings.ecoBatteryMode,
                                            spacebarCursorEnabled = false,
                                            swipeBackspaceWordEnabled = false,
                                            modifier = Modifier
                                                .weight(numKey.weight)
                                                .height((baseKeyHeight.value * 0.84f).dp),
                                            onTap = {
                                                alternatePickerKey = null
                                                handleKeyPress(numKey)
                                            },
                                            onLongPress = {
                                                if (numKey.alternates.isNotEmpty()) {
                                                    triggerFeedback(true)
                                                    alternatePickerKey = numKey
                                                }
                                            },
                                            onBackspaceRepeat = {},
                                            onSwipeDeleteWord = {},
                                            onSpaceCursorMove = {},
                                            onPressPreviewChanged = { offset ->
                                                pressedKeyPreview = offset?.let { numKey to it }
                                            },
                                            onBoundsMeasured = {}
                                        )
                                    }
                                }
                            }

                            rows.forEachIndexed { rowIdx, rowKeys ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = if (rowIdx == 1 && rowKeys.size == 9) 14.dp else 0.dp
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowKeys.forEach { key ->
                                        KeyboardKeyCap(
                                            keySpec = key,
                                            theme = theme,
                                            shiftState = shiftState,
                                            language = settings.language,
                                            ecoMode = settings.ecoBatteryMode,
                                            spacebarCursorEnabled = settings.spacebarCursorEnabled,
                                            swipeBackspaceWordEnabled = settings.swipeBackspaceDeleteWord,
                                            modifier = Modifier
                                                .weight(key.weight)
                                                .height(baseKeyHeight),
                                            onTap = {
                                                alternatePickerKey = null
                                                handleKeyPress(key)
                                            },
                                            onLongPress = {
                                                if (key.alternates.isNotEmpty()) {
                                                    triggerFeedback(true)
                                                    alternatePickerKey = key
                                                } else if (key.type == KeyType.EMOJI_TRIGGER) {
                                                    triggerFeedback(true)
                                                    actions.onMicClick()
                                                }
                                            },
                                            onBackspaceRepeat = {
                                                triggerFeedback(false)
                                                actions.onBackspace(1)
                                            },
                                            onSwipeDeleteWord = {
                                                triggerFeedback(true)
                                                actions.onDeletePreviousWord()
                                            },
                                            onSpaceCursorMove = { delta ->
                                                actions.onMoveCursor(delta)
                                            },
                                            onPressPreviewChanged = { offset ->
                                                pressedKeyPreview = if (offset != null && key.type == KeyType.CHARACTER) {
                                                    key to offset
                                                } else {
                                                    null
                                                }
                                            },
                                            onBoundsMeasured = { rect ->
                                                if (key.type == KeyType.CHARACTER && key.primary.length == 1) {
                                                    keyBounds[key.primary] = rect
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Glowing Glide Typing Trail Canvas
                        if (isGliding && glidePoints.size >= 2) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val path = Path().apply {
                                    moveTo(glidePoints.first().x, glidePoints.first().y)
                                    for (i in 1 until glidePoints.size) {
                                        lineTo(glidePoints[i].x, glidePoints[i].y)
                                    }
                                }
                                val strokeWidth = if (settings.ecoBatteryMode) 4f else 10f
                                drawPath(
                                    path = path,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            theme.accentColor.copy(alpha = 0.35f),
                                            theme.accentColor
                                        )
                                    ),
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Keypress popup bubble (hidden in Eco Mode for lowest draw cost)
                        val preview = pressedKeyPreview
                        if (preview != null && !settings.ecoBatteryMode && !isGliding) {
                            val (key, pos) = preview
                            val label = if (shiftState != ShiftState.OFF && settings.language != KeyboardLanguage.HINDI_DEVANAGARI) {
                                key.primary.uppercase()
                            } else {
                                key.primary
                            }
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(
                                    x = (pos.x - 18f).roundToInt(),
                                    y = (pos.y - 140f).roundToInt()
                                )
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.accentColor,
                                    shadowElevation = 6.dp,
                                    modifier = Modifier.size(width = 46.dp, height = 54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            color = Color.Black,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (settings.oneHandedMode == OneHandedMode.LEFT) {
                        OneHandedSideRail(
                            theme = theme,
                            isLeftDock = false,
                            onExpand = actions::onToggleOneHanded,
                            onSwitchSide = actions::onToggleOneHanded
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OneHandedSideRail(
    theme: HybridThemeStyle,
    isLeftDock: Boolean,
    onExpand: () -> Unit,
    onSwitchSide: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(44.dp)
            .height(210.dp)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(theme.specialKeyColor)
                .clickable(onClick = onExpand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
                contentDescription = "Expand Keyboard",
                tint = theme.accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(theme.specialKeyColor)
                .clickable(onClick = onSwitchSide),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLeftDock) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                contentDescription = "Switch One-Handed Side",
                tint = theme.keyTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun KeyboardKeyCap(
    keySpec: KeySpec,
    theme: HybridThemeStyle,
    shiftState: ShiftState,
    language: KeyboardLanguage,
    ecoMode: Boolean,
    spacebarCursorEnabled: Boolean,
    swipeBackspaceWordEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onBackspaceRepeat: () -> Unit,
    onSwipeDeleteWord: () -> Unit,
    onSpaceCursorMove: (Int) -> Unit,
    onPressPreviewChanged: (Offset?) -> Unit,
    onBoundsMeasured: (Rect) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var windowOrigin by remember { mutableStateOf(Offset.Zero) }
    var spaceDragAccum by remember { mutableFloatStateOf(0f) }
    var backspaceDragAccum by remember { mutableFloatStateOf(0f) }

    val isAction = keySpec.type == KeyType.ENTER
    val isSpecial = keySpec.type != KeyType.CHARACTER && keySpec.type != KeyType.SPACE &&
        keySpec.type != KeyType.COMMA && keySpec.type != KeyType.PERIOD

    val bgColor = when {
        isAction -> theme.actionKeyColor
        isPressed -> theme.accentColor.copy(alpha = 0.28f)
        keySpec.type == KeyType.SHIFT && shiftState != ShiftState.OFF -> theme.accentColor.copy(alpha = 0.25f)
        isSpecial -> theme.specialKeyColor
        else -> theme.keyColor
    }
    val fgColor = when {
        isAction -> if (theme == HybridThemeStyle.SAMSUNG_ONE_LIGHT) Color.White else Color(0xFF061018)
        keySpec.type == KeyType.SHIFT && shiftState != ShiftState.OFF -> theme.accentColor
        else -> theme.keyTextColor
    }

    val shape = RoundedCornerShape(7.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (ecoMode) 0.5.dp else 0.8.dp,
                color = if (isAction) theme.actionKeyColor else theme.keyBorderColor,
                shape = shape
            )
            .onGloballyPositioned { coords ->
                onBoundsMeasured(coords.boundsInParent())
                windowOrigin = coords.positionInWindow()
            }
            .then(
                when {
                    keySpec.type == KeyType.SPACE && spacebarCursorEnabled -> {
                        Modifier.pointerInput(spacebarCursorEnabled) {
                            detectDragGestures(
                                onDragStart = { spaceDragAccum = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    spaceDragAccum += dragAmount.x
                                    val stepPx = 22f
                                    if (abs(spaceDragAccum) >= stepPx) {
                                        val steps = (spaceDragAccum / stepPx).toInt()
                                        if (steps != 0) {
                                            onSpaceCursorMove(steps)
                                            spaceDragAccum -= steps * stepPx
                                        }
                                    }
                                }
                            )
                        }
                    }
                    keySpec.type == KeyType.BACKSPACE && swipeBackspaceWordEnabled -> {
                        Modifier.pointerInput(swipeBackspaceWordEnabled) {
                            detectDragGestures(
                                onDragStart = { backspaceDragAccum = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    backspaceDragAccum += dragAmount.x
                                    if (backspaceDragAccum <= -48f) {
                                        onSwipeDeleteWord()
                                        backspaceDragAccum = 0f
                                    }
                                }
                            )
                        }
                    }
                    else -> Modifier
                }
            )
            .pointerInput(keySpec.id, shiftState) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressPreviewChanged(windowOrigin)
                        val repeatJob = if (keySpec.type == KeyType.BACKSPACE) {
                            scope.launch {
                                delay(380)
                                while (isPressed) {
                                    onBackspaceRepeat()
                                    delay(65)
                                }
                            }
                        } else null
                        tryAwaitRelease()
                        repeatJob?.cancel()
                        isPressed = false
                        onPressPreviewChanged(null)
                    },
                    onLongPress = {
                        onLongPress()
                    },
                    onTap = {
                        onTap()
                    }
                )
            }
            .testTag("key_${keySpec.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Samsung-style top-right secondary glyph hint
        if (keySpec.secondary != null && keySpec.type == KeyType.CHARACTER) {
            Text(
                text = keySpec.secondary,
                color = theme.hintTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 2.dp)
            )
        }

        when (keySpec.type) {
            KeyType.SHIFT -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Shift",
                        tint = fgColor,
                        modifier = Modifier.size(19.dp)
                    )
                    if (shiftState == ShiftState.CAPS_LOCK) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(theme.accentColor)
                        )
                    }
                }
            }
            KeyType.BACKSPACE -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = fgColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            KeyType.ENTER -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = fgColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            KeyType.EMOJI_TRIGGER -> {
                Icon(
                    imageVector = Icons.Rounded.EmojiEmotions,
                    contentDescription = "Emoji",
                    tint = fgColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            KeyType.SPACE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SpaceBar,
                        contentDescription = null,
                        tint = theme.hintTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Hybrid · ${language.code}",
                        color = theme.hintTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                val displayText = if (shiftState != ShiftState.OFF &&
                    keySpec.type == KeyType.CHARACTER &&
                    language != KeyboardLanguage.HINDI_DEVANAGARI
                ) {
                    keySpec.primary.uppercase()
                } else {
                    keySpec.primary
                }
                Text(
                    text = displayText,
                    color = fgColor,
                    fontSize = if (keySpec.type == KeyType.SYMBOLS_SWITCH || keySpec.type == KeyType.SYMBOLS_PAGE_SWITCH) 12.sp else 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
