package org.phakfasu.konverttopfs

/**
 * Parser and renderer for the 信望愛 Hak-fa Dictionary (FHL dict) numeric format.
 *
 * FHL dict uses PFS orthography (same initials, rhymes, and ṳ handling) but
 * numbers tones using the POJ-style digit→diacritic mapping (1–8) instead of
 * the PFS sequential 1–6 scheme.  Si-yen Hakfa uses digits 1, 2, 3, 4, 5, 8
 * from this set; digits 6 and 7 are rejected.
 *
 * FHL_UNICODE output is identical to PFS_UNICODE — the diacritics are the same;
 * only the numeric format differs.
 */
internal object FhlDict {
    // FHL dict POJ-style digit → internal PFS tone number.
    private val fhlToInternal = mapOf(
        1 to 4,  // FHL 1 (unmarked)        → PFS 4
        2 to 3,  // FHL 2 (acute)           → PFS 3
        3 to 2,  // FHL 3 (grave)           → PFS 2
        4 to 6,  // FHL 4 (unmarked-checked) → PFS 6
        5 to 1,  // FHL 5 (circumflex)      → PFS 1
        8 to 5,  // FHL 8 (vline-checked)   → PFS 5
    )

    private val internalToFhl = fhlToInternal.entries.associate { (k, v) -> v to k }

    /**
     * Parses a single FHL-dict-input syllable (e.g. `hak8`, `ka5`, `fa1`).
     *
     * The orthography is PFS; only the trailing tone digit uses POJ-style
     * numbering.  Digits 6, 7, 9, and 0 are rejected (not valid in Si-yen).
     * When no digit is present, defaults follow PFS convention (checked → T6,
     * open/nasal → T4).
     */
    fun parseInput(s: String): HakfaSyllable? {
        if (s.isEmpty()) return null
        // ASCII digits only — non-ASCII decimal digits (Arabic-Indic, Devanagari, …)
        // must not be read as tone numbers; the token should pass through unchanged.
        val lastAsciiDigit = s.last().takeIf { it in '0'..'9' }
        val fhlTone = lastAsciiDigit?.digitToIntOrNull()

        if (fhlTone != null) {
            val pfsTone = fhlToInternal[fhlTone] ?: return null
            return Pfs.parseInput(s.dropLast(1) + pfsTone.toString())
        }
        // No digit present: delegate to Pfs.parseInput which defaults to internal tone 4
        // (open/nasal) or 6 (checked). This coincides with the FHL dict no-digit convention
        // (FHL 1 → internal 4, FHL 4 → internal 6), so no extra remapping is required here.
        return Pfs.parseInput(s)
    }

    fun renderInput(s: HakfaSyllable): String {
        val fhlTone = internalToFhl[s.tone] ?: return ""
        return "${Pfs.renderBase(s, useUnicodeVowel = false)}$fhlTone"
    }
}
