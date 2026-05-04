package org.phakfasu.konverttopfs

object KonvertToPfs {
    fun convert(text: String, from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String {
        if (from == to) return text
        
        // Simple tokenization for now
        val words = text.split(Regex("(?=[\\s\\p{Punct}])|(?<=[\\s\\p{Punct}])"))
        
        return words.joinToString("") { word ->
            if (word.isBlank() || word.all { !it.isLetter() && !it.isDigit() }) {
                word
            } else {
                convertWord(word, from, to)
            }
        }
    }
    
    private fun convertWord(word: String, from: LomajiFormat, to: LomajiFormat): String {
        // Step 1: Parse to intermediate Syllable list
        val syllables = parseWord(word, from) ?: return word
        
        // Step 2: Render to target format
        return renderWord(syllables, to)
    }
    
    private fun parseWord(word: String, format: LomajiFormat): List<HakkaSyllable>? {
        // Hyphen-separated syllables
        val parts = word.split("-")
        val syllables = mutableListOf<HakkaSyllable>()
        for (part in parts) {
            val s = parseSyllable(part, format) ?: return null
            syllables.add(s)
        }
        return syllables
    }
    
    private fun parseSyllable(syllableStr: String, format: LomajiFormat): HakkaSyllable? {
        val s = syllableStr.lowercase()
        return when (format) {
            LomajiFormat.KPPY_INPUT -> KPPinyin.parseInput(s)
            LomajiFormat.PFS_INPUT -> PhaKfhaSu.parseInput(s)
            LomajiFormat.KPPY_UNICODE -> KPPinyin.parseUnicode(s)
            LomajiFormat.PFS_UNICODE -> PhaKfhaSu.parseUnicode(s)
        }
    }
    
    private fun renderWord(syllables: List<HakkaSyllable>, format: LomajiFormat): String {
        return syllables.joinToString("-") { s ->
            when (format) {
                LomajiFormat.KPPY_INPUT -> KPPinyin.renderInput(s)
                LomajiFormat.PFS_INPUT -> PhaKfhaSu.renderInput(s)
                LomajiFormat.KPPY_UNICODE -> KPPinyin.renderUnicode(s)
                LomajiFormat.PFS_UNICODE -> PhaKfhaSu.renderUnicode(s)
            }
        }
    }
}
