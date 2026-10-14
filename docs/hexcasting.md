# Hex Casting module (`hexcasting`)

What the module does, what it deliberately does not do, and the exact source files each decision rests on. The
research was done against clones of the three candidate code bases on 2026-10-14:

| Tree | Commit | Notes |
|---|---|---|
| **HexCast NEOFORGED** `a1exrom/hex_cast_NEOFORGED` `main` | `4935c81` "bump to v0.11.1-17" (2026-07-29) | the jar players install (Modrinth `lZGsiJ1L`); `modVersion=0.11.1-17+neoforge.1.21.1`, Kotlin 2.0.21, NeoForge 21.1.228 |
| fork `plimbo-scrimbly/hex_cast_NEOFORGED` `main` | `ab342b2` (2026-10-08) | identical to the above except one file (`common/blocks/BlockQuenchedAllay.java`); **zero** differences under `api/` |
| official `FallingColors/HexMod` branch `1.21` | `7d5c157` | `modVersion=0.12.0-devel`, Kotlin 2.2.21, NeoForge 21.1.172, requires `inline`; unreleased |

Paths below are relative to `Common/src/main/java/at/petrak/hexcasting/` unless they start with `Forge/`. Every class
the module touches is in `at.petrak.hexcasting.api`; nothing from `common`, `xplat`, `forge` or a Kotlin-only type is
referenced, so the module is plain Java against the published jar and needs no Kotlin at compile time beyond the
stdlib already on the classpath (`build.gradle`).

## What the port is, in one paragraph

HexCast NEOFORGED is Hex Casting 0.11.1 (the 1.20.1 code base) moved to NeoForge 21.1 with the smallest possible
changes. The `Forge/` module still contains the old Forge capability classes (`Forge/.../forge/cap/HexCapabilities.java`
imports `net.neoforged.neoforge.common.capabilities.Capability`, which does not exist in NeoForge 21), but
`Forge/build.gradle` lines 174–176 **exclude** `ForgeCapabilityHandler.java`, `HexCapabilities.java` and
`CapClientCastingStack.java` from compilation; media holders are instead produced directly by
`Forge/.../forge/xplat/ForgeXplatImpl.java` (`findMediaHolder(ItemStack)`, line 319: `MediaHolderItem` items get a
`CapItemMediaHolder`, `hexcasting:amethyst_dust`, `minecraft:amethyst_shard`, `hexcasting:charged_amethyst`,
`hexcasting:quenched_allay_shard` and the quenched allay block get a `CapStaticMediaHolder`). **So there is no item
capability to query**; the public entry is `HexAPI.instance().findMediaHolder(stack)` (`api/HexAPI.java`, a default
method returning `null` on the dummy and delegating to `IXplatAbstractions` in `common/impl/HexAPIImpl.java` line 90).
Its registries (`common/lib/HexRegistries.java`: `hexcasting:action`, `special_handler`, `iota_type`, …) are vanilla
simple registries created through an accessor mixin (`ForgeXplatImpl.java` lines 481–505,
`ForgeAccessorBuiltInRegistries.hex$registerSimple`) and filled on NeoForge's `RegisterEvent`
(`Forge/.../forge/ForgeHexInitializer.java` lines 138–153). Data generated under the old folder names is renamed at
build time (`Forge/build.gradle` lines 194–202: `tags/items` → `tags/item`, `tags/blocks` → `tags/block`, …), so the
jar's tags live where 1.21 expects them. Kotlin is compiled with only `jvmTarget = "21"` (`build.gradle` lines 90, 107,
112; no `-Xjvm-default`), which matters for anything that would *implement* a Kotlin interface from Java (below).

## Decisions

### Chosen

