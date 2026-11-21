package com.example.engine

import com.example.data.CustomWordEntity
import com.example.data.KeyboardLanguage
import kotlin.math.abs

/**
 * Lightweight, low-battery multi-language dictionary, autocorrect, Hinglish transliteration,
 * next-word predictor, and geometric swipe/glide word decoder across all 11 languages.
 */
object DictionaryEngine {

    private val englishVocabulary = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "hello", "hey", "hi", "thanks", "thank", "please", "sorry", "welcome", "great",
        "awesome", "amazing", "perfect", "cool", "nice", "yes", "yeah", "okay", "ok",
        "hybrid", "keyboard", "samsung", "google", "gboard", "android", "voice", "microphone",
        "typing", "swipe", "glide", "battery", "smooth", "smart", "fast", "clean", "dark",
        "theme", "clipboard", "emoji", "message", "morning", "evening", "night", "today",
        "tomorrow", "meeting", "call", "phone", "mobile", "screen", "update", "ready",
        "working", "perfectly", "everything", "download", "feature", "settings",
        "language", "english", "hindi", "hinglish", "friend", "family", "home", "office",
        "love", "happy", "smile", "check", "send", "share", "copy", "paste", "delete",
        "system", "performance", "power", "saver", "mode", "quick", "easy", "better", "best"
    )

    private val hinglishVocabulary = listOf(
        "namaste", "kaise", "kaisa", "kya", "hai", "hain", "hoon", "main", "tum", "aap",
        "bhai", "yaar", "dost", "theek", "badiya", "badhiya", "accha", "achha", "bahut",
        "mast", "sahi", "chalo", "karo", "karna", "raha", "rahi", "mera", "meri", "tera",
        "apna", "ghar", "kaam", "samay", "abhi", "kal", "aaj", "subah", "shaam", "raat",
        "khana", "pani", "chai", "coffee", "milte", "baad", "jaldi", "dhanyawad", "shukriya",
        "haan", "nahi", "bilkul", "zaroor", "samajh", "bolna", "likhna", "sunna", "dekho"
    )

    private val spanishVocabulary = listOf(
        "hola", "buenos", "dias", "gracias", "por", "favor", "amigo", "como", "estas",
        "bien", "si", "no", "bueno", "noche", "tarde", "trabajo", "casa", "hoy", "mañana",
        "teclado", "mensaje", "excelente", "perfecto", "vale", "claro", "genial"
    )

    private val frenchVocabulary = listOf(
        "bonjour", "salut", "merci", "beaucoup", "oui", "non", "bien", "super", "parfait",
        "ami", "comment", "allez", "vous", "soir", "matin", "maison", "travail", "clavier",
        "message", "demain", "aujourd", "pardon", "excusez", "genial"
    )

    private val germanVocabulary = listOf(
        "hallo", "guten", "morgen", "abend", "danke", "bitte", "ja", "nein", "gut", "super",
        "freund", "wie", "geht", "heute", "haus", "arbeit", "tastatur", "nachricht", "perfekt",
        "alles", "klar", "gerne", "tschuss"
    )

    private val arabicVocabulary = listOf(
        "مرحبا", "شكرا", "أهلا", "كيف", "حالك", "بخير", "نعم", "لا", "صباح", "مساء", "ممتاز"
    )

    private val russianVocabulary = listOf(
        "привет", "спасибо", "хорошо", "да", "нет", "пожалуйста", "друг", "утро", "вечер", "отлично"
    )

    private val bengaliVocabulary = listOf(
        "নমস্কার", "ধন্যবাদ", "ভালো", "কেমন", "আছেন", "হ্যাঁ", "না", "বন্ধু", "শুভ", "সকাল"
    )

    private val marathiVocabulary = listOf(
        "नमस्कार", "धन्यवाद", "कसे", "आहात", "छान", "हो", "नाही", "मित्र", "शुभ", "सकाळ"
    )

    private val tamilVocabulary = listOf(
        "வணக்கம்", "நன்றி", "நல்லா", "எப்படி", "இருக்கீங்க", "ஆம்", "இல்லை", "நண்பா", "காலை"
    )

    private val hinglishTransliterations = mapOf(
        "namaste" to "नमस्ते",
        "kaise" to "कैसे",
        "kya" to "क्या",
        "hai" to "है",
        "hain" to "हैं",
        "hoon" to "हूँ",
        "bhai" to "भाई",
        "yaar" to "यार",
        "dost" to "दोस्त",
        "theek" to "ठीक",
        "badhiya" to "बढ़िया",
        "badiya" to "बढ़िया",
        "accha" to "अच्छा",
        "achha" to "अच्छा",
        "bahut" to "बहुत",
        "mast" to "मस्त",
        "sahi" to "सही",
        "aaj" to "आज",
        "kal" to "कल",
        "abhi" to "अभी",
        "haan" to "हाँ",
        "nahi" to "नहीं",
        "bilkul" to "बिल्कुल",
        "dhanyawad" to "धन्यवाद",
        "shukriya" to "शुक्रिया",
        "pyaar" to "प्यार",
        "subah" to "सुबह",
        "raat" to "रात"
    )

    private val nextWordMap = mapOf(
        "hello" to listOf("there", "friend", "everyone"),
        "hey" to listOf("there", "how", "buddy"),
        "how" to listOf("are", "is", "was"),
        "are" to listOf("you", "we", "they"),
        "thank" to listOf("you", "god", "heavens"),
        "good" to listOf("morning", "evening", "job"),
        "hybrid" to listOf("keyboard", "mode", "voice"),
        "samsung" to listOf("keyboard", "galaxy", "style"),
        "google" to listOf("keyboard", "voice", "search"),
        "voice" to listOf("typing", "mic", "dictation"),
        "battery" to listOf("saver", "mode", "life"),
        "low" to listOf("battery", "power", "latency"),
        "kya" to listOf("haal", "kar", "hai"),
        "kaise" to listOf("ho", "hain", "hai"),
        "theek" to listOf("hai", "hoon", "tha"),
        "bahut" to listOf("badhiya", "accha", "badiya"),
        "buenos" to listOf("días", "amigos", "deseos"),
        "guten" to listOf("Morgen", "Tag", "Abend"),
        "merci" to listOf("beaucoup", "bien", "infiniment")
    )

    private fun defaultSuggestionsFor(language: KeyboardLanguage): List<String> = when (language) {
        KeyboardLanguage.ENGLISH_US -> listOf("Hello", "Hybrid", "Thanks")
        KeyboardLanguage.HINGLISH -> listOf("Namaste", "Kya", "Badhiya")
        KeyboardLanguage.HINDI_DEVANAGARI -> listOf("नमस्ते", "क्या", "बढ़िया")
        KeyboardLanguage.SPANISH -> listOf("Hola", "Gracias", "Genial")
        KeyboardLanguage.FRENCH -> listOf("Bonjour", "Merci", "Super")
        KeyboardLanguage.GERMAN -> listOf("Hallo", "Danke", "Super")
        KeyboardLanguage.ARABIC -> listOf("مرحبا", "شكرا", "ممتاز")
        KeyboardLanguage.RUSSIAN -> listOf("Привет", "Спасибо", "Хорошо")
        KeyboardLanguage.BENGALI -> listOf("নমস্কার", "ধন্যবাদ", "ভালো")
        KeyboardLanguage.MARATHI -> listOf("नमस्कार", "धन्यवाद", "छान")
        KeyboardLanguage.TAMIL -> listOf("வணக்கம்", "நன்றி", "நல்லா")
    }

    private fun vocabularyFor(language: KeyboardLanguage): List<String> = when (language) {
        KeyboardLanguage.ENGLISH_US -> englishVocabulary
        KeyboardLanguage.HINGLISH -> hinglishVocabulary + englishVocabulary
        KeyboardLanguage.HINDI_DEVANAGARI -> listOf("नमस्ते", "क्या", "बढ़िया", "कैसे", "अच्छा", "धन्यवाद", "बहुत", "ठीक", "भारत")
        KeyboardLanguage.SPANISH -> spanishVocabulary + englishVocabulary
        KeyboardLanguage.FRENCH -> frenchVocabulary + englishVocabulary
        KeyboardLanguage.GERMAN -> germanVocabulary + englishVocabulary
        KeyboardLanguage.ARABIC -> arabicVocabulary
        KeyboardLanguage.RUSSIAN -> russianVocabulary
        KeyboardLanguage.BENGALI -> bengaliVocabulary
        KeyboardLanguage.MARATHI -> marathiVocabulary
        KeyboardLanguage.TAMIL -> tamilVocabulary
    }

    fun suggest(
        currentWord: String,
        previousWord: String,
        language: KeyboardLanguage,
        customWords: List<CustomWordEntity>,
        ecoMode: Boolean
    ): List<String> {
        val clean = currentWord.trim()
        if (clean.isEmpty()) {
            val prev = previousWord.trim().lowercase()
            val next = nextWordMap[prev]
            if (next != null) return next
            return defaultSuggestionsFor(language)
        }

        val lower = clean.lowercase()
        val results = LinkedHashSet<String>(4)

        // 1. Custom shortcut match first (zero-cost exact check)
        customWords.firstOrNull { it.shortcut.equals(lower, ignoreCase = true) }?.let {
            results.add(it.expansion)
        }

        // 2. Hinglish transliteration suggestion
        if (language == KeyboardLanguage.HINGLISH) {
            hinglishTransliterations[lower]?.let { results.add(it) }
        }

        // 3. Prefix matches
        val pool = vocabularyFor(language)
        val wasCapitalized = clean.firstOrNull()?.isUpperCase() == true
        for (word in pool) {
            if (word.startsWith(lower) && word != lower) {
                results.add(if (wasCapitalized) word.replaceFirstChar { it.uppercase() } else word)
                if (results.size >= 3) break
            }
        }

        // 4. Fuzzy autocorrect (skipped if Eco Mode & word already has >= 3 matches)
        if (!ecoMode && results.size < 3 && lower.length >= 3) {
            for (word in pool) {
                if (abs(word.length - lower.length) <= 1 && word.first() == lower.first()) {
                    if (editDistanceAtMostOne(lower, word)) {
                        results.add(if (wasCapitalized) word.replaceFirstChar { it.uppercase() } else word)
                        if (results.size >= 3) break
                    }
                }
            }
        }

        if (results.isEmpty()) {
            results.add(clean)
        }
        return results.take(3)
    }

    /**
     * Decodes a sequence of glided key letters (e.g. "h-e-l-l-o" or noisy "h-r-e-l-o")
     * into the closest word for Latin-script languages.
     */
    fun decodeGlideSequence(
        visitedKeys: List<String>,
        language: KeyboardLanguage
    ): String? {
        if (visitedKeys.size < 2) return null
        val collapsed = buildString {
            for (k in visitedKeys) {
                val c = k.lowercase().firstOrNull() ?: continue
                if (isEmpty() || last() != c) append(c)
            }
        }
        if (collapsed.length < 2) return null

        val firstChar = collapsed.first()
        val lastChar = collapsed.last()
        val candidates = vocabularyFor(language)

        var bestWord: String? = null
        var bestScore = Int.MIN_VALUE

        for (word in candidates) {
            if (word.length < 2) continue
            if (word.first() != firstChar) continue
            val wordCollapsed = collapseConsecutive(word)
            var score = 0
            if (word.last() == lastChar) score += 40
            if (isSubsequence(wordCollapsed, collapsed)) {
                score += 65 - abs(collapsed.length - wordCollapsed.length) * 5
                if (wordCollapsed == collapsed) score += 30
            } else {
                var wi = 0
                for (ch in collapsed) {
                    if (wi < wordCollapsed.length && wordCollapsed[wi] == ch) {
                        wi++
                        score += 10
                    }
                }
                score -= abs(wordCollapsed.length - wi) * 12
            }
            if (score > bestScore && score >= 45) {
                bestScore = score
                bestWord = word
            }
        }
        return bestWord
    }

    private fun collapseConsecutive(input: String): String = buildString(input.length) {
        for (ch in input) {
            if (isEmpty() || last() != ch) append(ch)
        }
    }

    private fun isSubsequence(word: String, path: String): Boolean {
        var wi = 0
        for (ch in path) {
            if (wi < word.length && word[wi] == ch) wi++
        }
        return wi == word.length
    }

    private fun editDistanceAtMostOne(a: String, b: String): Boolean {
        val lenDiff = abs(a.length - b.length)
        if (lenDiff > 1) return false
        var i = 0
        var j = 0
        var edits = 0
        while (i < a.length && j < b.length) {
            if (a[i] == b[j]) {
                i++
                j++
            } else {
                edits++
                if (edits > 1) return false
                if (a.length > b.length) i++
                else if (b.length > a.length) j++
                else {
                    i++
                    j++
                }
            }
        }
        if (i < a.length || j < b.length) edits++
        return edits <= 1
    }
}
