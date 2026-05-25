# KonvertToPFS

Kotlin Multiplatform library for converting Hakfa Roman Orthography between **PFS** (Pha̍k-fa-sṳ / 白話字), **KPPY** (Kàu-pō͘ Phin-yîm / 教育部客家語拼音方案), **FHL dictionary** (信望愛 Hak-fa Dictionary), and **IPA** (International Phonetic Alphabet), in both input and Unicode modes. Currently optimized for the Si-yen (四縣腔) dialect.

## Features

- **Zero dependencies**: pure Kotlin implementation.
- **Multiplatform**: targets JVM, JS, Wasm, and iOS.
- **Seven formats**: PFS, KPPY, FHL dictionary, and IPA — input, Unicode, and phonetic modes.
- **Non-destructive**: punctuation, whitespace, and non-Roman Orthography characters (e.g. CJK) pass through unchanged.
- **Casing preserving**: lowercase, Title-case, and ALL-CAPS inputs map to the same casing on output.
- **Tolerant input**: KPPY accepts both modifier-letter (`gaˋ`) and Latin-diacritic (`gà`) tone marks; PFS accepts NFC and NFD forms of `ṳ`.

## Formats

| Format | System | Tone style | Example (台灣客語白話字) |
|--------|--------|-----------|---------|
| `PFS_INPUT` | PFS | trailing digit (1–6) | `thoi2-van1 hak6-fa4 phak5-fa4-sii4` |
| `PFS_UNICODE` | PFS | combining mark on rhyme | `thòi-vân hak-fa pha̍k-fa-sṳ` |
| `KPPY_INPUT` | KPPY | trailing digit (1–6) | `toi2-van1 hag6-fa4 pag5-fa4-sii4` |
| `KPPY_UNICODE` | KPPY | modifier letter on syllable | `toiˇ-vanˊ hagˋ-fa pag-fa-sii` |
| `FHL_DICT_INPUT` | FHL dict | trailing digit (POJ-style 1–8) | `thoi3-van5 hak4-fa1 phak8-fa1-sii1` |
| `FHL_UNICODE` | FHL dict | combining mark (= PFS_UNICODE) | `thòi-vân hak-fa pha̍k-fa-sṳ` |
| `IPA` | IPA | Chao tone letters (render-only) | `tʰoi˩˩-ʋan˨˦ hak̚˨-fa˥˥ pʰak̚˥-fa˥˥-sɨ˥˥` |

Si-yen Hakfa uses six tones. The mapping across all systems is as follows:

| Tone | PFS marker | PFS value | KPPY marker | KPPY value | FHL dict # | IPA | Example (PFS / KPPY) |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| Tone 1 | `phâ` (circumflex) | 24 | `paˊ` (acute) | 24 | 5 | ˨˦ | 夫 fû / fuˊ |
| Tone 2 | `phà` (grave) | 11 | `paˇ` (caron) | 11 | 3 | ˩˩ | 扶 fù / fuˇ |
| Tone 3 | `phá` (acute) | 41 | `paˋ` (grave) | 31 | 2 | ˧˩ | 府 fú / fuˋ |
| Tone 4 | `pha` (unmarked) | 44 | `pa` (unmarked) | 55 | 1 | ˥˥ | 富 fu / fu |
| Tone 5 | `pha̍k` (vline, checked) | [55] | `pag` (unmarked, checked) | 5 | 8 | ˥ | 白 pha̍k / pag |
| Tone 6 | `phak` (unmarked, checked) | [22] | `pagˋ` (grave, checked) | 2 | 4 | ˨ | 福 fuk / fugˋ |

*Note: This library aligns PFS and KPPY tone digits 1↔1 (no 5↔6 swap). FHL dict uses POJ-style numbering (1–8); digits 6 and 7 are not used in Si-yen. IPA uses Chao tone letters (citation form only, no sandhi). The official 教育部 KPPY 號 reverses tones 5/6 relative to this library; do not transcode without confirming the source's convention.*

## Usage

