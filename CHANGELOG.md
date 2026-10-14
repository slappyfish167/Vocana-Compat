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
- EMI module (`docs/emi.md`): three EMI categories. `vocanacompat:imbue` lists every Imbue Bench recipe with its
  target gear, an imbued preview, materials, ritual catalysts, price, triggers, shade surcharge and channel step;
  `vocanacompat:ritual` explains the ritual figure, channelling at the bench and stripping an imbue;
  `vocanacompat:lexicon` has a proficiency page and one page per word, drawn from the player's snapshot so unheard
  forms stay blank slots and heard ones show form, respelling, class, chapter, group, meaning, binding and practice.
  The bench's recipes are read from the integrated server in single player and, when the module runs on a dedicated
  server with EMI, sent to clients by the optional payload `vocanacompat:imbue_recipes` on datapack sync. Config
  `vocanacompat-emi.toml`: a toggle per category and page, `syncRecipes`.

### Fixed
- `.gitignore` ignored `vocana/` anywhere, which hid `src/main/java/dev/vocana/` from git; the rule is now anchored (`/vocana/`).
