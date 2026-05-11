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
  HakkaSyllable.kt   — shared intermediate representation
  LomajiFormat.kt    — enum of the four supported formats
  KonvertToPfs.kt    — public API: KonvertToPfs.convert()
  KPPinyin.kt        — KPPY parser + renderer
  PhaKfhaSu.kt       — PFS parser + renderer
  SiyenTones.kt      — Siyen tone reference data (ToneInfo / SiyenTones)

lib/src/commonTest/kotlin/org/phakfasu/konverttopfs/
  KonvertToPfsTest.kt — unit + round-trip tests
```

## Internal representation

All syllables are normalised to `HakkaSyllable(initial, rhyme, tone)` using KPPY conventions:

- **Initials**: aspirated stops written with the base letter (`p b t d k g`), affricates as `z c`, sibilant as `s`. Variants `j q x` (used in KPPY before `i`) are unified to `z c s` internally.
- **Rhymes**: checked-tone codas use voiced stops (`b d g`); the special rhyme `ii` maps to PFS `ṳ`.
- **Tones 1–6**: 陰平 陽平 上聲 去聲 陰入 陽入 (MOE / internal ordering — PFS swaps 5 and 6 for 入聲).

## Tone systems

### Internal / MOE ordering (used in `HakkaSyllable.tone`)

| Tone | 調類 | MOE 調型 | MOE 調值 |
|------|------|---------|---------|
| 1 | 陰平 | oˊ | 24 |
| 2 | 陽平 | oˇ | 11 |
| 3 | 上聲 | o` | 31 |
| 4 | 去聲 | o | 55 |
| 5 | 陰入 | og` | 2 |
| 6 | 陽入 | og | 5 |

### PFS diacritic marks (combining, on the main vowel)

| PFS 號 | 記號 | 調類 | 四縣調值 | Internal tone |
|--------|------|------|---------|---------------|
| 1 | â | 陰平 | 24 | 1 |
| 2 | à | 陽平 | 11 | 2 |
| 3 | á | 上聲 | 41 | 3 |
| 4 | a | 去聲 | 44 | 4 |
| 5 | ák | 陽入 | [44] | **6** |
| 6 | ak | 陰入 | [43] | **5** |

PFS tones 5 and 6 are the reverse of the internal ordering for 入聲.

Full machine-readable data is in `SiyenTones.tones` (`SiyenTones.byInternal` and `SiyenTones.byPfsNumber` for lookup).

## Adding a new dialect or format

1. Create a parser/renderer object (see `KPPinyin.kt` or `PhaKfhaSu.kt` as templates).
2. Add a value to `LomajiFormat`.
3. Wire it into the `when` blocks in `KonvertToPfs.kt`.
4. Add round-trip tests in `KonvertToPfsTest.kt`.
