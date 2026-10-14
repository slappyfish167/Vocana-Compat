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
#### Wyrmroost
- Discovery: Wyrmroost's dragons, breath, eggs, geodes and platinum teach Vocana's words through
  `vocanacompat:wyrmroost/<word>` tags referenced from `vocana:hears/<word>` (FIRE and DRAGONFIRE from a Royal Red,
  FROST/WIND/SKY from an Alpine, WATER/LIGHTNING/SEA/THUNDER from a Butterfly Leviathan, SHADOW/NIGHT/TAKE from a
  Roost Stalker, LIFE/BEGIN from an egg, STONE/CRYSTAL/METAL from geodes and platinum, and more). Geode and platinum
  blocks count as ritual anchors for the Imbue Bench.
- Words: the Essence WYRM (`mespau`: found, any dragon; made, a dragon's weight, or a mending touch on one's own
  dragon), the Deed ROOST (`hespon`, State frame: settles or rouses one's own dragon, makes a wild one hesitate) and
  the Elder shade DRAGONFIRE of FIRE (`drakon`: hot, far-reaching, kindling, one more CP).
- Interplay: a dragon tamed by the caster (or a team-mate) refuses the caster's own magic damage; WYRM and ROOST
  never harm, command or touch a dragon on the caster's side or a stranger's tamed dragon. Vocana's SUMMON, FOLLOW,
  BIND and COMMAND already treat Wyrmroost's dragons as the tameable animals they are.
- Imbue recipes for red, blue and purple geodes, platinum and the soul crystal (a 20-CP channelled chant), and
  geode/platinum alternatives to the Ember, Rime, Earth and Shadow staff recipes.
- `config/vocanacompat-wyrmroost.toml`: toggles per word and feature, and every number.
- Unit tests for ROOST's rules and the module's data; GameTests that the words load with chapters and pronunciations
  and that nothing registers without Wyrmroost. `docs/wyrmroost.md` documents all of it and the API it verified.

### Fixed
- `.gitignore` ignored `src/main/java/dev/vocana/` along with the CI checkout of Vocana (the pattern `vocana/` was
  not anchored), so the skeleton's Java sources had never been committed; they are now.
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
