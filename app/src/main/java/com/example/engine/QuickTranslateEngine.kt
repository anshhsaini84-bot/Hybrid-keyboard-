package com.example.engine

/**
 * Instant, zero-battery offline multi-language converter & translator
 * built directly into Hybrid Keyboard's Convert panel.
 */
object QuickTranslateEngine {

    enum class TargetLanguage(val code: String, val label: String) {
        HINDI("HI", "हिन्दी"),
        ENGLISH("EN", "English"),
        SPANISH("ES", "Español"),
        FRENCH("FR", "Français"),
        GERMAN("DE", "Deutsch"),
        ARABIC("AR", "العربية"),
        RUSSIAN("RU", "Русский"),
        BENGALI("BN", "বাংলা"),
        MARATHI("MR", "मराठी"),
        TAMIL("TA", "தமிழ்")
    }

    private val toHindiDictionary = mapOf(
        "hello" to "नमस्ते", "hi" to "नमस्ते", "namaste" to "नमस्ते",
        "good" to "अच्छा", "morning" to "सुप्रभात", "evening" to "शुभ संध्या", "night" to "शुभ रात्रि",
        "how" to "कैसे", "are" to "हैं", "you" to "आप", "i" to "मैं", "am" to "हूँ",
        "fine" to "ठीक", "thanks" to "धन्यवाद", "thank" to "धन्यवाद", "please" to "कृपया",
        "sorry" to "माफ़ कीजिए", "yes" to "हाँ", "no" to "नहीं", "okay" to "ठीक है", "ok" to "ठीक है",
        "friend" to "दोस्त", "brother" to "भाई", "bhai" to "भाई", "yaar" to "यार",
        "kya" to "क्या", "haal" to "हाल", "hai" to "है", "hain" to "हैं", "hoon" to "हूँ",
        "main" to "मैं", "tum" to "तुम", "aap" to "आप", "kaise" to "कैसे", "kaisa" to "कैसा",
        "theek" to "ठीक", "badhiya" to "बढ़िया", "badiya" to "बढ़िया", "accha" to "अच्छा",
        "achha" to "अच्छा", "bahut" to "बहुत", "mast" to "मस्त", "sahi" to "सही",
        "bilkul" to "बिल्कुल", "chalo" to "चलो", "milte" to "मिलते", "kal" to "कल",
        "aaj" to "आज", "abhi" to "अभी", "ghar" to "घर", "kaam" to "काम", "love" to "प्यार",
        "pyaar" to "प्यार", "welcome" to "स्वागत है", "shukriya" to "शुक्रिया", "dhanyawad" to "धन्यवाद",
        "keyboard" to "कीबोर्ड", "voice" to "आवाज़", "battery" to "बैटरी", "fast" to "तेज़",
        "smooth" to "स्मूथ", "screenshot" to "स्क्रीनशॉट", "copy" to "कॉपी", "paste" to "पेस्ट",
        "code" to "कोड", "today" to "आज", "tomorrow" to "कल", "home" to "घर", "work" to "काम"
    )

    private val hinglishToEnglishMap = mapOf(
        "namaste" to "Hello", "kaise" to "how", "kaisa" to "how", "ho" to "are you",
        "kya" to "what", "haal" to "news", "hai" to "is", "hain" to "are", "hoon" to "am",
        "main" to "I", "mera" to "my", "meri" to "my", "tum" to "you", "aap" to "you",
        "bhai" to "brother", "yaar" to "friend", "dost" to "friend", "theek" to "fine",
        "badhiya" to "great", "badiya" to "great", "accha" to "good", "achha" to "good",
        "bahut" to "very", "mast" to "awesome", "sahi" to "right", "bilkul" to "definitely",
        "haan" to "yes", "nahi" to "no", "chalo" to "let's go", "kal" to "tomorrow",
        "aaj" to "today", "abhi" to "right now", "ghar" to "home", "kaam" to "work",
        "dhanyawad" to "thank you", "shukriya" to "thanks", "milte" to "see you"
    )

    private val toSpanishMap = mapOf(
        "hello" to "hola", "hi" to "hola", "namaste" to "hola", "good" to "bueno",
        "morning" to "mañana", "night" to "noche", "thanks" to "gracias", "thank" to "gracias",
        "please" to "por favor", "sorry" to "lo siento", "yes" to "sí", "no" to "no",
        "friend" to "amigo", "brother" to "hermano", "love" to "amor", "home" to "casa",
        "work" to "trabajo", "today" to "hoy", "tomorrow" to "mañana", "welcome" to "bienvenido",
        "fine" to "bien", "awesome" to "genial", "keyboard" to "teclado", "copy" to "copiar", "paste" to "pegar"
    )