```kotlin
import org.phakfasu.konverttopfs.KonvertToPfs
import org.phakfasu.konverttopfs.LomajiFormat.*

// Within a single system: input ↔ unicode
KonvertToPfs.convert("thoi2-van1 hak6-fa4 phak5-fa4-sii4", PFS_INPUT, PFS_UNICODE)
// → "thòi-vân hak-fa pha̍k-fa-sṳ"

// Across systems: PFS → KPPY
KonvertToPfs.convert("thoi2-van1 hak6-fa4 phak5-fa4-sii4", PFS_INPUT, KPPY_UNICODE)
// → "toiˇ-vanˊ hagˋ-fa pag-fa-sii"

// KPPY → PFS
KonvertToPfs.convert("toiˇ-vanˊ hagˋ-fa pag-fa-sii", KPPY_UNICODE, PFS_UNICODE)
// → "thòi-vân hak-fa pha̍k-fa-sṳ"

// FHL dictionary (POJ-style numbering) → PFS
KonvertToPfs.convert("hak8-fa3-sii1", FHL_DICT_INPUT, PFS_INPUT)
// → "hak5-fa2-sii4"

// FHL dictionary → Unicode (same glyphs as PFS_UNICODE)
KonvertToPfs.convert("hak8-ka5-fa1", FHL_DICT_INPUT, FHL_UNICODE)
// → "ha̍k-kâ-fa"

// Any format → IPA (render-only, with Chao tone letters)
KonvertToPfs.convert("hak5-ka1-fa4", PFS_INPUT, IPA)
// → "hak̚˥-ka˨˦-fa˥˥"

// KPPY → IPA
KonvertToPfs.convert("hag5-gaˊ-fa", KPPY_UNICODE, IPA)
// → "hak̚˥-ka˨˦-fa˥˥"

// Mixed text: only Roman Orthography is converted, the rest is preserved verbatim.
KonvertToPfs.convert("Ngài ke生日he 1月5號。", PFS_UNICODE, KPPY_UNICODE)
// → "Ngaiˇ ge生日he 1月5號。"
```

## Phonology mapping

### Consonants

| PFS | KPPY | IPA |
|-----|------|-----|
| p | b | [p] |
| ph | p | [pʰ] |
| m | m | [m] |
| f | f | [f] |
| v | v | [ʋ] |
| t | d | [t] |
| th | t | [tʰ] |
| n | n | [n] |
| l | l | [l] |
| ch (ts) | z (j) | [ts] ([tɕ]) |
| chh (tsh) | c (q) | [tsʰ] ([tɕʰ]) |
| s | s (x) | [s] ([ɕ]) |
| k | g | [k] |
| kh | k | [kʰ] |
| ng | ng | [ŋ] ([ɲ]) |
| h | h | [h] |
| (none) | (none) | ∅ |

PFS has two spellings for the affricates: traditional ch/chh and modern ts/tsh. Both are accepted on input; output uses ch/chh. KPPY uses j/q/x before i-vowels (allophonic variants of z/c/s).

Si-yen `v` is canonically realised as a labio-dental approximant [ʋ], not the fricative [v]. IPA parenthetical forms show palatalization before /i/: [ts]→[tɕ], [tsʰ]→[tɕʰ], [s]→[ɕ], [ŋ]→[ɲ].

### Vowels

| PFS | KPPY | IPA |
|-----|------|-----|
| a | a | [a] |
| e | e | [e] |
| i | i | [i] |
| o | o | [o] |
| u | u | [u] |
| ṳ | ii | [ɨ] |
| er | er | [ɤ] |

PFS_INPUT uses `ii` for `ṳ`; PFS_UNICODE uses `ṳ`. The rhyme `er` ([ɤ], mid-back unrounded) appears in a small number of Si-yen forms.

### Codas

| Type | PFS | KPPY | IPA |
|------|-----|------|-----|
| nasal | -m | -m | [m] |
| nasal | -n | -n | [n] |
| nasal | -ng | -ng | [ŋ] |
| checked | -p | -b | [p̚] |
| checked | -t | -d | [t̚] |
| checked | -k | -g | [k̚] |