1. **Hex staves are Vocana Catalysts** (`HexStaves.java`, `VocanaApi.registerCatalyst`). Hex recognises its staves
   by the item tag `hexcasting:staves`: `api/mod/HexTags.java` (`Items.STAVES = create("staves")`), checked by the
   casting screen in `client/gui/GuiSpellcasting.kt` line 126 (`heldItem.is(HexTags.Items.STAVES)`); the generated
   tag `Forge/src/generated/resources/data/hexcasting/tags/items/staves.json` lists all fourteen staves
   (`staff/oak` … `staff/mindsplice`). The module builds the same `TagKey` itself, so it works against both jars and
   needs no Hex class. Capacity bonus from config (`staves.capacityBonus`, default 2, like Vocana's first staff). A
   side effect worth knowing: because `Vocana.catalyst` now accepts a Hex staff, KEEP with an empty Hex staff in hand
   writes a Bound Catalyst into it (`Latents.vessel`, last branch), and `CastService.holdsChime` lets a Hex staff carry
   a spoken chant.
2. **The Deed DRAW (`hainsu`, State frame): media → Thrum** (`MediaDraw.java`). Holders are found exactly as Hex finds
   them for a cast, through `HexAPI.instance().findMediaHolder(stack)` over the inventory and off hand (compare
   `api/utils/MediaHelper.kt` `scanPlayerForMediaStuff`), filtered by `ADMediaHolder.canProvide()` (packaged Hexes
   answer false: `common/items/magic/ItemPackagedHex.java` `canProvideMedia`), by `canRecharge()` unless
   `draw.fromBatteries` (phials: `common/items/magic/ItemMediaBattery.java` answers true to both), and sorted by
   `getConsumptionPriority()` descending, Hex's own order (`api/addldata/ADMediaHolder.java`: dust 3000, shard 2000,
   charged 1000, batteries 4000). Media is spent with `ADMediaHolder.withdrawMedia(cost, simulate)`; for raw amethyst
   `Forge/.../forge/cap/adimpl/CapStaticMediaHolder.java` rounds up to whole items, shrinks the stack and returns what
   it took, which is why `MediaMath.quanta` asks in whole units (a simulated withdrawal of 1 reveals one item's
   worth). Rate `draw.mediaPerThrum` (default 2500: one dust = 4 Thrum, a shard 20, a charged crystal 40, against a
   pool of 40–376), cap `draw.maxThrumPerCast` (default 20, a measure on the Deed scales it up to twofold). Aimed at
   another creature it refuses; it only ever touches the caster's own stores.
3. **The Deed AWAKEN (`rauspe`, Force frame): cast a held packaged Hex** (`HexAwaken.java`). Every item that holds a
   packaged Hex implements the API interface `api/item/HexHolderItem.java` (`hasHex`, `getHex`, …); the cypher,
   trinket and artifact extend `common/items/magic/ItemPackagedHex.java`. There is no public "cast now" method: the
   casting lives in `ItemPackagedHex.use` (builds a `PackagedItemCastEnv`, `CastingVM.empty(ctx)`,
   `queueExecuteAndWrapIotas`, cooldown, particles, breaks a spent cypher). The Deed therefore finds a `HexHolderItem`
   in either hand, checks `hasHex` and the item cooldown, and calls `Item.use(level, player, hand)` on it, so Hex's own
   media accounting, mishaps and cooldowns apply unchanged. Both the port and HexMod `1.21` keep `HexHolderItem` and
   that `use` (the `1.21` branch stores the program in a data component instead of NBT, behind the same interface).
4. **Data** (needs no Java): three Imbue Bench recipes paid in Hex media
   (`data/vocanacompat/vocana_imbue/hexcasting/`: `dust_essence` — any essence into any gear for four
   `hexcasting:amethyst_dust`; `edified_staff` — a staff affinity for `#hexcasting:edified_planks` and dust;
   `charged_chant` — a chant of up to 20 CP for one `hexcasting:charged_amethyst`, a book and a four-anchor ritual with
   channelling); Hex's amethyst blocks, sconce, quenched allay and edified logs as `vocana:ritual_anchors`; and
   `vocana:hears/*` tags: `light` (conjured light, sconce, scroll-paper lantern), `crystal` (amethyst blocks),
   `stone` (slate blocks), `life` (edified logs and leaves), `remember` (akashic record and bookshelf), `store`
   (bookshelf, connector), `seeming` (conjured block and light; the wall scroll entity), `soul` (quenched allay), and
   the module's own words `draw` (amethyst dust block, pillar, sconce) and `awaken` (slate, conjured block; the wall
   scroll). Ids were read from `common/lib/HexBlocks.java`, `common/lib/HexItems.java`,
   `common/entities/HexEntities.java` (`wall_scroll`); all are optional entries, and all exist in HexMod `1.21` too
   except `akashic_connector`, which `1.21` renamed to `akashic_ligature` (harmless: optional).