    private val toFrenchMap = mapOf(
        "hello" to "bonjour", "hi" to "salut", "namaste" to "bonjour", "good" to "bon",
        "morning" to "matin", "night" to "nuit", "thanks" to "merci", "thank" to "merci",
        "please" to "s'il vous plaît", "sorry" to "désolé", "yes" to "oui", "no" to "non",
        "friend" to "ami", "brother" to "frère", "love" to "amour", "home" to "maison",
        "work" to "travail", "today" to "aujourd'hui", "tomorrow" to "demain", "welcome" to "bienvenue",
        "fine" to "bien", "awesome" to "génial", "keyboard" to "clavier"
    )

    private val toGermanMap = mapOf(
        "hello" to "hallo", "hi" to "hallo", "namaste" to "hallo", "good" to "gut",
        "morning" to "morgen", "night" to "nacht", "thanks" to "danke", "thank" to "danke",
        "please" to "bitte", "sorry" to "entschuldigung", "yes" to "ja", "no" to "nein",
        "friend" to "freund", "brother" to "bruder", "love" to "liebe", "home" to "zuhause",
        "work" to "arbeit", "today" to "heute", "tomorrow" to "morgen", "welcome" to "willkommen",
        "fine" to "gut", "awesome" to "super", "keyboard" to "tastatur"
    )

    private val toArabicMap = mapOf(
        "hello" to "مرحبا", "hi" to "أهلا", "namaste" to "مرحبا", "good" to "جيد",
        "morning" to "صباح", "night" to "ليلة", "thanks" to "شكرا", "thank" to "شكرا",
        "please" to "من فضلك", "sorry" to "آسف", "yes" to "نعم", "no" to "لا",
        "friend" to "صديق", "brother" to "أخي", "love" to "حب", "home" to "بيت",
        "work" to "عمل", "today" to "اليوم", "tomorrow" to "غدا", "welcome" to "أهلا بك",
        "fine" to "بخير", "awesome" to "رائع"
    )

    private val toRussianMap = mapOf(
        "hello" to "привет", "hi" to "привет", "namaste" to "привет", "good" to "хороший",
        "morning" to "утро", "night" to "ночь", "thanks" to "спасибо", "thank" to "спасибо",
        "please" to "пожалуйста", "sorry" to "извините", "yes" to "да", "no" to "нет",
        "friend" to "друг", "brother" to "брат", "love" to "любовь", "home" to "дом",
        "work" to "работа", "today" to "сегодня", "tomorrow" to "завтра", "welcome" to "добро пожаловать",
        "fine" to "хорошо", "awesome" to "отлично"
    )

    private val toBengaliMap = mapOf(
        "hello" to "নমস্কার", "hi" to "হ্যালো", "namaste" to "নমস্কার", "good" to "ভালো",
        "morning" to "সকাল", "night" to "রাত", "thanks" to "ধন্যবাদ", "thank" to "ধন্যবাদ",
        "please" to "দয়া করে", "sorry" to "দুঃখিত", "yes" to "হ্যাঁ", "no" to "না",
        "friend" to "বন্ধু", "brother" to "ভাই", "love" to "ভালোবাসা", "home" to "বাড়ি",
        "work" to "কাজ", "today" to "আজ", "tomorrow" to "আগামীকাল", "welcome" to "স্বাগতম",
        "fine" to "ভালো", "awesome" to "দারুণ"
    )

    private val toMarathiMap = mapOf(
        "hello" to "नमस्कार", "hi" to "नमस्कार", "namaste" to "नमस्कार", "good" to "चांगले",
        "morning" to "सकाळ", "night" to "रात्र", "thanks" to "धन्यवाद", "thank" to "धन्यवाद",
        "please" to "कृपया", "sorry" to "माफ करा", "yes" to "हो", "no" to "नाही",
        "friend" to "मित्र", "brother" to "भाऊ", "love" to "प्रेम", "home" to "घर",
        "work" to "काम", "today" to "आज", "tomorrow" to "उद्या", "welcome" to "स्वागत आहे",
        "fine" to "ठीक", "awesome" to "छान"
    )