Checked codas are unreleased and only appear with tones 5/6. Conversely, syllabic nasals (`m`, `ng` standing alone as a syllable) lack a stop coda and so cannot carry tones 5/6 — only tones 1–4 are accepted; inputs like `m5` or `ng6` are passed through unchanged.

### Initials — Si-yen and Nam-Si-yen

Source: Taiwan MOE *Hakfa Phonetic Scheme User Manual* (2012), §2.2 (initial-symbol usage notes) and §2.4 (cross-dialect initial / final comparison table). Initials are given in KPPY orthography (the library's internal canonical form); the **PFS** column shows the Pha̍k-fa-sṳ equivalent. Si-yen and Nam-Si-yen share an identical initial inventory.

| KPPY | PFS      | IPA  | Si-yen | Nam-Si-yen | KPPY examples |
|------|----------|------|:------:|:----------:|:--------------|
| ø    | (none)   | ∅    | ✓ | ✓ | a-阿亞, ai-哀矮, on-安鞍, iu-油又 |
| b    | p        | [p]  | ✓ | ✓ | ba-巴爸, bun-本笨, bi-比筆 |
| p    | ph       | [pʰ] | ✓ | ✓ | pa-怕爬, pun-噴盆, pi-皮鼻 |
| m    | m        | [m]  | ✓ | ✓ | ma-媽馬, mun-悶問, mi-米眯 |
| f    | f        | [f]  | ✓ | ✓ | fa-花化, fun-分粉, fi-□ |
| v    | v        | [ʋ]  | ✓ | ✓ | va-蛙娃, vun-溫穩, vi-位胃 |
| d    | t        | [t]  | ✓ | ✓ | da-打, dun-敦頓, di-知帝 |
| t    | th       | [tʰ] | ✓ | ✓ | ta-他塔, tun-吞屯, ti-提弟 |
| n    | n        | [n]  | ✓ | ✓ | na-拿那, nun-嫩, ni-□ (palatalised to ngi) |
| l    | l        | [l]  | ✓ | ✓ | la-拉, lun-論輪, li-梨利 |
| z    | ch (ts)  | [ts] | ✓ | ✓ | za-渣榨, zun-準遵, zii-資子 |
| c    | chh (tsh)| [tsʰ]| ✓ | ✓ | ca-差查, cun-村寸, cii-次詞 |
| s    | s        | [s]  | ✓ | ✓ | sa-沙紗, sun-孫損, sii-私士 |
| j    | ch (ts)  | [tɕ] | ✓ | ✓ | ji-知支 (allophone of z before i) |
| q    | chh (tsh)| [tɕʰ]| ✓ | ✓ | qi-妻欺 (allophone of c before i) |
| x    | s        | [ɕ]  | ✓ | ✓ | xi-西犀 (allophone of s before i) |
| g    | k        | [k]  | ✓ | ✓ | ga-家加, gun-滾棍, gi-居佢 |
| k    | kh       | [kʰ] | ✓ | ✓ | ka-卡, kun-綑捆, ki-企其 |
| ng   | ng       | [ŋ]  | ✓ | ✓ | nga-牙瓦, ngo-鵝餓, ngi-蟻 (→ [ɲ]) |
| h    | h        | [h]  | ✓ | ✓ | ha-蝦下, hun-訓婚, hi-去戲 |

#### KPPY ↔ PFS initial differences

Five KPPY initial letters spell their PFS equivalents differently:

| KPPY | PFS | Note |
|------|-----|------|
| `b`  | `p` | Voiceless unaspirated stop; KPPY uses the voiced-letter convention. |
| `p`  | `ph` | Aspirated stop; PFS marks aspiration with `h`. |
| `d`  | `t` | Voiceless unaspirated stop. |
| `t`  | `th` | Aspirated stop. |
| `g`  | `k` | Voiceless unaspirated stop. |
| `k`  | `kh` | Aspirated stop. |
| `z`  | `ch` (`ts`) | Affricate; PFS accepts both traditional `ch` and modern `ts` on input, renders `ch`. |
| `c`  | `chh` (`tsh`) | Aspirated affricate; PFS accepts both `chh` and `tsh`, renders `chh`. |

#### Allophonic j / q / x before i

KPPY uses `j q x` as the alveolo-palatal allophones of `z c s` before the high front vowel `i` (but **not** before the apical vowel `ii` = PFS `ṳ`). PFS does not orthographically distinguish them — `ji`, `qi`, `xi` all render the same affricate/sibilant letters (`chi`, `chhi`, `si`) as before any other vowel. The library normalizes `j q x` → `z c s` internally and restores them on KPPY output when the rhyme begins with `i` (not `ii`).

| KPPY  | Internal | KPPY render | PFS render | IPA   |
|-------|----------|-------------|------------|-------|
| `ji`  | `zi`     | `ji`        | `chi`      | [tɕi] |
| `qi`  | `ci`     | `qi`        | `chhi`     | [tɕʰi]|
| `xi`  | `si`     | `xi`        | `si`       | [ɕi]  |
| `zii` | `zii`    | `zii`       | `chṳ`      | [tsɨ] |
| `cii` | `cii`    | `cii`       | `chhṳ`     | [tsʰɨ]|
| `sii` | `sii`    | `sii`       | `sṳ`       | [sɨ]  |

#### Notes

- **Zero initial (ø)**: syllables beginning with a vowel have no initial consonant. Common with finals `a, ai, au, e, eu, i, o, oi, on, u, un, iu`, etc.
- **`v` realisation**: Si-yen `v` is a labio-dental approximant [ʋ], not the fricative [v].
- **`ng` palatalisation**: `ng` before `i` palatalises to [ɲ] (e.g. `ngi` → [ɲi]).
- **`n` before `i`**: largely merged into `ng` (→ [ɲ]) in Si-yen; standalone `ni` is rare.
- **Syllabic nasals**: `m`, `n`, `ng` may stand alone as syllables (no rhyme) — see the [Syllabic nasals](#syllabic-nasals) row of the Finals section.

### Finals — Si-yen and Nam-Si-yen

Source: Taiwan MOE *Hakfa Phonetic Scheme User Manual* (2012). Finals are given in KPPY orthography (the library's internal canonical form); the **PFS** column shows the Pha̍k-fa-sṳ equivalent. Checked finals carry tones 5/6 only. The five finals built on the apical vowel **ii** (`ii, iim, iin, iib, iid`) occur only in Si-yen and Nam-Si-yen — they are absent from Hailu, Tapu, Raoping, and Zhao'an (MOE note 16).

#### Open finals

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| a    | a   | [a]   | ✓ | ✓ | b-爸把, m-媽罵, d-打 |
| o    | o   | [o]   | ✓ | ✓ | g-哥高, s-嫂掃, d-多倒 |
| e    | e   | [e]   | ✓ | ✓ | m-姆, h-係, s-細 |
| ii   | ṳ   | [ɨ]   | ✓ | ✓ | z-資子, c-次詞, s-私士 |
| i    | i   | [i]   | ✓ | ✓ | d-知帝, g-居佢, k-企其 |
| u    | u   | [u]   | ✓ | ✓ | d-都肚, t-涂度, f-呼腐 |
| ai   | ai  | [ai]  | ✓ | ✓ | z-災債, c-採猜, s-晒徙 |
| au   | au  | [au]  | ✓ | ✓ | b-包豹, p-跑刨, m-矛貌 |
| eu   | eu  | [eu]  | ✓ | ✓ | ø-歐漚, d-斗鬥, h-候侯 |
| oi   | oi  | [oi]  | ✓ | ✓ | b-背, p-賠, m-妹 |
| ia   | ia  | [ia]  | ✓ | ✓ | d-踩, p-跛, ng-惹 |
| ie   | ie  | [ie]  | ✓ | ✓ | g-計解, k-契乞, ng-蟻艾 |
| io   | io  | [io]  | ✓ | ✓ | k-癰, ng-揉, h-靴 |
| iu   | iu  | [iu]  | ✓ | ✓ | d-丟, l-流柳, k-久救 |
| iau  | iau | [iau] | ✓ | ✓ | ø-柺, h-曉, ng-攬 |
| ieu  | ieu | [ieu] | ✓ | ✓ | g-鉤溝, k-摳扣, ng-偶藕 |
| ioi  | ioi | [ioi] | ✓ | ✓ | 痠 (Si-yen); MOE note 20 marks this final as limited |
| ua   | oa  | [ua]  | ✓ | ✓ | g-瓜掛, k-誇 (☆ Si-yen lacks `ngua` — MOE note 19) |
| uai  | oai | [uai] | ✓ | ✓ | g-乖怪, k-快 |
| ui   | ui  | [ui]  | ✓ | ✓ | g-鬼貴, d-追, l-類雷 |
| ue   | oe  | [ue]  | ✓ | ✓ | k-口 (no grapheme) |

#### Bilabial nasal coda (-m)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| am   | am  | [am]   | ✓ | ✓ | f-范凡, d-擔膽, l-藍覽 |
| em   | em  | [em]   | ✓ | ✓ | z-砧, c-岑, s-森參 |
| im   | im  | [im]   | ✓ | ✓ | g-金, k-欽, h-歆 |
| iim  | ṳm  | [ɨm]   | ✓ | ✓ | z-斟枕, c-深沉, s-沈甚 |
| iam  | iam | [iam]  | ✓ | ✓ | g-兼劍, k-欠謙, ng-驗嚴 |
| iem  | iem | [iem]  | ✓ | ✓ | g-□, k-□, ng-□ (no graphemes) |

#### Alveolar nasal coda (-n)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| an   | an   | [an]   | ✓ | ✓ | b-班半, d-單旦, z-贊盞 |
| en   | en   | [en]   | ✓ | ✓ | ø-恩應, z-曾贈, d-丁等 |
| in   | in   | [in]   | ✓ | ✓ | b-兵併, g-斤緊, ng-人認 |
| iin  | ṳn   | [ɨn]   | ✓ | ✓ | z-真蒸, c-稱神, s-勝甚 |
| ien  | ien  | [ien]  | ✓ | ✓ | b-編扁, g-見揭, ng-願原 |
| ian  | ian  | [ian]  | ✗ | ✓ | **Nam-Si-yen only** (after velar / zero initial — MOE note 22). Si-yen merges these into **ien** |
| on   | on   | [on]   | ✓ | ✓ | ø-安鞍, g-乾干, d-端短 |
| ion  | ion  | [ion]  | ✓ | ✓ | q-/c-吮全 |
| un   | un   | [un]   | ✓ | ✓ | b-本, t-屯吞, z-俊 |
| iun  | iun  | [iun]  | ✓ | ✓ | g-君僅, k-裙近, ng-勒 |
| uan  | oan  | [uan]  | ✓ | ✓ | g-關慣, k-款環, ng-頑玩 |
| uen  | oen  | [uen]  | ✓ | ✓ | g-耿 |

#### Velar nasal coda (-ng)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| ang  | ang  | [aŋ]   | ✓ | ✓ | ø-盎, m-猛蕻, g-耕庚 |
| iang | iang | [iaŋ]  | ✓ | ✓ | p-平病, g-驚鏡, l-領 |
| uang | oang | [uaŋ]  | ✓ | ✓ | g-桄莖 |
| ong  | ong  | [oŋ]   | ✓ | ✓ | b-榜幫, d-當擋, l-狼浪 |
| iong | iong | [ioŋ]  | ✓ | ✓ | b-枋放, t-暢, ng-讓娘 |
| ung  | ung  | [uŋ]   | ✓ | ✓ | p-蜂縫, d-東董, s-雙送 |
| iung | iung | [iuŋ]  | ✓ | ✓ | l-龍壟, g-芎拱, k-共 |

#### Bilabial stop coda (KPPY `-b` / PFS `-p`)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| ab   | ap  | [ap̚]   | ✓ | ✓ | d-答搭, t-塔踏, h-合盒 |
| eb   | ep  | [ep̚]   | ✓ | ✓ | d-□〔擖〕, l-□〔垃〕, s-澀齕 |
| ib   | ip  | [ip̚]   | ✓ | ✓ | l-立, g-急, k-及 |
| iib  | ṳp  | [ɨp̚]   | ✓ | ✓ | z-汁執, s-濕十 |
| ieb  | iep | [iep̚]  | ✓ | ✓ | g-□〔激〕, k-□〔搦〕 |
| iab  | iap | [iap̚]  | ✓ | ✓ | t-帖墊, l-粒獵, g-挾劫 |

#### Alveolar stop coda (KPPY `-d` / PFS `-t`)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| ad   | at  | [at̚]   | ✓ | ✓ | m-抹襪, t-達捷, l-辣 |
| ed   | et  | [et̚]   | ✓ | ✓ | b-北逼, d-德得, z-則仄 |
| id   | it  | [it̚]   | ✓ | ✓ | b-筆必, l-力栗, z-特職 |
| iid  | ṳt  | [ɨt̚]   | ✓ | ✓ | z-質職, c-直姪, s-食失 |
| ied  | iet | [iet̚]  | ✓ | ✓ | b-鱉, g-結蕨, ng-熱月 |
| iad  | iat | [iat̚]  | ✗ | ✓ | **Nam-Si-yen only** (after velar / zero initial — MOE note 24). Si-yen merges these into **ied** |
| od   | ot  | [ot̚]   | ✓ | ✓ | g-割葛, t-脫奪 (MOE unified table: 脫剝) |
| iod  | iot | [iot̚]  | ✓ | ✓ | j-/z-噭 |
| ud   | ut  | [ut̚]   | ✓ | ✓ | b-不, f-佛, m-沒歿 |
| iud  | iut | [iut̚]  | ✓ | ✓ | k-屈 |
| uad  | oat | [uat̚]  | ✓ | ✓ | g-刮括 |
| ued  | oet | [uet̚]  | ✓ | ✓ | g-嘓 |

#### Velar stop coda (KPPY `-g` / PFS `-k`)

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | KPPY examples |
|------|-----|-----|:------:|:----------:|:--------------|
| ag   | ak  | [ak̚]   | ✓ | ✓ | b-伯, t-踢, g-隔 |
| og   | ok  | [ok̚]   | ✓ | ✓ | ø-惡, b-博殼, g-各角 |
| ug   | uk  | [uk̚]   | ✓ | ✓ | b-卜, d-篤督, g-谷穀 |
| iag  | iak | [iak̚]  | ✓ | ✓ | b-壁, g-屐, k-展 |
| iog  | iok | [iok̚]  | ✓ | ✓ | p-縛, l-略掠, ng-弱 |
| iug  | iuk | [iuk̚]  | ✓ | ✓ | l-陸綠, g-菊局, ng-玉肉 |
| uag  | oak | [uak̚]  | ✓ | ✓ | no grapheme (MOE uses compound 口硬 = "very hard" as illustration) |

#### Syllabic nasals

| KPPY | PFS | IPA | Si-yen | Nam-Si-yen | Examples |
|------|-----|-----|:------:|:----------:|:---------|
| m    | m   | [m̩]   | ✓ | ✓ | 毋 m̌ (PFS `m̀`, KPPY `mˇ`) |
| n    | n   | [n̩]   | ✓ | ✓ | 嗯 |
| ng   | ng  | [ŋ̍]   | ✓ | ✓ | 魚 ngˇ, 五 ngˇ, 吳 ngˇ |

> Syllabic nasals carry tones 1–4 only. In Zhao'an the morphemes 魚/五/吳 are realised as syllabic `m` and 你 as `hen`; Si-yen and Nam-Si-yen do not share that pattern (MOE note 25).

#### Si-yen vs Nam-Si-yen — final inventory differences

Nam-Si-yen and Si-yen share an identical final inventory **except** for two finals that Nam-Si-yen introduces after velar (`g, k, ng`) or zero initials:

| Nam-Si-yen final | Si-yen equivalent | Example (Nam-Si-yen → Si-yen) |
|------------------|-------------------|-------------------------------|
| **ian** [ian]    | merges into **ien** [ien]   | 願 ngian → ngien |
| **iad** [iat̚]    | merges into **ied** [iet̚]   | 月 ngiad → ngied |

Sources: Taiwan MOE *Hakfa Phonetic Scheme User Manual* (2012), §2.3 (final-symbol usage notes) and §2.4 (cross-dialect initial / final comparison table); appendix syllable tables for Si-yen (pp. 15–37) and Nam-Si-yen (pp. 141–).

### Tones

`SiyenTones.tones` is a list of `ToneInfo` entries covering all six Siyen tones with values from multiple official sources:

| Internal/PFS | PFS 記號 | PFS 四縣調值 | KPPY 號 | KPPY 調型 | KPPY 調值 | FHL dict # | IPA |
|--------------|---------|------------|--------|---------|---------|-----------|-----|
| 1 | phâ | 24 | 1 | paˊ | 24 | 5 | ˨˦ |
| 2 | phà | 11 | 2 | paˇ | 11 | 3 | ˩˩ |
| 3 | phá | 41 | 3 | paˋ | 31 | 2 | ˧˩ |
| 4 | pha | 44 | 4 | pa | 55 | 1 | ˥˥ |
| 5 | pha̍k | [55] | 5 | pag | 5 | 8 | ˥ |
| 6 | phak | [22] | 6 | pagˋ | 2 | 4 | ˨ |

Note: Tones 5 and 6 are aligned between PFS and KPPY in this library. FHL dict uses POJ-style 1–8 numbering (digits 6, 7 unused in Si-yen). IPA renders citation tones only (no sandhi).

### PFS tone-mark placement

For `PFS_UNICODE` (and `FHL_UNICODE`) output, the tone diacritic lands on a single letter chosen by these rules:

1. **Single vowel** → mark that vowel.
2. **No vowel** (syllabic nasal) → mark the nasal letter; for `ng` the combining diacritic attaches to `n` (the leading codepoint), since `ng` is one positional unit.
3. **Compound vowels** → mark the **2nd letter from the right** of the syllable. The final `-ng` coda counts as **one unit** for the position count.
   - **Exception 1**: if the 2nd-from-right letter is `i`, mark the **rightmost** letter instead.
   - **Exception 2**: in a checked syllable, if the 2nd-from-right letter is `i` or `u` and the vowel cluster is **not** `iu` immediately before the stop coda, mark the **3rd letter** from the right. (Vestigial in Si-yen — does not fire on standard Si-yen syllables outside the `iu`+stop pattern below.)
   - **Special**: `iu` + stop coda → normal 2nd-from-right (e.g. `liu̍k`).

`ṳ` (PFS_INPUT `ii`) counts as a single vowel letter for position counting; the trema-below (U+0324) stays on `u` and the tone-combining mark stacks on top via NFC (e.g. `sṳ̂` = `s` + `u` + U+0324 + U+0302).

| Syllable | Letter units (`ng` as 1) | 2nd from right | Rule fired | Marked output |
|:---|:---|:---|:---|:---|
| `sî` | s, i | (single vowel) | Rule 1 | `sî` |
| `khóa` | k, h, o, a | `o` | Rule 3 default | `khóa` |
| `koái` | k, o, a, i | `a` | Rule 3 default | `koái` |
| `khoán` | k, h, o, a, n | `a` | Rule 3 default | `khoán` |
| `koe̍t` | k, o, e, t | `e` | Rule 3 default | `koe̍t` |
| `liâng` | l, i, a, **ng** | `a` | Rule 3 default | `liâng` |
| `siông` | s, i, o, **ng** | `o` | Rule 3 default | `siông` |
| `siá` | s, i, a | `i` | Exception 1 → rightmost | `siá` |
| `liù` | l, i, u | `i` | Exception 1 → rightmost | `liù` |
| `kúi` | k, u, i | `u` | Rule 3 default | `kúi` |
| `liu̍k` | l, i, u, k | `u` (checked, `iu`+stop) | Special → 2nd-from-right | `liu̍k` |
| `ha̍k` | h, a, k | (single vowel) | Rule 1 | `ha̍k` |
| `ǹg` | **ng** | — | Rule 2 | `ǹg` (combining on `n`) |
| `m̀` | m | — | Rule 2 | `m̀` |
| `sṳ̂` | s, u (+ U+0324) | `u` | Rule 1 | `sṳ̂` (NFC stack) |

> The full rule (with empirical justification from the FHL Hak-fa Dictionary corpus) is published at [ThoivanHakfa/hakfa-agent-skills · linguistic_rules.md §2](https://github.com/ThoivanHakfa/hakfa-agent-skills/blob/main/hakfa-roman-orthography-converter/references/linguistic_rules.md#2-pfs-tone-mark-placement-rules).

### IPA details

IPA output uses single-syllable citation form only — tone sandhi is not applied. Each syllable receives its citation tone from the tone table above; multi-syllable input is converted syllable-by-syllable with no sandhi inference.

**Palatalization before /i/**: The alveolar series palatalizes to alveolo-palatal before /i/ (but not before /ii/ = [ɨ]):

| PFS onset + i | IPA |
|:---|:---|
| ch(i) / ts(i) | tɕi |
| chh(i) / tsh(i) | tɕʰi |
| s(i) | ɕi |
| ng(i) | ɲi |

**Syllabic nasals**: Syllabic `m` (e.g. 唔 m̀) → [m̩]; syllabic `ng` → [ŋ̍].

**Stop codas**: Unreleased in IPA: -p → [p̚], -t → [t̚], -k → [k̚]. Only appear with tones 5 and 6.

**Checked-tone letters**: Tones 5 and 6 use a single Chao tone letter (˥ and ˨) reflecting the short duration of stop-final syllables, versus two-letter contours for open/nasal tones.

### FHL dictionary format

The 信望愛 Hak-fa Dictionary (FHL dict) uses PFS orthography for consonants, vowels, and codas, but numbers tones using the POJ-style digit→diacritic mapping (1–8) instead of PFS sequential 1–6.

| FHL dict # | Diacritic | PFS # | PFS diacritic |
|:---:|:---|:---:|:---|
| 1 | unmarked | 4 | unmarked |
| 2 | acute (á) | 3 | acute (á) |
| 3 | grave (à) | 2 | grave (à) |
| 4 | unmarked + stop | 6 | unmarked + stop |
| 5 | circumflex (â) | 1 | circumflex (â) |
| 8 | vertical line (a̍) + stop | 5 | vertical line (a̍) + stop |

Digits 6 and 7 are not used in Si-yen and are rejected. FHL_UNICODE output is identical in glyphs to PFS_UNICODE — only the numeric input format differs.

**FHL IME vs. FHL dict**: The 信望愛客語輸入法 (FHL Input Method) uses PFS-style 1–6 numbering — its input format is identical to PFS_INPUT. Do not confuse it with the FHL dictionary's POJ-style 1–8 numbering.

### Numbering systems compared

Four numbering systems use small integers with incompatible mappings. Always pivot via the PFS tone number — never transcode by digit value alone.

| PFS # | FHL IME | FHL dict | KPPY 調號 |
|:---:|:---:|:---:|:---:|
| 1 | 1 | 5 | 1 |
| 2 | 2 | 3 | 5 |
| 3 | 3 | 2 | 2 |
| 4 | 4 | 1 | 3 |
| 5 | 5 | 8 | 8 |
| 6 | 6 | 4 | 4 |

*Note: This library aligns PFS and KPPY tone digits 1↔1 internally. The official 教育部 KPPY 調號 follows 八聲 numbering (shown above), which diverges from PFS at every slot except Tone 1. The KPPY 調號 column is provided for reference when working with 教育部 source data.*

## Build

Requires JDK 17+.

```bash
./gradlew build
```

## License

KonvertToPFS is released under the **GNU General Public License v3.0 or later** (GPL-3.0-or-later). See [LICENSE](LICENSE) for the full text.

Copyright © 2026 Ngô͘ Hê-bí.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY — see the LICENSE file for details.
