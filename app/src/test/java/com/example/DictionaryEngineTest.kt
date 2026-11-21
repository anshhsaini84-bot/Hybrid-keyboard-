package com.example

import com.example.data.CustomWordEntity
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.engine.ClipboardSyncManager
import com.example.engine.DictionaryEngine
import com.example.engine.KeyboardLayouts
import com.example.engine.QuickTranslateEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryEngineTest {

    @Test
    fun glideDecoder_recognizesHelloAndHybrid() {
        val hello = DictionaryEngine.decodeGlideSequence(
            visitedKeys = listOf("h", "e", "l", "l", "o"),
            language = KeyboardLanguage.ENGLISH_US
        )
        assertEquals("hello", hello)

        val hybrid = DictionaryEngine.decodeGlideSequence(
            visitedKeys = listOf("h", "y", "b", "r", "i", "d"),
            language = KeyboardLanguage.ENGLISH_US
        )
        assertEquals("hybrid", hybrid)
    }

    @Test
    fun suggest_expandsCustomShortcutAndAllLanguages() {
        val custom = listOf(CustomWordEntity(id = 1, shortcut = "omw", expansion = "On my way!"))
        val shortcutSuggestions = DictionaryEngine.suggest(
            currentWord = "omw",
            previousWord = "",
            language = KeyboardLanguage.ENGLISH_US,
            customWords = custom,
            ecoMode = false
        )
        assertEquals("On my way!", shortcutSuggestions.first())

        val hinglishSuggestions = DictionaryEngine.suggest(
            currentWord = "namaste",
            previousWord = "",
            language = KeyboardLanguage.HINGLISH,
            customWords = emptyList(),
            ecoMode = false
        )
        assertTrue(hinglishSuggestions.contains("नमस्ते"))

        // Verify all 11 languages return valid layouts and default suggestions
        assertEquals(11, KeyboardLanguage.entries.size)
        KeyboardLanguage.entries.forEach { lang ->
            val rows = KeyboardLayouts.rowsForAlpha(lang, isShifted = false)
            assertTrue("Expected 4 rows for $lang", rows.size == 4)
            val emptySuggestions = DictionaryEngine.suggest("", "", lang, emptyList(), ecoMode = false)
            assertTrue("Expected suggestions for $lang", emptySuggestions.isNotEmpty())
        }
    }

    @Test
    fun quickTranslateEngine_translatesMultiLanguagePhrases() {
        assertEquals(
            "शुभ प्रभात",
            QuickTranslateEngine.translate("Good morning", QuickTranslateEngine.TargetLanguage.HINDI)
        )
        assertEquals(
            "How are you doing?",
            QuickTranslateEngine.translate("kya haal hai", QuickTranslateEngine.TargetLanguage.ENGLISH)
        )
        assertEquals(
            "Buenos días",
            QuickTranslateEngine.translate("Good morning", QuickTranslateEngine.TargetLanguage.SPANISH)
        )
        assertEquals(
            "Merci beaucoup",
            QuickTranslateEngine.translate("Thank you", QuickTranslateEngine.TargetLanguage.FRENCH)
        )
        assertEquals(
            "صباح الخير",
            QuickTranslateEngine.translate("Good morning", QuickTranslateEngine.TargetLanguage.ARABIC)
        )
    }

    @Test
    fun themesAndLarge50kLineClipboardMetrics_workCleanly() {
        assertTrue("Expected at least 18 themes", HybridThemeStyle.entries.size >= 18)
        assertEquals(HybridThemeStyle.SYSTEM_AUTO, HybridThemeStyle.entries.first())

        val fiftyThousandLines = buildString(1_100_000) {
            for (i in 1..50_000) {
                append("val line$i = $i")
                if (i < 50_000) append('\n')
            }
        }
        assertEquals(50_000, ClipboardSyncManager.countLines(fiftyThousandLines))
        val preview = ClipboardSyncManager.buildPreview(fiftyThousandLines, 50_000)
        assertTrue(preview.length <= 240)
        assertTrue(preview.startsWith("val line1 = 1"))
    }

    @Test
    fun emojiSearch_findsExpectedEmoji() {
        val matches = KeyboardLayouts.searchEmojis("namaste")
        assertTrue(matches.contains("🙏"))
    }
}