5. **Config** (`HexcastingConfig.java`, `config/vocanacompat-hexcasting.toml`): a toggle per feature and every number.
6. **Tests**: `gametest/HexcastingGameTests.java` runs without Hex (module unloaded, words present with their frames
   and chapter, no stray registrations, conditioned data absent) and touches neither Hex nor the module package;
   `src/test/.../hexcasting/MediaMathTest.java` and `HexcastingDataTest.java` (conditions on every data file, tag
   shapes, recipe shape, lexicon fields).

### Rejected, and why

- **Vocana staffs as Hex staves.** The tag `hexcasting:staves` is only *checked* by the client screen; the screen is
  opened by `common/items/ItemStaff.java` `use` through `IXplatAbstractions.INSTANCE.getStaffcastVM` and the packet
  `MsgOpenSpellGuiS2C`, both outside the API. A Vocana staff in the tag would be accepted by the screen but nothing
  would open it, and opening it ourselves means calling `xplat` internals or mixing into `ItemStaff`. Skipped;
  possible later as a mixin-free event handler only if the port exposes the packet.
- **A Hex pattern ("action") that speaks or feeds Vocana.** Registering is feasible from Java in principle: an
  `ActionRegistryEntry(HexPattern, Action)` (`api/casting/ActionRegistryEntry.java`, a Java record) in the
  `hexcasting:action` registry, which NeoForge's `RegisterEvent` serves (so a `DeferredRegister` on
  `HexRegistries.ACTION` would work), with `HexPattern.fromAngles` being `@JvmStatic`
  (`api/casting/math/HexPattern.kt` line 143). The problem is the `Action` side: `api/casting/castables/Action.kt`,
  `ConstMediaAction.kt` and `SpellAction.kt` are Kotlin interfaces with default bodies, compiled **without**
  `-Xjvm-default`, so from Java every member (`getArgc`, `getMediaCost`, `executeWithOpCount`, `operate`, …) must be
  implemented by hand and `operate` must rebuild an `OperationResult` from `CastingImage.copy(...)` (a data class with
  a private constructor) and `OperatorSideEffect.ConsumeMedia`; HexMod `1.21` compiles with Kotlin 2.2.21, whose
  default flips to generating JVM default methods, so the same Java would face a different interface shape. Also,
  `HexRegistries` is in `common.lib`, not `api`. Without a compiler in reach this is too fragile to ship; the recipe is
  recorded here for when CI can compile it.
- **An imbue recipe that stores Thrum into a Hex media item.** Not expressible: `ImbueRecipe` lays an essence or a
  chant into gear and takes materials; it has no output item, and Hex's media is written only through
  `ADMediaHolder.insertMedia` on rechargeable holders. A Deed that *fills* a phial from Thrum would be the Java route
  (the inverse of DRAW, through `insertMedia`), left out to keep the economy one-directional until play shows it is
  wanted.
- **Packaged Hexes as KEEP vessels.** `Latents.vessel` is core code and the brief forbids changing it; AWAKEN covers
  the useful half (a cypher in the other hand fires mid-chant).
- **`bears` for Hex's conjured blocks.** `Essence.bears(level, pos, state, essence)` has no extension hook for blocks
  (its `default` returns false); only entities of a *registered essence* can bear through `EssenceBehaviour.bears`.
- **Hex's events.** The port exposes none on the NeoForge bus beyond vanilla ones; `CastingEnvironment` has
  `addCreateEventListener(Consumer<CastingEnvironment>)` (`api/casting/eval/CastingEnvironment.java` line 49), a
  static Java hook fired for every cast environment. Usable, but nothing in this module needs to react to a Hex cast.
- **`VocanaApi.registerCostFactor` / `registerCapacity` bridges.** No Hex attribute or state maps cleanly onto a
  Vocana price (the port has no `media_consumption` attribute; `1.21` adds one). Left for a balance pass.

## HexMod `1.21` (0.12.0-devel): would the module compile against it?

