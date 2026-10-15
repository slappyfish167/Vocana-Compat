# The Ice and Fire module

Ice and Fire: Community Edition (`iceandfire` 2.1.x, branch `1.21.1` of `IAFEnvoy/IceAndFire-CE`) and Vocana.
Loaded when the mod is installed and `modules.iceandfire.enabled` is on; settings in
`config/vocanacompat-iceandfire.toml`. The module never touches an Ice and Fire class: it works by entity and item id
(read from `registry/IafEntities.java` and `registry/IafItems.java` on that branch), by NeoForge events and by data.
Wyrmsteel needs Create as well; without Create the alloy and its gear simply have no recipe.

## Sirens (`sirens.removeSirens`, on; `sirens.removeSirenIslands`, off)

Ice and Fire brings sirens into the world two ways, and the module closes both:

| Path in Ice and Fire | What the module does |
|---|---|
| `world/siren/SirenSpawnBiomeModifier` (`iceandfire:siren_spawns`, `data/iceandfire/neoforge/biome_modifier/siren_beach_night.json`): adds a night beach spawn in the ADD phase, only when its own `siren.beachSpawns` is on (off by default) | `data/vocanacompat/neoforge/biome_modifier/iceandfire/remove_siren_spawns.json`: `neoforge:remove_spawns` of `iceandfire:siren` in `#minecraft:is_overworld`, REMOVE phase, so it runs after theirs. In code, `FinalizeSpawnEvent` refuses natural, chunk-generation, structure, patrol and reinforcement spawns (`setSpawnCancelled`). |
| `world/structure/SirenIslandStructure`: the island piece builds one to three `SirenEntity` by hand in `postProcess` and `addFreshEntity`s them, with no spawn event | `EntityJoinLevelEvent`: a siren joining a server level that was not loaded from disk and carries no `vocanacompat.siren_allowed` tag is turned away. The tag is given in `FinalizeSpawnEvent` to sirens someone asked for (spawn egg, `/summon`, spawner, dispenser, bucket, summoning), so those still work. |
| The island itself (`worldgen/structure/siren_island.json`, biomes `#iceandfire:structure_gen/siren_island`) | Only with `removeSirenIslands`: `data/iceandfire/tags/worldgen/biome/structure_gen/siren_island.json` with `replace: true` and no values, so the structure has nowhere to generate. Islands already generated stay. This relies on the mod's data pack sitting after Ice and Fire's (the mod orders itself `AFTER` `iceandfire`); if a world still gets new islands, report it. |

Sirens already in a world are left alone (they were loaded from disk). Everything is behind the data condition
`vocanacompat:module_flag` (`{"type": "vocanacompat:module_flag", "module": "iceandfire", "key": "removeSirens"}`), which
reads the module's own switches, beside the usual `neoforge:mod_loaded` and `vocanacompat:module_enabled`. The type is
registered by the core, not the module, so the files still parse in a game without Ice and Fire (NeoForge decodes a
condition list before testing it, and an unknown type would make the biome modifier a registry-loading error).

## Dragonsteel (`dragonsteel.rebalance`, on)

