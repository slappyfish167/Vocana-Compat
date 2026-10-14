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
