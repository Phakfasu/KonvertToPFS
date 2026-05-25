package org.phakfasu.konverttopfs

// 四縣腔聲調 (Siyen Hakfa tones) from two official sources.
//
// KPPY source: 教育部《四縣腔聲調表》
//   調型 uses modifier-letter tone marks appended after the syllable (e.g. oˊ).
//
// PFS source: 白話字聲調表
//   記號 uses combining diacritics placed over the main vowel (e.g. â).
//
// Internal tone numbering follows PFS/KPPY (used in HakfaSyllable.tone): 1–6.
data class ToneInfo(
    val internalTone: Int,      // tone number (1–6), shared by PFS and KPPY in this library
    val kppyContour: String,    // KPPY 調型 on canonical syllable, e.g. "paˊ"
    val kppyValue: String,      // KPPY 調值, e.g. "24"
    val kppyNumber: Int,        // KPPY tone number (== internalTone; this library aligns digits with PFS)
    val pfsMark: String,        // PFS 聲調記號 on canonical syllable, e.g. "phâ"
    val pfsSiyenValue: String,  // PFS 四縣調值, e.g. "24" or "[55]"
    val fhlDictNumber: Int,     // FHL dictionary POJ-style tone digit (Si-yen uses 1,2,3,4,5,8)
    val ipaToneLetters: String, // Chao tone letters, e.g. "˨˦"
)

object SiyenTones {
    // Internal/PFS | KPPY 調型 | KPPY 調值 | KPPY 號 | PFS 記號 | PFS 四縣調值
    //      1       |   paˊ     |    24   |    1   |   phâ    |     24
    //      2       |   paˇ     |    11   |    2   |   phà    |     11
    //      3       |   paˋ     |    31   |    3   |   phá    |     41
    //      4       |   pa      |    55   |    4   |   pha    |     44
    //      5       |   pag     |     5   |    5   |  pha̍k    |    [55]
    //      6       |   pagˋ    |     2   |    6   |   phak   |    [22]
    val tones: List<ToneInfo> = listOf(
        ToneInfo(1, "paˊ",  "24", 1, "phâ",  "24",  5, "˨˦"),
        ToneInfo(2, "paˇ",  "11", 2, "phà",  "11",  3, "˩˩"),
        ToneInfo(3, "paˋ",  "31", 3, "phá",  "41",  2, "˧˩"),
        ToneInfo(4, "pa",   "55", 4, "pha",  "44",  1, "˥˥"),
        ToneInfo(5, "pag",  "5",  5, "pha̍k", "[55]", 8, "˥"),
        ToneInfo(6, "pagˋ", "2",  6, "phak", "[22]", 4, "˨"),
    )

    val byInternal: Map<Int, ToneInfo> = tones.associateBy { it.internalTone }

    // Equivalent to [byInternal] today (digits are aligned 1↔1). Kept as a separate
    // lookup so callers ingesting 教育部 raw data — where KPPY 號 swaps 5↔6 — can
    // wire up a different ToneInfo set without breaking client code that already
    // looks tones up via this map.
    val byKppyNumber: Map<Int, ToneInfo> = tones.associateBy { it.kppyNumber }

    val byFhlDictNumber: Map<Int, ToneInfo> = tones.associateBy { it.fhlDictNumber }
}
