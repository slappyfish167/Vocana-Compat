# Target mods

What each module compiles against, where that was verified, and what a module agent should read first. Every
version lives in `gradle.properties`; this file is the evidence behind those numbers. All facts below were read
from the mods' own repositories on 2026-10-14 (URLs given). What could *not* be read from this container is marked
**unverified**: the mod mavens (maven.createmod.net, maven.blamejared.com, maven.terraformersmc.com,
maven.theillusivec4.top), CurseForge, Modrinth and their mirrors were unreachable here, so maven directory listings
and CurseForge/Modrinth file ids come from the mods' build files, GitHub tags, or search-engine snippets of the
CurseForge pages.

Common toolchain: Minecraft 1.21.1, NeoForge 21.1.255 (Vocana's floor; every target builds against an older 21.1.x,
which is fine: 21.1 is binary-stable), Java 21, Mojang mappings. Mod ids were read from each mod's
`neoforge.mods.toml` (or its template) and are what `Module` uses.

| Module | Mod id | Version compiled against | Loader built against | Coordinates (compile) | Licence |
|---|---|---|---|---|---|
| create | `create` | 6.0.10 (6.0.11 in development) | NeoForge 21.1.219 | `com.simibubi.create:create-1.21.1:6.0.10` @ maven.createmod.net | MIT (code), ARR (assets) |
| wyrmroost | `wyrmroost` | 1.4.0 on CurseForge (source is 1.4.1) | NeoForge 21.1.233 | `curse.maven:wyrmroost-340333:8555464` | All Rights Reserved |
| iceandfire | `iceandfire` | 2.1.4 (source); **file id pinned to 2.0** | NeoForge 21.1.248 | `curse.maven:iceandfire-ce-1040076:<file>` | LGPL-3.0 |
| emi | `emi` | 1.1.24+1.21.1 | NeoForge 21.1.28 | `dev.emi:emi-neoforge:1.1.24+1.21.1:api` @ maven.terraformersmc.com / repo.sleeping.town | MIT |
| curios | `curios` | 9.5.1+1.21.1 | NeoForge 21.1.60 | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api` @ maven.theillusivec4.top | LGPL-3.0-or-later |
| hexcasting | `hexcasting` | HexCast NEOFORGED 0.11.1-17+neoforge.1.21.1 | NeoForge 21.1.228 | `maven.modrinth:hexcast-neoforged:<version>` | MIT |
| ars_nouveau | `ars_nouveau` | 5.13.3 | NeoForge 21.1.228 | `com.hollingsworth.ars_nouveau:ars_nouveau-1.21.1:5.13.3` @ maven.blamejared.com | LGPL-3.0 (code), ARR (art) |

---

## Create

- **Source**: https://github.com/Creators-of-Create/Create, branch `mc1.21.1/dev` (the 1.21.1 line; the default
  branch is now `mc26.1/dev`).
- **Versions** (`gradle.properties` on the branch, https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.21.1/dev/gradle.properties):
  `mod_id=create`, `mod_version=6.0.11`, `minecraft_version=1.21.1`, `neo_version=21.1.219`, `flywheel_version=1.0.6`
  (`flywheel_version_range=[1.0.0,2.0)`), `vanillin_version=1.1.3-41`, `ponder_version=1.0.85`,
  `registrate_version=MC1.21-1.3.0+67`, `curios_version=9.2.2`, `mod_license=Read attached LICENSE.md`.
  The branch's `changelog.md` tops out at **6.0.10**, so 6.0.11 is the version in development; `create_version`
  is 6.0.10 until 6.0.11 appears on the maven (**unverified** which of the two the maven serves; Create's CI appends
  `-<build>` to dev builds and publishes releases bare).
- **Maven** (`build.gradle` on the branch): publishes `group = "com.simibubi.create"`, artifactId = root project
  name = `"$mod_id-$minecraft_version"` → `create-1.21.1`, to https://maven.createmod.net. Flywheel and Ponder are on
  the same maven; Registrate on https://maven.ithundxr.dev/snapshots.
- **Its own dependencies the compile classpath needs** (all read from the same `build.gradle`):
  - Flywheel API: `dev.engine-room.flywheel:flywheel-neoforge-api-1.21.1:1.0.6` (runtime `flywheel-neoforge-1.21.1`,
    embedded in the Create jar). Flywheel source: https://github.com/Engine-Room/Flywheel branch `1.21.1/dev`
    (its `gradle.properties` already says 1.0.7; Create pins 1.0.6).
  - Ponder: `net.createmod.ponder:ponder-neoforge:1.0.85+mc1.21.1` (embedded in the Create jar). Source:
    https://github.com/Creators-of-Create/Ponder branch `mc1.21.1/dev`.
  - Registrate: `com.tterrag.registrate:Registrate:MC1.21-1.3.0+67` (embedded in the Create jar).
  - Create's mods.toml (template `src/main/templates/META-INF/neoforge.mods.toml`) requires `flywheel` and `ponder`
    at runtime, which the jar carries itself; nothing else is needed to run it in dev (`-Pcompat.run=create`).
- **Licence**: `LICENSE.md` on the branch: MIT for the code, "All Rights Reserved" for `src/main/resources/assets`.
- **Integration surface** (read in this order):
  - `com.simibubi.create.api` (branch `mc1.21.1/dev`, `src/main/java/com/simibubi/create/api/`): sub-packages
    `behaviour`, `boiler`, `connectivity`, `contraption`, `data`, `effect`, `equipment`, `event`
    (`BlockEntityBehaviourEvent`, `PipeCollisionEvent`, `TrackGraphMergeEvent`), `packager`, `registrate`,
    `registry` (`CreateRegistries`, `CreateBuiltInRegistries`, `CreateDataMaps`, `SimpleRegistry`), `schematic`,
    `stress`. Stress and speed hooks for Vocana essences (WIND/LIGHTNING turning kinetics) live in `stress` and
    `behaviour`.
  - Recipes are plain data: `src/generated/resources/data/create/recipe/sequenced_assembly/precision_mechanism.json`
    has `"type": "create:sequenced_assembly"` with keys `ingredient`, `loops`, `results` (weighted), `sequence`
    (steps such as `"type": "create:deploying"`), `transitional_item`. Other processing types follow the same
    pattern (`create:mixing`, `create:pressing`, `create:crushing`, `create:filling`, …): a module adds JSON under
    `data/vocanacompat/recipe/create/` behind the conditions in `docs/adding_a_module.md`.
  - Vocana side: imbue recipes (`data/vocanacompat/vocana_imbue/create/*.json`) for brass/andesite gear, and
    `vocana:hears/<gloss>` tags for Create blocks (steam engines hear FIRE and WATER, encased fans WIND, …).

## Wyrmroost

- **Source**: https://github.com/NordAct/Wyrmroost, branch `main` (default); "Port of Wyrmroost for NeoForge 1.21.1",
  a fork of MWall541/Wyrmroost (itself from WolfShotz/Wyrmroost, 1.16).
- **Versions** (https://raw.githubusercontent.com/NordAct/Wyrmroost/main/gradle.properties): `mod_id=wyrmroost`,
  `mod_version=1.4.1`, `minecraft_version=1.21.1`, `neo_version=21.1.233`, `mod_group_id=com.github.wolfshotz`,
  `mod_license=All Rights Reserved`, ModDevGradle 2.0.141. `build.gradle` declares **no dependencies** at all (no
  GeckoLib), and no publishing block: there is no maven.
- **Mods.toml**: `src/main/templates/META-INF/neoforge.mods.toml`; only `neoforge` and `minecraft` are required.
- **Distribution**: CurseForge https://www.curseforge.com/minecraft/mc-mods/wyrmroost, project id **340333**
  (from the Curse Maven snippet of its 1.21.1 files); newest NeoForge 1.21.1 file found: `wyrmroost-1.4.0.jar`,
  file id **8555464** (1 Aug 2026); `wyrmroost-1.3.0.jar` is 8395122. No 1.4.1 file was found (**unverified**:
  CurseForge itself could not be opened; the source tree is already 1.4.1, so check the Files tab and bump
  `wyrmroost_curse_file` when it lands). A Modrinth page exists (`modrinth.com/mod/wyrmroost`) but its version ids
  could not be read.
- **Licence**: All Rights Reserved with the README's exceptions (private modification, forks for pull requests);
  compile against it, never ship any of it.
- **Integration surface**: root package `com.github.wolfshotz.wyrmroost` with `Wyrmroost.java`, `CommonEvents.java`
  and packages `attachments`, `client`, `config`, `containers`, `data`, `entities`, `items`, `mixin`,
  `network/packets`, `registry`, `util`. Registries: `registry/WREntities`, `WRItems`, `WRBlocks`, `WRSounds`,
  `WRAttachments`, `WRDataComponentTypes`, `WRDamageTypes`, `WRAttributes`. Dragons: `entities/dragon/
  AbstractDragonEntity` and the species (`AlpineEntity`, `ButterflyLeviathanEntity`, `CanariWyvernEntity`,
  `CoinDragonEntity`, `DragonFruitDrakeEntity`, `LDWyrmEntity`, `OWDrakeEntity`, `RoostStalkerEntity`,
  `RoyalRedEntity`, `SilverGliderEntity`), `entities/dragonegg`, `entities/projectile`. No API package: the module
  works through entity-type tags (`vocana:hears/<gloss>` on dragons: Royal Red hears FIRE, Butterfly Leviathan
  LIGHTNING and WATER, …) and NeoForge entity events, and should keep direct class references to the dragon base
  class and registries.

## Ice and Fire: Community Edition

- **Source**: https://github.com/IAFEnvoy/IceAndFire-CE, branch **`1.21.1`** (the default `master` has moved to
  Minecraft 26.1.2 / mod 2.2-beta; `1.20.1` is the other maintained line). No tags or GitHub releases exist for 1.21.1.
- **Versions** (https://raw.githubusercontent.com/IAFEnvoy/IceAndFire-CE/1.21.1/gradle.properties): `mod_id=iceandfire`,
  `mod_name=Ice And Fire Community Edition`, `mod_version=2.1.4`, `mod_group_id=com.iafenvoy.iceandfire`,
  `minecraft_version=1.21.1`, `neo_version=21.1.248`, `mod_license=LGPL-3.0`, `curios_version=9.5.1+1.21.1`,
  `geckolib_version=4.7.7`, `emi_version=1.1.19+1.21.1`, `jei_version=19.21.0.247`, `ponder_version=1.0.61`,
  `ars_version=5.10.3.1204`, `integration_version=0.2`.
- **Dependencies** (`build.gradle` on the branch): required libraries by the same author, pulled from CurseMaven:
  `curse.maven:uranus-1010827:8682870` and `curse.maven:jupiter-1072905:7738312` (mods.toml requires `uranus`
  `[2.3.2,)` and `jupiter` `[2.3,)`), plus `com.github.IAFEnvoy.Integration:integration-neoforge:0.2` from JitPack,
  embedded (jarJar). Curios is `api`, so it is a hard dependency at runtime (`-Pcompat.run=iceandfire` needs
  `curios` too). Repositories it uses: terraformersmc, theillusivec4, cursemaven, jitpack, nucleoid, blamejared,
  modmaven, createmod, GeckoLib's cloudsmith.
- **Maven**: none; its `publishing` block writes to a local `repo/` directory. docs.iafenvoy.com could not be reached
  from here and the README has no developer section (**unverified** whether a maven exists there).
- **Distribution**: CurseForge https://www.curseforge.com/minecraft/mc-mods/iceandfire-ce, project id **1040076**
  (from a Curse Maven snippet, e.g. `curse.maven:iceandfire-ce-1040076:7722420` for 2.0-beta.15). Known file ids:
  2.0-beta.9 = 7210417, 2.0-beta.15 = 7722420, 2.0-beta.17 = 8147245, `iceandfire-2.0.jar` = **8457180** (the
  pinned one), 1.1-1.21.1-neoforge = 6758480. `iceandfire-2.1.4.jar` (NeoForge 1.21.1, "latest release", 11 Oct 2026)
  exists but its file id could not be read (**unverified**): open its page from
  https://www.curseforge.com/minecraft/mc-mods/iceandfire-ce/files/all and copy the Curse Maven snippet into
  `iceandfire_curse_file`. Modrinth slug `iceandfire-ce`.
- **Licence**: LGPL-3.0 (`LICENSE` on the branch).
- **Integration surface**: root package `com.iafenvoy.iceandfire` (`IceAndFire.java`, `IceAndFireClient.java`) with
  `client`, `compat` (its own bridges: Curios, EMI, JEI, Jade, Ponder, Ars, ProjectE), `config`, `data`, `effect`,
  `entity`, `event`, `item`, `loot`, `mixin`, `network`, `particle`, `recipe`, `registry`, `render`, `screen`,
  `util`, `world`. No `api` package. Events: `event/DragonFireEvent`, `DragonFireDamageWorldEvent`,
  `GriefBreakBlockEvent`, `CollectDragonSkullModelEvent`, and `event/handler/`. Entities: `entity/DragonBaseEntity`
  (base of `FireDragonEntity`, ice and lightning dragons), `DragonPartEntity`, `DragonChargeEntity`,
  `DragonEggEntity`, hippogryphs, sea serpents, cyclopes, dread mobs, `AmphithereEntity`, `CockatriceEntity`,
  `DeathWormEntity`, `GhostEntity`. Registries: `registry/IafEntities`, `IafItems`, `IafBlocks`, `IafMobEffects`,
  `IafDamageTypes`, `IafDragonTypes`, `IafDragonColors`, `IafAttachments`, `IafDataComponents`, `IafTags`
  (`registry/tag`). The module teaches FIRE/FROST/LIGHTNING from the three dragon kinds, can register an essence
  for dragon fire through `VocanaApi.registerEssence`, and adds imbue recipes for dragon-bone and dragonsteel gear.

## EMI

- **Source**: https://github.com/emilyploszaj/emi, branch `1.21` (the default; it builds for Minecraft 1.21.1 with
  `enabled_platforms=fabric,neoforge`). Layout: `xplat`, `neoforge`, `fabric` sub-projects.
- **Versions** (`gradle.properties` on the branch): `mod_version=1.1.24`, `minecraft_version=1.21.1`,
  `neoforge_version=21.1.28`, `maven_group=dev.emi`, `archives_base_name=emi`. Tags
  (https://github.com/emilyploszaj/emi/tags): `1.1.24+1.21.1` (13 May 2026), `1.1.23+1.21.1`, `1.1.22+1.21.1`.
- **Maven** (README on the branch): repository https://repo.sleeping.town/ ("Sleeping Town"), NeoForge
  `compileOnly "dev.emi:emi-neoforge:${emi_version}:api"`, `runtimeOnly "dev.emi:emi-neoforge:${emi_version}"`, with
  `emi_version` of the form `<mod>+<mc>` → `1.1.24+1.21.1`. Ars Nouveau's build takes the same artifact from
  https://maven.terraformersmc.com/; both are declared here, filtered to the `dev.emi` group. The publication's
  artifactId is `"${rootProject.name}-${project.name}"` = `emi-neoforge` (`neoforge/build.gradle`).
- **Mods.toml** (`neoforge/src/main/resources/META-INF/neoforge.mods.toml`): `modId="emi"`, MIT, requires only
  `neoforge [20.4.72-beta,)`. No dependencies of its own.
- **Licence**: MIT.
- **Integration surface**:
  - `dev.emi.emi.api` (`xplat/src/main/java/dev/emi/emi/api/`): `EmiEntrypoint` (a marker annotation), `EmiPlugin`
    (`register(EmiRegistry)`), `EmiRegistry`, `EmiApi`, `EmiInitRegistry`, `EmiStackProvider`, `EmiDragDropHandler`,
    `EmiExclusionArea`, `EmiFillAction`, `EmiScreenBoundsProvider`; sub-packages `recipe` (`EmiRecipe`,
    `EmiRecipeCategory`), `render`, `stack` (`EmiStack`, `EmiIngredient`), `widget` (`WidgetHolder`).
  - On NeoForge EMI finds plugins itself (`neoforge/src/main/java/dev/emi/emi/platform/neoforge/EmiAgnosNeoForge.java`,
    `getPluginsAgnos`): it walks `ModList.get().getAllScanData()`, takes every class annotated `@EmiEntrypoint`
    that implements `EmiPlugin`, instantiates it by its no-arg constructor and tags it with the owning mod id. So the
    module's plugin class is `dev.vocana.compat.emi.VocanaEmiPlugin` with `@EmiEntrypoint`; nothing registers it.
    EMI is client-side; the plugin shows Vocana's imbue recipes (`data/*/vocana_imbue`) as an `EmiRecipeCategory`
    and the Grimoire words as information pages.

## Curios API

- **Source**: https://github.com/TheIllusiveC4/Curios (the old `illusivesoulworks/curios` URL redirects there),
  branch **`1.21.1`** (the default `26.x` and `1.21.x` have moved on; `1.21.x` is now 14.0.0+1.21.11). Layout:
  `common`, `neoforge`, `buildSrc`.
- **Versions** (https://raw.githubusercontent.com/TheIllusiveC4/Curios/1.21.1/gradle.properties):
  `version=9.5.1+1.21.1`, `group=top.theillusivec4.curios`, `minecraft_version=1.21.1`, `neoforge_version=21.1.60`,
  `license=LGPL-3.0-or-later`. `CHANGELOG.md` on `1.21.x` confirms `[9.5.1+1.21.1] - 2025.05.14` as the last 1.21.1
  entry (before it 9.5.0+1.21.1, 9.4.2+1.21.1). Ice and Fire CE and Hex 1.21 build against the same 9.5.1.
- **Maven** (README): https://maven.theillusivec4.top/, `top.theillusivec4.curios:curios-neoforge:${version}` at
  runtime and `...:${version}:api` at compile time. CurseForge project 309927, Modrinth `vvuO3ImH` (from its
  `gradle.properties`).
- **Mods.toml**: `modId="curios"`; depends on NeoForge only.
- **Licence**: LGPL-3.0-or-later.
- **Integration surface**:
  - `top.theillusivec4.curios.api` in the `:api` jar: `CuriosApi` (`getCuriosInventory(LivingEntity)`,
    `getSlot`, `getItemStackSlots`, `registerCurio`), `CuriosCapability`, `SlotContext`, `SlotResult`,
    `type.capability.ICurio` / `ICuriosItemHandler`, `type.ISlotType`, `event.CurioChangeEvent`,
    `event.CurioEquipEvent`, `CurioAttributeModifierEvent`, `SlotModifiersUpdatedEvent`.
  - Slots and entity assignments are data (NeoForge: `data/<ns>/curios/slots/<slot>.json` with `replace`, `size`,
    `operation`, `icon`, `order`, `add_cosmetic`, `use_native_gui`, `render_toggle`, `drop_rule`, `validators`;
    `data/<ns>/curios/entities/<name>.json` with `entities` and `slots`). The exact field list is in the Curios docs
    (https://docs.illusivesoulworks.com/curios), which could not be opened from here (**unverified** field names;
    the module agent must open them). A `vocanacompat:torc` (or the shared `necklace`) slot for Vocana's Torc, with
    `VocanaApi.registerWorn(player -> <Torc in a curio slot>)`, is the expected first deliverable; item tags
    `curios:necklace` etc. select what fits a slot.

## Hex Casting (HexCast NEOFORGED)

Two candidates exist for Hex Casting on NeoForge 1.21.1:

1. **HexCast NEOFORGED** (the one players install): https://github.com/a1exrom/hex_cast_NEOFORGED, branch `main`
   (3 commits), Modrinth project **`lZGsiJ1L`**, slug `hexcast-neoforged`
   (https://modrinth.com/mod/hexcast-neoforged). `gradle.properties`: `modID=hexcasting`,
   `modName=HexCast NEOFORGED`, `modVersion=0.11.1-17+neoforge.1.21.1`, `minecraftVersion=1.21.1`,
   `neoForgeVersion=21.1.228`, `kotlinVersion=2.0.21`, `paucalVersion=0.7.0`, `patchouliVersion=93`,
   `architecturyVersion=13.0.8`, `jeiVersion=19.21.2.313`, `pehkuiVersion=3.8.3`; `Forge/gradle.properties`:
   `kotlinForForgeVersion=5.9.0`, `curiosVersion=9.2.2+1.21.1`, `caelusVersion=7.0.1+1.21.1`.
   `Forge/src/main/resources/META-INF/neoforge.mods.toml`: `modId="hexcasting"`, `modLoader="kotlinforforge"`,
   MIT, requires `neoforge [21.1,)`, `minecraft [1.21.1,1.21.2)`, `paucal [0.7.0,0.8.0)`, `patchouli [1.21.1-90,)`,
   `caelus [7.0.1,)`; optional `curios [9.2.2,)`, `jei [19.21.2,)`. No maven, no GitHub releases; PAUCAL, Patchouli,
   JEI and Architectury are taken from a `libs/` folder in its build. Licence MIT (`LICENSE.txt`).
2. **The official port**: https://github.com/FallingColors/HexMod, branch `1.21` (`Common`, `Fabric`, `Neoforge`):
   `modVersion=0.12.0-devel`, `kotlinVersion=2.2.21`, `paucalVersion=0.7.1-pre-27`, `patchouliVersion=88`,
   `Neoforge/gradle.properties`: `neoforgeVersion=21.1.172`, `kotlinForForgeVersion=5.8.0`, `curiosVersion=9.5.1`,
   `caelusVersion=7.0.1+1.21.1`. Its mods.toml requires `inline [1.21.1-1.2.2,)` as well. README: artifacts on
   https://maven.blamejared.com/at/petra-k/hexcasting/ in `hexcasting-[PLATFORM]-[MC VERSION]/[MOD VERSION]`, i.e.
   `at.petra-k.hexcasting:hexcasting-neoforge-1.21.1:<version>` (versions `0.12.0-devel-pre-<build>`;
   **unverified** which builds exist). Licence MIT.

**Recommendation: compile against HexCast NEOFORGED** (`maven.modrinth:hexcast-neoforged:<version>`), because it is
the jar servers run today (`hexcasting` 0.11.x API, no `inline` dependency), while 0.12.0-devel is unreleased and
has API changes in `at.petrak.hexcasting.api.casting`. Revisit when HexMod 0.12 ships for 1.21.1; the module should
touch only `at.petrak.hexcasting.api` so the switch is a version bump. The Modrinth maven accepts a version number
or version id; the number `0.11.1-17+neoforge.1.21.1` comes from the build file and the Modrinth versions page
could not be opened (**unverified**: confirm at https://modrinth.com/mod/hexcast-neoforged/versions; a version id
from that page is the safest value for `hexcast_version`).

- **Libraries on the compile classpath** (from HexMod's `Neoforge/build.gradle`, the only build of the two that
  resolves them from mavens): Kotlin for Forge `thedarkcolour:kotlinforforge-neoforge:<v>` from
  https://thedarkcolour.github.io/KotlinForForge/ (5.9.0 as HexCast builds; KFF `5.x` is the line for 1.20.6–1.21.8,
  README now cites 5.10.0 and `gradle.properties` 5.12.0), `org.jetbrains.kotlin:kotlin-stdlib:2.0.21` (Maven
  Central, so javac sees Kotlin signatures), PAUCAL `at.petra-k:paucal:<v>+1.21.1-neoforge` (BlameJared;
  https://github.com/gamma-delta/paucal, `0.7.1-pre-27` is the version HexMod pins, inside HexCast's `[0.7.0,0.8.0)`;
  **unverified** that 0.7.0 itself is on the maven under this name), Patchouli
  `vazkii.patchouli:Patchouli:1.21.1-93-NEOFORGE` (BlameJared; https://github.com/VazkiiMods/Patchouli branch
  `1.21.x` publishes `${mc_version}-${build_number}-NEOFORGE`, build 94 current; 93 matches the jar HexCast bundles;
  **unverified** listing), Caelus `com.illusivesoulworks.caelus:caelus-neoforge:7.0.1+1.21.1:api`
  (https://github.com/illusivesoulworks/caelus README; HexCast's own build still uses the older
  `top.theillusivec4.caelus` group, which maven.theillusivec4.top may also serve).
- **Integration surface** (`Common/src/main/java/at/petrak/hexcasting/api/` in HexCast, same layout as HexMod):
  `HexAPI.java`; `casting` (`ActionRegistryEntry`, `ActionUtils.kt`, `ParticleSpray.kt`, `PatternShapeMatch`,
  `RenderedSpell.kt`, `SpellList.kt`; sub-packages `arithmetic`, `castables` (`Action`, `ConstMediaAction`,
  `SpellAction`), `circles`, `eval` (`CastingVM`, `CastingEnvironment`), `iota`, `math` (`HexPattern`), `mishaps`),
  `addldata` (`ADMediaHolder`, `ADIotaHolder`), `item`, `pigment`, `player`, `mod` (`HexConfig`, `HexTags`,
  `HexStatistics`, `HexApiMessages`), `misc`, `block`, `advancements`, `client`. Actions are registered in the
  `hexcasting:action` registry with a `HexPattern`; a module adding a "speak a Vocana word" action registers an
  `ActionRegistryEntry` through a `DeferredRegister` on that registry and writes the pattern's Patchouli page as data.
  Media <-> Thrum bridges go through `VocanaApi.registerCapacity/registerRegen` and `ADMediaHolder`.

## Ars Nouveau

- **Source**: https://github.com/baileyholl/Ars-Nouveau, branch `main` (the 1.21.1 line).
- **Versions** (https://raw.githubusercontent.com/baileyholl/Ars-Nouveau/main/gradle.properties): `mod_id=ars_nouveau`,
  `mod_version=5.13.3`, `mod_group_id=com.hollingsworth.ars_nouveau`, `minecraft_version=1.21.1`,
  `neo_version=21.1.228`, `mod_license=LGPL`, `curios_version=9.3.1`, `geckolib_version=4.6.6`,
  `nuggets_version=1.1.0.48`, `caelus_version=7.0.0+1.21`, `emi_version=1.1.19+1.21.1`, `jei_version=19.21.0.247`.
- **Maven** (README and `build.gradle`): https://maven.blamejared.com,
  `com.hollingsworth.ars_nouveau:ars_nouveau-[MC_VERSION]:[ARS_VERSION]` (archivesName = `mod_id-minecraft_version`,
  artifactId = archivesBaseName). Its CI appends `.<BUILD_NUMBER>` to the version, so maven versions are four-part
  (Ice and Fire CE pins `5.10.3.1204`); whether a bare `5.13.3` exists is **unverified**: if it does not resolve,
  take the `5.13.3.<build>` from https://maven.blamejared.com/com/hollingsworth/ars_nouveau/ars_nouveau-1.21.1/.
- **Dependencies** (from `build.gradle`): GeckoLib `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.6.6` from
  https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/; Curios `top.theillusivec4.curios:curios-neoforge:9.3.1+1.21.1`
  (compiled here against 9.5.1, the mods.toml floor is `[1.19-5.0.7.1,)`); Nuggets
  `com.hollingsworth.nuggets:nuggets-neoforge-1.21.1:1.1.0.48` (embedded with jarJar; on the compile classpath in
  case API signatures expose it; **unverified** which maven serves it, BlameJared assumed); TerraBlender
  `com.github.glitchfiend:TerraBlender-neoforge:1.21.1-4.1.0.8` (implementation; not needed to compile a bridge).
  `neoforge.mods.toml`: `modId="ars_nouveau"`, requires `neoforge [21.0.86-beta,)`, `curios`, `geckolib [4.2.1,)`;
  licence string "GNU GENERAL PUBLIC LICENSE" while `license.txt` and the README say LGPL v3 for code, art ARR.
- **Integration surface** (`src/main/java/com/hollingsworth/arsnouveau/api/`): `ArsNouveauAPI`; `event`
  (`SpellCostCalcEvent`, `SpellCastEvent`, `SpellResolveEvent`, `EffectResolveEvent`, `SpellDamageEvent`,
  `SpellModifierEvent`, `ManaRegenCalcEvent`, `MaxManaCalcEvent`, `DispelEvent`, `SummonEvent`,
  `FamiliarSummonEvent`, `SpellProjectileHitEvent`, `DelayedSpellEvent`, `EventQueue`); `spell`
  (`AbstractSpellPart`, `AbstractEffect`, `AbstractAugment`, `AbstractCastMethod`, `Spell`, `SpellContext`,
  `SpellStats`, `SpellResolver`, `EntitySpellResolver`, `SpellSchool`/`SpellSchools`, `SpellTier`,
  `wrapped_caster`); `registry` (`GlyphRegistry`, `RitualRegistry`, `FamiliarRegistry`, `PerkRegistry`,
  `CasterTomeRegistry`, `ImbuementRecipeRegistry`, `SpellCasterRegistry`, `GenericRecipeRegistry`, …); also
  `mana`, `perk`, `ritual`, `familiar`, `item`, `recipe`, `source`, `imbuement_chamber`. Glyphs are
  `AbstractEffect` subclasses registered with `GlyphRegistry.registerSpell` during mod construction plus a glyph
  recipe (`data/<ns>/recipe/*.json`, type `ars_nouveau:glyph`) and documentation entries; balance bridges hang
  on `SpellCostCalcEvent` (Ars side) and `VocanaApi.registerCostFactor` (Vocana side).
- **Verified for the module** (read from `main` and from `baileyholl/Ars-Nouveau-Example-Addon` `main`, package
  `com.example.an_addon`, on 2026-10-14): `SpellCostCalcEvent` has public fields `context` and `currentCost`, is not
  cancellable, and `SpellResolver` posts its `Pre` subclass from `getResolveCost` and its `Post` subclass from
  `getExpendedCost` on `NeoForge.EVENT_BUS`, after `ManaUtil.getPlayerDiscounts`; `SpellContext.getCaster()
  .getCasterType()` tells players (`CasterType.PLAYER`, a `PlayerCaster`) from turrets and runes. `GlyphRegistry
  .registerSpell(AbstractSpellPart)` must run during mod construction: it files the glyph's config with
  `ModLoadingContext.get().getActiveContainer()` and Ars's `ItemsRegistry.onItemRegistry` later registers an item
  under each part's registry name from `GlyphRegistry.getGlyphItemMap()`; the addon names its glyph
  `an_addon:glyph_test` and ships `models/item/glyph_test.json` and a recipe `{"type":"ars_nouveau:glyph","exp":..,
  "inputs":[..],"output":{..}}`. Documentation: `ReloadDocumentationEvent.AddEntries` (game bus, every world join),
  `DocEntryBuilder(modid, Documentation.glyphCategory(tier), nameKey, id)`, `GlyphEntry.create(part)`,
  `DocumentationRegistry.registerEntry`. Mana: `CapabilityRegistry.getMana(LivingEntity)` returns a `ManaCap`
  (`IManaCap`: `getCurrentMana`, `getMaxMana`, `addMana`, `removeMana`; `syncToClient(ServerPlayer)`), registered
  for players only. Source: `SourceJarTile` extends `AbstractSourceMachine implements ISourceTile`
  (`canAcceptSource`, `getSource`, `addSource(int)` clamps and updates the block); `TileCaster.expendMana` draws one
  source per mana for turrets. The enchanting apparatus takes `IEnchantingRecipe.sourceCost()` straight from the
  recipe (`EnchantingApparatusTile`, `SourceUtil.takeSourceMultipleWithParticles`): there is no cost event. Ids:
  blocks `source_jar`, `creative_source_jar`, `magebloom_crop`, `magebloom_block`, `sourceberry_bush`,
  `<red|blue|green|purple>_archwood_<log|leaves|sapling>`, `source_gem_block`, `imbuement_chamber`,
  `enchanting_apparatus`, `*_sourcelink`; items `source_gem`, `magebloom`, `magebloom_fiber`, `enchanters_sword`,
  `novice_spell_book`, `manipulation_essence`; entities `starbuncle` (no `carbuncle` in 5.x), `whirlisprig`,
  `drygmy`, `bookwyrm`, `wixie`, `wilden_hunter`, `wilden_stalker`, `wilden_guardian`, `wilden_boss`,
  `familiar_<name>`. Recipe types `ars_nouveau:glyph`, `ars_nouveau:imbuement` (`input`, `output`, `pedestalItems`,
  `source`), `ars_nouveau:enchanting_apparatus` (`reagent`, `pedestalItems`, `result`, `sourceCost`,
  `keepNbtOfReagent`). Mana defaults (`ServerConfig`): 100 max, 5 regen a second.

---

## What could not be verified from this container

- Any maven directory listing (createmod, blamejared, terraformersmc, theillusivec4, cloudsmith, KFF pages): every
  coordinate above is derived from the mods' own build files and READMEs, not from seeing the artifact.
- CurseForge file ids beyond those quoted in search snippets (Wyrmroost 1.4.1, Ice and Fire CE 2.1.x).
- Modrinth version ids/numbers (HexCast NEOFORGED, Wyrmroost, Ice and Fire CE).
- Whether Create's maven carries bare `6.0.10`/`6.0.11` or build-suffixed versions only.
- Curios' slot JSON field names (docs site unreachable; the repository's generated data was not where expected).

The CI workflow is the compile check: the first run tells which of these need a bump.
