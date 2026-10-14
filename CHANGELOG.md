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
- Ice and Fire module (`docs/iceandfire.md`): sirens removed (`removeSirens`, a `neoforge:remove_spawns` modifier
  plus spawn and join hooks that let spawn eggs and `/summon` through; `removeSirenIslands` empties the island's
  biome tag); dragonsteel rebalanced towards netherite with a lean per kind through `ItemAttributeModifierEvent`
  (fire hits harder, ice is tougher and sheds freezing, lightning is quick; every offset in
  `vocanacompat-iceandfire.toml`); wyrmsteel, the netherite-dragonsteel alloy, made only in a superheated Create
  mixer, and its Wyrmsteel Staff (capacity 6, affinity FIRE, FROST and LIGHTNING), sword and pickaxe made only by
  Create sequenced assembly; `vocana:hears` tags for dragons, sea serpents, cyclopes, death worms, dread mobs,
  pixies, hippogryphs, ghosts and the dragon blocks; the Elder shades DRAGONFROST (`drakopagos`) and DRAGONBOLT
  (`drakastrape`); Imbue Bench recipes from dragon blood, scales and dragonsteel; the data condition
  `vocanacompat:iceandfire/config`.
