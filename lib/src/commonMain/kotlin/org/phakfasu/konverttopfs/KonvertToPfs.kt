package org.phakfasu.konverttopfs

/**
 * Entry point for converting Hakfa Roman Orthography between [LomajiFormat] variants.
 *
 * The library is text-oriented: the input may contain mixed Roman Orthography, whitespace,
 * punctuation, and non-Roman-Orthography characters (e.g. CJK ideographs). Each maximal
 * run of letters/digits/combining marks is treated as one syllable token and converted
 * independently; everything else is passed through unchanged.
 *
 * Tokens that do not parse as valid Si-yen Hakfa syllables — including unrecognized
 * letters, illegal tone numbers, or phonotactically illegal coda+tone combinations —
 * are also passed through unchanged so the call is non-destructive on mixed text.
 *
 * Casing of each syllable is preserved: lowercase, Title-case, and ALL-CAPS inputs map
 * to the same casing on output.
 */
object KonvertToPfs {
    /**
     * Converts [text] from [from] to [to].
     *
     * Returns [text] unchanged when [from] equals [to] or the input is empty.
     * Non-syllable content (whitespace, punctuation, CJK characters) and syllable-shaped
     * tokens that fail validation are emitted verbatim.
     */
    fun convert(text: String, from: LomajiFormat, to: LomajiFormat): String {
        if (from == to || text.isEmpty()) return text

        return buildString(text.length) {
            for ((token, isWord) in tokenize(text)) {
                if (isWord) append(convertWord(token, from, to))
                else append(token)
            }
        }
    }

    private fun convertWord(word: String, from: LomajiFormat, to: LomajiFormat): String {
        val casing = detectCasing(word)
        val lower = word.lowercase()
        parseSyllable(lower, from)?.let { return applyCasing(renderSyllable(it, to), casing) }
        // Compound token without separator (e.g. KPPY "ziinˊnaˇ"): the from-format's
        // tone markers terminate a syllable, so split on them and convert each piece.
        val syllables = splitIntoSyllables(lower, from) ?: return word
        // Insert a plain space between recovered syllables. A hyphen would assert
        // these belong to one compound word, but the joined input gives us no such
        // information — the writer may have meant a compound, a word boundary, or
        // simply omitted whitespace. Space is the neutral choice.
        val rendered = syllables.joinToString(" ") { renderSyllable(it, to) }
        return applyCasing(rendered, casing)
    }

    /**
     * Splits a word into multiple syllables by locating syllable-end markers in [format].
     * Returns null if the word cannot be split into ≥2 syllables that all parse cleanly.
     */
    private fun splitIntoSyllables(word: String, format: LomajiFormat): List<HakfaSyllable>? {
        val pieces = when (format) {
            LomajiFormat.KPPY_UNICODE -> splitKppyUnicode(word)
            LomajiFormat.KPPY_INPUT,
            LomajiFormat.PFS_INPUT,
            LomajiFormat.FHL_DICT_INPUT -> splitByTrailingDigit(word)
            LomajiFormat.PFS_UNICODE,
            LomajiFormat.FHL_UNICODE -> splitPfsUnicode(word)
            LomajiFormat.IPA -> return null
        }
        if (pieces.size < 2) return null
        return pieces.map { parseSyllable(it, format) ?: return null }
    }

    // Modifier letters and the visually identical combining marks. Any of these
    // terminates a KPPY-unicode syllable.
    private val kppyToneEndChars = setOf('ˊ', 'ˇ', 'ˋ', '́', '̌', '̀')

