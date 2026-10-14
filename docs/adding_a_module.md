# Adding (or filling) a module

A module is one package, one config file, one lexicon file, one Grimoire file and one data folder, all named after
the module id. Seven modules exist as stubs; this page is the contract each follows so that seven people can work
on them without touching the same file. Read `ARCHITECTURE_NOTES.md` in the Vocana repository first, especially
"2.1 shared interfaces".

## The pieces

| Piece | Path | Owner |
|---|---|---|
| Entry class | `src/main/java/dev/vocana/compat/<module>/<Module>Compat.java` implementing `dev.vocana.compat.CompatModule` | the module |
| Everything else Java | `src/main/java/dev/vocana/compat/<module>/**` | the module |
| Module settings | `<module>/<Module>Config.java` → `config/vocanacompat-<module>.toml`, registered in `init` with `VocanaCompat.registerModuleConfig(Module.X, SPEC)` | the module |
| Words | `src/main/lexicon/<module>.json` (merged into `META-INF/vocana/lexicon.json` by the build) | the module |
| Grimoire groups | `src/main/grimoire/<module>.json` (merged into `META-INF/vocana/grimoire_groups.json`) | the module |
| Data | `src/main/resources/data/vocanacompat/<kind>/<module>/...` | the module |
| Assets | `src/main/resources/assets/vocanacompat/<kind>/<module>/...`; lang keys under `vocanacompat.<module>.*` in the shared `lang/en_us.json` (append, do not reorder) | the module (lang: additive) |
| GameTests | `src/main/java/dev/vocana/compat/gametest/<Module>GameTests.java` | the module |
| Unit tests | `src/test/java/dev/vocana/compat/<module>/**` | the module |
| Module toggle | `modules.<module>.enabled` in `CompatConfig` | the skeleton (never edited by a module) |
| Target versions | `gradle.properties` | the skeleton; a module may bump its own target's line |

The module ids and the enum constants: `create` (`Module.CREATE`), `wyrmroost`, `iceandfire` (`ICE_AND_FIRE`),
`emi`, `curios`, `hexcasting` (`HEXCAST`), `ars_nouveau` (`ARS_NOUVEAU`).

## Loading

`VocanaCompat`'s constructor walks `Module.values()`. For each: if `ModList.get().isLoaded(modId)` is false the
module is skipped silently; if `modules.<id>.enabled` is false it is skipped with a log line; otherwise the class
named `dev.vocana.compat.<id>.<Module>Compat` is loaded reflectively, instantiated with its no-arg constructor and
`init(modBus)` is called. Any exception or `LinkageError` from `init` is logged and the module is dropped; Vocana
keeps running. `init` runs during mod construction, which is where `DeferredRegister.register(modBus)`,
`modBus.addListener(...)`, `NeoForge.EVENT_BUS.register(...)`, `VocanaApi.register*` and the module's
`registerModuleConfig` belong. Anything that needs registries or configs loaded waits for `FMLCommonSetupEvent`
(add a listener on `modBus` in `init`).

The toggles file, `config/vocanacompat-modules.toml`, is a `STARTUP` config (read as soon as it is registered, before
any module loads). Module config files are `COMMON` and are read later, so read values lazily (`XConfig.get(value)`
returns the default before load).

## The one rule

**Never reference a target mod's classes outside the module's package.** Not from `VocanaCompat`, not from
`CompatConfig`, not from another module, not from `gametest` (the GameTest server runs without any target mod). A
class that mentions `com.simibubi.create.*` in a field, signature or method body anywhere else is loaded when the
mod loads and fails when Create is absent. Inside the package anything goes, including `@EventBusSubscriber`
classes and EMI's `@EmiEntrypoint` plugin class: they are only loaded when the module is loaded (EMI scans
annotations itself but only instantiates when EMI is present).

Corollaries: no lambdas capturing target types in shared classes; no `instanceof` against target classes outside the
package; data files are never Java, so they are fine anywhere under `data/vocanacompat/`.

## Words: `src/main/lexicon/<module>.json`

Same shape as Vocana's `data/vocana/lexicon.json`; any of the top-level arrays `roots`, `elder`, `blends`,
`callings`, `particles`, `measures`, `primes`, `derived_forms` (a module will almost always use `roots` and
`elder`). The build concatenates each array across modules in file-name order and fails if two entries share a
`form` or `gloss`; the unit test also checks against Vocana's core lexicon. Rules from Vocana:

