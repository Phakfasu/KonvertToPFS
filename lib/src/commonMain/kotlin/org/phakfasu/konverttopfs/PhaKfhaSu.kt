package org.phakfasu.konverttopfs

object PhaKfhaSu {
    private val initials = listOf("chh", "ch", "ph", "th", "kh", "ng", "p", "m", "f", "v", "t", "n", "l", "k", "h", "s")
    
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
        
        // Map to KPPY internal
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

    fun renderInput(s: HakkaSyllable): String {
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
        
        return "$initial$rhyme${s.tone}"
    }

    fun parseUnicode(s: String): HakkaSyllable? {
        var tone = 1
        var str = s
        
        if (str.contains("ˆ")) { tone = 2; str = str.replace("ˆ", "") }
        else if (str.contains("`")) { tone = 3; str = str.replace("`", "") }
        else if (str.contains("ˊ")) { tone = 4; str = str.replace("ˊ", "") }
        else if (str.contains("̍")) { tone = 6; str = str.replace("̍", "") }
        else if (str.contains("â")) { tone = 2; str = str.replace("â", "a") }
        else if (str.contains("ê")) { tone = 2; str = str.replace("ê", "e") }
        else if (str.contains("î")) { tone = 2; str = str.replace("î", "i") }
        else if (str.contains("ô")) { tone = 2; str = str.replace("ô", "o") }
        else if (str.contains("û")) { tone = 2; str = str.replace("û", "u") }
        else if (str.contains("à")) { tone = 3; str = str.replace("à", "a") }
        else if (str.contains("è")) { tone = 3; str = str.replace("è", "e") }
        else if (str.contains("ì")) { tone = 3; str = str.replace("ì", "i") }
        else if (str.contains("ò")) { tone = 3; str = str.replace("ò", "o") }
        else if (str.contains("ù")) { tone = 3; str = str.replace("ù", "u") }
        else if (str.contains("á")) { tone = 4; str = str.replace("á", "a") }
        else if (str.contains("é")) { tone = 4; str = str.replace("é", "e") }
        else if (str.contains("í")) { tone = 4; str = str.replace("í", "i") }
        else if (str.contains("ó")) { tone = 4; str = str.replace("ó", "o") }
        else if (str.contains("ú")) { tone = 4; str = str.replace("ú", "u") }
        else {
            tone = if (str.endsWith("p") || str.endsWith("t") || str.endsWith("k")) 5 else 1
        }
        
        return parseInput(str + tone.toString())
    }

    fun renderUnicode(s: HakkaSyllable): String {
        val base = renderInput(s).dropLast(1)
        if (s.tone == 1 || s.tone == 5) return base
        
        // Very basic tone placement: find first vowel and replace
        val vowels = listOf("a", "e", "i", "o", "u", "ṳ")
        var firstVowelIndex = -1
        for (i in base.indices) {
            if (vowels.any { base.startsWith(it, i) }) {
                firstVowelIndex = i
                break
            }
        }
        
        if (firstVowelIndex == -1) {
            // For syllabic m or ng
            if (s.tone == 6) {
                return base.replace("m", "m̍").replace("ng", "n̍g")
            }
            return base
        }
        
        val v = base[firstVowelIndex]
        val accented = when (s.tone) {
            2 -> when (v) { 'a' -> "â"; 'e' -> "ê"; 'i' -> "î"; 'o' -> "ô"; 'u' -> "û"; else -> "${v}ˆ" }
            3 -> when (v) { 'a' -> "à"; 'e' -> "è"; 'i' -> "ì"; 'o' -> "ò"; 'u' -> "ù"; else -> "${v}`" }
            4 -> when (v) { 'a' -> "á"; 'e' -> "é"; 'i' -> "í"; 'o' -> "ó"; 'u' -> "ú"; else -> "${v}ˊ" }
            6 -> "${v}̍" // combining vertical line above
            else -> v.toString()
        }
        
        return base.substring(0, firstVowelIndex) + accented + base.substring(firstVowelIndex + 1)
    }
}
