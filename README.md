# KonvertToPFS

Kotlin Multiplatform library for converting Hakka romanisation between **KPPY** (Kàu-pō͘ Phin-yîm / 教育部客家語拼音方案) and **PFS** (Pha̍k-fa-sṳ / 白話字), in both input and Unicode modes. Support is currently optimised for the Siyen (四縣腔) dialect.

## Features

- **Zero dependencies**: Pure Kotlin implementation.
- **Multiplatform**: Supports JVM, JS, iOS, and WasmJs targets.
- **Bidirectional**: Converts in any direction between KPPY and PFS.
- **Tone data**: `SiyenTones` provides official tone values and marks from the MOE 四縣腔聲調表 and the PFS tone table.

## Formats

Each romanisation system has two modes:

| Format | Example | Description |
|--------|---------|-------------|
| `PFS_INPUT` | `hak5-ka1-fa4` | PFS with tone numbers |
| `PFS_UNICODE` | `hak-kâ-fá` | PFS with tone diacritics on vowels |
| `KPPY_INPUT` | `hag5-ga1-fa4` | KPPY with tone numbers |
| `KPPY_UNICODE` | `hag-gaˋ-fáˊ` | KPPY with modifier-letter tone marks |

## Usage

```kotlin
import org.phakfasu.konverttopfs.KonvertToPfs
import org.phakfasu.konverttopfs.LomajiFormat.*

// Input → Unicode
KonvertToPfs.convert("hak5-ka1-fa4", PFS_INPUT, PFS_UNICODE)

// Cross-system (PFS → KPPY)
KonvertToPfs.convert("hak5-ka1-fa4", PFS_INPUT, KPPY_INPUT)

// Unicode → Input
KonvertToPfs.convert("hak-kâ-fá", PFS_UNICODE, PFS_INPUT)
```

## Tone data

`SiyenTones.tones` is a list of `ToneInfo` entries covering all six Siyen tones with values from two official MOE sources:

| Internal | 調類 | MOE 調型 | MOE 調值 | PFS 號 | PFS 記號 | PFS 四縣調值 |
|----------|------|---------|---------|--------|---------|------------|
| 1 | 陰平 | oˊ | 24 | 1 | â | 24 |
| 2 | 陽平 | oˇ | 11 | 2 | à | 11 |
| 3 | 上聲 | o` | 31 | 3 | á | 41 |
| 4 | 去聲 | o | 55 | 4 | a | 44 |
| 5 | 陰入 | og` | 2 | 6 | ak | [43] |
| 6 | 陽入 | og | 5 | 5 | ák | [44] |

Note: PFS numbers the two 入聲 tones in the reverse order from the internal/MOE convention.

## Build

Requires JDK 17+.

```bash
./gradlew build
```
