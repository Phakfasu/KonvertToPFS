package org.phakfasu.konverttopfs

/**
 * Parser and renderer for KPPY (Kàu-pō͘ Phin-yîm / 教育部客家語拼音方案),
 * optimized for the Si-yen (四縣腔) dialect.
 *
 * The unicode form uses modifier letters at the end of the syllable (`ˊ ˇ ˋ`) as the
 * canonical output. Precomposed Latin diacritics on the vowel (`á ǎ à` …) are also
 * accepted on input as a tolerant alternative.
 */
internal object Kppy {
    // IMPORTANT: longer prefixes must precede shorter ones so the linear scan finds
    // them first. Critical case: "ng" must come before "n" and "g"; otherwise the
    // single-letter scan matches "n" first and leaves a stray "g" in the rhyme.
    private val initials = listOf("ng", "b", "p", "m", "f", "v", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "z", "c", "s")

    // KPPY tone-marker assignment (per README §Mapping):
    //   T1 → ˊ        T2 → ˇ        T3 → ˋ        T4 → (none)
    //   T5 → (none) on checked   (e.g. pag)       T6 → ˋ on checked   (e.g. hagˋ)
    // This library aligns KPPY digit numbering with PFS — T5 is the high-pitched
    // checked tone; T6 is the low-pitched checked tone. The official 教育部 KPPY 號
    // reverses these two slots; this library does not follow that convention.
    // See README §Tones.
    private const val TONE_ACUTE_MOD = "ˊ"  // U+02CA — T1
    private const val TONE_CARON = "ˇ"      // U+02C7 — T2
    private const val TONE_GRAVE_MOD = "ˋ"  // U+02CB — T3 (open) / T6 (checked)

    // NFD-form combining marks visually indistinguishable from the modifier letters.
    private const val COMB_ACUTE = "́"      // U+0301 — T1
    private const val COMB_CARON = "̌"      // U+030C — T2
    private const val COMB_GRAVE = "̀"      // U+0300 — T3 / T6

    private val checkedFinals = listOf("b", "d", "g", "p", "t", "k")

    private val acuteMap = mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u')
    private val caronMap = mapOf('ǎ' to 'a', 'ě' to 'e', 'ǐ' to 'i', 'ǒ' to 'o', 'ǔ' to 'u')
    private val graveMap = mapOf('à' to 'a', 'è' to 'e', 'ì' to 'i', 'ò' to 'o', 'ù' to 'u')

    /**
     * Parses a single KPPY-input syllable (e.g. `hag6`, `ji4`, `ng2`).
     *
     * Returns `null` if the input is empty, has an out-of-range tone digit, contains
     * non-letter characters in the rhyme, or violates Si-yen phonotactics
     * (tones 5/6 require a stop-final coda; tones 1–4 forbid one;
     * syllabic m/ng cannot carry checked tones 5/6).
     *
     * When no trailing digit is present, defaults to T5 for stop-final
     * syllables (high-pitched checked) and T4 for open/nasal.
     */
    fun parseInput(s: String): HakfaSyllable? {
        if (s.isEmpty()) return null
        // ASCII digits only — non-ASCII decimal digits (Arabic-Indic, Devanagari, …)
        // must not be read as tone numbers; the token should pass through unchanged.
        val lastAsciiDigit = s.lastOrNull()?.takeIf { it in '0'..'9' }
        val kppyTone = lastAsciiDigit?.digitToIntOrNull()
            ?: if (endsWithChecked(s)) 5 else 4
        if (kppyTone !in 1..6) return null
        val str = if (lastAsciiDigit != null) s.dropLast(1) else s
        if (str.isEmpty()) return null

        var initial = ""
        var rhyme = str
        for (i in initials) {
            if (str.startsWith(i)) {
                initial = i
                rhyme = str.drop(i.length)
                break
            }
        }

        // Normalize j/q/x to z/c/s for the unified internal representation.
        val internalInitial = when (initial) {
            "j" -> "z"
            "q" -> "c"
            "x" -> "s"
            else -> initial
        }

        if (!isValidRhyme(rhyme, initial)) return null

        // Tolerate PFS-style stop codas on input by canonicalizing to KPPY's b/d/g.
        // Without this, "kap5" would store rhyme="ap" and later render as KPPY "gap"
        // instead of the canonical "gab".
        val canonicalRhyme = when {
            rhyme.endsWith("p") -> rhyme.dropLast(1) + "b"
            rhyme.endsWith("t") -> rhyme.dropLast(1) + "d"
            rhyme.endsWith("k") && !rhyme.endsWith("ng") -> rhyme.dropLast(1) + "g"
            else -> rhyme
        }

        // Library uses unified PFS-style numbering for both PFS and KPPY digits:
        // T5 = high-pitched checked, T6 = low-pitched checked. No swap.
        val internalTone = kppyTone

        // Si-yen phonotactics:
        //   - Ordinary syllables: tones 5/6 require a stop-final coda; tones 1-4 forbid one.
        //   - Syllabic m/ng: rejected for tones 5/6 because checked tones are defined by
        //     a stop coda, which a syllabic nasal lacks; also their KPPY_UNICODE form would
        //     be indistinguishable from tones 4/3 (ˋ alone cannot disambiguate), making
        //     parse/render lossy.
        val isSyllabic = canonicalRhyme.isEmpty()
        if (isSyllabic) {
            if (internalTone in 5..6) return null
        } else {
            val hasCheckedCoda = endsWithChecked(canonicalRhyme)
            if (internalTone in 5..6 && !hasCheckedCoda) return null
            if (internalTone in 1..4 && hasCheckedCoda) return null
        }

        return HakfaSyllable(internalInitial, canonicalRhyme, internalTone)
    }

