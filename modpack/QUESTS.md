# Quest book plan

**Status: a design, not yet a quest book.** FTB Quests (and FTB Library/Teams) publish their NeoForge 1.21.1 builds
on CurseForge only, and Heracles (the Modrinth-hosted alternative) has no NeoForge 1.21.1 build, so no quest mod is
in the Modrinth list and no SNBT is shipped. When the pack adopts a quest mod (FTB Quests through the opt-in
CurseForge extras, for a private build), this plan maps one-to-one onto chapters: every task below is an item or an
advancement that already exists, with ids read from the mods' sources.

Conventions: **Task** is what completes the quest (an item to obtain, or an advancement to earn — Vocana grants its
advancements from server-side events, so advancement tasks are the reliable way to detect "you spoke a chant").
**After** is the quest it depends on. Rewards stay small: the pack wants the world, not the book, to be generous.

## Chapter 1 — The Listening Path (Vocana)

*"Everything here has a voice. Learn to hear it, then learn to answer."*

| # | Quest | Task | After | Reward / note |
|---|---|---|---|---|
| 1 | A Ringing in Copper | item `vocana:tuning_chime` (amethyst shard + copper ingot + stick) | — | 2 `minecraft:amethyst_shard` |
| 2 | Something Answers | advancement `vocana:first_form` | 1 | Hint: hold the Chime near fire, water, stone… and listen |
| 3 | Second Hearing | advancement `vocana:sensed` | 2 | — |
| 4 | Bound in Leather | item `vocana:grimoire` (book + ink sac + copper ingot) | 1 | — |
| 5 | In Your Own Words | advancement `vocana:kept_note` | 4 | Teaches the note-keeping loop |
| 6 | It Rings True | advancement `vocana:known` | 5 | — |
| 7 | First Working | advancement `vocana:first_working` | 2 | Explains open … close inscription |
| 8 | Words Made Real | advancement `vocana:spoken` | 7 | Needs Simple Voice Chat: check the push-to-talk key (default V) and microphone |
| 9 | A Better Catalyst | item `vocana:apprentice_staff` | 7 | — |
| 10 | The Bench | item `vocana:imbue_bench` (copper, amethyst block, book, smooth stone) | 9 | Points at the bench chapter in the Grimoire |
| 11 | Forged in Words | advancement `vocana:imbued` | 10 | 1 `vocana:essence_residue` |
| 12 | The Torc | item `vocana:torc` (2 gold + copper + amethyst shard) | 11 | Worn in the Curios **necklace** slot (Vocana-Compat Curios module) |
| 13 | Kept for Later | advancement `vocana:latent` (into `vocana:inscribed_scroll` or `vocana:spell_crystal`) | 7 | — |
| 14 | Six Voices of Nature | advancement `vocana:six_elements` | 13 | Unlocks the elemental staves: `vocana:ember_staff`, `rime_staff`, `storm_staff`, `tide_staff`, `gale_staff`, `earth_staff` (one optional quest each) |
| 15 | Resonant | advancement `vocana:resonance_5` | 6 | — |
| 16 | Second Nature | advancement `vocana:mastered` | 15 | — |
| 17 | A Word of Power | advancement `vocana:called` | 16 | — |
| 18 | In Your Own Tongue | advancement `vocana:in_your_own_tongue` | 17 | — |
| 19 | Many Voices | advancement `vocana:chorus` | 8 | Co-op quest: lead a Chorus with friends |
| 20 | Borne | advancement `vocana:borne` | 18 | Chapter finale; `vocana:archmage_staff` as the goal item |
| 21 | Deep Resonance | advancement `vocana:resonance_10` | 20 | — |

## Chapter 2 — Gears of Song (Create, with Vocana-Compat)

| # | Quest | Task | After | Note |
|---|---|---|---|---|
| 1 | Andesite Age | item `create:andesite_alloy` | — | — |
| 2 | Turning Over | item `create:water_wheel` or `create:windmill_bearing` | 1 | — |
| 3 | The Press | item `create:mechanical_press` | 2 | — |
| 4 | Brass | item `create:brass_ingot` | 3 | Needs a mixer and heated basin |
| 5 | Precision | item `create:precision_mechanism` | 4 | Sequenced assembly |
| 6 | Wind in the Fan | item `create:encased_fan` | 3 | Vocana-Compat: encased fans hear WIND — try a Chime nearby |
| 7 | A Singing Engine | item `create:steam_engine` | 4 | Steam engines hear FIRE and WATER |
| 8 | Brass in the Bench | advancement `vocana:imbued` with brass gear | 5, L-11 | Vocana-Compat Create imbue recipes (shown in EMI) |

## Chapter 3 — Glyphs and Sources (Ars Nouveau)

| # | Quest | Task | After | Note |
|---|---|---|---|---|
| 1 | Novice | item `ars_nouveau:novice_spell_book` | — | — |
| 2 | Source | item `ars_nouveau:source_gem` | 1 | — |
| 3 | A Jar to Hold It | item `ars_nouveau:source_jar` | 2 | — |
| 4 | Imbuement | item `ars_nouveau:imbuement_chamber` | 3 | — |
| 5 | The Apparatus | item `ars_nouveau:enchanting_apparatus` | 4 | — |
| 6 | Two Tongues | advancement `vocana:first_working` | 1 | Vocana-Compat: Thrum and mana talk to each other — the module's cost bridge |

## Chapter 4 — Patterns of Media (Hex Casting)

| # | Quest | Task | After | Note |
|---|---|---|---|---|
| 1 | Amethyst Dust | item `hexcasting:amethyst_dust` | — | Ids as in the HexCast NEOFORGED port (mod id `hexcasting`) |
| 2 | A Staff that Draws | item `hexcasting:staff/oak` | 1 | Item id **unverified** for the port; check in EMI before writing SNBT |
| 3 | Media and Thrum | advancement `vocana:first_working` | 2 | Vocana-Compat media <-> Thrum bridge |

## Chapter 5 — Wings over the World (Ice and Fire CE, Wyrmroost)

| # | Quest | Task | After | Note |
|---|---|---|---|---|
| 1 | Platinum | item `wyrmroost:platinum_ingot` | — | — |
| 2 | Geodes | item `wyrmroost:blue_geode` | 1 | — |
| 3 | A Dragon Staff | item `wyrmroost:dragon_staff` | 2 | Command tamed Wyrmroost dragons |
| 4 | Silver | item `iceandfire:silver_ingot` | — | Ice and Fire item id **unverified** in the CE port |
| 5 | Dragonfire Heard | Vocana Chime near a fire dragon (no task id: checkmark quest) | 4, L-2 | Vocana-Compat teaches FIRE/FROST/LIGHTNING from dragons |

## Chapter 6 — Roads Untaken (exploration)

Checkmark and item quests that send players outward: a Nature's Compass (`naturescompass:naturescompass`) and an
Explorer's Compass (`explorerscompass:explorerscompass`), a Waystone (`waystones:waystone`), the Aether
(advancement `aether:enter_aether`, **unverified id**), the Otherside of Deeper and Darker, the Undergarden, a YUNG's
stronghold, a Towns and Towers village, a Dungeons and Taverns tavern. Each rewards a few `waystones:warp_dust`.

## Before writing the SNBT

- Confirm every id marked **unverified** in-game (EMI, F3 + H for advanced tooltips, `/advancement`).
- FTB Quests 1.21 keeps the book under `config/ftbquests/quests/` (`data.snbt`, `chapter_groups.snbt`,
  `chapters/<chapter>.snbt`); write it with the in-game editor and commit the exported files rather than by hand.
