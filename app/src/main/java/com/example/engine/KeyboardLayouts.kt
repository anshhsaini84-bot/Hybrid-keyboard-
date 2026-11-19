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
        listOf(
            KeySpec("sym", "?123", weight = 1.35f, type = KeyType.SYMBOLS_SWITCH),
            KeySpec("comma", ",", "!", listOf(",", "!", ";", ":"), weight = 0.9f, type = KeyType.COMMA),
            KeySpec("emoji", "☺", weight = 0.9f, type = KeyType.EMOJI_TRIGGER),
            KeySpec("space", "Space", weight = 4.2f, type = KeyType.SPACE),
            KeySpec("period", ".", "?", listOf(".", "?", "!", ",", ":", "'", "\"", "-"), weight = 0.95f, type = KeyType.PERIOD),
            KeySpec("enter", "↵", weight = 1.5f, type = KeyType.ENTER)
        )
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
        listOf(
            KeySpec("sym", "?123", weight = 1.35f, type = KeyType.SYMBOLS_SWITCH),
            KeySpec("comma", ",", "।", listOf(",", "।", "!"), weight = 0.9f, type = KeyType.COMMA),
            KeySpec("emoji", "☺", weight = 0.9f, type = KeyType.EMOJI_TRIGGER),
            KeySpec("space", "हिन्दी", weight = 4.2f, type = KeyType.SPACE),
            KeySpec("period", "।", ".", listOf("।", ".", "?", "!"), weight = 0.95f, type = KeyType.PERIOD),
            KeySpec("enter", "↵", weight = 1.5f, type = KeyType.ENTER)
        )
    )

    val hindiShiftRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("his_au", "औ", "ौ"),
            KeySpec("his_ai", "ऐ", "ै"),
            KeySpec("his_aa", "आ", "ा"),
            KeySpec("his_ii", "ई", "ी"),
            KeySpec("his_uu", "ऊ", "ू"),
            KeySpec("his_bha", "भ", "ब"),
            KeySpec("his_nga", "ङ", "ह"),
            KeySpec("his_gha", "घ", "ग"),
            KeySpec("his_dha", "ध", "द"),
            KeySpec("his_jha", "झ", "ज")
        ),
        listOf(
            KeySpec("his_o", "ओ", "ो"),
            KeySpec("his_e", "ए", "े"),
            KeySpec("his_a", "अ", "्"),
            KeySpec("his_i", "इ", "ि"),
            KeySpec("his_u", "उ", "ु"),
            KeySpec("his_pha", "फ", "प"),
            KeySpec("his_ri", "ऋ", "र"),
            KeySpec("his_kha", "ख", "क"),
            KeySpec("his_tha", "थ", "त"),
            KeySpec("his_chha", "छ", "च")
        ),
        listOf(
            KeySpec("shift", "⇧", weight = 1.35f, type = KeyType.SHIFT),
            KeySpec("his_chandra", "ँ", "ं"),
            KeySpec("his_nna", "ण", "म"),
            KeySpec("his_tta", "ट", "ठ"),
            KeySpec("his_dda", "ड", "ढ"),
            KeySpec("his_sha", "श", "ष"),
            KeySpec("his_ssha", "ष", "स"),
            KeySpec("his_ksha", "क्ष", "त्र", listOf("क्ष", "त्र", "ज्ञ", "श्र")),
            KeySpec("backspace", "⌫", weight = 1.35f, type = KeyType.BACKSPACE)
        ),
        hindiRows[3]
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

    fun rowsForAlpha(language: KeyboardLanguage, isHindiShifted: Boolean): List<List<KeySpec>> {
        return when (language) {
            KeyboardLanguage.HINDI_DEVANAGARI -> if (isHindiShifted) hindiShiftRows else hindiRows
            KeyboardLanguage.ENGLISH_US, KeyboardLanguage.HINGLISH -> qwertyRows
        }
    }
}
