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

#### Create
- Three words: **KINETIC** (`krenshu`, an Essence of Transmutation: a whirling contact that leaves a creature reeling,
  a wound-down IMBUE state, a weapon imbue that whirls, a tool imbue that digs faster among turning machinery;
  contraptions bear it), **SPIN** (`kirsau`, a Force Deed: whirls creatures and items, cranks hand cranks and valve
  handles by itself, stokes a blaze burner with the FIRE or LIGHTNING the chant carries) and **BRASS** (`orichalcum`,
  an Elder shade of METAL with the `encase` trait). Create's machines teach them through `vocana:hears` tags, and
  also teach METAL, FIRE, WIND, WATER and LIGHTNING.
- Imbue Bench recipes paid in precision mechanisms, andesite alloy and brass (`create/kinetic_tool`,
  `kinetic_weapon`, `kinetic_armor`, `brass_any`).
- Create recipes: a Torc on the assembly line (`create:sequenced_assembly`, with the transitional item
  `vocanacompat:create/incomplete_torc`) and a Tuning Chime alloyed over a seething burner (`create:mixing`).
- Create's structural and contraption parts without block entities join `vocana:immune`.
- `CreateRecipes`, Gson builders for Create's recipe shapes, and `docs/create_recipes.md`, templates for other modules.
- `config/vocanacompat-create.toml`: every number and a toggle per feature (`docs/create.md`).
- GameTests (without Create: the words load and are placed, the imbue recipes parse and stay out) and unit tests
  (recipe shapes, shipped data, Sphinx phones).

#### Skeleton fixes made by the Create module
- `.gitignore` ignored `vocana/` anywhere, which hid `src/main/java/dev/vocana/`; it is anchored to the root and the
  skeleton's Java sources are committed.
- `-Pcompat.skip=<module>` also leaves that module's unit tests out of the test source set.
