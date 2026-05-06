package org.phakfasu.konverttopfs

object KonvertToPfs {
    fun convert(text: String, from: LomajiFormat, to: LomajiFormat): String {
        if (from == to) return text

        val words = text.split(Regex("(?=[\\s\\p{P}])|(?<=[\\s\\p{P}])"))

        return words.joinToString("") { word ->
            if (word.isBlank() || word.all { !it.isLetter() && !it.isDigit() }) {
                word
            } else {
                convertWord(word, from, to)
            }
        }
    }

    private fun convertWord(word: String, from: LomajiFormat, to: LomajiFormat): String {
        val parts = word.split("-")
        val rendered = parts.map { part ->
            val casing = detectCasing(part)
            val syllable = parseSyllable(part.lowercase(), from) ?: return word
            applyCasing(renderSyllable(syllable, to), casing)
        }
        return rendered.joinToString("-")
    }

    private fun parseSyllable(syllableStr: String, format: LomajiFormat): HakkaSyllable? = when (format) {
        LomajiFormat.KPPY_INPUT -> KPPinyin.parseInput(syllableStr)
        LomajiFormat.PFS_INPUT -> PhaKfhaSu.parseInput(syllableStr)
        LomajiFormat.KPPY_UNICODE -> KPPinyin.parseUnicode(syllableStr)
        LomajiFormat.PFS_UNICODE -> PhaKfhaSu.parseUnicode(syllableStr)
    }

    private fun renderSyllable(s: HakkaSyllable, format: LomajiFormat): String = when (format) {
        LomajiFormat.KPPY_INPUT -> KPPinyin.renderInput(s)
        LomajiFormat.PFS_INPUT -> PhaKfhaSu.renderInput(s)
        LomajiFormat.KPPY_UNICODE -> KPPinyin.renderUnicode(s)
        LomajiFormat.PFS_UNICODE -> PhaKfhaSu.renderUnicode(s)
    }

    private enum class Casing { LOWER, TITLE, UPPER }

    private fun detectCasing(s: String): Casing {
        // Only consider cased letters; modifier-letter tone marks (Lm) and combining marks have no case.
        val letters = s.filter { it.isLetter() && (it.isUpperCase() || it.isLowerCase()) }
        if (letters.isEmpty() || letters.none { it.isUpperCase() }) return Casing.LOWER
        if (letters.all { it.isUpperCase() } && letters.length > 1) return Casing.UPPER
        return Casing.TITLE
    }

    private fun applyCasing(s: String, casing: Casing): String = when (casing) {
        Casing.LOWER -> s
        Casing.UPPER -> s.uppercase()
        Casing.TITLE -> s.replaceFirstChar { it.uppercase() }
    }
}
