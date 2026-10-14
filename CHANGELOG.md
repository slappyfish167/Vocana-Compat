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
- Ars Nouveau module (`docs/ars_nouveau.md`): Ars spells cast by players cost `arsCostFactor` (1.15) of Ars's own
  price through `SpellCostCalcEvent`, a Vocana staff in the off hand gives 5 % of it back, and Vocana keeps its own
  prices (`vocanaCostFactor` 1.0, never below one). The word SOURCE (`nauspi`, Thaumaturgy): a Working that touches
  the ground beside Ars source jars pours source into them, one that touches an Ars caster gives them mana, laid as a
  state it hastens mana regeneration, and SOURCE-imbued gear returns mana on a strike; everything a Working gives is
  capped by the Thrum it paid at a par rate less a 35 % conversion loss. The Ars glyph Resonate
  (`vocanacompat:glyph_vocana_thrum`, tier two) returns Thrum for its mana at the same rate, once per five seconds.
  Imbue Bench recipes paying in source gems and magebloom (any essence into the Enchanter's Sword, SOURCE into gear
  and staffs), the Enchanter's Sword as a +1 Catalyst, discovery tags (jars, magebloom, archwood, sourceberries,
  Wilden, starbuncles, drygmies), and GameTests and unit tests that run without Ars.
