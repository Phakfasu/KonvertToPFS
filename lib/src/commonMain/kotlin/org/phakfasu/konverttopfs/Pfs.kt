package org.phakfasu.konverttopfs

/**
 * Parser and renderer for PFS (Pha̍k-fa-sṳ / 白話字), the 長老教會 (Presbyterian)
 * Roman Orthography system for Hakfa, optimized for the Si-yen (四縣腔) dialect.
 *
 * The unicode form uses combining marks on the rhyme nucleus (̂ ̀ ́ ̍) and the
 * special vowel ṳ (U+1E73). NFC and NFD inputs are both accepted; output uses the
 * library-canonical mix (precomposed where Unicode provides one, otherwise
 * combining marks).
 */
internal object Pfs {
    // IMPORTANT: longer prefixes must precede shorter ones so the linear scan finds
    // them first (e.g. "chh" before "ch", "tsh" before "ts", "ph"/"th"/"kh" before
    // "p"/"t"/"k", "ng" before "n"). Reordering this list is a phonotactic hazard.
    private val initials = listOf("chh", "ch", "tsh", "ts", "ph", "th", "kh", "ng", "p", "m", "f", "v", "t", "n", "l", "k", "h", "s")

    private const val COMB_CIRCUMFLEX = "̂"  // ̂  combining circumflex above (Tone 1)
    private const val COMB_GRAVE = "̀"       // ̀  combining grave accent (Tone 2)
    private const val COMB_ACUTE = "́"       // ́  combining acute accent (Tone 3)
    private const val COMB_VLINE = "̍"       // ̍  combining vertical line above (Tone 5)
    private const val COMB_DIAERESIS_BELOW = "̤"  // fold u + U+0324 → ṳ before tone-mark detection

    private val checkedFinals = listOf("p", "t", "k")

    private val circumflexMap = mapOf('â' to 'a', 'ê' to 'e', 'î' to 'i', 'ô' to 'o', 'û' to 'u')
    private val graveMap = mapOf('à' to 'a', 'è' to 'e', 'ì' to 'i', 'ò' to 'o', 'ù' to 'u')
    private val acuteMap = mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u')

    // Characters that can legally appear in a Si-yen PFS rhyme:
    //   - vowels: a, e, i, o, u (and ṳ as a single codepoint, or the digraph "ii")
    //   - 'r' for the rare 'er' digraph
    //   - nasal codas: m, n, plus 'g' (which appears in nasal -ng and stop -g)
    //   - PFS stop codas: p, t, k (canonical input)
    //   - KPPY-style stop codas: b, d, g (tolerated on input)
    // All other a-z letters (c, f, h, j, l, q, s, v, w, x, y, z) cannot legally
    // appear in a Si-yen rhyme and should cause the token to be passed through
    // unchanged rather than silently producing an invalid syllable.
    private val rhymeChars = setOf('a', 'e', 'i', 'o', 'u', 'ṳ', 'r', 'm', 'n', 'g', 'b', 'd', 'p', 't', 'k')
    private val rhymeNuclei = setOf('a', 'e', 'i', 'o', 'u', 'ṳ')

    /**
     * Parses a single PFS-input syllable (e.g. `hak5`, `chii1`, `ng3`). The close-central
     * vowel may be written either as the ASCII digraph `ii` (canonical) or as `ṳ` (tolerant).
     *
     * Returns `null` if the input is empty, has an out-of-range tone digit, contains
     * non-letter characters in the rhyme, or violates Si-yen phonotactics
     * (tones 5/6 require a stop-final coda; tones 1–4 forbid one;
     * syllabic m/ng cannot carry checked tones 5/6).
     */
    fun parseInput(s: String): HakfaSyllable? {
        if (s.isEmpty()) return null
        val normalized = foldYOnglide(normalize(s))
        // ASCII digits only — non-ASCII decimal digits (Arabic-Indic, Devanagari, …)
        // must not be read as tone numbers; the token should pass through unchanged.
        val lastAsciiDigit = normalized.lastOrNull()?.takeIf { it in '0'..'9' }
        val tone = lastAsciiDigit?.digitToIntOrNull()
            ?: if (endsWithChecked(normalized)) 6 else 4
        if (tone !in 1..6) return null
        val str = if (lastAsciiDigit != null) normalized.dropLast(1) else normalized
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
            "ts" -> "z"
            "tsh" -> "c"
            "s" -> "s"
            else -> ""
        }

