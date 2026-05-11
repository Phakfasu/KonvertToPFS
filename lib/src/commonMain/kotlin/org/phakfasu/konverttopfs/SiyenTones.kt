package org.phakfasu.konverttopfs

// 四縣腔聲調 (Siyen Hakka tones) from two official sources.
//
// MOE source: 教育部《四縣腔聲調表》
//   調型 uses modifier-letter tone marks appended after the syllable (e.g. oˊ).
//
// PFS source: 教育部客家語拼音方案 (PFS) tone table
//   記號 uses combining diacritics placed over the main vowel (e.g. â).
//   PFS numbers 入聲 in the opposite order from the MOE/internal convention:
//   PFS 五 = 陽入 (internal 6), PFS 六 = 陰入 (internal 5).
//
// Internal tone numbering (used in HakkaSyllable.tone):
//   1 = 陰平, 2 = 陽平, 3 = 上聲, 4 = 去聲, 5 = 陰入, 6 = 陽入
data class ToneInfo(
    val internalTone: Int,      // tone number used in HakkaSyllable (1–6)
    val chineseName: String,    // 調類, e.g. "陰平"
    val moeContour: String,     // MOE 調型, e.g. "oˊ"
    val moeValue: String,       // MOE 調值, e.g. "24"
    val pfsNumber: Int,         // PFS tone number (differs for 入聲)
    val pfsMark: String,        // PFS 聲調記號, e.g. "â"
    val pfsSiyenValue: String,  // PFS 四縣調值, e.g. "24" or "[44]"
)

object SiyenTones {
    // Internal | 調類 | MOE 調型 | MOE 調值 | PFS 號 | PFS 記號 | PFS 四縣調值
    //    1     | 陰平 |   oˊ    |    24   |   1   |    â    |     24
    //    2     | 陽平 |   oˇ    |    11   |   2   |    à    |     11
    //    3     | 上聲 |   o`    |    31   |   3   |    á    |     41
    //    4     | 去聲 |   o     |    55   |   4   |    a    |     44
    //    5     | 陰入 |   og`   |     2   |   6   |   ak    |    [43]
    //    6     | 陽入 |   og    |     5   |   5   |   ák    |    [44]
    val tones: List<ToneInfo> = listOf(
        ToneInfo(1, "陰平", "oˊ",  "24", 1, "â",  "24"),
        ToneInfo(2, "陽平", "oˇ",  "11", 2, "à",  "11"),
        ToneInfo(3, "上聲", "o`",  "31", 3, "á",  "41"),
        ToneInfo(4, "去聲", "o",   "55", 4, "a",  "44"),
        ToneInfo(5, "陰入", "og`", "2",  6, "ak", "[43]"),
        ToneInfo(6, "陽入", "og",  "5",  5, "ák", "[44]"),
    )

    val byInternal: Map<Int, ToneInfo> = tones.associateBy { it.internalTone }
    val byPfsNumber: Map<Int, ToneInfo> = tones.associateBy { it.pfsNumber }
}
