# Vocana: Resonance

*A world that answers.*

A curated modpack for **Minecraft 1.21.1 on NeoForge 21.1.255**, built around [Vocana](https://github.com/slappyfish167/Vocana)
— magic you cast by learning a language from the world itself and speaking it aloud — and its companion,
[Vocana-Compat](../README.md), which teaches that language new words from Create's engines, Ars Nouveau's source,
Hex Casting's media and the dragons of Ice and Fire and Wyrmroost. Around that core: engineering that hums, wild
places worth the walk, creatures worth fearing, and the performance and quality-of-life work that lets ~150 mods feel
like one game.

The pack is ~120 hand-picked mods (121 Modrinth entries in [`mods.txt`](mods.txt)) plus the libraries they pull in,
Vocana and Vocana-Compat. It is a [packwiz](https://packwiz.infra.link/) pack that is **resolved in CI**: the
repository holds the curated list and the pack metadata, and the workflow turns them into a `.mrpack`.

- **Recommended memory**: 8 GB for the client (`-Xmx8G`; 6 GB works without shaders), 6–8 GB for a server.
- **Java**: 21.
- **Loader**: NeoForge 21.1.255 or newer 21.1.x (Vocana's floor).

## What's inside

Every line below is an entry of `mods.txt`. The full list of jars, including the libraries packwiz adds on its own,
is in the run summary of each build.

### Vocana and its world (8, required)

| Mod | Why it is here |
|---|---|
| Vocana *(added by hand, see below)* | The heart of the pack: listen with the Tuning Chime, keep a Grimoire, cast by writing or speaking chants. |
| Vocana-Compat *(added by hand)* | Bridges Vocana to the seven mods below; each module loads only with its mod. |
| Simple Voice Chat | Carries spoken chants to the server (Vocana needs its API 2.6.20+). Proximity voice for everyone else. |
| Create | Kinetic engineering, and Vocana's loudest teacher: fans hear WIND, steam engines FIRE and WATER. |
| Wyrmroost (NeoForge port) | Ten dragon species with their own ecology; Royal Reds hear FIRE. |
| Ice and Fire: Community Edition | Fire, ice and lightning dragons, their bones and steel; Vocana learns FIRE, FROST and LIGHTNING from them. |
| EMI | The recipe viewer, with a Vocana-Compat page for imbue recipes and Grimoire words. |
| Curios API | Accessory slots; the Torc sits in the necklace slot. |
| Hex Casting (HexCast NEOFORGED) | Spell-programming by drawn patterns; media and Thrum talk through Vocana-Compat. |
| Ars Nouveau | Glyph spellcraft and source; Vocana-Compat bridges spell costs both ways. |

### Libraries (13, explicit)

Architectury API, Cloth Config, GeckoLib, Kotlin for Forge (Hex Casting is Kotlin), Patchouli, PAUCAL and Caelus
(Hex Casting), Balm, Moonlight, YUNG's API, Lithostitched (Tectonic), CreativeCore (AmbientSounds), Sophisticated Core.
They are listed so their presence is visible; packwiz would add them anyway as required dependencies, together with
the rest (Searchables, TerraBlender, Resourceful Lib, Modonomicon, SmartBrainLib and so on).

### Performance & stability (16)

| Mod | Why |
|---|---|
| Sodium | The renderer. Sodium has official NeoForge builds for 1.21.1; Embeddium is end-of-life there, so Sodium it is. |
| Sodium Extra | Extra video options (fog, particles, animations) for low-end machines. |
| Reese's Sodium Options | Readable options screen; required by many Sodium add-ons. |
| Iris | Shader packs, on top of Sodium (Iris requires Sodium; never with Embeddium). |
| Lithium | Server-tick optimisations (CaffeineMC's own NeoForge build). |
| ModernFix | Faster launch, lower memory. |
| FerriteCore | Lower memory for block states and models. |
| ImmediatelyFast | Faster immediate-mode rendering (text, GUI, entities). |
| Entity Culling | Skips entities and block entities you cannot see. |
| More Culling | Culls more block faces (leaves, glass, item frames). |
| Noisium | Faster noise-based worldgen; matters with Terralith and Tectonic. |
| Dynamic FPS | Throttles the game when unfocused. |
| Clumps | Merges XP orbs. |
| LambDynamicLights | Held light sources light the world; the official build runs on NeoForge 1.21.1. |
| spark | Profiler, for "why is it slow?". |
| Chunky | Pre-generates the world on a server before players arrive. |

### Magic (6)

| Mod | Why |
|---|---|
| Ars Additions | QoL and immersion for Ars Nouveau. |
| Ars Creo | Ars Nouveau and Create working together. |
| Iron's Spells 'n Spellbooks | Spellbooks, schools and boss-grade combat magic — Vocana's quieter craft gets a loud rival. |
| Occultism | Ritual magic and summoned spirits that work for you; the "deep lore" path. |
| Theurgy | Alchemy by the same author: distillation, sulfur and salt; quiet, systematic, beautiful. |
| Artifacts | Rare wearable relics found in the world's ruins; fills Curios slots with discoveries, not crafts. |

### Engineering (8)

| Mod | Why |
|---|---|
| Create Crafts & Additions | Electricity bridge for Create (FE), useful with AE2 and Immersive Engineering. |
| Create: Connected | Small, well-made Create components (Create 6). |
| Create Deco | Industrial building blocks for Create builds. |
| Create: Copycats+ | Copycat shapes for seamless builds. |
| Create Central Kitchen | Create automation for Farmer's Delight. |
| Applied Energistics 2 | Late-game digital storage and autocrafting. |
| AE2 Wireless Terminals (ae2wtlib) | Wireless access to the AE2 network. |
| Immersive Engineering | Chunky multiblocks and power; a second engineering voice that suits Create. |

Mekanism is deliberately left out: Create, Immersive Engineering and AE2 already cover power, processing and storage
without its very fast progression.

### Storage (3)

Sophisticated Backpacks and Sophisticated Storage (upgradeable backpacks, chests and barrels), Storage Drawers
(bulk storage before AE2).

### Creatures & dragons (6)

| Mod | Why |
|---|---|
| Mowzie's Mobs | A handful of superbly animated bosses and creatures. |
| Naturalist | Real-world wildlife with believable behaviour. |
| Friends & Foes | The mob-vote creatures that never shipped (Copper Golem, Glare, Moobloom…). |
| Critters and Companions | Small, charming fauna: otters, ferrets, dragonflies. |
| Creeper Overhaul | Biome-specific creepers, so danger fits the place. |
| Aquaculture 2 | Fishing worth doing: new fish, rods and treasure. |

Alex's Mobs has only unofficial 1.21.1 ports (CurseForge), so Naturalist, Critters and Companions and Friends & Foes
fill the "living world" role.

### Worldgen & exploration (26)

| Mod | Why |
|---|---|
| Terralith | Vanilla-block biomes that make the overworld feel discovered rather than generated. |
| Tectonic | Continents, mountain ranges and rivers; as mods, Tectonic has Terralith compatibility built in (no Terratonic needed). |
| YUNG's Better Dungeons, Mineshafts, Strongholds, Desert Temples, Ocean Monuments, Witch Huts, Jungle Temples, Nether Fortresses, End Island; YUNG's Bridges, YUNG's Extras (11) | Vanilla structures rebuilt as places worth exploring. |
| Towns and Towers | Varied villages and outposts per biome. |
| Dungeons and Taverns | Taverns, keeps and dungeons with their own loot; the mod build for NeoForge. |
| Explorify | Small vanilla-style points of interest between the big ones. |
| ChoiceTheorem's Overhauled Village | Biome-specific village overhaul. |
| Repurposed Structures | Biome variants of vanilla structures. |
| Sparse Structures | Spaces the structure mods apart so the world does not feel crowded. |
| Nullscape | An End worth visiting. |
| Deeper and Darker | The Otherside, beyond the Ancient City. |
| The Aether | The classic sky dimension. |
| The Undergarden | A dark, dangerous underground dimension. |
| Nature's Compass, Explorer's Compass | Find biomes and structures without wikis. |
| Waystones | Travel between places you have found. |

### Food & farming (4)

Farmer's Delight (cooking, a whole food system), Brewin' and Chewin' (kegs, cheese and drinks), Serene Seasons
(seasons that change what grows — the world keeps its own time), AppleSkin (hunger and saturation shown).

### Building & decoration (15)

Supplementaries and Amendments (the small vanilla-plus touches), Handcrafted and Another Furniture (furniture),
Chipped (block variants through workbenches; chosen over Rechiseled), FramedBlocks (camouflaged shapes), and Macaw's
Furniture, Roofs, Windows, Doors, Trapdoors, Bridges, Lights and Lamps, Paths and Pavings, Fences and Walls.

### QoL & UI (16)

| Mod | Why |
|---|---|
| EMI Loot, EMI Ores | Loot tables and ore distribution in EMI (EMI Ores must be on both sides). |
| Jade | What am I looking at, and what's inside it. |
| Xaero's Minimap, Xaero's World Map | One map family (not JourneyMap as well), integrated with the claims mod. |
| Open Parties and Claims | Parties and chunk claims for servers, drawn on Xaero's maps. |
| Mouse Tweaks | Drag and scroll item handling. |
| Controlling | Searchable keybinds and conflict finder — 150 mods bring many keys. |
| Inventory Profiles Next | Sorting and auto-refill. |
| Polymorph | Choose between conflicting recipe outputs. |
| Carry On | Pick up chests and creatures. |
| Corpse | Your items wait on your corpse after death. |
| Default Options | Ships the pack's default keybinds and options without overwriting a player's own on update. |
| Enchantment Descriptions | Tooltips explaining enchantments. |
| Sound Physics Remastered | Reverb and occlusion — caves echo, and so do the voices of Simple Voice Chat. |
| AmbientSounds | Biome soundscapes; the world sounds alive. |

NeoForge 21.1 brings its own mod list and config screens, so Catalogue and Configured are not needed.

### CurseForge-only extras (opt-in, private builds only)

`cf:the-twilight-forest` (The Twilight Forest is not on Modrinth for 1.21.1) and `cf:irons-jewelry` (Iron's Gems
'n Jewelry). They are skipped unless the workflow runs with `include_curseforge: true`; such a build exports their
jars into the .mrpack's overrides, so it is a **private** build (see "Private builds").

## Excluded and unverified

What was considered and left out, and why. "Not on Modrinth" means no NeoForge 1.21.1 build was found on Modrinth
on 2026-10-16 (this repository resolves from Modrinth only).

| Mod | Reason |
|---|---|
| Embeddium | End-of-life on 1.21.1; Sodium has official NeoForge builds and Iris needs Sodium. |
| JEI | EMI instead: Vocana-Compat ships an EMI plugin, and two recipe viewers conflict. |
| JourneyMap | Xaero's maps instead (one map family; Open Parties and Claims integrates with Xaero's). |
| Rechiseled | Chipped instead (one chisel system). |
| Botania | The official 1.21.1 NeoForge build is not on Modrinth; only an unofficial port ("Botania Revived") is. |
| The Twilight Forest | CurseForge only for 1.21.1 → opt-in CurseForge extra. |
| Iron's Gems 'n Jewelry | CurseForge only → opt-in CurseForge extra. |
| Ars Elemental | Not on Modrinth for 1.21.1 (its author points to CurseForge). |
| FTB Quests (+ FTB Library, FTB Teams) | CurseForge only for 1.21.1; Heracles has no NeoForge 1.21.1 build. The quest book is a plan in [QUESTS.md](QUESTS.md). |
| Alex's Mobs | Only unofficial 1.21.1 ports, on CurseForge. |
| Create: Enchantment Industry | The Modrinth builds lag behind Create 6.0.10 (2.2.3 for 6.0.6 seen; 2.5.0 only on CurseForge): too likely to crash. |
| Create: Steam 'n' Rails | Only an unofficial 1.21.1 port. |
| Create Aeronautics | Not verified on Modrinth; it needs the Sable physics stack and a specific NeoForge build. |
| Create Big Cannons | On Modrinth, left out by curation (the pack's engineering is about wonder, not artillery). |
| Mekanism | Left out by curation (balance with Create, IE and AE2). |
| Malum | On Modrinth, left out by curation: one magic system too many, and Lodestone's rendering is a known Iris trouble spot. |
| Forbidden and Arcanus, Relics, Born in Chaos | On Modrinth, left out by curation (magic and curio overload; Born in Chaos's horror tone). |
| BetterF3, Better Advancements, YUNG's Menu Tweaks, Trade Cycling | No NeoForge 1.21.1 build found on Modrinth. |
| Legendary Tooltips | CurseForge only. |
| Incendium, Structory | Mod builds for NeoForge 1.21.1 not confirmed. |
| Ars Technica | Only a "0.1.0" listing found; not confirmed as the same project. |
| C2ME | No official NeoForge build; it rewrites chunk I/O under a dozen worldgen mods. Lithium and Noisium instead. |
| Catalogue, Configured | Not confirmed on Modrinth, and NeoForge 21.1's own mod list and config screens cover them. |

**Unverified within the list**: entries were checked against search-engine snapshots of Modrinth and its mirrors,
not the Modrinth API (unreachable from the development container). The first workflow run is the real check; its
summary table lists anything that did not resolve. Two version caveats: Modrinth's newest Wyrmroost may be 1.3.0
(1.4.x was seen on CurseForge; Vocana-Compat compiles against 1.4.0), and its newest Ars Nouveau 5.13.0 (Vocana-Compat
compiles against 5.13.3; same minor version). `wbgfS34j` is FramedBlocks' Modrinth project id, used because the slug
could not be confirmed.

## Building

### In GitHub Actions (the normal way)

`.github/workflows/modpack.yml` runs on every push that touches `modpack/` and on demand (**Actions → Modpack → Run
workflow**). It:

1. installs packwiz (`go install github.com/packwiz/packwiz@latest`, Go 1.24.7);
2. runs `scripts/resolve.sh`, which adds every `mods.txt` entry with `packwiz modrinth add https://modrinth.com/mod/<slug> -y`
   (a URL, so a wrong slug fails instead of installing a search hit), keeps going past failures, applies the `side=`
   overrides and writes a resolved/failed table to the run summary. The job fails only when a `required` entry fails;
3. optionally adds Vocana's jars (below);
4. `packwiz refresh` and `packwiz modrinth export` → `Vocana-Resonance-<version>.mrpack`;
5. boots the pack: installs the .mrpack as a dedicated server (`scripts/mrpack_install.py`, server-side files only),
   installs NeoForge, starts it and waits for `Done (`. A boot failure is shown in the summary with the error lines;
6. with `publish: true` (and a boot test that did not fail), attaches the .mrpack to the GitHub release
   `resonance-v<version>` (a pre-release, updated in place on re-runs).

No Actions artifact is uploaded (the account's artifact storage is full); publish to get the file.

Inputs: `publish`, `vocana_jars` (`none` | `build` | `release`), `vocana_release_tag`, `include_curseforge`,
`smoke_test`.

### Locally

```bash
go install github.com/packwiz/packwiz@latest     # packwiz on PATH (~/go/bin)
cd modpack
bash scripts/resolve.sh                          # writes mods/*.pw.toml and .resolve/report.md
packwiz refresh
packwiz modrinth export -o Vocana-Resonance-1.0.0.mrpack
```

`mods/`, `.resolve/` and `*.mrpack` are git-ignored: the committed source of truth is `mods.txt`. To pin the pack
for a release, commit the resolved `mods/*.pw.toml` and `index.toml` from a good run instead (remove `/mods/` from
`modpack/.gitignore`).

To add a mod: add its Modrinth slug to the right section of `mods.txt` (with `side=client` if it is client-only)
and a line to this README saying why it is there.

## Installing

1. Get `Vocana-Resonance-<version>.mrpack` (the release asset, or a local export).
2. **Prism Launcher**: *Add Instance → Import → choose the .mrpack*. **Modrinth App**: *Create instance → From
   file*. Both install the right NeoForge and download every mod from Modrinth.
3. Give the instance 8 GB of memory (*Settings → Java → Maximum memory*).
4. Add Vocana and Vocana-Compat (next section).

### Adding Vocana and Vocana-Compat

Neither is on Modrinth (Vocana's repository is private), so the public .mrpack does not contain them.

- **By hand**: drop `vocana-<v>.jar` (Vocana 2.1.0+) and `vocana-compat-<v>.jar` into the instance's `mods/` folder
  (Prism: *right-click the instance → Folder*), and into the server's `mods/`. Both are needed on both sides.
- **In the workflow** (needs the repository secret `VOCANA_TOKEN`, a fine-grained token with *Contents: read* on
  `slappyfish167/Vocana` — the same secret `build.yml` uses):
  - `vocana_jars: build` checks out Vocana at the ref in `vocana.ref`, builds it, builds Vocana-Compat from this
    commit, and puts both jars in `modpack/mods/` before export (they land in the .mrpack's `overrides/mods/`);
  - `vocana_jars: release` downloads `vocana-*.jar` from Vocana's GitHub release (`vocana_release_tag`, or the latest)
    with `gh release download -R slappyfish167/Vocana`, and `vocana-compat-*.jar` from this repository's releases.
    Vocana's only release today is 1.0.0, older than the 2.1.0 Vocana-Compat needs, so use `build` until a 2.1 release exists.

  Without the secret the workflow warns and builds the public pack as usual.

### Private builds

A build that bundles Vocana's jars or the CurseForge extras is named `Vocana-Resonance-<version>-private.mrpack`.
It contains All-Rights-Reserved files that may not be redistributed: it is for you and your server only and must
**never be uploaded to Modrinth**. With `publish: true` it goes to a **draft** release (visible only to the
repository's collaborators), never a public one.

## Server notes

- Install: unpack the .mrpack's server files with `python3 scripts/mrpack_install.py <pack.mrpack> <dir> --side server`
  (what the CI boot test does), install NeoForge 21.1.255 there with `java -jar neoforge-21.1.255-installer.jar
  --installServer`, add Vocana's jars, accept the EULA.
- **Simple Voice Chat** listens on **UDP 24454** (`config/voicechat/voicechat-server.properties`, `port`). Open or
  forward that UDP port as well as the game's TCP 25565, or spoken chants and voice chat will not connect.
- `defaultconfigs/vocana-server.toml` is copied into every **new** world's `serverconfig/`. The pack's choices:
  `magic.harmPlayers = false` (co-op first; set it to `true` for PvP servers — server PvP must be on too) and
  `progression.hoursPerResonancePoint = 2.5` (instead of 3.0). Everything else is Vocana's default.
- Pre-generate with Chunky (`/chunky radius 3000`, `/chunky start`) before opening: Terralith + Tectonic + many
  structure mods make first generation the heaviest load the server will see.
- Client-only mods (Sodium, Iris, the culling mods, sound and UI mods) are marked `side = "client"` and stay off the
  server automatically.

## Known conflicts considered

- **Sodium vs Embeddium** — Sodium (official NeoForge, faster, Iris-compatible). Never both.
- **Iris needs Sodium** — both client-side; shaders are optional per player.
- **EMI vs JEI** — EMI only (Vocana-Compat's EMI module; two recipe viewers fight over the screen).
- **Xaero's vs JourneyMap** — Xaero's only; Open Parties and Claims draws claims on it.
- **Chipped vs Rechiseled** — Chipped only.
- **Inventory Profiles Next and Mouse Tweaks** — they coexist; turn off IPN's own scroll/drag options if both
  respond to the same gesture.
- **Terralith + Tectonic** — as mods they cooperate out of the box; do not add Terratonic (the datapack variant).
- **Structure density** — five structure mods plus YUNG's: Sparse Structures spreads them out.
- **Lithium + Noisium** — compatible; C2ME is excluded (no official NeoForge build).
- **Sound Physics Remastered + Simple Voice Chat** — both by henkelmax and meant to be used together: voices get the
  same reverb and occlusion as the world.
- **Create addons** — only addons with Create 6 builds; Create: Enchantment Industry and Steam 'n' Rails are out
  until their Modrinth builds catch up.