    fun renderBase(s: HakfaSyllable): String {
        // Restore j/q/x when rhyme is an i-initial vowel that is NOT the ṳ vowel ("ii").
        // The ṳ vowel may stand alone or carry codas (iim/iin/iib/iid/iig), and z/c/s are kept in those cases.
        val isIVowel = s.rhyme.startsWith("i") && !s.rhyme.startsWith("ii")
        val initial = when (s.initial) {
            "z" -> if (isIVowel) "j" else "z"
            "c" -> if (isIVowel) "q" else "c"
            "s" -> if (isIVowel) "x" else "s"
            else -> s.initial
        }
        return "$initial${s.rhyme}"
    }

    fun renderInput(s: HakfaSyllable): String {
        return "${renderBase(s)}${s.tone}"
    }

    /**
     * Parses a single KPPY-unicode syllable (e.g. `hag`, `gaˊ`, `gaˇ`, `gaˋ`, or the tolerant
     * Latin-diacritic forms `gá`, `gǎ`, `gà`).
     *
     * Returns `null` for the same reasons as [parseInput].
     */
    fun parseUnicode(s: String): HakfaSyllable? {
        if (s.isEmpty()) return null
        var str = s
        val kppyTone: Int

        when {
            TONE_ACUTE_MOD in str -> { kppyTone = 1; str = str.replace(TONE_ACUTE_MOD, "") }
            TONE_CARON in str -> { kppyTone = 2; str = str.replace(TONE_CARON, "") }
            TONE_GRAVE_MOD in str -> {
                str = str.replace(TONE_GRAVE_MOD, "")
                kppyTone = if (endsWithChecked(str)) 6 else 3  // ˋ-on-checked = T6 (low-pitched checked)
            }
            COMB_ACUTE in str -> { kppyTone = 1; str = str.replace(COMB_ACUTE, "") }
            COMB_CARON in str -> { kppyTone = 2; str = str.replace(COMB_CARON, "") }
            COMB_GRAVE in str -> {
                str = str.replace(COMB_GRAVE, "")
                kppyTone = if (endsWithChecked(str)) 6 else 3
            }
            containsAny(str, acuteMap.keys) -> { kppyTone = 1; str = stripDiacritics(str, acuteMap) }
            containsAny(str, caronMap.keys) -> { kppyTone = 2; str = stripDiacritics(str, caronMap) }
            containsAny(str, graveMap.keys) -> {
                str = stripDiacritics(str, graveMap)
                kppyTone = if (endsWithChecked(str)) 6 else 3
            }
            else -> kppyTone = if (endsWithChecked(str)) 5 else 4  // unmarked checked = T5 (high-pitched checked)
        }

        return parseInput(str + kppyTone.toString())
    }

    fun renderUnicode(s: HakfaSyllable): String {
        val base = renderBase(s)
        val mark = when (s.tone) {
            1 -> TONE_ACUTE_MOD
            2 -> TONE_CARON
            3 -> TONE_GRAVE_MOD
            4 -> ""
            5 -> ""              // T5 (high-pitched checked) → unmarked on checked
            6 -> TONE_GRAVE_MOD  // T6 (low-pitched checked) → ˋ on checked
            else -> ""
        }
        return base + mark
    }

    private fun endsWithChecked(str: String): Boolean {
        // Syllabic m / ng have no coda at all — they are their own nucleus.
        // The trailing "g" of "ng" is part of the initial (or coda nasal),
        // not a stop-final. Likewise the nasal coda "ng" (e.g. "iang", "ong", "ngiang")
        // ends in "g" but is not a stop-final.
        if (str == "m" || str == "ng") return false
        if (str.endsWith("ng")) return false
        return checkedFinals.any { str.endsWith(it) }
    }

    private fun containsAny(str: String, chars: Set<Char>): Boolean = str.any { it in chars }

    private fun stripDiacritics(str: String, map: Map<Char, Char>): String =
        buildString(str.length) { str.forEach { append(map[it] ?: it) } }

    // Characters that can legally appear in a Si-yen KPPY rhyme:
    //   - vowels: a, e, i, o, u (the close-central vowel ṳ is written "ii" in KPPY)
    //   - 'r' for the rare 'er' digraph
    //   - nasal codas: m, n, plus 'g' (which appears in nasal -ng and stop -g)
    //   - KPPY stop codas: b, d, g (canonical)
    //   - PFS-style stop codas: p, t, k (tolerated on input)
    // All other a-z letters (c, f, h, j, l, q, s, v, w, x, y, z) cannot legally
    // appear in a Si-yen rhyme.
    private val rhymeChars = setOf('a', 'e', 'i', 'o', 'u', 'r', 'm', 'n', 'g', 'b', 'd', 'p', 't', 'k')
    private val rhymeNuclei = setOf('a', 'e', 'i', 'o', 'u')

    private fun isValidRhyme(rhyme: String, initial: String): Boolean {
        // Rhyme may be empty only when the initial is syllabic (m or ng).
        if (rhyme.isEmpty()) return initial == "m" || initial == "ng"
        // A Si-yen rhyme must begin with a vowel.
        if (rhyme[0] !in rhymeNuclei) return false
        return rhyme.all { it in rhymeChars }
    }
}
