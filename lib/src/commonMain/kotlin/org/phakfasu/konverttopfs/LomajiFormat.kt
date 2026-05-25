package org.phakfasu.konverttopfs

/**
 * The four supported representations of a Hakfa syllable.
 *
 * Each Roman Orthography system (PFS / KPPY) has two modes:
 * - **INPUT**: ASCII-safe representation with the tone written as a trailing digit
 *   (e.g. `hak5`, `kâ` → `ka2`). Suitable for keyboard input and storage in systems
 *   that do not handle combining marks well.
 * - **UNICODE**: tone written as a diacritic on the syllable. PFS uses combining marks
 *   on the rhyme nucleus (e.g. `kâ`, `a̍p`); KPPY uses modifier letters at the end of
 *   the syllable (e.g. `gaˊ`, `gaˋ`). Latin precomposed accents on the vowel are also
 *   accepted on input as a tolerant alternative.
 *
 * Tone numbers used by both INPUT formats are 1–6 (Si-yen Hakfa):
 * Tone 1, Tone 2, Tone 3, Tone 4, Tone 5 (checked), Tone 6 (checked).
 */
enum class LomajiFormat {
    /** PFS (Pha̍k-fa-sṳ) with tone numbers, e.g. `hak5-ka1-fa4`. */
    PFS_INPUT,

    /** PFS with tone diacritics on the rhyme, e.g. `ha̍k-kâ-fa`. */
    PFS_UNICODE,

    /** KPPY (Kàu-pō͘ Phin-yîm) with tone numbers, e.g. `hag5-ga1-fa4`. */
    KPPY_INPUT,

    /** KPPY with modifier-letter tone marks, e.g. `hag-gaˊ-fa`. */
    KPPY_UNICODE,

    /** FHL dictionary (信望愛 Hak-fa Dictionary) with POJ-style tone numbers, e.g. `hak8-ka5-fa1`. */
    FHL_DICT_INPUT,

    /** FHL dictionary with diacritics (identical glyphs to [PFS_UNICODE]), e.g. `ha̍k-kâ-fa`. */
    FHL_UNICODE,

    /** IPA with Chao tone letters (render-only; parsing IPA back is lossy), e.g. `hak̚˥-ka˨˦-fa˥˥`. */
    IPA,
}
