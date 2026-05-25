package org.phakfasu.konverttopfs

/**
 * The internal canonical representation of a parsed Hakfa syllable.
 *
 * The fields use a unified KPPY-flavoured form: the j/q/x prefixes are folded to
 * z/c/s and the ṳ vowel is written as the digraph `ii`. This lets the same value
 * be rendered as either PFS or KPPY without re-parsing.
 *
 * @property initial the syllable initial in canonical form (e.g. `z`, `c`, `s`,
 *   `m`, `ng`, or empty for vowel-initial syllables).
 * @property rhyme the rhyme using ASCII letters; `ii` denotes the ṳ vowel; coda
 *   stops are written `b`/`d`/`g` (KPPY-style).
 * @property tone Si-yen Hakfa tone in the range 1..6.
 */
internal data class HakfaSyllable(
    val initial: String,
    val rhyme: String,
    val tone: Int,
)
