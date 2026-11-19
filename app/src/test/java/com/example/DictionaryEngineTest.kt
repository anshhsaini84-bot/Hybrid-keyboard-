package com.example

import com.example.data.CustomWordEntity
import com.example.data.KeyboardLanguage
import com.example.engine.DictionaryEngine
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
    fun suggest_expandsCustomShortcutAndHinglish() {
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
    }
}
