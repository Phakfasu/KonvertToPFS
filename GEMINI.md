# GEMINI.md — KonvertToPFS (Hakfa Roman Orthography Converter)

This project is a Kotlin Multiplatform library for converting between various Roman Orthography systems for the Si-yen (四縣腔) dialect of Hakfa.

## Terminology & Style Constraints
- **Language Reference**: Always refer to the language as **Hakfa**.
- **Orthography Reference**: Use the term **Roman Orthography** instead of "Romanization".
- **No Traditional Phonology Names**: **DO NOT** use traditional 聲韻學 names (e.g., 陰平, 陽平, 上聲, 去聲, 陰入, 陽入, or their English equivalents like "yin-ping"). Always use **Tone Numbers** (Tone 1, Tone 2, Tone 3, Tone 4, Tone 5, Tone 6). Use "checked tone" to refer to 入聲.
- **Applies to**: All code comments, documentation (including README, CLAUDE.md), commit messages, and any generated content or skills.

## Project Overview
- **Purpose**: Bidirectional conversion between Pha̍k-fa-sṳ (PFS), Kàu-pō͘ Phin-yîm (KPPY), FHL dictionary (POJ-style numbering), and IPA (Chao tone letters).
- **Technology**: Kotlin Multiplatform (JVM, JS, Wasm, iOS).
- **Core Principle**: Zero external dependencies and non-destructive processing of mixed text (CJK, punctuation, whitespace).

## Key Features & Tolerant Input
- **Seven formats**: PFS, KPPY, FHL dictionary (POJ-style numbering), and IPA (render-only).
- **Casing Preserving**: Lowercase, Title-case, and ALL-CAPS inputs map to the same casing on output.
- **Tolerant Input**: 
  - KPPY accepts both modifier-letters (`gaˋ`) and Latin-diacritics (`gà`) for tones.
  - PFS accepts both NFC and NFD forms of `ṳ` (U+1E73 and U+0075 U+0324).
  - PFS accepts both traditional `ch`/`chh` and modern `ts`/`tsh` spellings; output defaults to `ch`/`chh`.

## Architecture & Data Flow
1. **Tokenization**: Text is split into word-content (letters, digits, combining marks) and non-word content.
2. **Parsing**: Syllables are parsed from the source format into `HakfaSyllable`.
3. **Internal Representation (`HakfaSyllable`)**:
   - **Initials**: Unified to `p b t d k g`, `z c s`, `m ng h`. KPPY's `j q x` are unified to `z c s`.
   - **Rhymes**: `ii` denotes PFS `ṳ`. Coda stops are written as `b d g`.
   - **Tones**: Internal/PFS ordering 1–6.
4. **Rendering**: Rendered into the target format based on `LomajiFormat` enum.

### Additional Formats
- **FHL Dictionary**: `FHL_DICT_INPUT` (POJ-style 1–8 numbering) ↔ `FHL_UNICODE` (= PFS_UNICODE glyphs). Parsed via `FhlDict.kt` with tone remapping: FHL 1→PFS 4, FHL 2→PFS 3, FHL 3→PFS 2, FHL 4→PFS 6, FHL 5→PFS 1, FHL 8→PFS 5.
- **IPA**: Render-only format via `Ipa.kt`. Chao tone letters, palatalization before /i/, unreleased stop codas. Citation tones only (no sandhi).

### Multiplatform APIs
- **Kotlin/JVM/Native**: Use `KonvertToPfs.convert(text, from, to)`.
- **JS/TS Facade**: A specialized `convertHakfa(text, from, to)` function is exported for JS/TS consumers.

## Reference: Tone Mapping (Siyen)

| Internal/PFS | PFS 記號 | PFS 四縣調值 | KPPY 號 | KPPY 調型 | KPPY 調值 |
|--------------|---------|------------|--------|---------|---------|
| 1 | phâ | 24 | 1 | paˊ | 24 |
| 2 | phà | 11 | 2 | paˇ | 11 |
| 3 | phá | 41 | 3 | paˋ | 31 |
| 4 | pha | 44 | 4 | pa | 55 |
| 5 | pha̍k | [55] | 5 | pag | 5 |
| 6 | phak | [22] | 6 | pagˋ | 2 |

**Note:** This library aligns KPPY digit numbering with PFS — Tone 5 is the high-pitched checked tone (PFS vline + stop; KPPY unmarked + stop); Tone 6 is the low-pitched checked tone (PFS unmarked + stop; KPPY ˋ + stop). The official 教育部 KPPY 號 reverses these two slots; this library does NOT honor that swap.

## IPA Details

- **Render-only** — reverse-mapping IPA back to an orthographic form is lossy and not supported.
- **Citation form only** — tone sandhi is not applied; each syllable gets its citation tone.
- **Palatalization**: Alveolar onsets palatalize before /i/ (not /ii/): ts→tɕ, tsʰ→tɕʰ, s→ɕ, ŋ→ɲ.
- **Syllabic nasals**: m→m̩, ng→ŋ̍.
- **Stop codas**: Unreleased (p̚, t̚, k̚); only with tones 5/6.
- **Checked tone letters**: Single letter (˥ or ˨) reflecting short duration.
- **v → ʋ**: Labio-dental approximant, not fricative.

## FHL Dictionary Details

- Uses PFS orthography with **POJ-style 1–8 tone numbering** (digits 6, 7 unused in Si-yen).
- FHL_UNICODE output is identical to PFS_UNICODE glyphs.
- **FHL IME ≠ FHL dict**: The 信望愛客語輸入法 (FHL Input Method) uses PFS-style 1–6 numbering (= PFS_INPUT). The FHL dictionary uses POJ-style 1–8 numbering (= FHL_DICT_INPUT). Do not confuse them.
- **Numbering pitfall**: FHL dict, PFS, FHL IME, and KPPY 調號 all use small integers but with incompatible mappings. Always pivot via the PFS tone number.
