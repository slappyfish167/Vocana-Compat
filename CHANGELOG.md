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
- Curios module (`docs/curios.md`): players get the `necklace`, `ring` (×2) and `charm` slots; the Torc fits the
  necklace slot and counts as worn there (`VocanaApi.registerWorn`), a Whispering Relic fits the charm slot and
  whispers its word after 30 s of wear; four focus items, the Ring of Focus (+2 Capacity each), Amulet of Resonance
  (Thrum regeneration ×1.25), Charm of Calm (Strain ×0.85) and Signet of the Elements (drinks an Essence Residue;
  Workings of that essence cost ×0.9), with recipes, sprites (`tools/generate_textures.py`) and settings in
  `config/vocanacompat-curios.toml`; a self-check logs the resolved slots and tags; GameTests that nothing of it
  exists without Curios and unit tests for its numbers.

### Fixed
- The `.gitignore` entry for the CI checkout of Vocana (`vocana/`) also hid `src/main/java/dev/vocana/`, so the
  skeleton's Java sources were never committed; the pattern is anchored to the root (`/vocana/`).