Ice and Fire's dragonsteel reads its figures from its own config (`config/iceandfire/iaf-common.json`, section
`armors`): 25 base damage (the tier's bonus is that less one), 8000 tool durability, 7/12/9/6 armour with 6 toughness,
all far beyond netherite. With the rebalance on, `ItemAttributeModifierEvent` rewrites each piece's modifiers as they
are read (per stack, both sides, undone by the toggle): netherite's figures plus an offset per kind, so every
dragonsteel kind is a netherite-class choice with its own lean, and wyrmsteel sits a step above. Durability is not an
attribute, and `IafTiers` is an enum implementing `Tier` (not a holder that could be wrapped), so tool durability stays
Ice and Fire's: lower `dragonSteelBaseDurability` in its config if you want netherite's 2031.

Defaults (netherite in the first column; every number is in `[dragonsteel.<kind>]`):

| | Netherite | Fire dragonsteel | Ice dragonsteel | Lightning dragonsteel | Wyrmsteel |
|---|---|---|---|---|---|
| Sword damage / attack speed | 8 / 1.6 | **10** / 1.4 | 9 / 1.3 | 8 / **2.0** | **11** / 1.6 |
| Axe | 9 / 1.0 | 11 / 0.8 | 10 / 0.7 | 9 / 1.4 | 12 / 1.0 |
| Pickaxe | 5 / 1.2 | 7 / 1.0 | 6 / 0.9 | 5 / 1.6 | 8 / 1.2 |
| Shovel | 6.5 / 1.0 | 8.5 / 0.8 | 7.5 / 0.7 | 6.5 / 1.4 | 9.5 / 1.0 |
| Hoe | 1 / 4.0 | 3 / 3.8 | 2 / 3.7 | 1 / 4.4 | 4 / 4.0 |
| Armour (helmet/chest/legs/boots) | 3/8/6/3 | 3/8/6/3 | 3/8/6/3 | 2/7/5/2 | **4/9/7/4** |
| Toughness per piece | 3 | 3 | **4** | 2 | **4** |
| Knockback resistance per piece | 0.1 | **0.15** | 0.1 | 0.05 | **0.15** |
| Movement speed per piece | — | — | −2 % | **+2 %** | — |
| Other | | Ice and Fire's own fire/breath protection stays | sheds freezing with 2+ pieces on (`iceFrostResistancePieces`) | | fire-resistant; tools 3000 durability, armour 495/720/675/585 |

Attack speed is shown as attacks per second (the player's 4 plus the modifier). The config holds the offsets:
`damage` and `attackSpeed` over netherite's tool, `armor` over netherite's piece (floored at 0), `toughness` and
`knockbackResistance` outright, `movementSpeed` as a fraction per piece (`ADD_MULTIPLIED_TOTAL`). The tool modifiers
keep vanilla's `minecraft:base_attack_damage`/`base_attack_speed` ids so tooltips print the green totals; armour ids
follow `minecraft:armor.<piece>`.

## Wyrmsteel (needs Create)

Wyrmsteel is the netherite-dragonsteel alloy, `vocanacompat:netherite_dragonsteel_ingot`, and a whole set: a staff,
five tools, four armour pieces and a storage block. No crafting-table recipe makes any of it:

- **Ingot**: `create:mixing`, `heat_requirement: superheated` (a seething blaze burner), one `#c:ingots/netherite` and
  one dragonsteel ingot of any kind (fire, ice or lightning) give two wyrmsteel ingots
  (`data/vocanacompat/recipe/iceandfire/netherite_dragonsteel_ingot.json`).
- **Wyrmsteel Staff** (`vocanacompat:wyrmsteel_staff`): a `StaffItem` with the Archmage's capacity (+6) and the
  affinity of FIRE, FROST and LIGHTNING at once (a fifth cheaper and stronger, as every staff affinity), Epic, fire
  resistant. Vocana treats any `StaffItem` as a Catalyst, so it inscribes, chants, tunes and holds a Bound Catalyst
  like the core staffs. Made by `create:sequenced_assembly` from an **Archmage's Staff**: deploy a wyrmsteel ingot,
  press, deploy a dragon bone, deploy dragon scales (any colour), deploy dragon blood (any kind), press; three loops.
- **Tools**: each from its netherite counterpart on its own line. Their tier mines what netherite mines, 3000
  durability, enchantability 20, repaired with the ingot; their attack figures come from `[dragonsteel.wyrmsteel]`
  like dragonsteel's. The items are built with those offsets' defaults, armour too, so turning `rebalance` off leaves
  wyrmsteel as it is until the offsets are edited.

  | Piece | From | Each loop | Loops |
  |---|---|---|---|
  | Sword | netherite sword | ingot, press, dragon blood (any kind), press | 2 |
  | Pickaxe, Axe, Shovel, Hoe | the netherite tool | ingot, dragon bone, press | 2 |

- **Armour** (`wyrmsteel` armour material): each from its netherite piece, deploying dragon scales of any colour.
  Netherite's 3/8/6/3 armour plus one, toughness 4 and knockback resistance 0.15 per piece, durability multiplier 45
  (netherite's 37),
  enchantability 15, netherite's equip sound, fire resistant, repaired with the ingot, and it takes armour trims.

  | Piece | Each loop | Loops |
  |---|---|---|
  | Helmet, Boots | ingot, dragon scales, press | 2 |
  | Chestplate | ingot, dragon scales, dragon blood, press | 3 |
  | Leggings | ingot, dragon scales, press | 3 |

- **Block of Wyrmsteel** (`vocanacompat:wyrmsteel_block`): nine ingots compacted in a basin under a press
  (`create:compacting`, no heat); it unpacks to nine on the crafting table, which makes no metal that was not already
  there. Netherite's hardness and blast resistance, needs a diamond pickaxe, a beacon base, and the ingot is a beacon
  payment. It is a ritual anchor for the Imbue Bench (`vocanacompat:iceandfire/ritual_anchors`, with Ice and Fire's
  three dragonsteel blocks) and teaches METAL.
- **Tags**: the gear is in `minecraft:swords`, `pickaxes`, `axes`, `shovels`, `hoes`, `head_armor`, `chest_armor`,
  `leg_armor` and `foot_armor` (so the enchanting table and anvil treat it as what it is), `minecraft:trimmable_armor`,
  `c:ingots/wyrmsteel` and `c:storage_blocks/wyrmsteel`. These shared files carry no module condition (other modules
  may add to them); every line is optional instead, and the items exist only when the module loaded.
- Each line rides its own incomplete piece (`incomplete_wyrmsteel_<piece>`), plain items, so Create never confuses one
  line with another. (They are not Create's `SequencedAssemblyItem`, which would put a Create class in this package
  and break `-Pcompat.skip=create`; the progress bar is the only loss.)
- Ice and Fire has no dragon-blood fluid, so blood is deployed as an item rather than filled.

Textures are drawn by `tools/textures_iceandfire.py` (Pillow; run `python3 tools/generate_textures.py iceandfire`): the
item sprites under `assets/vocanacompat/textures/item/iceandfire/`, the block under `block/iceandfire/` and the worn
armour layers `models/armor/wyrmsteel_layer_1.png` and `_layer_2.png`. They are committed and regenerate byte-for-byte.

## Words

Discovery tags (`vocana:hears/<gloss>`, one optional line each in `data/vocana/tags/.../hears/`, the ids in
`data/vocanacompat/tags/.../iceandfire/`):

| Hears | From |
|---|---|
| FIRE, SKY | fire dragons (and their fire charges); fire-coloured scale blocks, charred ground, the fire dragonsteel block |
| FROST, SKY | ice dragons; ice-coloured scale blocks, dragon ice, frozen ground, the ice dragonsteel block |
| LIGHTNING, SKY | lightning dragons; lightning-coloured scale blocks, crackled ground, the lightning dragonsteel block |
| WATER | sea serpents, hippocampi |
| STONE | cyclopes, gorgons, stone statues |
| DECAY | death worms, hydras |
| DEATH | the dread mobs; dread stone, dreadwood, the dread spawner |
| LIGHT | pixies |
| WIND | hippogryphs, amphitheres |
| SOUL | ghosts |
| METAL, BONE | the three dragonsteel blocks and silver ore; the dragon bone block and wall |

Dragon blood has no block, so it teaches nothing by itself; its imbue recipes are where it meets the tongue.

Two Elder shades (`src/main/lexicon/iceandfire.json`; the Wyrmroost module's `drakon` DRAGONFIRE is the FIRE shade of
the family):

| Form | Gloss | Shade of | Profile | Heard from |
|---|---|---|---|---|
| `drakopagos` (drah-koh-PAH-goss) | DRAGONFROST | FROST | power 1.25, reach 1.3, speed 0.9, duration 1.4, weight 1, trait `deepcold` (freezes solid and stills) | ice dragons; dragon ice, dragon ice spikes, the ice dragonsteel block |
| `drakastrape` (drah-kah-STRAH-pay) | DRAGONBOLT | LIGHTNING | power 1.2, speed 1.3, weight 1, trait `chain` (forks up to four times) | lightning dragons; the lightning dragonsteel block, crackled stone |

Both sit in their base's chapter and cost one more CP, like the core's heavy shades.

## Imbue Bench recipes (`data/vocanacompat/vocana_imbue/iceandfire/`)

| Id | Lays | Into | Materials | Levels | Thrum | Strength | Complexity |
|---|---|---|---|---|---|---|---|
| `dragon_blood_weapon_fire/frost/lightning` | that essence and its shades | weapons | 1 dragon blood of the kind, 1 dragon bone | 3 | 20 | 1.3 | 5 (+2 / +10 / +2 for a shade) |
| `dragon_scale_armor_fire/frost/lightning` | that essence and its shades | armour | 2 dragon scales of the kind's colours (`#vocanacompat:iceandfire/<kind>_dragonscales`), 1 amethyst shard | 3 | 20 | 1.2 | 5 (+2 / +10 / +2) |
| `dragonsteel_staff` | fire, frost or lightning | staffs | 1 dragonsteel ingot (`#vocanacompat:iceandfire/dragonsteel_ingots`) | 3 | 20 | 1.2 | 3 (+1 / +5 / +1) |
| `wyrmsteel_staff` | any essence | the Wyrmsteel Staff | 1 dragon bone, 1 amethyst shard | 2 | 15 | 1.5 | 2 (+1 / +5 / +1) |

All take priority over Vocana's generic recipes (priority 1; the wyrmsteel one 2).

## Testing in game (needs Ice and Fire CE and Create: `./gradlew runClient -Pcompat.run=iceandfire,create,curios`)

1. Sirens: `/locate structure iceandfire:siren_island`, fly there: the island stands empty; the log (with
   `verboseLogging`) says a siren was turned away. `/summon iceandfire:siren` and the spawn egg still work. Set Ice
   and Fire's `siren.beachSpawns` to true and spend a night on a beach: nothing. Turn `removeSirens` off and repeat:
   sirens are back. With `removeSirenIslands`, a new world finds no island.
2. Dragonsteel: hold each dragonsteel tool and read the tooltip against the table; wear the armour and compare the
   armour bar; walk through powder snow in two ice pieces (no freezing) and in one (freezing); toggle `rebalance` off
   and see Ice and Fire's figures return.
3. Wyrmsteel: a basin over a seething burner with a netherite ingot and a dragonsteel ingot gives two wyrmsteel
   ingots; a cold or merely kindled burner does not. Build the three lines (depot or belt, press, deployers) and watch
   an Archmage's Staff become a Wyrmsteel Staff; inscribe with it (Capacity +6, affinity Fire, Frost, Lightning), chant
   `open saunta raustau close` and see the fifth off. Run the tool and armour lines the same way; wear the full set
   and read the armour bar (4/9/7/4 with toughness 4), put a trim on the chestplate at a smithing table, and enchant
   the axe with Sharpness and the boots with Feather Falling. Compact nine ingots under a press into a block, unpack
   it on the crafting table, mine it (diamond pickaxe or better) and build a beacon on it. The incomplete pieces must
   not be craftable or usable as anything else. Check the creative tabs (Ingredients, Building Blocks, Combat, Tools).
4. Words: stand by an ice dragon with the Chime; FROST, SKY and (once FROST is known) DRAGONFROST surface; the same
   with a lightning dragon for DRAGONBOLT. Speak `open drakopagos raustau close` at a creature: it freezes and stills.
5. Bench: lay fire into a sword with fire dragon blood and a dragon bone; lay FROST into the Wyrmsteel Staff with a bone
   and a shard.
