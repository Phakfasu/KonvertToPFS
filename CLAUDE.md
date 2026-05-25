# KonvertToPFS — codebase guide

## Build & test

```bash
./gradlew build          # compile + test all targets
./gradlew jvmTest        # JVM tests only (fastest)
```

Requires JDK 17+. No other setup needed.

## Project layout

```
lib/src/commonMain/kotlin/org/phakfasu/konverttopfs/
  HakfaSyllable.kt   — shared intermediate representation
  LomajiFormat.kt    — enum of the seven supported formats
  KonvertToPfs.kt    — public API: KonvertToPfs.convert()
  Kppy.kt            — KPPY parser + renderer
  Pfs.kt             — PFS parser + renderer
  FhlDict.kt         — FHL dictionary parser + renderer (POJ-style tone remapping)
  Ipa.kt             — IPA renderer (render-only; Chao tone letters)
  SiyenTones.kt      — Siyen tone reference data (ToneInfo / SiyenTones)

lib/src/commonTest/kotlin/org/phakfasu/konverttopfs/
  KonvertToPfsTest.kt — unit + round-trip tests
```

## Internal representation

All syllables are normalised to `HakfaSyllable(initial, rhyme, tone)` using KPPY conventions:

- **Initials**: aspirated stops written with the base letter (`p b t d k g`), affricates as `z c`, sibilant as `s`. Variants `j q x` (used in KPPY before `i`) are unified to `z c s` internally.
- **Rhymes**: checked-tone codas use voiced stops (`b d g`); the special rhyme `ii` maps to PFS `ṳ`. The rising u-onglide is stored as `uV` (KPPY-style); PFS always renders it as `oV` (`ua`→`oa`, `ue`→`oe`, etc.) — there is no `ua` or `ue` in canonical PFS.
- **Tones 1–6**: Tone 1, Tone 2, Tone 3, Tone 4, Tone 5, Tone 6 (Aligned between PFS and KPPY).

### Rhyme spelling differences (KPPY ↔ PFS)

Source: 教育部 客語拼音方案 韻母表 (Si-yen) + the FHL Hakfa dictionary corpus.

| KPPY  | PFS    | Example                | Notes |
|-------|--------|------------------------|-------|
| `ua`  | `oa`   | 瓜 guaˊ / kôa          | Open form also shifts. |
| `uan` | `oan`  | 關 guanˊ / koân        | Tone mark lands on `a`. |
| `uang`| `oang` | 桄                     |       |
| `uad` | `oat`  | 刮 (coda b/d/g→p/t/k)  | Checked tone. |
| `uag` | `oak`  | 硬                     | Checked tone. |
| `uai` | `oai`  | 怪 guaiˋ / koái         | Triphthong. |
| `ue`  | `oe`   | 喂 (rare)              | Open form also shifts. |
| `uen` | `oen`  | 耿                     |       |
| `ued` | `oet`  | 國 guedˋ / koet         | Checked tone. |
| `ui`  | `ui`   | 醉 guiˋ / kúi           | Plain `u` nucleus, not an onglide — no shift. |
| `oi`  | `oi`   | 愛 oi / oi              | `o-` nucleus, not a `ua-` onglide — never folds. |

The rendering rule is "every rising u-onglide rhyme shifts to `oV`" — implemented in `Pfs.kt` `renderBase`. PFS-only inputs that use the KPPY-style spelling (`kua`, `kuan`, `kuai`, `kuang`, `kue`, `kuet`, `kuen`, `kued`) are accepted on parse but normalized to the canonical PFS shape on render (lossy round-trip, intentional).

### Zero-initial `i-` syllables (PFS `y-`)

Every zero-initial syllable beginning with `i` is written in PFS with `y`, except `ii`/`ṳ`:

