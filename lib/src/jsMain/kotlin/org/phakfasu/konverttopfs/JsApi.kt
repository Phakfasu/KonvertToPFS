@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package org.phakfasu.konverttopfs

/**
 * String-typed facade for JS / TS consumers.
 *
 * @param text   input text (may contain mixed Roman Orthography, CJK, punctuation)
 * @param from   one of `"PFS_INPUT"`, `"PFS_UNICODE"`, `"KPPY_INPUT"`, `"KPPY_UNICODE"`,
 *               `"FHL_DICT_INPUT"`, `"FHL_UNICODE"`, `"IPA"`
 * @param to     same set as [from]
 * @return       the converted text; if either [from] or [to] is not a recognized
 *               format name, [text] is returned unchanged (non-destructive contract).
 */
fun convertHakfa(text: String, from: String, to: String): String {
    val fromFmt = LomajiFormat.entries.firstOrNull { it.name == from } ?: return text
    val toFmt = LomajiFormat.entries.firstOrNull { it.name == to } ?: return text
    return KonvertToPfs.convert(text, fromFmt, toFmt)
}
