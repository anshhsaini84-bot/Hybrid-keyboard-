package com.example.engine

import com.example.data.KeyboardLanguage

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    SYMBOLS_SWITCH,
    COMMA,
    PERIOD,
    SPACE,
    ENTER,
    EMOJI_TRIGGER,
    SYMBOLS_PAGE_SWITCH
}

data class KeySpec(
    val id: String,
    val primary: String,
    val secondary: String? = null,
    val alternates: List<String> = emptyList(),
    val weight: Float = 1f,
    val type: KeyType = KeyType.CHARACTER
)

data class EmojiCategory(
    val title: String,
    val icon: String,
    val emojis: List<String>
)

object KeyboardLayouts {

    val numberRow: List<KeySpec> = listOf(
        KeySpec("n1", "1", "!", listOf("¹", "½", "⅓", "¼")),
        KeySpec("n2", "2", "@", listOf("²", "⅔")),
        KeySpec("n3", "3", "#", listOf("³", "¾")),
        KeySpec("n4", "4", "$", listOf("₹", "€", "£", "¥")),
        KeySpec("n5", "5", "%", listOf("‰")),
        KeySpec("n6", "6", "^", listOf("↑")),
        KeySpec("n7", "7", "&", listOf("§")),
        KeySpec("n8", "8", "*", listOf("•", "★")),
        KeySpec("n9", "9", "(", listOf("[", "{", "<")),
        KeySpec("n0", "0", ")", listOf("]", "}", ">", "°"))
    )

    private fun bottomRow(spaceLabel: String = "Space", periodPrimary: String = ".", commaSecondary: String = "!"): List<KeySpec> = listOf(
        KeySpec("sym", "?123", weight = 1.35f, type = KeyType.SYMBOLS_SWITCH),
        KeySpec("comma", ",", commaSecondary, listOf(",", commaSecondary, ";", ":"), weight = 0.9f, type = KeyType.COMMA),
        KeySpec("emoji", "☺", weight = 0.9f, type = KeyType.EMOJI_TRIGGER),
        KeySpec("space", spaceLabel, weight = 4.2f, type = KeyType.SPACE),
        KeySpec("period", periodPrimary, "?", listOf(periodPrimary, "?", "!", ",", ":", "'", "\"", "-"), weight = 0.95f, type = KeyType.PERIOD),
        KeySpec("enter", "↵", weight = 1.5f, type = KeyType.ENTER)
    )