- **Onglide `iV`** (i + another vowel): `i` is *replaced* by `y` — `ia → ya`, `iu → yu`, `iong → yong`, `iam → yam`. E.g. KPPY `iuˊ` / `iangˋ` → PFS `yû` / `yáng`.
- **Nucleus `i`** (bare `i`, `i` + nasal coda, `i` + stop coda): `i` is *prefixed* with `y` — `i → yi`, `im → yim`, `in → yin`, `ip → yip`. E.g. KPPY `iˊ` → PFS `yî` (衣); KPPY `inˊ` → PFS `yîn` (因); KPPY `imˊ` → PFS `yîm` (音).
- **Close-central `ii`** (PFS_UNICODE `ṳ`): keeps the `i`-spelling, never gets `y`.

Consonant-initial syllables (`kia`, `liong`) are unaffected. Implemented in `Pfs.kt` `renderBase`; `parseInput` folds a leading `y` back: `y` + `i…` drops the `y` (nucleus form), `y` + other vowel replaces `y` with `i` (onglide form) — so PFS input with `y` parses correctly and round-trips through KPPY.

## Tone systems

### Internal / PFS ordering (used in `HakfaSyllable.tone`)

| Tone | PFS 記號 | PFS 四縣調值 | KPPY 號 | KPPY 調型 | KPPY 調值 | FHL dict # | IPA |
|------|---------|------------|--------|---------|---------|-----------|-----|
| 1 | phâ | 24 | 1 | paˊ | 24 | 5 | ˨˦ |
| 2 | phà | 11 | 2 | paˇ | 11 | 3 | ˩˩ |
| 3 | phá | 41 | 3 | paˋ | 31 | 2 | ˧˩ |
| 4 | pha | 44 | 4 | pa | 55 | 1 | ˥˥ |
| 5 | pha̍k | [55] | 5 | pag | 5 | 8 | ˥ |
| 6 | phak | [22] | 6 | pagˋ | 2 | 4 | ˨ |

Note: This library uses **PFS-style numbering** for both PFS and KPPY digits — Tone 5 is the high-pitched checked tone (PFS vline + stop; KPPY unmarked + stop); Tone 6 is the low-pitched checked tone (PFS unmarked + stop; KPPY ˋ + stop). The official 教育部 KPPY 號 reverses these two slots; this library does NOT honor that swap. FHL dict uses POJ-style 1–8 numbering (digits 6, 7 unused in Si-yen). IPA uses Chao tone letters (citation form only, no sandhi).

Full machine-readable data is in `SiyenTones.tones` (`SiyenTones.byInternal`, `SiyenTones.byKppyNumber`, and `SiyenTones.byFhlDictNumber` for lookup).

### IPA details

- **Render-only**: `Ipa.render(s)` produces IPA; reverse parsing returns null (tokens pass through).
- **Citation form only**: no tone sandhi.
- **Palatalization**: z/c/s/ng → tɕ/tɕʰ/ɕ/ɲ before /i/ (not /ii/).
- **Syllabic nasals**: m → m̩, ng → ŋ̍.
- **v → ʋ** (approximant); **er → ɤ** (mid-back unrounded).
- **Stop codas**: unreleased (p̚, t̚, k̚).
- **Checked tones**: single Chao letter (˥ or ˨).

### FHL dictionary format

- `FhlDict.kt` handles POJ-style 1–8 tone numbering (digits 6, 7 rejected for Si-yen).
- Mapping: FHL 1→PFS 4, FHL 2→PFS 3, FHL 3→PFS 2, FHL 4→PFS 6, FHL 5→PFS 1, FHL 8→PFS 5.
- FHL_UNICODE = PFS_UNICODE glyphs (same parser/renderer).
- **FHL IME ≠ FHL dict**: FHL IME uses PFS-style 1–6 (= PFS_INPUT); FHL dict uses POJ-style 1–8.

### Numbering system warning

Four numbering systems (PFS, FHL IME, FHL dict, KPPY 調號) use incompatible small-integer tone digits. Always pivot via the PFS tone number (1–6). Never transcode by digit value alone.

## Adding a new dialect or format

1. Create a parser/renderer object (see `Kppy.kt` or `Pfs.kt` as templates).
2. Add a value to `LomajiFormat`.
3. Wire it into the `when` blocks in `KonvertToPfs.kt`.
4. Add round-trip tests in `KonvertToPfsTest.kt`.