- every word needs `form`, `gloss`, `class`, `respelling` (the Chime syllabifies it), `ipa` (or `arpabet`: a
  space-separated Sphinx phone string; absent, Vocana's `Pronouncer` derives one), `meaning`, `beginner_clue`,
  and a `chapter` naming an existing `Chapter` constant so the Grimoire can place it;
- an Essence or Deed gloss the core does not implement needs behaviour registered in `init` through
  `VocanaApi.registerEssence(gloss, …)` or `registerDeed(gloss, …)`; a Deed must use the Force, State or Ruin frame;
- an `elder` word names its base in `variant_of` and carries a `profile`;
- do not reuse a form or gloss, do not invent a new `class`.

Keep the file valid JSON with the four arrays even when empty (the stub shape); the test requires one file per module.

## Grimoire groups: `src/main/grimoire/<module>.json`

`{"groups": [...]}`. The build merges the `groups` arrays into `META-INF/vocana/grimoire_groups.json` and fails on a
duplicate `id`. Give every group an `id` of the form `vocanacompat:<module>/<name>`. The entry shape follows
Vocana's `data/vocana/grimoire/groups.json` (workstream E of the 2.1 update); until that lands, Vocana does not
read this file and it is harmless.

## Discovery tags: `vocana:hears/<gloss>`

Vocana teaches a word from any block or entity tagged `vocana:hears/<gloss lowercase>`. To make a target's blocks
or mobs teach a word without seven modules editing the same tag file:

1. The module owns `data/vocanacompat/tags/block/<module>/<gloss>.json` (or `tags/entity_type/...`) listing the
   target's ids with `"required": false`, inside `neoforge:conditions` (below).
2. The shared file `data/vocana/tags/block/hears/<gloss>.json` (or `entity_type`) in **this** repository holds one
   line per module: `{"id": "#vocanacompat:<module>/<gloss>", "required": false}` with `"replace": false`. Add your
   line; never touch another module's. Because the reference is optional, a disabled module's missing tag is
   ignored.

## Data conditions

Every data file a module ships (recipes, `vocana_imbue` recipes, tags, loot modifiers, advancements) carries both
conditions, so the file is dropped when the mod is missing or the module is off:

```json
"neoforge:conditions": [
  { "type": "neoforge:mod_loaded", "modid": "create" },
  { "type": "vocanacompat:module_enabled", "module": "create" }
]
```

`vocanacompat:module_enabled` (`dev.vocana.compat.ModuleEnabledCondition`) is true only for a module that actually
loaded in this game. In a tag file the conditions key sits at the top level beside `values`; in a recipe at the top
level beside `type`.

## Imbue recipes

Vocana's bench reads `data/<ns>/vocana_imbue/*.json`. A module writes
`data/vocanacompat/vocana_imbue/<module>/<name>.json` (the loader walks sub-folders) in the shape documented by
Vocana's `dev.vocana.server.ImbueRecipes`, with the conditions above. The recipe names the target's items by id
inside the module folder, which is fine: the file never loads without the mod.

## Config

Add values to `<Module>Config` only. Translation keys go under `vocanacompat.configuration.<module>.*` in
`en_us.json`. The stub value `verboseLogging` may be kept or removed. A spec with no values is skipped by NeoForge,
so a module that needs no settings may leave the class empty rather than inventing options.

## Mixins

`vocanacompat.mixins.json` is wired in and empty. A module that needs a mixin into a target class puts it in
`dev.vocana.compat.mixin.<module>` and adds it to the `mixins`/`client` list of that file guarded by a plugin or,
simpler, by placing it behind a `@Mixin(targets = "…")` string target so the class is never resolved when the mod
is absent; prefer events and data over mixins.

## Build and run

- Compile everything: `./gradlew build` (needs Vocana in the local maven; see README).
- Leave a target out while its maven is down: `./gradlew build -Pcompat.skip=create,emi` (its package is excluded
  from compilation and its coordinates from the classpath; the loader then logs that the build has no such module).
- Run the client with target mods: `./gradlew runClient -Pcompat.run=create,curios` (the listed mods and their
  required libraries join the runtime classpath; Ice and Fire also needs `curios`, Ars Nouveau needs `curios`).
- GameTests: `./gradlew runGameTestServer`; only Vocana and this mod are present, so a module's GameTests can
  assert that the module did *not* load and that its data stayed out, and test pure helpers.
- Unit tests (no Minecraft): `./gradlew test`.

## Checklist before a module is done

- `init` registers nothing outside the module package and survives the mod being absent (`runGameTestServer` passes).
- Every word has a chapter, pronunciation and behaviour; `./gradlew test` passes (no duplicate forms/glosses).
- Every data file carries both conditions; shared `vocana:hears` tags only gained one-line optional references.
- `docs/targets.md` still describes the version you compiled against; `CHANGELOG.md` has a line under 1.0.0.
- No model names, no credentials, nothing from the target mod's jar copied into this repository.