    val qwertyRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("q", "q", "1", listOf("1", "q")),
            KeySpec("w", "w", "2", listOf("2", "w")),
            KeySpec("e", "e", "3", listOf("3", "é", "è", "ê", "ë", "€")),
            KeySpec("r", "r", "4", listOf("4", "₹")),
            KeySpec("t", "t", "5", listOf("5", "™")),
            KeySpec("y", "y", "6", listOf("6", "ý", "¥")),
            KeySpec("u", "u", "7", listOf("7", "ú", "ù", "û", "ü")),
            KeySpec("i", "i", "8", listOf("8", "í", "ì", "î", "ï")),
            KeySpec("o", "o", "9", listOf("9", "ó", "ò", "ô", "ö", "ø")),
            KeySpec("p", "p", "0", listOf("0", "π"))
        ),
        listOf(
            KeySpec("a", "a", "@", listOf("@", "á", "à", "â", "ä", "ã", "å")),
            KeySpec("s", "s", "#", listOf("#", "ß", "ś", "š", "$")),
            KeySpec("d", "d", "$", listOf("$", "ð", "°")),
            KeySpec("f", "f", "%", listOf("%", "ƒ")),
            KeySpec("g", "g", "&", listOf("&")),
            KeySpec("h", "h", "-", listOf("-", "_")),
            KeySpec("j", "j", "+", listOf("+", "=")),
            KeySpec("k", "k", "(", listOf("(", "[", "{")),
            KeySpec("l", "l", ")", listOf(")", "]", "}"))
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.45f, type = KeyType.SHIFT),
            KeySpec("z", "z", "*", listOf("*", "ž", "ź")),
            KeySpec("x", "x", "\"", listOf("\"", "×")),
            KeySpec("c", "c", "'", listOf("'", "ç", "©", "ć")),
            KeySpec("v", "v", ":", listOf(":", ";")),
            KeySpec("b", "b", ";", listOf(";", "/")),
            KeySpec("n", "n", "!", listOf("!", "ñ", "ń")),
            KeySpec("m", "m", "?", listOf("?", "¿")),
            KeySpec("backspace", "⌫", weight = 1.45f, type = KeyType.BACKSPACE)
        ),
        bottomRow()
    )

    val spanishRows: List<List<KeySpec>> = listOf(
        qwertyRows[0],
        qwertyRows[1] + KeySpec("es_n", "ñ", "!", listOf("ñ", "n")),
        qwertyRows[2],
        bottomRow("Español", ".", "¿")
    )

    val frenchAzertyRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("fr_a", "a", "1", listOf("1", "à", "â", "æ")),
            KeySpec("fr_z", "z", "2", listOf("2")),
            KeySpec("fr_e", "e", "3", listOf("3", "é", "è", "ê", "ë", "€")),
            KeySpec("fr_r", "r", "4", listOf("4")),
            KeySpec("fr_t", "t", "5", listOf("5")),
            KeySpec("fr_y", "y", "6", listOf("6", "ÿ")),
            KeySpec("fr_u", "u", "7", listOf("7", "ù", "û", "ü")),
            KeySpec("fr_i", "i", "8", listOf("8", "î", "ï")),
            KeySpec("fr_o", "o", "9", listOf("9", "ô", "œ")),
            KeySpec("fr_p", "p", "0", listOf("0"))
        ),
        listOf(
            KeySpec("fr_q", "q", "@"), KeySpec("fr_s", "s", "#"), KeySpec("fr_d", "d", "$"),
            KeySpec("fr_f", "f", "%"), KeySpec("fr_g", "g", "&"), KeySpec("fr_h", "h", "-"),
            KeySpec("fr_j", "j", "+"), KeySpec("fr_k", "k", "("), KeySpec("fr_l", "l", ")"),
            KeySpec("fr_m", "m", "'")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.45f, type = KeyType.SHIFT),
            KeySpec("fr_w", "w", "*"), KeySpec("fr_x", "x", "\""),
            KeySpec("fr_c", "c", "ç", listOf("ç", "'")),
            KeySpec("fr_v", "v", ":"), KeySpec("fr_b", "b", ";"), KeySpec("fr_n", "n", "!"),
            KeySpec("fr_ap", "'", "?"),
            KeySpec("backspace", "⌫", weight = 1.45f, type = KeyType.BACKSPACE)
        ),
        bottomRow("Français")
    )

    val germanQwertzRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("de_q", "q", "1"), KeySpec("de_w", "w", "2"), KeySpec("de_e", "e", "3", listOf("3", "é", "€")),
            KeySpec("de_r", "r", "4"), KeySpec("de_t", "t", "5"), KeySpec("de_z", "z", "6"),
            KeySpec("de_u", "u", "7", listOf("7", "ü")), KeySpec("de_i", "i", "8"),
            KeySpec("de_o", "o", "9", listOf("9", "ö")), KeySpec("de_p", "p", "0")
        ),
        listOf(
            KeySpec("de_a", "a", "ä", listOf("ä", "@")), KeySpec("de_s", "s", "ß", listOf("ß", "#")),
            KeySpec("de_d", "d", "$"), KeySpec("de_f", "f", "%"), KeySpec("de_g", "g", "&"),
            KeySpec("de_h", "h", "-"), KeySpec("de_j", "j", "+"), KeySpec("de_k", "k", "("),
            KeySpec("de_l", "l", ")")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.45f, type = KeyType.SHIFT),
            KeySpec("de_y", "y", "*"), KeySpec("de_x", "x", "\""), KeySpec("de_c", "c", "'"),
            KeySpec("de_v", "v", ":"), KeySpec("de_b", "b", ";"), KeySpec("de_n", "n", "!"),
            KeySpec("de_m", "m", "?"),
            KeySpec("backspace", "⌫", weight = 1.45f, type = KeyType.BACKSPACE)
        ),
        bottomRow("Deutsch")
    )

    val hindiRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("hi_au", "ौ", "औ", listOf("औ", "ौ")),
            KeySpec("hi_ai", "ै", "ऐ", listOf("ऐ", "ै")),
            KeySpec("hi_aa", "ा", "आ", listOf("आ", "ा")),
            KeySpec("hi_ii", "ी", "ई", listOf("ई", "ी")),
            KeySpec("hi_uu", "ू", "ऊ", listOf("ऊ", "ू")),
            KeySpec("hi_ba", "ब", "भ", listOf("ब", "भ")),
            KeySpec("hi_ha", "ह", "ङ", listOf("ह", "ङ")),
            KeySpec("hi_ga", "ग", "घ", listOf("ग", "घ")),
            KeySpec("hi_da", "द", "ध", listOf("द", "ध")),
            KeySpec("hi_ja", "ज", "झ", listOf("ज", "झ"))
        ),
        listOf(
            KeySpec("hi_o", "ो", "ओ", listOf("ओ", "ो")),
            KeySpec("hi_e", "े", "ए", listOf("ए", "े")),
            KeySpec("hi_halant", "्", "अ", listOf("अ", "्")),
            KeySpec("hi_i", "ि", "इ", listOf("इ", "ि")),
            KeySpec("hi_u", "ु", "उ", listOf("उ", "ु")),
            KeySpec("hi_pa", "प", "फ", listOf("प", "फ")),
            KeySpec("hi_ra", "र", "ऋ", listOf("र", "ऋ")),
            KeySpec("hi_ka", "क", "ख", listOf("क", "ख")),
            KeySpec("hi_ta", "त", "थ", listOf("त", "थ")),
            KeySpec("hi_cha", "च", "छ", listOf("च", "छ"))
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("hi_anusvara", "ं", "ँ", listOf("ं", "ँ", "ः")),
            KeySpec("hi_ma", "म", "ण", listOf("म", "ण")),
            KeySpec("hi_na", "न", "ञ", listOf("न", "ञ")),
            KeySpec("hi_va", "व", "ळ", listOf("व", "ळ")),
            KeySpec("hi_la", "ल", "श", listOf("ल", "श")),
            KeySpec("hi_sa", "स", "ष", listOf("स", "ष")),
            KeySpec("hi_ya", "य", "ट", listOf("य", "ट", "ठ", "ड", "ढ")),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        bottomRow("हिन्दी", "।", "।")
    )

    val hindiShiftRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("his_au", "औ", "ौ"), KeySpec("his_ai", "ऐ", "ै"), KeySpec("his_aa", "आ", "ा"),
            KeySpec("his_ii", "ई", "ी"), KeySpec("his_uu", "ऊ", "ू"), KeySpec("his_bha", "भ", "ब"),
            KeySpec("his_nga", "ङ", "ह"), KeySpec("his_gha", "घ", "ग"), KeySpec("his_dha", "ध", "द"),
            KeySpec("his_jha", "झ", "ज")
        ),
        listOf(
            KeySpec("his_o", "ओ", "ो"), KeySpec("his_e", "ए", "े"), KeySpec("his_a", "अ", "्"),
            KeySpec("his_i", "इ", "ि"), KeySpec("his_u", "उ", "ु"), KeySpec("his_pha", "फ", "प"),
            KeySpec("his_ri", "ऋ", "र"), KeySpec("his_kha", "ख", "क"), KeySpec("his_tha", "थ", "त"),
            KeySpec("his_chha", "छ", "च")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("his_chandra", "ँ", "ं"), KeySpec("his_nna", "ण", "म"), KeySpec("his_tta", "ट", "ठ"),
            KeySpec("his_dda", "ड", "ढ"), KeySpec("his_sha", "श", "ष"), KeySpec("his_ssha", "ष", "स"),
            KeySpec("his_ksha", "क्ष", "त्र", listOf("क्ष", "त्र", "ज्ञ", "श्र")),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        hindiRows[3]
    )

    val arabicRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("ar_dad", "ض"), KeySpec("ar_sad", "ص"), KeySpec("ar_tha", "ث"),
            KeySpec("ar_qaf", "ق"), KeySpec("ar_fa", "ف"), KeySpec("ar_ghain", "غ"),
            KeySpec("ar_ain", "ع"), KeySpec("ar_ha", "ه"), KeySpec("ar_kha", "خ"),
            KeySpec("ar_hah", "ح"), KeySpec("ar_jeem", "ج")
        ),
        listOf(
            KeySpec("ar_sheen", "ش"), KeySpec("ar_seen", "س"), KeySpec("ar_yeh", "ي", "ى", listOf("ي", "ى", "ئ")),
            KeySpec("ar_beh", "ب"), KeySpec("ar_lam", "ل"), KeySpec("ar_alef", "ا", "أ", listOf("ا", "أ", "إ", "آ")),
            KeySpec("ar_teh", "ت", "ة", listOf("ت", "ة")), KeySpec("ar_noon", "ن"),
            KeySpec("ar_meem", "م"), KeySpec("ar_kaf", "ك")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.25f, type = KeyType.SHIFT),
            KeySpec("ar_hamza", "ئ"), KeySpec("ar_hamza2", "ء"), KeySpec("ar_waw", "و", "ؤ"),
            KeySpec("ar_ra", "ر"), KeySpec("ar_dal", "د", "ذ"), KeySpec("ar_zay", "ز"),
            KeySpec("ar_tah", "ط", "ظ"),
            KeySpec("backspace", "⌫", weight = 1.25f, type = KeyType.BACKSPACE)
        ),
        bottomRow("العربية", ".", "،")
    )

    val russianRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("ru_1", "й"), KeySpec("ru_2", "ц"), KeySpec("ru_3", "у"), KeySpec("ru_4", "к"),
            KeySpec("ru_5", "е", "ё", listOf("е", "ё")), KeySpec("ru_6", "н"), KeySpec("ru_7", "г"),
            KeySpec("ru_8", "ш"), KeySpec("ru_9", "щ"), KeySpec("ru_10", "з"), KeySpec("ru_11", "х")
        ),
        listOf(
            KeySpec("ru_12", "ф"), KeySpec("ru_13", "ы"), KeySpec("ru_14", "в"), KeySpec("ru_15", "а"),
            KeySpec("ru_16", "п"), KeySpec("ru_17", "р"), KeySpec("ru_18", "о"), KeySpec("ru_19", "л"),
            KeySpec("ru_20", "д"), KeySpec("ru_21", "ж"), KeySpec("ru_22", "э")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("ru_23", "я"), KeySpec("ru_24", "ч"), KeySpec("ru_25", "с"), KeySpec("ru_26", "м"),
            KeySpec("ru_27", "и"), KeySpec("ru_28", "т"), KeySpec("ru_29", "ь", "ъ"),
            KeySpec("ru_30", "б"), KeySpec("ru_31", "ю"),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        bottomRow("Русский")
    )

    val bengaliRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("bn_1", "ৌ", "ঔ"), KeySpec("bn_2", "ৈ", "ঐ"), KeySpec("bn_3", "া", "আ"),
            KeySpec("bn_4", "ী", "ঈ"), KeySpec("bn_5", "ূ", "ঊ"), KeySpec("bn_6", "ব", "ভ"),
            KeySpec("bn_7", "হ", "ঙ"), KeySpec("bn_8", "গ", "ঘ"), KeySpec("bn_9", "দ", "ধ"),
            KeySpec("bn_10", "জ", "ঝ")
        ),
        listOf(
            KeySpec("bn_11", "ো", "ও"), KeySpec("bn_12", "ে", "এ"), KeySpec("bn_13", "্", "অ"),
            KeySpec("bn_14", "ি", "ই"), KeySpec("bn_15", "ু", "উ"), KeySpec("bn_16", "প", "ফ"),
            KeySpec("bn_17", "র", "ড়"), KeySpec("bn_18", "ক", "খ"), KeySpec("bn_19", "ত", "থ"),
            KeySpec("bn_20", "চ", "ছ")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("bn_21", "ং", "ঁ"), KeySpec("bn_22", "ম", "ণ"), KeySpec("bn_23", "ন", "ঞ"),
            KeySpec("bn_24", "য", "য়"), KeySpec("bn_25", "ল", "শ"), KeySpec("bn_26", "স", "ষ"),
            KeySpec("bn_27", "ট", "ঠ", listOf("ট", "ঠ", "ড", "ঢ")),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        bottomRow("বাংলা", "।", "।")
    )

    val tamilRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("ta_1", "ௌ", "ஔ"), KeySpec("ta_2", "ை", "ஐ"), KeySpec("ta_3", "ா", "ஆ"),
            KeySpec("ta_4", "ீ", "ஈ"), KeySpec("ta_5", "ூ", "ஊ"), KeySpec("ta_6", "ப"),
            KeySpec("ta_7", "ஹ", "ங"), KeySpec("ta_8", "க"), KeySpec("ta_9", "த"),
            KeySpec("ta_10", "ஜ", "ச")
        ),
        listOf(
            KeySpec("ta_11", "ோ", "ஓ"), KeySpec("ta_12", "ே", "ஏ"), KeySpec("ta_13", "்", "அ"),
            KeySpec("ta_14", "ி", "இ"), KeySpec("ta_15", "ு", "உ"), KeySpec("ta_16", "ர", "ற"),
            KeySpec("ta_17", "ல", "ள"), KeySpec("ta_18", "வ"), KeySpec("ta_19", "ட"),
            KeySpec("ta_20", "ண", "ந")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("ta_21", "ெ", "எ"), KeySpec("ta_22", "ொ", "ஒ"), KeySpec("ta_23", "ம"),
            KeySpec("ta_24", "ன", "ஞ"), KeySpec("ta_25", "ய"), KeySpec("ta_26", "ழ"),
            KeySpec("ta_27", "ஸ", "ஷ"),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        bottomRow("தமிழ்")
    )

    val symbolsPage1: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("s1", "1"), KeySpec("s2", "2"), KeySpec("s3", "3"),
            KeySpec("s4", "4"), KeySpec("s5", "5"), KeySpec("s6", "6"),
            KeySpec("s7", "7"), KeySpec("s8", "8"), KeySpec("s9", "9"), KeySpec("s0", "0")
        ),
        listOf(
            KeySpec("sat", "@"), KeySpec("shash", "#"), KeySpec("srupee", "₹", "$", listOf("₹", "$", "€", "£")),
            KeySpec("sunder", "_"), KeySpec("samp", "&"), KeySpec("sminus", "-"),
            KeySpec("splus", "+"), KeySpec("slp", "("), KeySpec("srp", ")"), KeySpec("sslash", "/")
        ),
        listOf(
            KeySpec("sym2", "=\\<", weight = 1.45f, type = KeyType.SYMBOLS_PAGE_SWITCH),
            KeySpec("sstar", "*"), KeySpec("sdq", "\""), KeySpec("ssq", "'"),
            KeySpec("scol", ":"), KeySpec("sscol", ";"), KeySpec("sexcl", "!"),
            KeySpec("sques", "?"),
            KeySpec("backspace", "⌫", weight = 1.45f, type = KeyType.BACKSPACE)
        ),
        listOf(
            KeySpec("abc", "ABC", weight = 1.35f, type = KeyType.SYMBOLS_SWITCH),
            KeySpec("comma", ",", weight = 0.9f, type = KeyType.COMMA),
            KeySpec("emoji", "☺", weight = 0.9f, type = KeyType.EMOJI_TRIGGER),
            KeySpec("space", "Space", weight = 4.2f, type = KeyType.SPACE),
            KeySpec("period", ".", weight = 0.95f, type = KeyType.PERIOD),
            KeySpec("enter", "↵", weight = 1.5f, type = KeyType.ENTER)
        )
    )

    val symbolsPage2: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("s2_tilde", "~"), KeySpec("s2_grave", "`"), KeySpec("s2_pipe", "|"),
            KeySpec("s2_dot", "•"), KeySpec("s2_sqrt", "√"), KeySpec("s2_pi", "π"),
            KeySpec("s2_div", "÷"), KeySpec("s2_mul", "×"), KeySpec("s2_para", "¶"), KeySpec("s2_delta", "Δ")
        ),
        listOf(
            KeySpec("s2_pound", "£"), KeySpec("s2_euro", "€"), KeySpec("s2_yen", "¥"),
            KeySpec("s2_cent", "¢"), KeySpec("s2_caret", "^"), KeySpec("s2_deg", "°"),
            KeySpec("s2_eq", "="), KeySpec("s2_lcb", "{"), KeySpec("s2_rcb", "}"), KeySpec("s2_bslash", "\\")
        ),
        listOf(
            KeySpec("sym1", "?123", weight = 1.45f, type = KeyType.SYMBOLS_PAGE_SWITCH),
            KeySpec("s2_pct", "%"), KeySpec("s2_copy", "©"), KeySpec("s2_reg", "®"),
            KeySpec("s2_tm", "™"), KeySpec("s2_check", "✓"), KeySpec("s2_lb", "["),
            KeySpec("s2_rb", "]"),
            KeySpec("backspace", "⌫", weight = 1.45f, type = KeyType.BACKSPACE)
        ),
        symbolsPage1[3]
    )

    val emojiCategories: List<EmojiCategory> = listOf(
        EmojiCategory(
            title = "Smileys",
            icon = "😀",
            emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
                "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
                "😘", "😋", "😜", "🤪", "🤨", "🧐", "🤓", "😎",
                "🥳", "😏", "😒", "😔", "😴", "🤯", "🥺", "😭"
            )
        ),
        EmojiCategory(
            title = "Gestures",
            icon = "👍",
            emojis = listOf(
                "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🤟",
                "🤘", "🤙", "👈", "👉", "👆", "👇", "👋", "🤚",
                "👏", "🙌", "🤝", "🙏", "💪", "🫶", "✍️", "👀"
            )
        ),
        EmojiCategory(
            title = "Hearts & Vibes",
            icon = "❤️",
            emojis = listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
                "💖", "💘", "💝", "🔥", "✨", "⚡", "💫", "🌟",
                "🎉", "🎊", "🏆", "🥇", "💯", "✅", "🚀", "💎"
            )
        ),
        EmojiCategory(
            title = "Tech & Work",
            icon = "⌨️",
            emojis = listOf(
                "⌨️", "🎙️", "📱", "💻", "🖥️", "🔋", "🔌", "💡",
                "📌", "📎", "📝", "📅", "📊", "🔒", "🛡️", "⚙️",
                "🌐", "🛰️", "🤖", "🎧", "📷", "🎬", "☕", "🌙"
            )
        )
    )

    val kaomojiList: List<String> = listOf(
        "(◕‿◕)", "(◠‿◠)", "¯\\_(ツ)_/¯", "(づ｡◕‿‿◕｡)づ",
        "ʕ•ᴥ•ʔ", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "(☞ﾟヮﾟ)☞", "☜(ﾟヮﾟ☜)",
        "(¬‿¬)", "(•_•)", "(⌐■_■)", "(ಥ﹏ಥ)",
        "(╥_╥)", "ヾ(⌐■_■)ノ♪", "(ง •̀_•́)ง", "♥(ˆ⌣ˆԅ)",
        "(*^▽^*)", "(☆▽☆)", "(⊙_⊙)", "(ノಠ益ಠ)ノ彡┻━┻"
    )

    val emojiKeywords: Map<String, List<String>> = mapOf(
        "😀" to listOf("happy", "smile", "grin", "joy"),
        "😂" to listOf("laugh", "lol", "funny", "haha", "tears"),
        "🤣" to listOf("rofl", "laugh", "funny"),
        "😍" to listOf("love", "heart", "eyes", "crush"),
        "🥰" to listOf("love", "hearts", "cute", "warm"),
        "😎" to listOf("cool", "sunglasses", "swag"),
        "🥳" to listOf("party", "celebrate", "birthday"),
        "😭" to listOf("cry", "sad", "sob"),
        "🙏" to listOf("namaste", "please", "thanks", "pray"),
        "👍" to listOf("ok", "yes", "good", "thumbs", "like"),
        "👌" to listOf("nice", "perfect", "ok"),
        "💪" to listOf("strong", "power", "gym"),
        "❤️" to listOf("heart", "love", "red"),
        "🔥" to listOf("fire", "lit", "hot", "awesome"),
        "✨" to listOf("sparkle", "magic", "shine", "new"),
        "⚡" to listOf("fast", "bolt", "battery", "power"),
        "🚀" to listOf("rocket", "launch", "fast"),
        "💯" to listOf("100", "perfect", "real"),
        "✅" to listOf("check", "done", "yes"),
        "⌨️" to listOf("keyboard", "type", "hybrid"),
        "🎙️" to listOf("mic", "voice", "speak"),
        "🔋" to listOf("battery", "eco", "charge"),
        "☕" to listOf("coffee", "chai", "tea", "morning"),
        "🌙" to listOf("night", "moon", "sleep")
    )

    fun searchEmojis(query: String): List<String> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emojiCategories.first().emojis
        val matched = emojiKeywords.entries
            .filter { (_, words) -> words.any { it.contains(q) } }
            .map { it.key }
        return if (matched.isNotEmpty()) matched else emojiCategories.flatMap { it.emojis }.take(16)
    }

    fun usesCaseShift(language: KeyboardLanguage): Boolean = when (language) {
        KeyboardLanguage.ENGLISH_US,
        KeyboardLanguage.HINGLISH,
        KeyboardLanguage.SPANISH,
        KeyboardLanguage.FRENCH,
        KeyboardLanguage.GERMAN,
        KeyboardLanguage.RUSSIAN -> true
        else -> false
    }

    fun supportsGlide(language: KeyboardLanguage): Boolean = when (language) {
        KeyboardLanguage.ENGLISH_US,
        KeyboardLanguage.HINGLISH,
        KeyboardLanguage.SPANISH,
        KeyboardLanguage.FRENCH,
        KeyboardLanguage.GERMAN -> true
        else -> false
    }

    fun rowsForAlpha(language: KeyboardLanguage, isShifted: Boolean): List<List<KeySpec>> {
        return when (language) {
            KeyboardLanguage.ENGLISH_US, KeyboardLanguage.HINGLISH -> qwertyRows
            KeyboardLanguage.HINDI_DEVANAGARI, KeyboardLanguage.MARATHI ->
                if (isShifted) hindiShiftRows else hindiRows
            KeyboardLanguage.SPANISH -> spanishRows
            KeyboardLanguage.FRENCH -> frenchAzertyRows
            KeyboardLanguage.GERMAN -> germanQwertzRows
            KeyboardLanguage.ARABIC -> arabicRows
            KeyboardLanguage.RUSSIAN -> russianRows
            KeyboardLanguage.BENGALI -> bengaliRows
            KeyboardLanguage.TAMIL -> tamilRows
        }
    }
}