        if (!isValidRhyme(rhyme, initial)) return null

        var internalRhyme = rhyme
        internalRhyme = internalRhyme.replace("ṳ", "ii")
        if (internalRhyme.endsWith("p")) internalRhyme = internalRhyme.dropLast(1) + "b"
        if (internalRhyme.endsWith("t")) internalRhyme = internalRhyme.dropLast(1) + "d"
        if (internalRhyme.endsWith("k")) internalRhyme = internalRhyme.dropLast(1) + "g"
        // PFS writes the rising u-onglide uniformly as `oV` (瓜 kôa, 關 koân, 怪 koái,
        // 國 koet); KPPY (and the internal form) writes them as `uV` (gua, guan, guai,
        // gued). Fold here so the internal form is canonical. KPPY's `oi` is a different
        // rhyme (愛 oi, /oi/) — only `oa-`/`oe-` shift, not bare `o-`.
        if (internalRhyme.startsWith("oa")) internalRhyme = "ua" + internalRhyme.drop(2)
        if (internalRhyme.startsWith("oe")) internalRhyme = "ue" + internalRhyme.drop(2)

        // Si-yen phonotactics:
        //   - Ordinary syllables: tones 5/6 require a stop-final coda; tones 1-4 forbid one.
        //   - Syllabic m/ng: rejected for tones 5/6 because checked tones are defined by
        //     a stop coda, which a syllabic nasal lacks; also their PFS_UNICODE form would
        //     be indistinguishable from tone 4 (m6 → "m" collides with m4 → "m"), making
        //     parse/render lossy.
        // Note: trailing 'g' is a stop coda only when it is not part of the nasal "ng" coda.
        val isSyllabic = rhyme.isEmpty()
        if (isSyllabic) {
            if (tone in 5..6) return null
        } else {
            val hasCheckedCoda = internalRhyme.endsWith("b") || internalRhyme.endsWith("d") ||
                (internalRhyme.endsWith("g") && !internalRhyme.endsWith("ng"))
            if (tone in 5..6 && !hasCheckedCoda) return null
            if (tone in 1..4 && hasCheckedCoda) return null
        }

