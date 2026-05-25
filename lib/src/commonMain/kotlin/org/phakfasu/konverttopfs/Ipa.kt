package org.phakfasu.konverttopfs

/**
 * Renderer for IPA (International Phonetic Alphabet) with Chao tone letters.
 *
 * This is a **render-only** format: IPA collapses spelling distinctions that
 * PFS / KPPY preserve, so reverse-mapping IPA back to an orthographic form is
 * lossy and not supported.  Each syllable receives its citation tone from the
 * master tone table — tone sandhi is not applied.
 *
 * Dialect: Si-yen (四縣腔).
 */
internal object Ipa {
    private val initialToIpa = mapOf(
        "b"  to "p",
        "p"  to "pʰ",
        "m"  to "m",
        "f"  to "f",
        "v"  to "ʋ",
        "d"  to "t",
        "t"  to "tʰ",
        "n"  to "n",
        "l"  to "l",
        "z"  to "ts",
        "c"  to "tsʰ",
        "s"  to "s",
        "g"  to "k",
        "k"  to "kʰ",
        "ng" to "ŋ",
        "h"  to "h",
    )

    private val palatalizedToIpa = mapOf(
        "z"  to "tɕ",
        "c"  to "tɕʰ",
        "s"  to "ɕ",
        "ng" to "ɲ",
    )

    private val vowelToIpa = mapOf(
        'a' to "a",
        'e' to "e",
        'i' to "i",
        'o' to "o",
        'u' to "u",
    )

    private val stopCodaToIpa = mapOf(
        'b' to "p̚",
        'd' to "t̚",
        'g' to "k̚",
    )

    private val toneToIpa = mapOf(
        1 to "˨˦",
        2 to "˩˩",
        3 to "˧˩",
        4 to "˥˥",
        5 to "˥",
        6 to "˨",
    )

    fun render(s: HakfaSyllable): String {
        if (s.rhyme.isEmpty()) {
            val nucleus = when (s.initial) {
                "m"  -> "m̩"
                "ng" -> "ŋ̍"
                else -> return ""
            }
            return nucleus + (toneToIpa[s.tone] ?: "")
        }

        val beforeI = s.rhyme.startsWith("i") && !s.rhyme.startsWith("ii")
        val ipaInitial = if (beforeI && s.initial in palatalizedToIpa) {
            palatalizedToIpa[s.initial]!!
        } else {
            initialToIpa[s.initial] ?: ""
        }

        return ipaInitial + mapRhyme(s.rhyme) + (toneToIpa[s.tone] ?: "")
    }

    private fun mapRhyme(rhyme: String): String = buildString {
        var i = 0

        if (rhyme.startsWith("ii")) {
            append("ɨ")
            i = 2
        }

        while (i < rhyme.length) {
            val remaining = rhyme.length - i

            if (remaining == 2 && rhyme[i] == 'n' && rhyme[i + 1] == 'g') {
                append("ŋ")
                break
            }

            if (remaining == 1 && rhyme[i] in stopCodaToIpa) {
                append(stopCodaToIpa[rhyme[i]])
                break
            }

            if (remaining >= 2 && rhyme[i] == 'e' && rhyme[i + 1] == 'r') {
                append("ɤ")
                i += 2
                continue
            }

            val ipa = vowelToIpa[rhyme[i]]
            if (ipa != null) {
                append(ipa)
            } else {
                append(rhyme[i])
            }
            i++
        }
    }
}
