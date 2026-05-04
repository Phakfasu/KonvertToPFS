package org.phakfasu.konverttopfs

object KPPinyin {
    private val initials = listOf("ng", "b", "p", "m", "f", "v", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "z", "c", "s")
    
    fun parseInput(s: String): HakkaSyllable? {
        val tone = s.lastOrNull()?.digitToIntOrNull() ?: if (s.endsWith("b") || s.endsWith("d") || s.endsWith("g")) 5 else 1
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
        
        // Normalize j/q/x to z/c/s
        val internalInitial = when (initial) {
            "j" -> "z"
            "q" -> "c"
            "x" -> "s"
            else -> initial
        }
        
        return HakkaSyllable(internalInitial, rhyme, tone)
    }
    
    fun renderInput(s: HakkaSyllable): String {
        // Restore j/q/x if rhyme starts with 'i' but not 'ii'
        val isIVowel = s.rhyme.startsWith("i") && s.rhyme != "ii"
        val initial = when (s.initial) {
            "z" -> if (isIVowel) "j" else "z"
            "c" -> if (isIVowel) "q" else "c"
            "s" -> if (isIVowel) "x" else "s"
            else -> s.initial
        }
        return "$initial${s.rhyme}${s.tone}"
    }

    fun parseUnicode(s: String): HakkaSyllable? {
        // Map unicode diacritics to tone numbers
        var tone = 1
        var str = s
        
        if (str.contains("ˊ")) { tone = 1; str = str.replace("ˊ", "") }
        else if (str.contains("ˇ")) { tone = 3; str = str.replace("ˇ", "") }
        else if (str.contains("ˋ")) {
            str = str.replace("ˋ", "")
            tone = if (str.endsWith("b") || str.endsWith("d") || str.endsWith("g")) 6 else 4
        } else {
            tone = if (str.endsWith("b") || str.endsWith("d") || str.endsWith("g")) 5 else 2
        }

        // Now we just parse the base
        return parseInput(str + tone.toString())
    }

    fun renderUnicode(s: HakkaSyllable): String {
        val base = renderInput(s).dropLast(1)
        val mark = when (s.tone) {
            1 -> "ˊ"
            2 -> ""
            3 -> "ˇ"
            4 -> "ˋ"
            5 -> ""
            6 -> "ˋ"
            else -> ""
        }
        // In KPPY, tone marks usually go at the end of the syllable
        return base + mark
    }
}