    private val toTamilMap = mapOf(
        "hello" to "வணக்கம்", "hi" to "வணக்கம்", "namaste" to "வணக்கம்", "good" to "நல்ல",
        "morning" to "காலை", "night" to "இரவு", "thanks" to "நன்றி", "thank" to "நன்றி",
        "please" to "தயவுசெய்து", "sorry" to "மன்னிக்கவும்", "yes" to "ஆம்", "no" to "இல்லை",
        "friend" to "நண்பர்", "brother" to "சகோதரன்", "love" to "அன்பு", "home" to "வீடு",
        "work" to "வேலை", "today" to "இன்று", "tomorrow" to "நாளை", "welcome" to "நல்வரவு",
        "fine" to "நல்லா", "awesome" to "சூப்பர்"
    )

    private val phraseOverrides: Map<TargetLanguage, Map<String, String>> = mapOf(
        TargetLanguage.HINDI to mapOf(
            "good morning" to "शुभ प्रभात",
            "good night" to "शुभ रात्रि",
            "how are you" to "आप कैसे हैं?",
            "thank you" to "धन्यवाद",
            "kya haal hai" to "क्या हाल है?",
            "kaise ho" to "कैसे हो?",
            "bahut badhiya" to "बहुत बढ़िया",
            "on my way" to "मैं रास्ते में हूँ"
        ),
        TargetLanguage.ENGLISH to mapOf(
            "kya haal hai" to "How are you doing?",
            "kaise ho" to "How are you?",
            "bahut badhiya" to "Really awesome",
            "main theek hoon" to "I am doing well",
            "kal milte hain" to "See you tomorrow",
            "abhi aa raha hoon" to "Coming right now"
        ),
        TargetLanguage.SPANISH to mapOf(
            "good morning" to "Buenos días",
            "good night" to "Buenas noches",
            "how are you" to "¿Cómo estás?",
            "thank you" to "Muchas gracias"
        ),
        TargetLanguage.FRENCH to mapOf(
            "good morning" to "Bonjour",
            "good night" to "Bonne nuit",
            "how are you" to "Comment allez-vous ?",
            "thank you" to "Merci beaucoup"
        ),
        TargetLanguage.GERMAN to mapOf(
            "good morning" to "Guten Morgen",
            "good night" to "Gute Nacht",
            "how are you" to "Wie geht es dir?",
            "thank you" to "Vielen Dank"
        ),
        TargetLanguage.ARABIC to mapOf(
            "good morning" to "صباح الخير",
            "good night" to "تصبح على خير",
            "how are you" to "كيف حالك؟",
            "thank you" to "شكراً جزيلاً"
        ),
        TargetLanguage.RUSSIAN to mapOf(
            "good morning" to "Доброе утро",
            "good night" to "Спокойной ночи",
            "how are you" to "Как дела?",
            "thank you" to "Большое спасибо"
        ),
        TargetLanguage.BENGALI to mapOf(
            "good morning" to "শুভ সকাল",
            "good night" to "শুভ রাত্রি",
            "how are you" to "আপনি কেমন আছেন?",
            "thank you" to "ধন্যবাদ"
        ),
        TargetLanguage.MARATHI to mapOf(
            "good morning" to "शुभ सकाळ",
            "good night" to "शुभ रात्री",
            "how are you" to "तुम्ही कसे आहात?",
            "thank you" to "खूप धन्यवाद"
        ),
        TargetLanguage.TAMIL to mapOf(
            "good morning" to "காலை வணக்கம்",
            "good night" to "இனிய இரவு",
            "how are you" to "எப்படி இருக்கீங்க?",
            "thank you" to "மிக்க நன்றி"
        )
    )

    fun translate(input: String, target: TargetLanguage): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val lower = trimmed.lowercase()

        phraseOverrides[target]?.get(lower)?.let { return it }

        val dictionary = when (target) {
            TargetLanguage.HINDI -> toHindiDictionary
            TargetLanguage.ENGLISH -> hinglishToEnglishMap
            TargetLanguage.SPANISH -> toSpanishMap
            TargetLanguage.FRENCH -> toFrenchMap
            TargetLanguage.GERMAN -> toGermanMap
            TargetLanguage.ARABIC -> toArabicMap
            TargetLanguage.RUSSIAN -> toRussianMap
            TargetLanguage.BENGALI -> toBengaliMap
            TargetLanguage.MARATHI -> toMarathiMap
            TargetLanguage.TAMIL -> toTamilMap
        }

        val converted = trimmed.split(Regex("\\s+")).joinToString(" ") { raw ->
            val clean = raw.lowercase().trimEnd('.', ',', '!', '?')
            val punct = raw.substring(clean.length)
            val mapped = dictionary[clean] ?: raw
            "$mapped$punct"
        }
        return if (target == TargetLanguage.ENGLISH) {
            converted.replaceFirstChar { it.uppercase() }
        } else {
            converted
        }
    }
}
