# Curios module

Bridges Vocana and the [Curios API](https://github.com/TheIllusiveC4/Curios) (9.5.1+1.21.1): Vocana's Torc and
Whispering Relics fit Curios slots, four new focus items are worn there, and what is worn speaks to the Seal through
`VocanaApi`. Loaded only when `curios` is installed and `modules.curios.enabled` is on; settings in
`config/vocanacompat-curios.toml`. Code: `dev.vocana.compat.curios`.

## Slots

Curios 9.5.1 ships the preset slot *types* (`data/curios/curios/slots/necklace.json`, `ring.json`, `charm.json`, …,
each with the `curios:tag` validator) but gives none of them to any entity until a mod asks. This module asks for
players with `data/vocanacompat/curios/entities/curios/player.json` (`"entities": ["minecraft:player"]`,
`"slots": ["necklace", "ring", "charm"]`, `"replace": false`, so it merges with what other mods grant) and raises the
ring slot to two with `data/vocanacompat/curios/slots/ring.json` (a slot file's id is its path under `curios/slots`, so this one must not sit in a module subfolder) (`"size": 2`; Curios keeps the larger of the
sizes mods ask for). Both files carry `neoforge:mod_loaded` + `vocanacompat:module_enabled`; Curios' loaders check
`neoforge:conditions` themselves.

What fits a slot is the item tag `curios:<slot>` (`data/curios/tags/item/<slot>.json`; every entry is optional, so a
missing item never breaks the tag):

| Slot | Items |
|---|---|
| `necklace` | `vocana:torc`, `vocanacompat:amulet_of_resonance` |
| `ring` (2) | `vocanacompat:ring_of_focus`, `vocanacompat:signet_of_the_elements` |
| `charm` | `vocana:whispering_relic`, `vocanacompat:charm_of_calm` |

A pack can tag more of Vocana's items into a slot; an essence-imbued piece tagged into `curios:ring` gets the Signet's
discount for its essences (below).

## Vocana's items in slots

- **Torc** in the necklace slot (or any slot a pack tags it into): `VocanaApi.registerWorn` is answered by
  `CurioSlots.wearsTorc`, so the wearer can chant hands-free as with a Torc in the inventory. Vocana's own rule, a Torc
  anywhere in the inventory counts as worn (`TorcItem.worn`), is not something this module can switch off: `VocanaApi`
  only lets another place be *added*. A setting that would make the slot the only place therefore does not exist
  here; it needs a hook in Vocana first.
- **Whispering Relic** in the charm slot: after `relicWhisperSeconds` (30) of wear it whispers its word to its wearer
  exactly as using it would (a silent relic takes a random word, an Elder word is heard as its base by one who does
  not know that base, `Progress.learn` records it), once per wearing. Off with `relicWhispers = false`.
- Both go on by right-click (`equipFromUse`).
- **Spell Crystals and Inscribed Scrolls are deliberately not curios.** Vocana's waking words look only at the hands
  and the inventory (`Latents.carried`, which has no extension point), so a vessel in a charm slot would lie dormant;
  tagging it would only mislead. If Vocana grows a hook for carried vessels, add the tag and nothing else.

## The focus items

All four are shapeless recipes from gold, copper, amethyst shards, an echo shard (and string), unlocked by picking up
an amethyst shard, and sit in Tools & Utilities with Vocana's items. Every effect is applied by Vocana at the Seal or
on its regen tick through the `VocanaApi` caster modifiers; the items themselves carry no state and no tick.

| Item | Slot | Effect | Setting (default) | Through |
|---|---|---|---|---|
| Ring of Focus | ring | +Capacity per ring worn | `ringCapacity` (2) | `registerCapacity` |
| Amulet of Resonance | necklace | Thrum regeneration × | `amuletRegen` (1.25) | `registerRegen` |
| Charm of Calm | charm | Strain at the Seal × | `charmStrain` (0.85) | `registerStrain` |
| Signet of the Elements | ring | Workings of its essence cost × Thrum | `signetCostFactor` (0.9) | `registerCostFactor` |

**The Signet** is attuned in the world rather than at the Imbue Bench (the bench takes weapons, armour, tools and
staffs only): use it with an **Essence Residue** (what stripping an essence imbue at the bench leaves) in the other
hand; the residue is spent and the Signet carries its essence in Vocana's `vocana:word` component, shown in its
tooltip and by its glint. Drinking another residue replaces the essence. At the Seal, the chant's root glosses are
compared with the base essences carried by everything in the ring slots: the Signet's essence, every essence imbued in
the piece (`vocana:imbues`, and the 2.0 `vocana:imbuement`); any match applies the factor once (two matching rings do
not stack). The arithmetic is `CurioRules.costFactor`.

Recipes: Ring of Focus = 2 gold, amethyst shard, copper · Amulet of Resonance = gold, copper, 2 amethyst shards, echo
shard · Charm of Calm = 2 copper, 2 amethyst shards, string · Signet of the Elements = 3 gold, echo shard, amethyst
shard. Recipe files: `data/vocanacompat/recipe/curios/*.json` (ids `vocanacompat:curios/<name>`), unlock advancements
beside them, models in `assets/vocanacompat/models/item/` (a model must sit at its item's path, so these are not in a
`curios/` sub-folder; the textures are, under `textures/item/curios/`, drawn by `tools/textures_curios.py` (run `python3 tools/generate_textures.py curios`)).

## Config (`config/vocanacompat-curios.toml`)

| Key | Default | Meaning |
|---|---|---|
| `general.verboseLogging` | `false` | The self-check and the module's lines at INFO instead of DEBUG. |
| `general.equipFromUse` | `true` | Right-clicking a focus item, the Torc or a relic puts it into a free slot. |
| `items.ringCapacity` | `2` | Capacity each worn Ring of Focus adds (0–20). |
| `items.amuletRegen` | `1.25` | Thrum regeneration multiplier with an Amulet of Resonance (0.1–5). |
| `items.charmStrain` | `0.85` | Strain multiplier at the Seal with a Charm of Calm (0.1–2). |
| `items.signetCostFactor` | `0.9` | Thrum multiplier on a Working of an essence a ring-slot item carries (0.1–2). |
| `relics.relicWhispers` | `true` | A relic worn in a charm slot whispers its word. |
| `relics.relicWhisperSeconds` | `30` | Seconds of wear before it does (1–600). |

## Curios APIs used (verified in the `1.21.1` branch, tag 9.5.1)

- `CuriosApi.getCuriosInventory(LivingEntity)` → `Optional<ICuriosItemHandler>`; `ICuriosItemHandler.findCurios(Predicate)`,
  `findCurios(String...)`, `isEquipped(Predicate)` (`neoforge/src/main/java/top/theillusivec4/curios/api/...`).
- `ICurioItem` implemented by the focus items (Curios' item-capability provider in `neoforge/.../Curios.java` takes
  `CuriosApi.registerCurio` registrations first, then an item that `instanceof ICurioItem`); `canEquipFromUse`,
  `curioTick(SlotContext, ItemStack)`, `onUnequip(SlotContext, ItemStack, ItemStack)`; `SlotContext.entity()`.
- `CuriosApi.registerCurio(Item, ICurioItem)` for the Torc and the relic, in common setup.
- `CuriosTags.createItemTag(String)` and `CuriosApi.getPlayerSlots(boolean)` in the self-check.
- Data: `data/<ns>/curios/slots/<slot>.json` (`replace`, `size`, `operation` SET/ADD/REMOVE, `order`, `icon`,
  `add_cosmetic`, `use_native_gui`, `render_toggle`, `drop_rule`, `validators`), `data/<ns>/curios/entities/*.json`
  (`replace`, `entities` (ids or `#tags`), `slots`), both reading `neoforge:conditions`
  (`CuriosSlotManager.fromJson`, `CuriosEntityManager.getSlotsForEntities`); the validator `curios:tag` accepts
  `curios:<slot>` or `curios:curio` (`CuriosImplMixinHooks`).

## Testing in game (needs Curios: `./gradlew runClient -Pcompat.run=curios`)

Set `verboseLogging = true` first; the log then shows, at common setup, the six item→slot pairings and the numbers,
and once the world is up the player's slot set (`necklace`, `ring`×2, `charm` among them) and one line per item saying
it is in its `curios:<slot>` tag, or a warning naming the data file that did not apply.

1. Open the Curios screen (the button in the inventory): necklace, two ring and charm slots are there.
2. Put the Torc in the necklace slot, hold nothing, press the chant key: spoken listening opens (hands-free). Take it
   out (and have none in the inventory): it does not. Right-click a Torc from the hotbar: it goes into the slot.
3. Wear a Whispering Relic in the charm slot; after 30 s the resonate sound plays and the action bar says what it
   whispers; the word is in the Grimoire. It whispers once; take it off and put it back to hear it again.
4. Ring of Focus: Capacity is what a chant's CP is measured against, so take a chant one or two CP above your
   Capacity (8 + 2.5 × Focus with the Chime in hand): `/vocana parse <chant>` or the F8 panel reads it as Overreach
   without the ring and cleanly with it; two rings, +4.
5. Amulet of Resonance: the Thrum meter refills faster (×1.25) after a chant.
6. Charm of Calm: a chant that would strain into a given band strains less; compare the F8 panel's Strain with and
   without.
7. Signet: strip an essence at the Imbue Bench for a residue (or `/give … vocana:essence_residue[vocana:word="FIRE"]`),
   hold the residue off-hand and right-click the Signet: the message names the essence, the Signet glints and its
   tooltip says "Carries saunta". Wear it: a FIRE chant's quoted Thrum (F8 panel) is 0.9 of the price without it; a
   WATER chant is unchanged.
8. Turn `equipFromUse` off: right-clicking a ring does nothing; the slot still takes it by drag.
9. `modules.curios.enabled = false`: the four items are gone, the recipes are gone, the slots the module granted are
   gone unless another mod grants them, the Torc still works from the inventory.
