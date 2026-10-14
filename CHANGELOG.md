# Changelog

All notable changes to Vocana Compat. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/);
versions follow Vocana's: a Vocana release that changes the extension API gets a matching Compat release.

## [1.0.0] - Unreleased

Built against Vocana 2.1.0, NeoForge 21.1.255, Minecraft 1.21.1.

### Added
- The module skeleton: `VocanaCompat` loads one module per target mod, only when the mod is installed and its toggle
  in `config/vocanacompat-modules.toml` is on, by reflective lookup of `dev.vocana.compat.<module>.<Module>Compat`.
- Modules (all stubs awaiting their content): Create, Wyrmroost, Ice and Fire: Community Edition, EMI, Curios API,
  Hex Casting (HexCast NEOFORGED), Ars Nouveau. Each has its own config file `vocanacompat-<module>.toml`.
- The data condition `vocanacompat:module_enabled` so a disabled module's recipes, tags and advancements stay out.
- Lexicon and Grimoire-group assembly: `src/main/lexicon/<module>.json` and `src/main/grimoire/<module>.json` are
  merged by the build into `META-INF/vocana/lexicon.json` and `META-INF/vocana/grimoire_groups.json`.
- An empty mixin config, `vocanacompat.mixins.json`, wired into the mod metadata.
- Unit tests for the lexicon files and GameTests that the mod, its toggles and `VocanaApi` load with no target mod.
- CI (`.github/workflows/build.yml`): builds Vocana from the ref in `vocana.ref`, then builds, unit-tests and
  game-tests this mod.
- `docs/targets.md`: the verified versions, coordinates and integration surfaces of the seven targets;
  `docs/adding_a_module.md`: the conventions a module follows.

#### Hex Casting (HexCast NEOFORGED)
- Hex staves (the `hexcasting:staves` tag) are Vocana Catalysts: they open the Chime screen, carry a spoken chant,
  tune a Chorus, take a Bound Catalyst, and add `staves.capacityBonus` Capacity (default 2).
- The Deed **DRAW** (`hainsu`, State frame, Thaumaturgy): the caster breathes Thrum in from the media they carry,
  through Hex's own media holders (amethyst dust first, then shards, then charged amethyst; phials only if
  `draw.fromBatteries`), at `draw.mediaPerThrum` (default 2500 media, so a dust is four Thrum), capped by
  `draw.maxThrumPerCast` (default 20; a measure such as TWO raises it up to twofold).
- The Deed **AWAKEN** (`rauspe`, Force frame, Thaumaturgy): a cypher, trinket or artifact held in the caster's hand
  casts its packaged Hex as if used, with Hex's own media, mishaps and cooldown.
- Imbue Bench recipes paid in Hex media: any essence into any gear for four amethyst dust, a staff affinity for
  edified planks and dust, and a chant of up to 20 CP for a charged amethyst, a book and a four-anchor ritual.
- Hex's amethyst blocks, sconce, quenched allay and edified logs count as `vocana:ritual_anchors`; `vocana:hears`
  tags teach LIGHT, CRYSTAL, STONE, LIFE, REMEMBER, STORE, SEEMING, SOUL, DRAW and AWAKEN from Hex's blocks and the
  wall scroll.
- `config/vocanacompat-hexcasting.toml`: a toggle per feature and every number above.
- `docs/hexcasting.md`: what was chosen, what was rejected and why, the source files each API was verified in, and
  whether the module would compile against the official HexMod `1.21` branch (it would).

### Fixed
- The skeleton's `.gitignore` rule `vocana/` (meant for the CI checkout of Vocana) also matched
  `src/main/java/dev/vocana/`, so no Java source had been committed; the rule is now anchored (`/vocana/`) and the
  skeleton sources are tracked.