    private fun splitKppyUnicode(word: String): List<String> {
        val result = ArrayList<String>()
        val current = StringBuilder()
        for (c in word) {
            current.append(c)
            if (c in kppyToneEndChars) {
                result.add(current.toString()); current.clear()
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }

    private fun splitByTrailingDigit(word: String): List<String> {
        val result = ArrayList<String>()
        val current = StringBuilder()
        for (c in word) {
            current.append(c)
            if (c in '0'..'9') {
                result.add(current.toString()); current.clear()
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }

    // Combining tone marks used by PFS / FHL_UNICODE.
    private val pfsCombiningToneMarks = setOf('̂', '̀', '́', '̍')
    // Precomposed Latin vowels that carry a PFS tone (circumflex/grave/acute on a/e/i/o/u).
    private val pfsPrecomposedToneVowels = setOf(
        'â', 'ê', 'î', 'ô', 'û',
        'à', 'è', 'ì', 'ò', 'ù',
        'á', 'é', 'í', 'ó', 'ú',
    )
    // Coda chars accepted by Pfs.parseInput (canonical p/t/k plus KPPY-tolerant b/d/g, nasals m/n/g).
    private val pfsCodaChars = setOf('m', 'n', 'g', 'p', 't', 'k', 'b', 'd')

    /**
     * Splits a PFS-unicode word into syllables. Locates each tone-marked nucleus, then
     * consumes the maximal valid coda (`ng`, or a single coda char) before splitting.
     * Returns the input as a single-element list when no tone mark is present, signalling
     * that the input is either a T4/T6 syllable (handled by the single-parse path) or
     * truly ambiguous and must be passed through.
     */
    private fun splitPfsUnicode(word: String): List<String> {
        val result = ArrayList<String>()
        var sylStart = 0
        var i = 0
        while (i < word.length) {
            val c = word[i]
            if (c !in pfsCombiningToneMarks && c !in pfsPrecomposedToneVowels) {
                i++; continue
            }
            // Tone mark found. Advance past it and consume the maximal coda.
            var end = i + 1
            if (end < word.length && word[end] in pfsCodaChars) {
                // "ng" is the only legal 2-letter coda; otherwise take a single char.
                if (word[end] == 'n' && end + 1 < word.length && word[end + 1] == 'g') {
                    end += 2
                } else {
                    end += 1
                }
            }
            result.add(word.substring(sylStart, end))
            sylStart = end
            i = end
        }
        if (sylStart < word.length) result.add(word.substring(sylStart))
        return result
    }

    private fun parseSyllable(syllableStr: String, format: LomajiFormat): HakfaSyllable? = when (format) {
        LomajiFormat.KPPY_INPUT -> Kppy.parseInput(syllableStr)
        LomajiFormat.PFS_INPUT -> Pfs.parseInput(syllableStr)
        LomajiFormat.KPPY_UNICODE -> Kppy.parseUnicode(syllableStr)
        LomajiFormat.PFS_UNICODE, LomajiFormat.FHL_UNICODE -> Pfs.parseUnicode(syllableStr)
        LomajiFormat.FHL_DICT_INPUT -> FhlDict.parseInput(syllableStr)
        LomajiFormat.IPA -> null
    }

    private fun renderSyllable(s: HakfaSyllable, format: LomajiFormat): String = when (format) {
        LomajiFormat.KPPY_INPUT -> Kppy.renderInput(s)
        LomajiFormat.PFS_INPUT -> Pfs.renderInput(s)
        LomajiFormat.KPPY_UNICODE -> Kppy.renderUnicode(s)
        LomajiFormat.PFS_UNICODE, LomajiFormat.FHL_UNICODE -> Pfs.renderUnicode(s)
        LomajiFormat.FHL_DICT_INPUT -> FhlDict.renderInput(s)
        LomajiFormat.IPA -> Ipa.render(s)
    }

    private enum class Casing { LOWER, TITLE, UPPER }

    private fun detectCasing(s: String): Casing {
        // Modifier letters (Lm, e.g. ˊ ˇ ˋ) and combining marks have no case; they must
        // not be counted when classifying the syllable's casing.
        val cased = s.filter { it.isLetter() && (it.isUpperCase() || it.isLowerCase()) }
        if (cased.isEmpty() || cased.none { it.isUpperCase() }) return Casing.LOWER
        if (cased.all { it.isUpperCase() }) return Casing.UPPER
        // Only classify as TITLE when the *first* cased letter is uppercase. A
        // mid-word capital (e.g. "nGiang2") is treated as LOWER so the renderer
        // emits a lowercase output instead of silently moving the capital to
        // the start of the syllable.
        if (cased.first().isUpperCase()) return Casing.TITLE
        return Casing.LOWER
    }

    private fun applyCasing(s: String, casing: Casing): String = when (casing) {
        Casing.LOWER -> s
        Casing.UPPER -> s.uppercase()
        Casing.TITLE -> s.replaceFirstChar { it.uppercase() }
    }

    /**
     * Splits text into runs of word-content (letters, digits, combining marks) and
     * non-word content (whitespace, punctuation), preserving the latter verbatim.
     *
     * The implementation is a manual scan to stay free of regex features (Unicode
     * property classes, lookbehind) that vary in availability across Kotlin/JS,
     * Kotlin/Wasm, and the JVM.
     */
    private fun tokenize(text: String): List<Pair<String, Boolean>> {
        if (text.isEmpty()) return emptyList()
        val result = ArrayList<Pair<String, Boolean>>()
        var start = 0
        var currentIsWord = isWordChar(text[0])
        for (i in 1 until text.length) {
            val charIsWord = isWordChar(text[i])
            if (charIsWord != currentIsWord) {
                result.add(text.substring(start, i) to currentIsWord)
                start = i
                currentIsWord = charIsWord
            }
        }
        result.add(text.substring(start) to currentIsWord)
        return result
    }

    private fun isWordChar(c: Char): Boolean =
        (c in 'a'..'z' || c in 'A'..'Z') ||
            // Char.isDigit() accepts non-ASCII decimal digits too (Arabic-Indic, etc.);
            // they are kept in the word token so the whole syllable-like substring
            // passes through unchanged when the parser rejects the non-ASCII digit.
            c.isDigit() ||
            isCombiningMark(c) ||
            isLatinExtended(c) ||
            isModifierLetter(c)

    private fun isLatinExtended(c: Char): Boolean =
        c in '\u00C0'..'\u00FF' || // Latin-1 Supplement
            c in '\u0100'..'\u017F' || // Latin Extended-A
            c in '\u0180'..'\u024F' || // Latin Extended-B
            c in '\u1E00'..'\u1EFF'    // Latin Extended Additional (includes ṳ)

    private fun isModifierLetter(c: Char): Boolean =
        c in '\u02B0'..'\u02FF'

    // Combining Diacritical Marks block (U+0300–U+036F) covers all tone marks used
    // by PFS (̂ ̀ ́ ̍) and any incidental combining marks attached to letters.
    private fun isCombiningMark(c: Char): Boolean = c in '\u0300'..'\u036F'
}
