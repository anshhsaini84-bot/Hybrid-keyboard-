package com.example.engine

/**
 * Instant, zero-battery offline translator & Hinglish <-> Hindi Devanagari converter
 * built directly into the Hybrid Keyboard Translate panel.
 */
object QuickTranslateEngine {

    enum class Direction(val label: String) {
        EN_OR_HINGLISH_TO_HINDI("Eng / Hinglish → हिन्दी"),
        HINGLISH_TO_ENGLISH("Hinglish → English")
    }

    private val toHindiDictionary = mapOf(
        "hello" to "नमस्ते",
        "hi" to "नमस्ते",
        "namaste" to "नमस्ते",
        "good" to "अच्छा",
        "morning" to "सुप्रभात",
        "evening" to "शुभ संध्या",
        "night" to "शुभ रात्रि",
        "how" to "कैसे",
        "are" to "हैं",
        "you" to "आप",
        "i" to "मैं",
        "am" to "हूँ",
        "fine" to "ठीक",
        "thanks" to "धन्यवाद",
        "thank" to "धन्यवाद",
        "please" to "कृपया",
        "sorry" to "माफ़ कीजिए",
        "yes" to "हाँ",
        "no" to "नहीं",
        "okay" to "ठीक है",
        "ok" to "ठीक है",
        "friend" to "दोस्त",
        "brother" to "भाई",
        "bhai" to "भाई",
        "yaar" to "यार",
        "kya" to "क्या",
        "haal" to "हाल",
        "hai" to "है",
        "hain" to "हैं",
        "hoon" to "हूँ",
        "main" to "मैं",
        "tum" to "तुम",
        "aap" to "आप",
        "kaise" to "कैसे",
        "kaisa" to "कैसा",
        "theek" to "ठीक",
        "badhiya" to "बढ़िया",
        "badiya" to "बढ़िया",
        "accha" to "अच्छा",
        "achha" to "अच्छा",
        "bahut" to "बहुत",
        "mast" to "मस्त",
        "sahi" to "सही",
        "bilkul" to "बिल्कुल",
        "chalo" to "चलो",
        "milte" to "मिलते",
        "kal" to "कल",
        "aaj" to "आज",
        "abhi" to "अभी",
        "ghar" to "घर",
        "kaam" to "काम",
        "love" to "प्यार",
        "pyaar" to "प्यार",
        "welcome" to "स्वागत है",
        "shukriya" to "शुक्रिया",
        "dhanyawad" to "धन्यवाद",
        "keyboard" to "कीबोर्ड",
        "voice" to "आवाज़",
        "battery" to "बैटरी",
        "fast" to "तेज़",
        "smooth" to "स्मूथ",
        "screenshot" to "स्क्रीनशॉट",
        "copy" to "कॉपी",
        "paste" to "पेस्ट"
    )

    private val hinglishToEnglishMap = mapOf(
        "namaste" to "Hello",
        "kaise" to "how",
        "kaisa" to "how",
        "ho" to "are you",
        "kya" to "what",
        "haal" to "news",
        "hai" to "is",
        "hain" to "are",
        "hoon" to "am",
        "main" to "I",
        "mera" to "my",
        "meri" to "my",
        "tum" to "you",
        "aap" to "you",
        "bhai" to "brother",
        "yaar" to "friend",
        "dost" to "friend",
        "theek" to "fine",
        "badhiya" to "great",
        "badiya" to "great",
        "accha" to "good",
        "achha" to "good",
        "bahut" to "very",
        "mast" to "awesome",
        "sahi" to "right",
        "bilkul" to "definitely",
        "haan" to "yes",
        "nahi" to "no",
        "chalo" to "let's go",
        "kal" to "tomorrow",
        "aaj" to "today",
        "abhi" to "right now",
        "ghar" to "home",
        "kaam" to "work",
        "dhanyawad" to "thank you",
        "shukriya" to "thanks",
        "milte" to "see you"
    )

    private val phraseOverridesToHindi = mapOf(
        "good morning" to "शुभ प्रभात",
        "good night" to "शुभ रात्रि",
        "how are you" to "आप कैसे हैं?",
        "thank you" to "धन्यवाद",
        "kya haal hai" to "क्या हाल है?",
        "kaise ho" to "कैसे हो?",
        "bahut badhiya" to "बहुत बढ़िया",
        "on my way" to "मैं रास्ते में हूँ"
    )

    private val phraseOverridesToEnglish = mapOf(
        "kya haal hai" to "How are you doing?",
        "kaise ho" to "How are you?",
        "bahut badhiya" to "Really awesome",
        "main theek hoon" to "I am doing well",
        "kal milte hain" to "See you tomorrow",
        "abhi aa raha hoon" to "Coming right now"
    )

    fun translate(input: String, direction: Direction): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val lower = trimmed.lowercase()

        when (direction) {
            Direction.EN_OR_HINGLISH_TO_HINDI -> {
                phraseOverridesToHindi[lower]?.let { return it }
                return trimmed.split(Regex("\\s+")).joinToString(" ") { raw ->
                    val clean = raw.lowercase().trimEnd('.', ',', '!', '?')
                    val punct = raw.substring(clean.length)
                    val mapped = toHindiDictionary[clean] ?: raw
                    "$mapped$punct"
                }
            }
            Direction.HINGLISH_TO_ENGLISH -> {
                phraseOverridesToEnglish[lower]?.let { return it }
                val converted = trimmed.split(Regex("\\s+")).joinToString(" ") { raw ->
                    val clean = raw.lowercase().trimEnd('.', ',', '!', '?')
                    val punct = raw.substring(clean.length)
                    val mapped = hinglishToEnglishMap[clean] ?: raw
                    "$mapped$punct"
                }
                return converted.replaceFirstChar { it.uppercase() }
            }
        }
    }
}