        return HakfaSyllable(internalInitial, internalRhyme, tone)
    }

    fun renderBase(s: HakfaSyllable): String = renderBase(s, useUnicodeVowel = true)

    // PFS_INPUT keeps the ASCII digraph `ii` for the close-central vowel; ṳ is the PFS_UNICODE form.
    internal fun renderBase(s: HakfaSyllable, useUnicodeVowel: Boolean): String {
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

        var rhyme = if (useUnicodeVowel) s.rhyme.replace("ii", "ṳ") else s.rhyme
        if (rhyme.endsWith("b")) rhyme = rhyme.dropLast(1) + "p"
        if (rhyme.endsWith("d")) rhyme = rhyme.dropLast(1) + "t"
        // Trailing 'g' is a stop-final only when not part of the nasal "ng" coda.
        if (rhyme.endsWith("g") && !rhyme.endsWith("ng")) rhyme = rhyme.dropLast(1) + "k"
        // PFS writes the rising u-onglide uniformly as `oV` — open `ua`/`ue` and
        // closed/triphthong forms all shift (瓜 kôa, 關 koân, 怪 koái, 國 koet).
        // KPPY (and the internal form) writes them uniformly as `uV`.
        if (rhyme.startsWith("ua")) rhyme = "oa" + rhyme.drop(2)
        if (rhyme.startsWith("ue")) rhyme = "oe" + rhyme.drop(2)
        // PFS convention: a zero-initial syllable beginning with `i` is written with `y`:
        //   - `i` as onglide (followed by another vowel): replace with `y`
        //     (ia → ya, iu → yu, iong → yong, iam → yam).
        //   - `i` as nucleus (bare `i`, `i`+nasal, `i`+stop): prefix with `y`
        //     (i → yi, im → yim, in → yin, ip → yip).
        //   - close-central `ii` (PFS_UNICODE `ṳ`) keeps the `i`-spelling.
        if (initial.isEmpty() && rhyme.isNotEmpty() && rhyme[0] == 'i') {
            val isIi = rhyme.length >= 2 && rhyme[1] == 'i'
            val isOnglide = rhyme.length >= 2 && rhyme[1] in "aeou"
            rhyme = when {
                isIi -> rhyme
                isOnglide -> "y" + rhyme.drop(1)
                else -> "y" + rhyme
            }
        }

        return "$initial$rhyme"
    }

    fun renderInput(s: HakfaSyllable): String = "${renderBase(s, useUnicodeVowel = false)}${s.tone}"

    /**
     * Parses a single PFS-unicode syllable (e.g. `hak`, `kâ`, `chṳ̂`, `a̍p`).
     *
     * Accepts both NFC (`â`) and NFD (`a` + combining mark) forms of accented vowels,
     * and folds NFD `u` + COMB DIAERESIS BELOW to the canonical `ṳ`.
     * An unmarked stop-final syllable (no diacritic, e.g. `hak`) defaults to Tone 6
     * (PFS unmarked-checked); Tone 5 requires the explicit vline diacritic.
     * Returns `null` for the same reasons as [parseInput].
     */
    fun parseUnicode(s: String): HakfaSyllable? {
        if (s.isEmpty()) return null
        var str = normalize(s)
        val tone: Int

        when {
            COMB_VLINE in str -> { tone = 5; str = str.replace(COMB_VLINE, "") }
            COMB_CIRCUMFLEX in str -> { tone = 1; str = str.replace(COMB_CIRCUMFLEX, "") }
            COMB_GRAVE in str -> { tone = 2; str = str.replace(COMB_GRAVE, "") }
            COMB_ACUTE in str -> { tone = 3; str = str.replace(COMB_ACUTE, "") }
            containsAny(str, circumflexMap.keys) -> { tone = 1; str = stripDiacritics(str, circumflexMap) }
            containsAny(str, graveMap.keys) -> { tone = 2; str = stripDiacritics(str, graveMap) }
            containsAny(str, acuteMap.keys) -> { tone = 3; str = stripDiacritics(str, acuteMap) }
            else -> tone = if (endsWithChecked(str)) 6 else 4
        }

        return parseInput(str + tone.toString())
    }

    fun renderUnicode(s: HakfaSyllable): String {
        val base = renderBase(s)
        if (s.tone == 4 || s.tone == 6) return base

        val (nucleusIndex, nucleusLength) = findNucleus(base)

        if (nucleusIndex == -1) {
            // Syllabic m or ng — apply the tone diacritic to the syllable nucleus.
            // Tones 5 and 6 are rejected for syllabic nasals at parse time
            // (see parseInput), so only tones 1, 2, 3 reach this branch.
            return when (s.tone) {
                1 -> if (base == "ng") "n${COMB_CIRCUMFLEX}g" else base + COMB_CIRCUMFLEX
                2 -> if (base == "ng") "n${COMB_GRAVE}g" else base + COMB_GRAVE
                3 -> if (base == "ng") "n${COMB_ACUTE}g" else base + COMB_ACUTE
                else -> base
            }
        }

        val vowel = base.substring(nucleusIndex, nucleusIndex + nucleusLength)
        val accented = applyTone(vowel, s.tone)
        return base.substring(0, nucleusIndex) + accented + base.substring(nucleusIndex + nucleusLength)
    }

    // PFS tone-mark placement (fixed canonical rule):
    //   1. Single vowel → mark that vowel.
    //   2. No vowel → syllabic nasal (handled by the caller in renderUnicode).
    //   3. Compound vowels → mark the 2nd letter from the right; the final -ng coda
    //      counts as 1 unit.
    //      Exception 1: if the 2nd-from-right letter is `i`, mark the rightmost
    //                   letter instead (`liù`, `siá`).
    //      Exception 2: in a checked syllable, if the 2nd-from-right letter is
    //                   `i` or `u` and the vowel cluster is NOT `iu` immediately
    //                   before the stop coda, mark the 3rd letter from the right.
    //      Special:     `iu` + stop coda → normal 2nd-from-right (e.g. `liu̍k`).
    //
    // ṳ counts as a single vowel letter; the trema-below (U+0324) stays on `u` and
    // the tone-combining mark stacks on top via NFC.
    private fun findNucleus(base: String): Pair<Int, Int> {
        val initial = initials.firstOrNull { base.startsWith(it) } ?: ""
        val rhymeStart = initial.length
        val rhyme = base.substring(rhymeStart)
        if (rhyme.isEmpty()) return -1 to 0

        val vowelIndices = rhyme.indices.filter { rhyme[it] in "aeiouṳ" }
        if (vowelIndices.isEmpty()) return -1 to 0
        if (vowelIndices.size == 1) return (rhymeStart + vowelIndices[0]) to 1

        // Build letter units; final -ng (when it is the coda) is one unit of length 2.
        val units = mutableListOf<Pair<Int, Int>>()
        var i = 0
        while (i < rhyme.length) {
            if (i == rhyme.length - 2 && rhyme[i] == 'n' && rhyme[i + 1] == 'g') {
                units.add(i to 2); i += 2
            } else {
                units.add(i to 1); i += 1
            }
        }
        val rightmost = units.last()
        val second = units[units.size - 2]
        val sndChar = rhyme[second.first]

        val codaChar = rhyme.last()
        val isChecked = codaChar in "ptk"

        if (isChecked && (sndChar == 'i' || sndChar == 'u')) {
            val isIuStop = rhyme.length >= 3 &&
                rhyme[rhyme.length - 3] == 'i' && rhyme[rhyme.length - 2] == 'u'
            if (!isIuStop && units.size >= 3) {
                val third = units[units.size - 3]
                return (rhymeStart + third.first) to third.second
            }
        }

        if (sndChar == 'i') {
            return (rhymeStart + rightmost.first) to rightmost.second
        }

        return (rhymeStart + second.first) to second.second
    }

    private fun applyTone(vowel: String, tone: Int): String {
        if (vowel.length == 1 && vowel[0] in "aeiou") {
            return when (tone) {
                1 -> when (vowel) { "a" -> "â"; "e" -> "ê"; "i" -> "î"; "o" -> "ô"; "u" -> "û"; else -> vowel }
                2 -> when (vowel) { "a" -> "à"; "e" -> "è"; "i" -> "ì"; "o" -> "ò"; "u" -> "ù"; else -> vowel }
                3 -> when (vowel) { "a" -> "á"; "e" -> "é"; "i" -> "í"; "o" -> "ó"; "u" -> "ú"; else -> vowel }
                5 -> vowel + COMB_VLINE
                else -> vowel
            }
        }
        return when (tone) {
            1 -> vowel + COMB_CIRCUMFLEX
            2 -> vowel + COMB_GRAVE
            3 -> vowel + COMB_ACUTE
            5 -> vowel + COMB_VLINE
            else -> vowel
        }
    }

    // PFS checked finals are -p/-t/-k, which cannot be confused with the nasal coda -ng,
    // so no -ng guard is needed here (unlike Kppy.endsWithChecked, where the -g of -ng
    // collides with the checked-stop -g). If checkedFinals ever gains "g", add the guard.
    private fun endsWithChecked(str: String): Boolean = checkedFinals.any { str.endsWith(it) }

    private fun containsAny(str: String, chars: Set<Char>): Boolean = str.any { it in chars }

    private fun stripDiacritics(str: String, map: Map<Char, Char>): String =
        buildString(str.length) { str.forEach { append(map[it] ?: it) } }

    private fun isValidRhyme(rhyme: String, initial: String): Boolean {
        if (rhyme.isEmpty()) return initial == "m" || initial == "ng"
        // A Si-yen rhyme must begin with a vowel (a/e/i/o/u or ṳ).
        if (rhyme[0] !in rhymeNuclei) return false
        return rhyme.all { it in rhymeChars }
    }

    // Folds the canonical decomposition u + COMB_DIAERESIS_BELOW into the precomposed ṳ
    // so that NFD-form input behaves identically to NFC-form input. We deliberately
    // avoid invoking a full NFC normalizer because Kotlin/Multiplatform does not
    // ship one across all targets.
    private fun normalize(s: String): String =
        if (COMB_DIAERESIS_BELOW in s) s.replace("u$COMB_DIAERESIS_BELOW", "ṳ") else s

    // Accept the PFS y-spelling on input by folding the leading `y`:
    //   - `y` + `i` (yi, yim, yin, yip): drop the `y` so the `i` nucleus is restored.
    //   - `y` + other vowel (ya, yu, yong, yâ): replace `y` with `i` so the onglide is restored.
    // Bare `y` is not a valid PFS syllable and is rejected downstream by the rhyme-validity check.
    private fun foldYOnglide(s: String): String {
        if (s.length < 2 || s[0] != 'y') return s
        val next = s[1]
        return when {
            next == 'i' -> s.drop(1)
            next in "aeou" -> "i" + s.drop(1)
            else -> s
        }
    }
}
