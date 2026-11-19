package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
        val SOUND_ON_KEY = booleanPreferencesKey("sound_on_key")
        val SOFT_MIC_VISIBLE = booleanPreferencesKey("soft_mic_visible")
        val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
        val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val HEIGHT_SCALE = floatPreferencesKey("height_scale")
        val ECO_BATTERY_MODE = booleanPreferencesKey("eco_battery_mode")
        val AUTO_ECO_LOW_BATTERY = booleanPreferencesKey("auto_eco_low_battery")
    }

    val settingsFlow: Flow<KeyboardSettings> = context.keyboardDataStore.data.map { prefs ->
        KeyboardSettings(
            themeStyle = prefs[Keys.THEME_STYLE]?.let {
                runCatching { HybridThemeStyle.valueOf(it) }.getOrNull()
            } ?: HybridThemeStyle.OLED_OBSIDIAN,
            showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: true,
            glideTypingEnabled = prefs[Keys.GLIDE_TYPING] ?: true,
            spacebarCursorEnabled = prefs[Keys.SPACEBAR_CURSOR] ?: true,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            soundOnKeypress = prefs[Keys.SOUND_ON_KEY] ?: false,
            softMicAlwaysVisible = prefs[Keys.SOFT_MIC_VISIBLE] ?: true,
            autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: true,
            oneHandedMode = prefs[Keys.ONE_HANDED_MODE]?.let {
                runCatching { OneHandedMode.valueOf(it) }.getOrNull()
            } ?: OneHandedMode.OFF,
            language = prefs[Keys.LANGUAGE]?.let {
                runCatching { KeyboardLanguage.valueOf(it) }.getOrNull()
            } ?: KeyboardLanguage.ENGLISH_US,
            heightScale = (prefs[Keys.HEIGHT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.15f),
            ecoBatteryMode = prefs[Keys.ECO_BATTERY_MODE] ?: false,
            autoEcoOnLowBattery = prefs[Keys.AUTO_ECO_LOW_BATTERY] ?: true
        )
    }

    suspend fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
        context.keyboardDataStore.edit { prefs ->
            val current = KeyboardSettings(
                themeStyle = prefs[Keys.THEME_STYLE]?.let {
                    runCatching { HybridThemeStyle.valueOf(it) }.getOrNull()
                } ?: HybridThemeStyle.OLED_OBSIDIAN,
                showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: true,
                glideTypingEnabled = prefs[Keys.GLIDE_TYPING] ?: true,
                spacebarCursorEnabled = prefs[Keys.SPACEBAR_CURSOR] ?: true,
                hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
                soundOnKeypress = prefs[Keys.SOUND_ON_KEY] ?: false,
                softMicAlwaysVisible = prefs[Keys.SOFT_MIC_VISIBLE] ?: true,
                autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: true,
                oneHandedMode = prefs[Keys.ONE_HANDED_MODE]?.let {
                    runCatching { OneHandedMode.valueOf(it) }.getOrNull()
                } ?: OneHandedMode.OFF,
                language = prefs[Keys.LANGUAGE]?.let {
                    runCatching { KeyboardLanguage.valueOf(it) }.getOrNull()
                } ?: KeyboardLanguage.ENGLISH_US,
                heightScale = (prefs[Keys.HEIGHT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.15f),
                ecoBatteryMode = prefs[Keys.ECO_BATTERY_MODE] ?: false,
                autoEcoOnLowBattery = prefs[Keys.AUTO_ECO_LOW_BATTERY] ?: true
            )
            val updated = transform(current)
            prefs[Keys.THEME_STYLE] = updated.themeStyle.name
            prefs[Keys.SHOW_NUMBER_ROW] = updated.showNumberRow
            prefs[Keys.GLIDE_TYPING] = updated.glideTypingEnabled
            prefs[Keys.SPACEBAR_CURSOR] = updated.spacebarCursorEnabled
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedbackEnabled
            prefs[Keys.SOUND_ON_KEY] = updated.soundOnKeypress
            prefs[Keys.SOFT_MIC_VISIBLE] = updated.softMicAlwaysVisible
            prefs[Keys.AUTO_CAPITALIZE] = updated.autoCapitalize
            prefs[Keys.ONE_HANDED_MODE] = updated.oneHandedMode.name
            prefs[Keys.LANGUAGE] = updated.language.name
            prefs[Keys.HEIGHT_SCALE] = updated.heightScale
            prefs[Keys.ECO_BATTERY_MODE] = updated.ecoBatteryMode
            prefs[Keys.AUTO_ECO_LOW_BATTERY] = updated.autoEcoOnLowBattery
        }
    }
}
