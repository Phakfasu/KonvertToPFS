package org.phakfasu.konverttopfs

import kotlin.test.Test
import kotlin.test.assertEquals

class KonvertToPfsTest {

    @Test
    fun testPfsToKppy_Input() {
        val pfs = "hak5-ka1-fa4"
        val expectedKppy = "hag5-ga1-fa4" // Library aligns KPPY digits with PFS — T5 stays T5.
        assertEquals(expectedKppy, KonvertToPfs.convert(pfs, LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testMixedHanLo_NoSpaces() {
        // Mixed Han-Lo text without spaces should now tokenize correctly.
        val input = "Ngài ke生日he 1月5號。"
        // PFS Tone 4 (ke, he) -> KPPY Tone 4 (unmarked).
        // PFS Tone 2 (Ngài) -> KPPY Tone 2 (ˇ).
        val expected = "Ngaiˇ ge生日he 1月5號。"
        assertEquals(expected, KonvertToPfs.convert(input, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testKppyToPfs_Unicode() {
        // KPPY tone 3 via the canonical modifier letter ˋ -> PFS tone 3 (acute).
        assertEquals("ká", KonvertToPfs.convert("gaˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        // KPPY tone 2 via the canonical modifier letter ˇ -> PFS tone 2 (grave).
        assertEquals("kà", KonvertToPfs.convert("gaˇ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testKppyToPfs_Unicode_LatinDiacritics() {
        // KPPY accepts precomposed Latin diacritics on input as a tolerant alternative
        // to modifier letters.
        // acute (á) -> tone 1 -> PFS 1 (â)
        // caron (ǎ) -> tone 2 -> PFS 2 (à)
        // grave (à) -> tone 3 -> PFS 3 (á) (or tone 6 if checked)
        assertEquals("kâ", KonvertToPfs.convert("gá", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("kà", KonvertToPfs.convert("gǎ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("ká", KonvertToPfs.convert("gà", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        // Stop-final + grave -> T6 (low-pitched checked) -> PFS unmarked-checked.
        assertEquals("ap", KonvertToPfs.convert("àb", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testZCS_JQX_Initials() {
        assertEquals("chi1", KonvertToPfs.convert("ji1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhi1", KonvertToPfs.convert("qi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("si1", KonvertToPfs.convert("xi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))

        assertEquals("chii1", KonvertToPfs.convert("zii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhii1", KonvertToPfs.convert("cii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("sii1", KonvertToPfs.convert("sii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testKPPY_IiVowel_WithCodas() {
        // The ṳ (internal: ii) vowel can carry codas. The j/q/x → z/c/s restoration in
        // KPPY render must be suppressed when the rhyme starts with the ii cluster, even
        // when the cluster is followed by a coda such as m/n/b/d/g.
        assertEquals("chiim2", KonvertToPfs.convert("ziim2", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chiin2", KonvertToPfs.convert("ziin2", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chiip5", KonvertToPfs.convert("ziib5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chiit5", KonvertToPfs.convert("ziid5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chiik5", KonvertToPfs.convert("ziig5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        // Round-trip through PFS preserves the input.
        assertEquals("ziim2", KonvertToPfs.convert("chiim2", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testKPPY_PlainI_KeepsJqx() {
        // Plain "i" (non-ṳ) MUST retain the j/q/x restoration.
        assertEquals("ji1", KonvertToPfs.convert("chi1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // PFS "chh" (aspirated) -> KPPY "q" before i.
        assertEquals("qim1", KonvertToPfs.convert("chhim1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // PFS "s" -> KPPY "x" before i.
        assertEquals("xim1", KonvertToPfs.convert("sim1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testCheckedCodas() {
        // Digit numbering is aligned: KPPY 5 (high-pitched checked) ↔ PFS 5.
        assertEquals("ap5", KonvertToPfs.convert("ab5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("at5", KonvertToPfs.convert("ad5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("ak5", KonvertToPfs.convert("ag5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testTones() {
        assertEquals("a1", KonvertToPfs.convert("â", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a2", KonvertToPfs.convert("à", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a3", KonvertToPfs.convert("á", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a4", KonvertToPfs.convert("a", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("ap5", KonvertToPfs.convert("a̍p", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("ap6", KonvertToPfs.convert("ap", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testSyllabic() {
        assertEquals("m̂", KonvertToPfs.convert("m1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("n̂g", KonvertToPfs.convert("ng1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("m", KonvertToPfs.convert("m4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ng", KonvertToPfs.convert("ng4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testCapitalization_Title() {
        // Sentence-initial capital must survive conversion.
        assertEquals("Hag5-ga1-fa4", KonvertToPfs.convert("Hak5-ka1-fa4", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testCapitalization_AllCaps() {
        assertEquals("HAG5-GA1", KonvertToPfs.convert("HAK5-KA1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testJoinedSyllables_KppyUnicode() {
        // Compound tokens without separator: tone markers (ˊ ˇ ˋ) terminate each syllable.
        // 菅榛林: gonˊ + ziinˊ + naˇ → kôn + chṳ̂n + nà. Output uses a plain space
        // between recovered syllables — hyphen would falsely assert "compound word".
        assertEquals("chṳ̂n nà", KonvertToPfs.convert("ziinˊnaˇ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("kôn chṳ̂n nà", KonvertToPfs.convert("gonˊziinˊnaˇ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        // Mixed text with one joined compound: only the joined token is split; surrounding text passes through.
        assertEquals(
            "kôn chṳ̂n nà (菅榛林)",
            KonvertToPfs.convert("gonˊ ziinˊnaˇ (菅榛林)", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE),
        )
    }

    @Test
    fun testJoinedSyllables_DigitFormats() {
        // KPPY input: each digit ends a syllable.
        assertEquals("chṳ̂n nà", KonvertToPfs.convert("ziin1na2", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_UNICODE))
        // PFS input round-trip.
        assertEquals("ziinˊ naˇ", KonvertToPfs.convert("chiin1na2", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testJoinedSyllables_PfsUnicode() {
        // PFS unicode: tone-mark + maximal coda splits the compound.
        assertEquals("ziinˊ naˇ", KonvertToPfs.convert("chṳ̂nnà", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testToneNumbersAligned() {
        // Library aligns KPPY digit numbering with PFS — no 5↔6 swap.
        // PFS 5 (high-pitched checked) -> KPPY 5.
        assertEquals("hag5", KonvertToPfs.convert("hak5", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // PFS 6 (low-pitched checked) -> KPPY 6.
        assertEquals("hag6", KonvertToPfs.convert("hak6", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    // ---- Round-trip tests (parse then render returns original) ----

    private fun assertRoundTrip(input: String, format: LomajiFormat) {
        val intermediate = when (format) {
            LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT -> if (format == LomajiFormat.PFS_INPUT) LomajiFormat.PFS_UNICODE else LomajiFormat.KPPY_UNICODE
            LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE -> if (format == LomajiFormat.PFS_UNICODE) LomajiFormat.PFS_INPUT else LomajiFormat.KPPY_INPUT
            LomajiFormat.FHL_DICT_INPUT -> LomajiFormat.FHL_UNICODE
            LomajiFormat.FHL_UNICODE -> LomajiFormat.FHL_DICT_INPUT
            LomajiFormat.IPA -> throw IllegalArgumentException("IPA round-trip not supported")
        }
        val there = KonvertToPfs.convert(input, format, intermediate)
        val back = KonvertToPfs.convert(there, intermediate, format)
        assertEquals(input, back, "round-trip failed: $input -> $there -> $back")
    }

    @Test
    fun testRoundTrip_PfsInput_BasicTones() {
        assertRoundTrip("hak5-ka1-fa4", LomajiFormat.PFS_INPUT)
        assertRoundTrip("a1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("a2", LomajiFormat.PFS_INPUT)
        assertRoundTrip("a3", LomajiFormat.PFS_INPUT)
        assertRoundTrip("a4", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ap5", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ap6", LomajiFormat.PFS_INPUT)
    }

    @Test
    fun testRoundTrip_PfsInput_Iu() {
        // The ṳ vowel — previously broken in renderUnicode for non-tone-1/5.
        assertRoundTrip("chii1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chii2", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chii3", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chii4", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chii6", LomajiFormat.PFS_INPUT)
        assertRoundTrip("sii2", LomajiFormat.PFS_INPUT)
    }

    @Test
    fun testRoundTrip_PfsInput_Syllabic() {
        assertRoundTrip("m1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("m2", LomajiFormat.PFS_INPUT)
        assertRoundTrip("m3", LomajiFormat.PFS_INPUT)
        assertRoundTrip("m4", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng2", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng3", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng4", LomajiFormat.PFS_INPUT)
    }

    @Test
    fun testSyllabic_PFS_AllTones_Render() {
        // 長老教會 PFS tone diacritics: 1=circumflex, 2=grave, 3=acute, 4=unmarked.
        // Syllabic outputs use combining marks (NFD-like) so that all diacritic tones
        // share one consistent representation, even where no precomposed codepoint exists.
        // Tones 5/6 are rejected for syllabic m/ng — see testSyllabic_CheckedTones_Rejected.
        assertEquals("m̂", KonvertToPfs.convert("m1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("m̀", KonvertToPfs.convert("m2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ḿ", KonvertToPfs.convert("m3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("m", KonvertToPfs.convert("m4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("n̂g", KonvertToPfs.convert("ng1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ǹg", KonvertToPfs.convert("ng2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ńg", KonvertToPfs.convert("ng3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ng", KonvertToPfs.convert("ng4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testSyllabic_CheckedTones_Rejected() {
        // Checked tones (5, 6) require a stop coda; syllabic m/ng lack one and are
        // rejected by both PFS and KPPY parsers. The token passes through unchanged
        // (non-destructive contract).
        for (tone in 5..6) {
            for (nasal in listOf("m", "ng")) {
                val input = "$nasal$tone"
                assertEquals(null, Pfs.parseInput(input),
                    "Pfs.parseInput should reject syllabic checked tone: $input")
                assertEquals(null, Kppy.parseInput(input),
                    "Kppy.parseInput should reject syllabic checked tone: $input")
                assertEquals(input,
                    KonvertToPfs.convert(input, LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE),
                    "convert should preserve rejected syllabic checked tone: $input")
            }
        }
    }

    @Test
    fun testSyllabic_KPPY_RoundTrip() {
        // Syllabic m/ng round-trip through KPPY tones 1-4 only. Tones 5/6 are rejected for
        // syllabic nasals — see testSyllabic_CheckedTones_Rejected.
        for (tone in 1..4) {
            assertRoundTrip("m$tone", LomajiFormat.KPPY_INPUT)
            assertRoundTrip("ng$tone", LomajiFormat.KPPY_INPUT)
        }
    }

    @Test
    fun testRoundTrip_KppyInput() {
        assertRoundTrip("hag6-ga1-fa4", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ab5", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ad6", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ag6", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ji1", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("zii1", LomajiFormat.KPPY_INPUT)
    }

    @Test
    fun testRoundTrip_PfsUnicode_Iu() {
        // Tone marks combine on top of ṳ via combining marks.
        assertRoundTrip("chṳ̂", LomajiFormat.PFS_UNICODE)
        assertRoundTrip("chṳ̀", LomajiFormat.PFS_UNICODE)
        assertRoundTrip("chṳ́", LomajiFormat.PFS_UNICODE)
        assertRoundTrip("chṳ̍", LomajiFormat.PFS_UNICODE)
    }

    @Test
    fun testNFD_DecomposedIu() {
        // PFS u + U+0324 (COMB DIAERESIS BELOW) is the NFD form of ṳ. The library should
        // accept it and fold it to the canonical ṳ form internally.
        // (NFD form below uses literal `ṳ`; NFC uses the precomposed U+1E73.
        // Sanity check that they are byte-distinct so the test actually exercises NFD folding.)
        val nfdIu = "chṳ"  // chu + COMB_DIAERESIS_BELOW
        val nfcIu = "chṳ"
        assertEquals(false, nfdIu == nfcIu, "expected nfdIu and nfcIu to be byte-distinct")
        assertEquals(
            KonvertToPfs.convert(nfcIu, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_INPUT),
            KonvertToPfs.convert(nfdIu, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_INPUT)
        )
    }

    @Test
    fun testCapitalization_KppyUnicode_AllCaps() {
        // All-caps KPPY_UNICODE with a modifier-letter tone mark — the mark has Lm category
        // and must not break casing detection.
        val original = "NGˊ"
        val toInput = KonvertToPfs.convert(original, LomajiFormat.KPPY_UNICODE, LomajiFormat.KPPY_INPUT)
        assertEquals("NG1", toInput)
        val back = KonvertToPfs.convert(toInput, LomajiFormat.KPPY_INPUT, LomajiFormat.KPPY_UNICODE)
        assertEquals(original, back)
    }

    @Test
    fun testRoundTrip_PfsInput_CrossSystem() {
        // Round-trip via the other system.
        val original = "hak5-ka1-fa4"
        val viaKppy = KonvertToPfs.convert(original, LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT)
        val back = KonvertToPfs.convert(viaKppy, LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT)
        assertEquals(original, back)
    }

    // ---- Robustness: non-Roman-Orthography tokens and invalid input pass through unchanged ----

    @Test
    fun testNonRomanOrthography_CJK_Preserved() {
        // CJK characters are not Hakfa Roman Orthography and must pass through unchanged.
        val input = "佢 ngiâ ke 名係 ma1?"
        val result = KonvertToPfs.convert(input, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE)
        // CJK preserved, Roman Orthography converted, punctuation/whitespace preserved.
        assertEquals("佢", result.split(" ")[0])
        assertEquals("名係", result.split(" ")[3])
    }

    @Test
    fun testInvalidTone_Preserved() {
        // Tone 0 and tone 7+ are not valid Si-yen Hakfa tones; the input should pass through unchanged.
        assertEquals("ka0", KonvertToPfs.convert("ka0", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        assertEquals("ka7", KonvertToPfs.convert("ka7", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testNonAsciiDigit_NotReadAsTone() {
        // Arabic-Indic and Devanagari decimal digits must not be parsed as tone numbers.
        // The tokens must pass through unchanged per the non-destructive contract.
        // Arabic-Indic 3 (U+0663): "ka٣" should NOT become PFS T3 / KPPY ˋ.
        assertEquals("ka٣", KonvertToPfs.convert("ka٣", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // Devanagari 5 (U+096B): "hak५" should NOT become PFS T5 → KPPY hag5.
        assertEquals("hak५", KonvertToPfs.convert("hak५", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // Per-parser null contract.
        assertEquals(null, Pfs.parseInput("ka٣"))
        assertEquals(null, Kppy.parseInput("ga٣"))
        assertEquals(null, FhlDict.parseInput("ka٣"))
    }

    @Test
    fun testInvalidCheckedTone_Preserved() {
        // Tone 1 with a stop-final coda is phonotactically illegal in Hakfa; preserve as-is.
        assertEquals("kap1", KonvertToPfs.convert("kap1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // Tone 5 without a stop-final coda is illegal; preserve as-is.
        assertEquals("ka5", KonvertToPfs.convert("ka5", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testEmpty_Preserved() {
        assertEquals("", KonvertToPfs.convert("", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testIdentityConversion() {
        // Same source and target -> input is returned verbatim.
        assertEquals("hak5-ka1-fa4", KonvertToPfs.convert("hak5-ka1-fa4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testParseInput_NullContract() {
        // Pfs.parseInput returns null on every shape the library treats as invalid.
        assertEquals(null, Pfs.parseInput(""))           // empty
        assertEquals(null, Pfs.parseInput("0"))          // tone-only, no syllable
        assertEquals(null, Pfs.parseInput("ka0"))        // tone 0
        assertEquals(null, Pfs.parseInput("ka7"))        // tone 7
        assertEquals(null, Pfs.parseInput("ka9"))        // tone 9
        assertEquals(null, Pfs.parseInput("kap1"))       // stop-final coda + tone 1–4 (illegal)
        assertEquals(null, Pfs.parseInput("ka5"))        // tone 5–6 requires a stop-final coda
        assertEquals(null, Pfs.parseInput("m5"))         // syllabic m + checked tone (illegal)
        assertEquals(null, Pfs.parseInput("m6"))         // syllabic m + checked tone (illegal)
        assertEquals(null, Pfs.parseInput("ng5"))        // syllabic ng + checked tone (illegal)
        assertEquals(null, Pfs.parseInput("ng6"))        // syllabic ng + checked tone (illegal)
        assertEquals(null, Pfs.parseInput("佢1"))         // non-letter rhyme
        // Illegal rhyme letters — these letters cannot appear in a Si-yen rhyme.
        assertEquals(null, Pfs.parseInput("fqz4"))       // rhyme contains q, z (not legal)
        assertEquals(null, Pfs.parseInput("kxyz4"))      // rhyme contains x, y, z
        assertEquals(null, Pfs.parseInput("kcaw1"))      // rhyme contains c, w
        // Rhyme must begin with a vowel.
        assertEquals(null, Pfs.parseInput("kmang1"))     // rhyme starts with 'm' after initial 'k'
    }

    @Test
    fun testParseInput_ValidContract() {
        // Valid PFS input syllables round-trip through parse/render unchanged.
        val cases = listOf("hak5", "ka1", "ka2", "fa4", "chii1", "m1", "ng3", "ngiang2")
        for (c in cases) {
            val parsed = Pfs.parseInput(c) ?: error("expected $c to parse")
            assertEquals(c, Pfs.renderInput(parsed))
        }
    }

    @Test
    fun testKPPY_ParseInput_NullContract() {
        assertEquals(null, Kppy.parseInput(""))
        assertEquals(null, Kppy.parseInput("ka7"))
        assertEquals(null, Kppy.parseInput("kab1"))   // stop-final coda + tone 1–4 (illegal)
        assertEquals(null, Kppy.parseInput("ka5"))    // tone 5–6 requires a stop-final coda
        assertEquals(null, Kppy.parseInput("m5"))     // syllabic m + checked tone (illegal)
        assertEquals(null, Kppy.parseInput("m6"))     // syllabic m + checked tone (illegal)
        assertEquals(null, Kppy.parseInput("ng5"))    // syllabic ng + checked tone (illegal)
        assertEquals(null, Kppy.parseInput("ng6"))    // syllabic ng + checked tone (illegal)
        assertEquals(null, Kppy.parseInput("佢"))      // CJK
        // Illegal rhyme letters — these letters cannot appear in a Si-yen rhyme.
        assertEquals(null, Kppy.parseInput("fqz4"))   // rhyme contains q, z (not legal)
        assertEquals(null, Kppy.parseInput("gxyz4"))  // rhyme contains x, y, z
        // Rhyme must begin with a vowel.
        assertEquals(null, Kppy.parseInput("gmang1")) // rhyme starts with 'm' after initial 'g'
    }

    @Test
    fun testNasalCoda_NotCheckedTone() {
        // -ng / -n / -m nasal codas must not trigger checked-tone validation.
        // PFS canonical for closed /u̯a/+ŋ is `oang`, not `uang` (see Pfs.kt renderBase).
        for (rhyme in listOf("iang", "iong", "ang", "ong", "ung", "im", "in", "iam", "iong", "oang")) {
            for (tone in 1..4) {
                val input = "k$rhyme$tone"
                val parsed = Pfs.parseInput(input)
                    ?: error("expected k$rhyme tone $tone to parse")
                assertEquals(input, Pfs.renderInput(parsed),
                    "PFS round-trip lost rhyme for $input")
            }
        }
    }

    @Test
    fun testNasalCoda_RoundTripCrossSystem() {
        // -ng nasal coda survives a PFS -> KPPY -> PFS round-trip.
        val original = "ngiang2"
        val viaKppy = KonvertToPfs.convert(original, LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT)
        assertEquals("ngiang2", viaKppy)
        val back = KonvertToPfs.convert(viaKppy, LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT)
        assertEquals(original, back)
    }

    // ---- README examples ----

    @Test
    fun testReadme_PfsInput_to_PfsUnicode() {
        // PFS 5 is a̍k (high-pitched checked).
        assertEquals("ha̍k-kâ-fa", KonvertToPfs.convert("hak5-ka1-fa4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testReadme_PfsInput_Uses_Ii_Not_U() {
        // Per README: PFS_INPUT uses the ASCII `ii` digraph; ṳ is reserved for PFS_UNICODE.
        // Verbatim example from README §Formats / §Usage.
        assertEquals(
            "thòi-vân hak-fa pha̍k-fa-sṳ",
            KonvertToPfs.convert("thoi2-van1 hak6-fa4 phak5-fa4-sii4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE)
        )
        // And reversed: PFS_UNICODE → PFS_INPUT must emit `sii4`, not `sṳ4`.
        assertEquals(
            "thoi2-van1 hak6-fa4 phak5-fa4-sii4",
            KonvertToPfs.convert("thòi-vân hak-fa pha̍k-fa-sṳ", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT)
        )
    }

    @Test
    fun testReadme_PfsInput_to_KppyUnicode() {
        // PFS T5 (high-pitched checked) -> KPPY unmarked-checked (digit 5 in this library).
        // PFS T1 -> KPPY ˊ.
        // PFS T4 -> KPPY unmarked.
        assertEquals("hag-gaˊ-fa", KonvertToPfs.convert("hak5-ka1-fa4", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testReadme_MixedText() {
        // Mixed text: Roman Orthography converts, the rest stays verbatim.
        val input = "Ngài ke生日he 1月5號。"
        val expected = "Ngaiˇ ge生日he 1月5號。"
        assertEquals(expected, KonvertToPfs.convert(input, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
    }

    // ---- PFS tone-mark placement on the syllable nucleus, not the first vowel ----

    @Test
    fun testPfs_NucleusPlacement_GlideThenA() {
        // i + a → mark on a (2nd-from-right is `i`, Exception 1 → rightmost letter `a`).
        assertEquals("kià", KonvertToPfs.convert("kia2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("kiá", KonvertToPfs.convert("kia3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("kia", KonvertToPfs.convert("kia4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ngiàng", KonvertToPfs.convert("ngiang2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("liàu", KonvertToPfs.convert("liau2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_NucleusPlacement_GlideThenO() {
        // i + o → mark on o.
        assertEquals("liòng", KonvertToPfs.convert("liong2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        // Tone 6 is unmarked in PFS; on a stop-final iok rhyme.
        assertEquals("kiok", KonvertToPfs.convert("kiok6", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_NucleusPlacement_IuUi() {
        // iu → mark u (2nd-from-right is `i`, so the rightmost letter takes the tone).
        // ui → mark u (2nd-from-right is `u`; no exception fires).
        assertEquals("liù", KonvertToPfs.convert("liu2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("liú", KonvertToPfs.convert("liu3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("thui", KonvertToPfs.convert("thui4", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_NucleusPlacement_IaException() {
        // ia → mark a (2nd-from-right is `i`, exception → rightmost letter).
        assertEquals("siá", KonvertToPfs.convert("sia3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("kià", KonvertToPfs.convert("kia2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_NucleusPlacement_WorkedExamples() {
        // Worked examples from the upstream PFS placement rule
        // (ThoivanHakfa/hakfa-agent-skills · linguistic_rules.md §2).
        // PFS_INPUT digit → expected PFS_UNICODE marking.
        val cases = listOf(
            "si1"    to "sî",        // Rule 1: single vowel
            "khua3"  to "khóa",      // KPPY-shape `ua` normalizes to canonical PFS `oa`; 2nd-from-right = o
            "koai3"  to "koái",      // Rule 3 default: 2nd-from-right of o,a,i = a
            "kuai3"  to "koái",      // KPPY-shape `uai` normalizes to canonical PFS `oai`
            "khoan3" to "khoán",     // Rule 3 default; -n coda
            "koet5"  to "koe̍t",     // Rule 3 default; checked, 2nd-from-right = e
            "liang1" to "liâng",     // Rule 3 default; -ng counts as 1 unit
            "siong1" to "siông",     // Rule 3 default; -ng counts as 1 unit
            "sia3"   to "siá",       // Exception 1: 2nd-from-right is i → rightmost
            "liu2"   to "liù",       // Exception 1
            "kui3"   to "kúi",       // Rule 3 default: 2nd-from-right = u (not i)
            "liuk5"  to "liu̍k",     // Special: iu+stop → 2nd-from-right (u)
            "hak5"   to "ha̍k",      // Rule 1 (single vowel) + vline-checked
            "sii1"   to "sṳ̂",       // Rule 1: ṳ counts as single vowel, NFC-stacks tone
        )
        for ((input, expected) in cases) {
            assertEquals(expected,
                KonvertToPfs.convert(input, LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE),
                "PFS placement diverged for $input")
        }
    }

    @Test
    fun testPfs_NucleusPlacement_IuStop() {
        // iu + stop coda: the `i`-exception does NOT fire (2nd-from-right is `u`),
        // so the tone lands on `u` per the default rule. `liuk5` → `liu̍k`.
        assertEquals("liu̍k", KonvertToPfs.convert("liuk5", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_NucleusPlacement_RoundTrip() {
        // Each of the previously-broken rhymes round-trips through PFS unicode losslessly.
        val cases = listOf("kia2", "kia3", "kia4", "ngiang2", "liau2", "liong2", "kiok6", "liu2", "liu3", "liuk5", "sia3", "thui4")
        for (c in cases) {
            val unicode = KonvertToPfs.convert(c, LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE)
            val back = KonvertToPfs.convert(unicode, LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT)
            assertEquals(c, back, "round-trip lost info for $c (unicode form: $unicode)")
        }
    }

    // ---- PFS y-onglide spelling (zero-initial `iV` → `yV`) ----

    @Test
    fun testPfs_YOnglide_FromKppy() {
        // PFS convention: every zero-initial syllable beginning with `i` is written
        // with `y` (onglide → replace, nucleus → prefix). Only `ii`/`ṳ` keeps the `i`.
        assertEquals("yû", KonvertToPfs.convert("iuˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yáng", KonvertToPfs.convert("iangˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yû-yáng", KonvertToPfs.convert("iuˊ-iangˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yông", KonvertToPfs.convert("iongˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yâm", KonvertToPfs.convert("iamˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        // Bare `i` (衣) and `i`+nasal coda (因, 音) also get the `y` prefix.
        assertEquals("yî", KonvertToPfs.convert("iˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yîn", KonvertToPfs.convert("inˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("yîm", KonvertToPfs.convert("imˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        // Consonant-initial syllables keep `i` after the consonant.
        assertEquals("khiâ", KonvertToPfs.convert("kiaˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testPfs_YOnglide_ParseBack() {
        // The y-spelling must parse back to the same internal syllable, so KPPY round-trip works.
        assertEquals("iuˊ", KonvertToPfs.convert("yû", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("iangˋ", KonvertToPfs.convert("yáng", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        // Nucleus form: bare `i` and `i`+nasal parse back through the y-prefix.
        assertEquals("iˊ", KonvertToPfs.convert("yî", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("inˊ", KonvertToPfs.convert("yîn", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("imˊ", KonvertToPfs.convert("yîm", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        // And PFS_INPUT digit form accepts `y` too.
        assertEquals("iangˋ", KonvertToPfs.convert("yang3", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))
        assertEquals("iˊ", KonvertToPfs.convert("yi1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))
        assertEquals("inˊ", KonvertToPfs.convert("yin1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))
        // Round-trip from KPPY through PFS unicode and back.
        val original = "iongˊ"
        val viaPfs = KonvertToPfs.convert(original, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE)
        assertEquals("yông", viaPfs)
        assertEquals(original, KonvertToPfs.convert(viaPfs, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        // Nucleus-form round-trip.
        for (kppy in listOf("iˊ", "inˊ", "imˊ")) {
            val pfs = KonvertToPfs.convert(kppy, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE)
            assertEquals(kppy, KonvertToPfs.convert(pfs, LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE),
                "round-trip lost info for $kppy (PFS form: $pfs)")
        }
    }

    // ---- KPPY NFD Latin diacritic input ----

    @Test
    fun testCapitalization_SingleUppercaseLetter() {
        // Single all-caps cased letter must classify as UPPER, not LOWER.
        // The previous `length > 1` guard ate this case and silently lowercased the output.
        assertEquals("M1", KonvertToPfs.convert("M1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        assertEquals("A1", KonvertToPfs.convert("A1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // And the all-caps round-trip survives a unicode pass.
        // Use Tone 1 (circumflex) — syllabic m/ng cannot carry checked tones 5/6
        // (see testSyllabic_CheckedTones_Rejected).
        val upper = "M̂"
        val viaInput = KonvertToPfs.convert(upper, LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT)
        assertEquals("M1", viaInput)
        assertEquals(upper, KonvertToPfs.convert(viaInput, LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testCapitalization_MidWordCapital_NotTitleCased() {
        // A mid-token capital is not a Title-case input — the renderer should not move
        // the capital to the start of the output (that would mangle the intent).
        // Treat as lowercase so the output stays lowercase rather than silently
        // promoting the mid-cap to syllable-initial.
        // (Identity conversion `convert(x, F, F)` is a no-op early return, so the
        // casing path is only exercised cross-format.)
        assertEquals("ngiang2", KonvertToPfs.convert("nGiang2", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // Multi-uppercase mid-word also lowercases.
        assertEquals("ngiang2", KonvertToPfs.convert("ngIANg2", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testKPPY_PFSStyleCoda_Canonicalized() {
        // PFS_INPUT "kap5" (T5, high-pitched checked) crosses systems to KPPY canonical "gab" (digit 5, unmarked-checked).
        assertEquals("gab", KonvertToPfs.convert("kap5", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_UNICODE))

        // KPPY tolerates PFS-style stop codas (p/t/k) on input but canonicalizes the coda
        // to b/d/g when rendering KPPY. The KPPY initial "k" (aspirated) is preserved.
        // KPPY_INPUT digit 5 (high-pitched checked) -> unmarked on KPPY-UNICODE.
        assertEquals("kab", KonvertToPfs.convert("kap5", LomajiFormat.KPPY_INPUT, LomajiFormat.KPPY_UNICODE))
        // KPPY_INPUT digit 6 (low-pitched checked) -> ˋ on KPPY-UNICODE.
        assertEquals("kadˋ", KonvertToPfs.convert("kat6", LomajiFormat.KPPY_INPUT, LomajiFormat.KPPY_UNICODE))
        assertEquals("kagˋ", KonvertToPfs.convert("kak6", LomajiFormat.KPPY_INPUT, LomajiFormat.KPPY_UNICODE))

        // Canonical KPPY input passes through (b/d/g already canonical); digit 6 → ˋ-checked.
        assertEquals("gabˋ", KonvertToPfs.convert("gab6", LomajiFormat.KPPY_INPUT, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testKppy_NFD_LatinDiacritics() {
        // NFD form: base vowel followed by a separate combining mark codepoint.
        val nfdAcute = "gá"
        val nfdCaron = "gǎ"
        val nfdGrave = "gà"
        val nfdGraveStop = "ab̀"
        assertEquals("kâ", KonvertToPfs.convert(nfdAcute, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))   // tone 1
        assertEquals("kà", KonvertToPfs.convert(nfdCaron, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))   // tone 2
        assertEquals("ká", KonvertToPfs.convert(nfdGrave, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))   // tone 3
        assertEquals("ap", KonvertToPfs.convert(nfdGraveStop, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE)) // ˋ-on-checked = digit 6 = internal T6 (PFS unmarked-checked)
    }

    @Test
    fun testWhitespace_Preserved() {
        // Leading, trailing, and interior whitespace must be preserved verbatim.
        assertEquals("  hag5  ga1  ", KonvertToPfs.convert("  hak5  ka1  ", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    // ---- Modern PFS ts/tsh spelling ----

    @Test
    fun testPfs_TsTsh_AcceptedOnInput() {
        // Modern PFS writes ts/tsh; older PFS writes ch/chh. Both must parse identically.
        assertEquals(Pfs.parseInput("cha1"), Pfs.parseInput("tsa1"))
        assertEquals(Pfs.parseInput("chha1"), Pfs.parseInput("tsha1"))
    }

    @Test
    fun testPfs_TsTsh_NormalizesToChChh() {
        // ts/tsh input normalizes to ch/chh on PFS output (FHL tradition).
        val tsa = Pfs.parseInput("tsa1")!!
        assertEquals("cha1", Pfs.renderInput(tsa))
        val tsha = Pfs.parseInput("tsha1")!!
        assertEquals("chha1", Pfs.renderInput(tsha))
    }

    @Test
    fun testPfs_TsTsh_CrossSystem() {
        assertEquals("za1", KonvertToPfs.convert("tsa1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        assertEquals("ca1", KonvertToPfs.convert("tsha1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testPfs_NucleusPlacement_UiToned() {
        // ui → mark u (2nd-from-right is `u`; no exception fires).
        assertEquals("thùi", KonvertToPfs.convert("thui2", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("thúi", KonvertToPfs.convert("thui3", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
    }
    @Test
    fun testUserReportedCheckedTones() {
        // 服 fu̍k (T5, high-pitched checked) -> KPPY unmarked-checked, digit 5.
        assertEquals("fug", KonvertToPfs.convert("fu̍k", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("fug5", KonvertToPfs.convert("fu̍k", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_INPUT))

        // PFS "ak" (T6, low-pitched checked) -> KPPY ˋ-checked, digit 6.
        assertEquals("agˋ", KonvertToPfs.convert("ak", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("ag6", KonvertToPfs.convert("ak", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_INPUT))

        // Round trip
        assertEquals("fu̍k", KonvertToPfs.convert("fug", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("ak", KonvertToPfs.convert("agˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }

    // ---- FHL dictionary (POJ-style tone numbering) ----

    @Test
    fun testFhlDict_ToneMapping() {
        // FHL dict 5 = PFS 1 (circumflex).
        assertEquals("hak5", KonvertToPfs.convert("hak5", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("ka1", KonvertToPfs.convert("ka5", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        // FHL dict 3 = PFS 2 (grave).
        assertEquals("ka2", KonvertToPfs.convert("ka3", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        // FHL dict 2 = PFS 3 (acute).
        assertEquals("ka3", KonvertToPfs.convert("ka2", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        // FHL dict 1 = PFS 4 (unmarked).
        assertEquals("ka4", KonvertToPfs.convert("ka1", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        // FHL dict 8 = PFS 5 (vline-checked).
        assertEquals("hak5", KonvertToPfs.convert("hak8", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        // FHL dict 4 = PFS 6 (unmarked-checked).
        assertEquals("hak6", KonvertToPfs.convert("hak4", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testFhlDict_ReverseToneMapping() {
        // PFS → FHL dict number.
        assertEquals("ka5", KonvertToPfs.convert("ka1", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("ka3", KonvertToPfs.convert("ka2", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("ka2", KonvertToPfs.convert("ka3", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("ka1", KonvertToPfs.convert("ka4", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("hak8", KonvertToPfs.convert("hak5", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
        assertEquals("hak4", KonvertToPfs.convert("hak6", LomajiFormat.PFS_INPUT, LomajiFormat.FHL_DICT_INPUT))
    }

    @Test
    fun testFhlDict_InvalidDigits_Preserved() {
        // FHL dict digits 6, 7, 9, 0 are not valid in Si-yen.
        assertEquals("ka6", KonvertToPfs.convert("ka6", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("ka7", KonvertToPfs.convert("ka7", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testFhlDict_ToUnicode() {
        // FHL dict 5 (= PFS 1) → circumflex.
        assertEquals("kâ", KonvertToPfs.convert("ka5", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.FHL_UNICODE))
        // FHL dict 8 (= PFS 5) → vline-checked.
        assertEquals("ha̍k", KonvertToPfs.convert("hak8", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.FHL_UNICODE))
        // FHL dict 4 (= PFS 6) → unmarked-checked.
        assertEquals("hak", KonvertToPfs.convert("hak4", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.FHL_UNICODE))
    }

    @Test
    fun testFhlUnicode_IdenticalToPfsUnicode() {
        // FHL_UNICODE glyphs are identical to PFS_UNICODE.
        val pfsInput = "hak5-ka1-fa4"
        val viaFhl = KonvertToPfs.convert(pfsInput, LomajiFormat.PFS_INPUT, LomajiFormat.FHL_UNICODE)
        val viaPfs = KonvertToPfs.convert(pfsInput, LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE)
        assertEquals(viaPfs, viaFhl)
    }

    @Test
    fun testFhlDict_CrossSystem() {
        // FHL dict → KPPY.
        assertEquals("hag5", KonvertToPfs.convert("hak8", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.KPPY_INPUT))
        assertEquals("ga1", KonvertToPfs.convert("ka5", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testRoundTrip_FhlDictInput() {
        assertRoundTrip("ka5", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("ka3", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("ka2", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("ka1", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("hak8", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("hak4", LomajiFormat.FHL_DICT_INPUT)
        assertRoundTrip("chii5", LomajiFormat.FHL_DICT_INPUT)
    }

    @Test
    fun testFhlDict_FullPhrase() {
        // 白話字 = hak8-fa3-sṳ1 (FHL dict) = hak5-fa2-sii4 (PFS input).
        assertEquals(
            "hak5-fa2-sii4",
            KonvertToPfs.convert("hak8-fa3-sii1", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.PFS_INPUT)
        )
    }

    @Test
    fun testFhlUnicode_AsSource_ToKppy() {
        // FHL_UNICODE shares glyphs with PFS_UNICODE; treat it as a valid source format.
        // ha̍k (PFS 5 = vline-checked) → KPPY digit 5 (unmarked-checked, coda -k → -g)
        assertEquals("hag5", KonvertToPfs.convert("ha̍k", LomajiFormat.FHL_UNICODE, LomajiFormat.KPPY_INPUT))
        // kâ (PFS 1 = circumflex) → KPPY modifier-letter ˊ
        assertEquals("gaˊ", KonvertToPfs.convert("kâ", LomajiFormat.FHL_UNICODE, LomajiFormat.KPPY_UNICODE))
    }

    @Test
    fun testFhlUnicode_AsSource_ToIpa() {
        // ha̍k (PFS 5, checked) → IPA hak̚˥
        assertEquals("hak̚˥", KonvertToPfs.convert("ha̍k", LomajiFormat.FHL_UNICODE, LomajiFormat.IPA))
        // sṳ (PFS 4) → IPA sɨ˥˥
        assertEquals("sɨ˥˥", KonvertToPfs.convert("sṳ", LomajiFormat.FHL_UNICODE, LomajiFormat.IPA))
    }

    // ---- IPA (render-only) ----

    @Test
    fun testIpa_BasicInitials() {
        assertEquals("ka˨˦", KonvertToPfs.convert("ka1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("kʰa˨˦", KonvertToPfs.convert("kha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ta˨˦", KonvertToPfs.convert("ta1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tʰa˨˦", KonvertToPfs.convert("tha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("pa˨˦", KonvertToPfs.convert("pa1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("pʰa˨˦", KonvertToPfs.convert("pha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ha˨˦", KonvertToPfs.convert("ha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ʋa˨˦", KonvertToPfs.convert("va1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("fa˨˦", KonvertToPfs.convert("fa1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("la˨˦", KonvertToPfs.convert("la1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ma˨˦", KonvertToPfs.convert("ma1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("na˨˦", KonvertToPfs.convert("na1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ŋa˨˦", KonvertToPfs.convert("nga1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("sa˨˦", KonvertToPfs.convert("sa1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsa˨˦", KonvertToPfs.convert("cha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsʰa˨˦", KonvertToPfs.convert("chha1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_Palatalization() {
        // z/c/s/ng before /i/ → tɕ/tɕʰ/ɕ/ɲ.
        assertEquals("tɕi˨˦", KonvertToPfs.convert("chi1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tɕʰi˨˦", KonvertToPfs.convert("chhi1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ɕi˨˦", KonvertToPfs.convert("si1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ɲi˨˦", KonvertToPfs.convert("ngi1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        // No palatalization before ṳ (ii).
        assertEquals("tsɨ˨˦", KonvertToPfs.convert("chii1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_Tones() {
        assertEquals("a˨˦", KonvertToPfs.convert("a1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("a˩˩", KonvertToPfs.convert("a2", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("a˧˩", KonvertToPfs.convert("a3", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("a˥˥", KonvertToPfs.convert("a4", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ap̚˥", KonvertToPfs.convert("ap5", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ap̚˨", KonvertToPfs.convert("ap6", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_Codas() {
        // Nasal codas.
        assertEquals("am˨˦", KonvertToPfs.convert("am1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("an˨˦", KonvertToPfs.convert("an1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("aŋ˨˦", KonvertToPfs.convert("ang1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        // Stop codas (unreleased).
        assertEquals("ap̚˥", KonvertToPfs.convert("ap5", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("at̚˥", KonvertToPfs.convert("at5", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ak̚˥", KonvertToPfs.convert("ak5", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_Vowels() {
        assertEquals("ɨ˨˦", KonvertToPfs.convert("chii1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA).removePrefix("ts"))
        // The ṳ vowel renders as ɨ.
        assertEquals("tsɨ˨˦", KonvertToPfs.convert("chii1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_IiRhyme_WithCodas() {
        // ṳ vowel followed by nasal / stop codas — exercise mapRhyme past the ii prefix.
        // No palatalization applies (palatalization is gated on /i/, not /ii/ = ɨ).
        assertEquals("tsɨm˩˩",  KonvertToPfs.convert("chiim2",  LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsɨn˩˩",  KonvertToPfs.convert("chiin2",  LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsɨŋ˨˦",  KonvertToPfs.convert("chiing1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsɨp̚˥",  KonvertToPfs.convert("chiip5",  LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsɨt̚˥",  KonvertToPfs.convert("chiit5",  LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("tsɨk̚˥",  KonvertToPfs.convert("chiik5",  LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_Syllabic() {
        assertEquals("m̩˨˦", KonvertToPfs.convert("m1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("m̩˥˥", KonvertToPfs.convert("m4", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ŋ̍˨˦", KonvertToPfs.convert("ng1", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
        assertEquals("ŋ̍˥˥", KonvertToPfs.convert("ng4", LomajiFormat.PFS_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_FullPhrase() {
        // ha̍k-kâ-fa (PFS unicode) → IPA.
        assertEquals(
            "hak̚˥-ka˨˦-fa˥˥",
            KonvertToPfs.convert("hak5-ka1-fa4", LomajiFormat.PFS_INPUT, LomajiFormat.IPA)
        )
    }

    @Test
    fun testIpa_FromKppy() {
        // KPPY → IPA cross-system.
        assertEquals("hak̚˥", KonvertToPfs.convert("hag5", LomajiFormat.KPPY_INPUT, LomajiFormat.IPA))
        assertEquals("ka˨˦", KonvertToPfs.convert("gaˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_AsSource_PassThrough() {
        // IPA as source format: all tokens pass through unchanged (parsing not supported).
        val ipaText = "hak̚˥-ka˨˦-fa˥˥"
        assertEquals(ipaText, KonvertToPfs.convert(ipaText, LomajiFormat.IPA, LomajiFormat.PFS_INPUT))
        assertEquals(ipaText, KonvertToPfs.convert(ipaText, LomajiFormat.IPA, LomajiFormat.IPA))
    }

    @Test
    fun testIpa_ViaFhlDict() {
        // FHL dict → IPA cross-system.
        assertEquals("hak̚˥", KonvertToPfs.convert("hak8", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.IPA))
        assertEquals("ka˨˦", KonvertToPfs.convert("ka5", LomajiFormat.FHL_DICT_INPUT, LomajiFormat.IPA))
    }

    @Test
    fun testRhyme_UeOe_Mapping() {
        // KPPY writes the rising diphthong as `ue` (國 gued); PFS writes it as `oe` (國 koet).
        // KPPY → PFS (unicode and input):
        assertEquals("koet", KonvertToPfs.convert("guedˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("koet6", KonvertToPfs.convert("gued6", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        // PFS → KPPY (unicode and input):
        assertEquals("guedˋ", KonvertToPfs.convert("koet", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))
        assertEquals("gued6", KonvertToPfs.convert("koet6", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
        // Full MOE-doc example (三和國中):
        assertEquals(
            "sâm fò koet chûng",
            KonvertToPfs.convert("samˊ foˇ guedˋ zungˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE)
        )
    }

    @Test
    fun testRhyme_UaOa_Mapping() {
        // KPPY writes the rising u-onglide uniformly as `uV`; PFS writes it uniformly as
        // `oV` (open, closed, and triphthong — there is no `ua`/`ue` in canonical PFS).

        // Closed `uan` / `oan` (e.g. 關 guanˊ ↔ koân) — tone mark lands on `a` (2nd-from-right of o,a,n).
        assertEquals("koân", KonvertToPfs.convert("guanˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("guanˊ", KonvertToPfs.convert("koân", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))

        // Closed `uang` / `oang`:
        assertEquals("koang1", KonvertToPfs.convert("guang1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("guang1", KonvertToPfs.convert("koang1", LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))

        // Checked `uad`/`uag` (KPPY) ↔ `oat`/`oak` (PFS):
        assertEquals("koat5", KonvertToPfs.convert("guad5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("koak5", KonvertToPfs.convert("guag5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))

        // Triphthong `uai` / `oai` (e.g. 怪 guaiˋ ↔ koái):
        assertEquals("koái", KonvertToPfs.convert("guaiˋ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("guaiˋ", KonvertToPfs.convert("koái", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))

        // Open `ua` also shifts: 瓜 guaˊ ↔ kôa (PFS has no `ua`).
        assertEquals("kôa", KonvertToPfs.convert("guaˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
        assertEquals("guaˊ", KonvertToPfs.convert("kôa", LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE))

        // `ui` (KPPY) stays `ui` in PFS — plain `u` nucleus, not an onglide.
        assertEquals("kûi", KonvertToPfs.convert("guiˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))

        // `oi` (KPPY 愛 oi) is a distinct rhyme — must NOT be folded to `ui`.
        assertEquals("ôi", KonvertToPfs.convert("oiˊ", LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }
}
