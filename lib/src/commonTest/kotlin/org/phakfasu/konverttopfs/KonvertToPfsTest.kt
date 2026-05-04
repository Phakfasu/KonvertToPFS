package org.phakfasu.konverttopfs

import kotlin.test.Test
import kotlin.test.assertEquals

class KonvertToPfsTest {

    @Test
    fun testPfsToKppy_Input() {
        val pfs = "hak5-ka1-fa4" // Hak-kâ-fa (Hakka language)
        val expectedKppy = "hag5-ga1-fa4" // wait, KPPY input. Let's see: hak5 -> hag5, ka1 -> ga1, fa4 -> fa4.
        assertEquals(expectedKppy, KonvertToPfs.convert(pfs, LomajiFormat.PFS_INPUT, LomajiFormat.KPPY_INPUT))
    }

    @Test
    fun testKppyToPfs_Unicode() {
        // gá (ga4) -> ká
        val kppy = "gá"
        val expectedPfs = "ká"
        assertEquals(expectedPfs, KonvertToPfs.convert(kppy, LomajiFormat.KPPY_UNICODE, LomajiFormat.PFS_UNICODE))
    }
    
    @Test
    fun testZCS_JQX_Initials() {
        // KPPY "zi1" -> PFS "chṳ1" (wait, internal rhyme "ii", wait KPPY input for zii is zii1. zi1 is something else? In Sixian, zi/ci/si do not exist in KPPY, they are ji/qi/xi)
        // Let's test "ji1" (KPPY) -> "chi1" (PFS)
        assertEquals("chi1", KonvertToPfs.convert("ji1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhi1", KonvertToPfs.convert("qi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("si1", KonvertToPfs.convert("xi1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        
        // KPPY "zii1" -> PFS "chṳ1"
        assertEquals("chṳ1", KonvertToPfs.convert("zii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("chhṳ1", KonvertToPfs.convert("cii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("sṳ1", KonvertToPfs.convert("sii1", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }
    
    @Test
    fun testCheckedCodas() {
        // KPPY "ab5", "ad5", "ag5" -> PFS "ap5", "at5", "ak5"
        assertEquals("ap5", KonvertToPfs.convert("ab5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("at5", KonvertToPfs.convert("ad5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
        assertEquals("ak5", KonvertToPfs.convert("ag5", LomajiFormat.KPPY_INPUT, LomajiFormat.PFS_INPUT))
    }
    
    @Test
    fun testTones() {
        // PFS: a (1), â (2), à (3), á (4), ap (5), a̍p (6)
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
}
