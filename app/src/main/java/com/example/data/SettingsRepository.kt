package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.keyboardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hybrid_keyboard_prefs"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_STYLE = stringPreferencesKey("theme_style")
        val SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val GLIDE_TYPING = booleanPreferencesKey("glide_typing")
        val SPACEBAR_CURSOR = booleanPreferencesKey("spacebar_cursor")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val HAPTIC_STRENGTH_MS = intPreferencesKey("haptic_strength_ms")
        val SOUND_ON_KEY = booleanPreferencesKey("sound_on_key")
        val SOFT_MIC_VISIBLE = booleanPreferencesKey("soft_mic_visible")
        val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
        val DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
        val SWIPE_BACKSPACE_WORD = booleanPreferencesKey("swipe_backspace_word")
        val FLOATING_MODE = booleanPreferencesKey("floating_mode")
        val AUTO_CAPTURE_CLIPBOARD = booleanPreferencesKey("auto_capture_clipboard")
        val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val HEIGHT_SCALE = floatPreferencesKey("height_scale")
        val PINNED_TOOLBAR_TOOLS = stringPreferencesKey("pinned_toolbar_tools")
        val ECO_BATTERY_MODE = booleanPreferencesKey("eco_battery_mode")
        val AUTO_ECO_LOW_BATTERY = booleanPreferencesKey("auto_eco_low_battery")
    }

    private fun readFromPreferences(prefs: Preferences): KeyboardSettings {
        val parsedTools = prefs[Keys.PINNED_TOOLBAR_TOOLS]
            ?.split(",")
            ?.mapNotNull { name -> runCatching { ToolbarTool.valueOf(name) }.getOrNull() }
            ?.distinct()
            ?.take(5)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_PINNED_TOOLS

        return KeyboardSettings(
            themeStyle = prefs[Keys.THEME_STYLE]?.let {
                runCatching { HybridThemeStyle.valueOf(it) }.getOrNull()
            } ?: HybridThemeStyle.SYSTEM_AUTO,
            showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: true,
            glideTypingEnabled = prefs[Keys.GLIDE_TYPING] ?: true,
            spacebarCursorEnabled = prefs[Keys.SPACEBAR_CURSOR] ?: true,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            hapticStrengthMs = (prefs[Keys.HAPTIC_STRENGTH_MS] ?: 12).coerceIn(5, 30),
            soundOnKeypress = prefs[Keys.SOUND_ON_KEY] ?: false,
            softMicAlwaysVisible = prefs[Keys.SOFT_MIC_VISIBLE] ?: true,
            autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: true,
            doubleSpacePeriod = prefs[Keys.DOUBLE_SPACE_PERIOD] ?: true,
            swipeBackspaceDeleteWord = prefs[Keys.SWIPE_BACKSPACE_WORD] ?: true,
            floatingMode = prefs[Keys.FLOATING_MODE] ?: false,
            autoCaptureClipboard = prefs[Keys.AUTO_CAPTURE_CLIPBOARD] ?: true,
            oneHandedMode = prefs[Keys.ONE_HANDED_MODE]?.let {
                runCatching { OneHandedMode.valueOf(it) }.getOrNull()
            } ?: OneHandedMode.OFF,
            language = prefs[Keys.LANGUAGE]?.let {
                runCatching { KeyboardLanguage.valueOf(it) }.getOrNull()
            } ?: KeyboardLanguage.ENGLISH_US,
            heightScale = (prefs[Keys.HEIGHT_SCALE] ?: 1.0f).coerceIn(0.72f, 1.35f),
            pinnedToolbarTools = parsedTools,
            ecoBatteryMode = prefs[Keys.ECO_BATTERY_MODE] ?: false,
            autoEcoOnLowBattery = prefs[Keys.AUTO_ECO_LOW_BATTERY] ?: true
        )
    }

    val settingsFlow: Flow<KeyboardSettings> = context.keyboardDataStore.data.map(::readFromPreferences)

    suspend fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
        context.keyboardDataStore.edit { prefs ->
            val updated = transform(readFromPreferences(prefs))
            prefs[Keys.THEME_STYLE] = updated.themeStyle.name
            prefs[Keys.SHOW_NUMBER_ROW] = updated.showNumberRow
            prefs[Keys.GLIDE_TYPING] = updated.glideTypingEnabled
            prefs[Keys.SPACEBAR_CURSOR] = updated.spacebarCursorEnabled
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedbackEnabled
            prefs[Keys.HAPTIC_STRENGTH_MS] = updated.hapticStrengthMs.coerceIn(5, 30)
            prefs[Keys.SOUND_ON_KEY] = updated.soundOnKeypress
            prefs[Keys.SOFT_MIC_VISIBLE] = updated.softMicAlwaysVisible
            prefs[Keys.AUTO_CAPITALIZE] = updated.autoCapitalize
            prefs[Keys.DOUBLE_SPACE_PERIOD] = updated.doubleSpacePeriod
            prefs[Keys.SWIPE_BACKSPACE_WORD] = updated.swipeBackspaceDeleteWord
            prefs[Keys.FLOATING_MODE] = updated.floatingMode
            prefs[Keys.AUTO_CAPTURE_CLIPBOARD] = updated.autoCaptureClipboard
            prefs[Keys.ONE_HANDED_MODE] = updated.oneHandedMode.name
            prefs[Keys.LANGUAGE] = updated.language.name
            prefs[Keys.HEIGHT_SCALE] = updated.heightScale.coerceIn(0.72f, 1.35f)
            prefs[Keys.PINNED_TOOLBAR_TOOLS] = updated.pinnedToolbarTools.take(5).joinToString(",") { it.name }
            prefs[Keys.ECO_BATTERY_MODE] = updated.ecoBatteryMode
            prefs[Keys.AUTO_ECO_LOW_BATTERY] = updated.autoEcoOnLowBattery
        }
    }
}
