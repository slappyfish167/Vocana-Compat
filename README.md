# Vocana Compat

Companion mod for [Vocana](https://github.com/slappyfish167/Vocana) (NeoForge 21.1, Minecraft 1.21.1): soft-dependency
bridges that teach Vocana's spoken language new words from other mods' worlds and let their gear, slots and magic
systems talk to the Seal. Nothing here is required by Vocana; each bridge is a **module** that only loads when its
mod is installed, and every module can be switched off.

| Module | Mod | Toggle (`config/vocanacompat-modules.toml`) | Own settings |
|---|---|---|---|
| Create | `create` 6.0.x | `modules.create.enabled` | `config/vocanacompat-create.toml` |
| Wyrmroost | `wyrmroost` 1.4.x (NeoForge port) | `modules.wyrmroost.enabled` | `config/vocanacompat-wyrmroost.toml` |
| Ice and Fire: Community Edition | `iceandfire` 2.1.x | `modules.iceandfire.enabled` | `config/vocanacompat-iceandfire.toml` |
| EMI | `emi` 1.1.x | `modules.emi.enabled` | `config/vocanacompat-emi.toml` |
| Curios API | `curios` 9.5.x | `modules.curios.enabled` | `config/vocanacompat-curios.toml` |
| Hex Casting (HexCast NEOFORGED) | `hexcasting` 0.11.x | `modules.hexcasting.enabled` | `config/vocanacompat-hexcasting.toml` |
| Ars Nouveau | `ars_nouveau` 5.13.x | `modules.ars_nouveau.enabled` | `config/vocanacompat-ars_nouveau.toml` |

All toggles default to on. A module that is off registers nothing, and its data (recipes, tags, advancements)
stays out of the game through the `vocanacompat:module_enabled` condition. Its words are the one exception: the
merged lexicon is a static file Vocana reads whole, so a module's words exist in every game as unheard blank slots in
the Grimoire, and come to life only where the module's tags and behaviours are present.

## Install

Requires Vocana 2.1.0 or later and NeoForge 21.1.255 or later on Minecraft 1.21.1. Drop `vocana-compat-<version>.jar`
in `mods/` next to Vocana and whichever of the seven mods you play with. It runs on both sides; a client without it
can join a server that has it only if the loaded modules add nothing the client must know (words are server-side,
but Curios slots and EMI pages are not), so install it on both.

## Build

The build needs Vocana in your local maven, because Vocana publishes no public artifact:

```bash
git clone https://github.com/slappyfish167/Vocana.git && cd Vocana
git checkout "$(cat ../Vocana-Compat/vocana.ref)"     # the Vocana ref this skeleton was built against
./gradlew publishToMavenLocal -x test                  # publishes dev.vocana:vocana:<version>
cd ../Vocana-Compat
./gradlew build                                        # jar in build/libs, unit tests
./gradlew runGameTestServer                            # GameTests (Vocana + this mod, no target mods)
./gradlew runClient -Pcompat.run=create,curios         # dev client with those target mods on the classpath
./gradlew build -Pcompat.skip=hexcasting               # leave a module out while its maven is unreachable
```

Toolchain: Java 21, Gradle 9.2.1 (wrapper included), ModDevGradle 2.0.148, Mojang mappings. Every target's version
is in `gradle.properties`; `docs/targets.md` records where each was verified and what is still in doubt. The
GitHub Actions workflow (`.github/workflows/build.yml`) does the same steps: it checks out Vocana at the ref in
`vocana.ref`, publishes it locally, then builds, unit-tests and game-tests this repository. It uploads no artifact;
read the job log.

## The modpack

`modpack/` holds **Vocana: Resonance**, a curated NeoForge 1.21.1 pack built around Vocana and every target above:
about 140 mods for magic, engineering, creatures, exploration and quality of life, with Sodium-based performance. It
is a packwiz source resolved on Modrinth by `.github/workflows/modpack.yml`, which exports a `.mrpack` and boots it
as a dedicated server on every push. See [modpack/README.md](modpack/README.md).

## Repository map

- `src/main/java/dev/vocana/compat/` — `VocanaCompat` (loads modules), `Module` (the enum), `CompatModule`,
  `CompatConfig` (the toggles), `ModuleEnabledCondition`; one sub-package per module; `gametest/` (dev only).
- `src/main/lexicon/<module>.json`, `src/main/grimoire/<module>.json` — merged by the build into
  `META-INF/vocana/lexicon.json` and `META-INF/vocana/grimoire_groups.json`, which Vocana reads from every jar.
- `src/main/resources/data/vocanacompat/` — module data behind `neoforge:mod_loaded` + `vocanacompat:module_enabled`.
- `docs/targets.md` — the seven targets: versions, coordinates, licences, APIs to read.
- `docs/adding_a_module.md` — the conventions a module follows (paths, loading, the one rule, tags, conditions).
- `docs/curios.md` — the Curios module: slots, the focus items, its config and the in-game checklist.
- `vocana.ref` — the Vocana git ref CI builds against; bump it with Vocana.
- `modpack/` — the Vocana: Resonance modpack (packwiz source, curated `mods.txt`, default configs, quest plan).

## Licence

All Rights Reserved (see `LICENSE`), like Vocana. The target mods remain under their own licences; this repository
contains none of their code or assets.

## Continuous integration

`.github/workflows/build.yml` builds Vocana from the ref in `vocana.ref`, publishes it to the runner's local Maven and then builds this mod against it and every target mod's Maven. While the Vocana repository is private the workflow needs a repository secret **`VOCANA_TOKEN`**: a fine-grained personal access token with *Contents: read* on `slappyfish167/Vocana`. Without it the run stops at its first step with that message.
