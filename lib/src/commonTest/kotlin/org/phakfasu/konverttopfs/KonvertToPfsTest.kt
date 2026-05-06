package org.phakfasu.konverttopfs

import kotlin.test.Test
import kotlin.test.assertEquals

class KonvertToPfsTest {

    @Test
    fun testPfsToKppy_Input() {
        val pfs = "hak5-ka1-fa4"
        val expectedKppy = "hag5-ga1-fa4"
        assertEquals(expectedKppy, KonvertToPfs.convert(pfs, LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testKppyToPfs_Unicode() {
        val kppy = "gá"
        val expectedPfs = "ká"
        assertEquals(expectedPfs, KonvertToPfs.convert(kppy, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }

    @Test
    fun testZCS_JQX_Initials() {
        assertEquals("chi1", KonvertToPfs.convert("ji1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhi1", KonvertToPfs.convert("qi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("si1", KonvertToPfs.convert("xi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))

        assertEquals("chṳ1", KonvertToPfs.convert("zii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhṳ1", KonvertToPfs.convert("cii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("sṳ1", KonvertToPfs.convert("sii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testCheckedCodas() {
        assertEquals("ap5", KonvertToPfs.convert("ab5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("at5", KonvertToPfs.convert("ad5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("ak5", KonvertToPfs.convert("ag5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testTones() {
        assertEquals("a1", KonvertToPfs.convert("a", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a2", KonvertToPfs.convert("â", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a3", KonvertToPfs.convert("à", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("a4", KonvertToPfs.convert("á", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("ap5", KonvertToPfs.convert("ap", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
        assertEquals("ap6", KonvertToPfs.convert("a̍p", LomajiFormat.PFS_UNICODE, LomajiFormat.PFS_INPUT))
    }

    @Test
    fun testSyllabic() {
        assertEquals("m", KonvertToPfs.convert("m1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("ng", KonvertToPfs.convert("ng1", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("m̍", KonvertToPfs.convert("m6", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
        assertEquals("n̍g", KonvertToPfs.convert("ng6", LomajiFormat.PFS_INPUT, LomajiFormat.PFS_UNICODE))
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

    // ---- Round-trip tests (parse then render returns original) ----

    private fun assertRoundTrip(input: String, format: LomajiFormat) {
        val intermediate = when (format) {
            LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT -> if (format == LomajiFormat.PFS_INPUT) LomajiFormat.PFS_UNICODE else LomajiFormat.KPPY_UNICODE
            LomajiFormat.PFS_UNICODE, LomajiFormat.KPPY_UNICODE -> if (format == LomajiFormat.PFS_UNICODE) LomajiFormat.PFS_INPUT else LomajiFormat.KPPY_INPUT
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
        assertRoundTrip("chṳ1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chṳ2", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chṳ3", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chṳ4", LomajiFormat.PFS_INPUT)
        assertRoundTrip("chṳ6", LomajiFormat.PFS_INPUT)
        assertRoundTrip("sṳ2", LomajiFormat.PFS_INPUT)
    }

    @Test
    fun testRoundTrip_PfsInput_Syllabic() {
        assertRoundTrip("m1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("m6", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng1", LomajiFormat.PFS_INPUT)
        assertRoundTrip("ng6", LomajiFormat.PFS_INPUT)
    }

    @Test
    fun testRoundTrip_KppyInput() {
        assertRoundTrip("hag5-ga1-fa4", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ab5", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ad5", LomajiFormat.KPPY_INPUT)
        assertRoundTrip("ag5", LomajiFormat.KPPY_INPUT)
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
}
