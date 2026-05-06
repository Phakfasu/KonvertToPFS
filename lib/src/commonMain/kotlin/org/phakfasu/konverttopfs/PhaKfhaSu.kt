package org.phakfasu.konverttopfs

object PhaKfhaSu {
    private val initials = listOf("chh", "ch", "ph", "th", "kh", "ng", "p", "m", "f", "v", "t", "n", "l", "k", "h", "s")

    private const val COMB_CIRCUMFLEX = "̂"
    private const val COMB_GRAVE = "̀"
    private const val COMB_ACUTE = "́"
    private const val COMB_VLINE = "̍"

    fun parseInput(s: String): HakkaSyllable? {
        val tone = s.lastOrNull()?.digitToIntOrNull() ?: if (s.endsWith("p") || s.endsWith("t") || s.endsWith("k")) 5 else 1
        val str = if (s.lastOrNull()?.isDigit() == true) s.dropLast(1) else s

        var initial = ""
        var rhyme = str
        for (i in initials) {
            if (str.startsWith(i)) {
                initial = i
                rhyme = str.drop(i.length)
                break
            }
        }

        val internalInitial = when (initial) {
            "p" -> "b"
            "ph" -> "p"
            "m" -> "m"
            "f" -> "f"
            "v" -> "v"
            "t" -> "d"
            "th" -> "t"
            "n" -> "n"
            "l" -> "l"
            "k" -> "g"
            "kh" -> "k"
            "ng" -> "ng"
            "h" -> "h"
            "ch" -> "z"
            "chh" -> "c"
            "s" -> "s"
            else -> ""
        }

        var internalRhyme = rhyme
        internalRhyme = internalRhyme.replace("ṳ", "ii")
        if (internalRhyme.endsWith("p")) internalRhyme = internalRhyme.dropLast(1) + "b"
        if (internalRhyme.endsWith("t")) internalRhyme = internalRhyme.dropLast(1) + "d"
        if (internalRhyme.endsWith("k")) internalRhyme = internalRhyme.dropLast(1) + "g"

        return HakkaSyllable(internalInitial, internalRhyme, tone)
    }

    fun renderBase(s: HakkaSyllable): String {
        val initial = when (s.initial) {
            "b" -> "p"
            "p" -> "ph"
            "d" -> "t"
            "t" -> "th"
            "g" -> "k"
            "k" -> "kh"
            "z" -> "ch"
            "c" -> "chh"
            else -> s.initial
        }

        var rhyme = s.rhyme.replace("ii", "ṳ")
        if (rhyme.endsWith("b")) rhyme = rhyme.dropLast(1) + "p"
        if (rhyme.endsWith("d")) rhyme = rhyme.dropLast(1) + "t"
        if (rhyme.endsWith("g")) rhyme = rhyme.dropLast(1) + "k"

        return "$initial$rhyme"
    }

    fun renderInput(s: HakkaSyllable): String = "${renderBase(s)}${s.tone}"

    fun parseUnicode(s: String): HakkaSyllable? {
        var str = s
        val tone: Int

        when {
            COMB_VLINE in str -> { tone = 6; str = str.replace(COMB_VLINE, "") }
            COMB_CIRCUMFLEX in str -> { tone = 2; str = str.replace(COMB_CIRCUMFLEX, "") }
            COMB_GRAVE in str -> { tone = 3; str = str.replace(COMB_GRAVE, "") }
            COMB_ACUTE in str -> { tone = 4; str = str.replace(COMB_ACUTE, "") }
            "â" in str -> { tone = 2; str = str.replace("â", "a") }
            "ê" in str -> { tone = 2; str = str.replace("ê", "e") }
            "î" in str -> { tone = 2; str = str.replace("î", "i") }
            "ô" in str -> { tone = 2; str = str.replace("ô", "o") }
            "û" in str -> { tone = 2; str = str.replace("û", "u") }
            "à" in str -> { tone = 3; str = str.replace("à", "a") }
            "è" in str -> { tone = 3; str = str.replace("è", "e") }
            "ì" in str -> { tone = 3; str = str.replace("ì", "i") }
            "ò" in str -> { tone = 3; str = str.replace("ò", "o") }
            "ù" in str -> { tone = 3; str = str.replace("ù", "u") }
            "á" in str -> { tone = 4; str = str.replace("á", "a") }
            "é" in str -> { tone = 4; str = str.replace("é", "e") }
            "í" in str -> { tone = 4; str = str.replace("í", "i") }
            "ó" in str -> { tone = 4; str = str.replace("ó", "o") }
            "ú" in str -> { tone = 4; str = str.replace("ú", "u") }
            else -> tone = if (str.endsWith("p") || str.endsWith("t") || str.endsWith("k")) 5 else 1
        }

        return parseInput(str + tone.toString())
    }

    fun renderUnicode(s: HakkaSyllable): String {
        val base = renderBase(s)
        if (s.tone == 1 || s.tone == 5) return base

        val vowels = listOf("ṳ", "a", "e", "i", "o", "u")
        var firstVowelIndex = -1
        var firstVowelLength = 0
        outer@ for (i in base.indices) {
            for (v in vowels) {
                if (base.startsWith(v, i)) {
                    firstVowelIndex = i
                    firstVowelLength = v.length
                    break@outer
                }
            }
        }

        if (firstVowelIndex == -1) {
            // Syllabic m or ng
            if (s.tone == 6) {
                return when (base) {
                    "ng" -> "n${COMB_VLINE}g"
                    else -> base + COMB_VLINE
                }
            }
            return base
        }

        val vowel = base.substring(firstVowelIndex, firstVowelIndex + firstVowelLength)
        val accented = applyTone(vowel, s.tone)
        return base.substring(0, firstVowelIndex) + accented + base.substring(firstVowelIndex + firstVowelLength)
    }

    private fun applyTone(vowel: String, tone: Int): String {
        if (vowel.length == 1 && vowel[0] in "aeiou") {
            return when (tone) {
                2 -> when (vowel) { "a" -> "â"; "e" -> "ê"; "i" -> "î"; "o" -> "ô"; "u" -> "û"; else -> vowel }
                3 -> when (vowel) { "a" -> "à"; "e" -> "è"; "i" -> "ì"; "o" -> "ò"; "u" -> "ù"; else -> vowel }
                4 -> when (vowel) { "a" -> "á"; "e" -> "é"; "i" -> "í"; "o" -> "ó"; "u" -> "ú"; else -> vowel }
                6 -> vowel + COMB_VLINE
                else -> vowel
            }
        }
        return when (tone) {
            2 -> vowel + COMB_CIRCUMFLEX
            3 -> vowel + COMB_GRAVE
            4 -> vowel + COMB_ACUTE
            6 -> vowel + COMB_VLINE
            else -> vowel
        }
    }
}
