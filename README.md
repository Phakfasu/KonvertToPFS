# KonvertToPFS

Kotlin Multiplatform library for converting Hakka Roman Orthography between **KPPY** (Kàu-pō͘ Phin-yîm / 教育部客家語拼音方案) and **PFS** (Pha̍k-fa-sṳ / 白話字), in both input and Unicode modes. Support is currently optimized for the Sixian (四縣腔) dialect.

## Features

- **Zero dependencies**: Pure Kotlin implementation.
- **Multiplatform**: Supports JVM, JS, iOS, and WasmJs targets.
- **Bidirectional**: Converts between KPPY and PFS in both formats.

## Formats

Each Roman Orthography system has two modes:
* **Input** — tone numbers appended to syllables, ASCII-safe (e.g. `fong1`)
* **Unicode** — tone diacriticals on vowels (e.g. `fông`)

| Format | Example | Description |
|--------|---------|-------------|
| `PFS_INPUT` | `hak5-ka1-fa4` | PFS with tone numbers |
| `PFS_UNICODE` | `hak-kâ-fá` | PFS with tone diacriticals |
| `KPPY_INPUT` | `hag5-ga1-fa4` | KPPY with tone numbers |
| `KPPY_UNICODE` | `hag-ga-fá` | KPPY with tone diacriticals |

## Usage

```kotlin
import org.phakfasu.konverttopfs.KonvertToPfs
import org.phakfasu.konverttopfs.LomajiFormat.*

// Input → Unicode
KonvertToPfs.convert("hak5-ka1-fa4", PFS_INPUT, PFS_UNICODE)

// Cross-system (PFS Input → KPPY Unicode)
KonvertToPfs.convert("hak5-ka1-fa4", PFS_INPUT, KPPY_UNICODE)
```

## Build

Requires JDK 17+.

```bash
./gradlew build
```