Yes, as written. Everything used is present and signature-identical on the `1.21` branch:
`api/HexAPI.java#findMediaHolder` (the diff between the trees touches `getRawHookI18nKey`, `getEntityLookDirSpecial`
and the armour material, not this method), `api/addldata/ADMediaHolder.java` (no diff), `api/item/HexHolderItem.java`
(only a `@Nullable` on `writeHex`, unused here), `api/misc/MediaConstants.java` (no diff), the `hexcasting:staves`
tag (`api/mod/HexTags.java` adds tags, removes none), and `Item.use` on the packaged items. The data ids match except
`akashic_connector` → `akashic_ligature`. The build would need the maven coordinate swapped to
`at.petra-k.hexcasting:hexcasting-neoforge-1.21.1:<0.12.0-devel-pre-N>` on BlameJared plus `inline` on the runtime
classpath; the skeleton's choice (`maven.modrinth:hexcast-neoforged:0.11.1-17+neoforge.1.21.1`) is kept because it is
the jar servers run and 0.12 is unreleased. Neither Modrinth's API nor its maven could be reached from this container
(the proxy refuses `api.modrinth.com`), so the Modrinth version string is still **unverified**; if CI cannot resolve
it, copy the version id from https://modrinth.com/mod/hexcast-neoforged/versions into `hexcast_version`.

## API doubts (for the first CI run)

1. `maven.modrinth:hexcast-neoforged:0.11.1-17+neoforge.1.21.1` resolving at all (above). The compile needs only the
   `api` package; if the Modrinth artifact is unreachable, compiling against the official
   `hexcasting-neoforge-1.21.1` jar is a valid fallback for this module (see the previous section).
2. `HexAPI.instance()` at the moment DRAW runs: it loads `common.impl.HexAPIImpl` reflectively and falls back to a
   dummy whose `findMediaHolder` returns `null` (then DRAW simply reports "no media"). Hex is a required
   `ordering="AFTER"` dependency in `neoforge.mods.toml`, so the real implementation is present in practice.
3. `HexHolderItem.hasHex` is annotated `@ApiStatus.OverrideOnly`; calling it is fine on the JVM, the annotation is
   advice for IDEs. If a future jar hides it, `ADMediaHolder` is unaffected and only AWAKEN's "holds no Hex" check
   would need `Item.use`'s own `fail` result instead.
4. `ModConfigSpec.Builder.defineInRange(String, long, long, long)` (a `LongValue`) and the
   `Inventory.items`/`offhand` public lists are standard NeoForge 21.1 / Minecraft 1.21.1 API; no Hex dependency.
5. Word forms `hainsu` and `rauspe` were chosen against Vocana's core lexicon; a clash with another module's words
   is caught by the build's merge and `LexiconExtensionTest`.

## In-game checklist (HexCast NEOFORGED 0.11.1-17 + KFF 5.9, PAUCAL 0.7, Patchouli 93, Caelus 7.0.1)

1. Hold any Hex staff: the Chime screen opens, a chant can be inscribed and spoken; the F8 panel shows the staff's
   Capacity +2 (config `staves.capacityBonus`). KEEP with the empty Hex staff in the other hand writes a Bound Catalyst.
2. With 5 amethyst dust in the inventory and the pool half empty, inscribe `OPEN DRAW SELF SEAL` (`open hainsu tenhu
   close`): the action bar says "Drew 20.0 Thrum from 5.0 dust", the dust is gone, the pool rose by 20. `OPEN DRAW TWO
   SELF SEAL` draws up to 40. A full pool refuses; aiming at a mob refuses; a phial is untouched unless
   `draw.fromBatteries`.
3. With a charged cypher in the off hand and a Catalyst in the main hand, `OPEN AWAKEN SEAL`: the cypher's Hex casts
   with its own media, the item goes on its Hex cooldown; a second AWAKEN inside the cooldown says it is settling.
4. Bench: four amethyst dust + any gear lays any essence (`dust_essence`); two edified planks + two dust lay a staff
   affinity; one charged amethyst + a book inside a figure of four Hex amethyst/edified blocks (`ritual_anchors`)
   lays a chant.
5. Discovery: standing by an amethyst sconce teaches LIGHT and DRAW; a slate teaches STONE and AWAKEN; an akashic
   record REMEMBER; a wall scroll SEEMING.
6. Turn `modules.hexcasting.enabled` off: no recipe, tag or Deed behaviour remains; the words still read in the
   Grimoire but refuse ("the Deed faltered" is *not* shown; the core says nothing happened).
