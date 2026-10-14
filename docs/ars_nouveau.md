# Ars Nouveau module

Bridges between Vocana and Ars Nouveau 5.13.x (`ars_nouveau`, with GeckoLib and Curios), loaded only when Ars is
installed and `modules.ars_nouveau.enabled` is on. Settings: `config/vocanacompat-ars_nouveau.toml`. Code:
`dev.vocana.compat.ars_nouveau`. The module touches nothing of Ars outside that package.

## What it does

| Piece | Where | Summary |
|---|---|---|
| Spell cost factor | `SpellCosts`, Ars's `SpellCostCalcEvent` | Every Ars spell a player casts costs 1.15x what Ars computed (after Ars's own discounts). Turrets and runes are left alone unless `applyToMachines`. |
| Staff discount | same event | A Vocana staff in the off hand makes the spell 5 % cheaper again (1.15 x 0.95 = 1.09: still dearer than Vocana). The one place Vocana gear helps Ars. |
| Vocana cost factor | `VocanaApi.registerCostFactor` | 1.0 by default and never below one: Vocana is never cheaper than its own numbers. Owners who want Vocana to be the dearer art raise it and lower `arsCostFactor`. |
| The word SOURCE | `SourceEssence`, lexicon `nauspi` | An Essence of Thaumaturgy. Touching the ground beside source jars fills them; touching an Ars caster gives mana; laid as a state (IMBUE) it is Ars's mana-regeneration effect; imbued in gear it returns mana on a strike. |
| The glyph Resonate | `ResonateEffect`, `vocanacompat:glyph_vocana_thrum` | A tier-two Ars effect: returns Thrum to a caster who has tuned a Chime, for the mana it costs, once per five seconds. Scribes Table recipe and Worn Notebook page included. |
| Imbue Bench recipes | `data/vocanacompat/vocana_imbue/ars_nouveau/` | Any essence into the Enchanter's Sword; SOURCE into weapons and armour; SOURCE into staffs. All paid in source gems and magebloom. |
| Catalysts | `VocanaApi.registerCatalyst` | The Enchanter's Sword is a Catalyst with +1 Capacity (on by default); spell books can be (off by default). |
| Discovery | `vocana:hears/*` tags | Jars, magebloom, archwood, sourceberries teach SOURCE; archwood saplings and leaves, magebloom and sourceberries teach LIFE; Wilden teach SHADOW and DEATH; starbuncles LIGHT; drygmies and whirlisprigs LIFE; Ars's magical creatures bear SOURCE. |

## Balance rationale

The brief: Ars Nouveau 10-25 % dearer than Vocana, nothing making Vocana cheaper than itself, and no way to turn one
system's resource into more of the other's by going round. The numbers (all in the config):

- **`arsCostFactor` 1.15.** The only lever Ars exposes on spell cost is `SpellCostCalcEvent`, whose `currentCost` is
  already net of Ars's rings, threads and caster-tool discounts; the module multiplies that and rounds (a 1-mana spell
  stays at 1, a 100-mana spell becomes 115). Ars fires the event twice per cast (the affordability check and the
  payment) and the module answers both the same way, so a spell never passes the check and then costs more.
- **`vocanaStaffArsDiscount` 0.95**, range 0.8-1.0. Modest by design, and `Conversion.guardsHold` (checked by the
  GameTests at the defaults) requires `arsCostFactor x discount >= 1`.
- **`arsResourceFactor` 1.15.** Ars prices its machines' resources in its own recipe JSON (`sourceCost` of an
  apparatus recipe, `source` of an imbuement recipe) and `EnchantingApparatusTile` draws exactly that number from the
  jars: there is no event to multiply it at run time. The factor therefore applies where *this module* prices Ars's
  resources: a SOURCE Working's budget is divided by it, and the Resonate glyph's Scribes Table recipe asks 63
  experience where Ars's comparable tier-two glyph (Heal) asks 55. Owners who want Ars's own recipes dearer can
  install `docs/ars_nouveau/datapack/` as a data pack: it overrides `ars_nouveau:imbuement_amethyst` (amethyst shard
  to source gem, the bridge's main material) from 500 to 575 source, the same 1.15 applied by hand.
- **Par rates.** `sourcePerMana` 1: Ars's own spell turrets draw one source per point of mana their spell costs
  (`TileCaster.expendMana`), so source and mana are already at par inside Ars. `manaPerThrum` 5: a fresh Ars caster
  regains 5 mana a second from a pool of 100, a novice Chime 0.725 Thrum a second from a pool of 82; weighed by pool
  one Thrum would be about one mana, weighed by regeneration about seven, and sustained casting is what matters, so
  five. At that rate the whole novice Thrum pool is worth about 410 mana or 410 source.
- **`conversionLoss` 0.35** (minimum 0.05; the tests expect at least 0.3). Every exchange keeps 65 % of par, a round
  trip 42 %. Mana to Thrum (Resonate) and Thrum to source or mana (SOURCE) are the only conversions; source never
  becomes Thrum.
- **The SOURCE budget.** However a SOURCE Working is shaped, it may give out at most
  `paid Thrum x manaPerThrum x sourcePerMana x (1 - loss) / arsResourceFactor` source (or that much mana at
  `sourcePerMana`), tracked per Working id, so AREA, repeats and many jars cannot multiply it. A touch gives
  `sourcePerCast x power` (40 at power 1) up to that budget. A four-Thrum Whisper fills about 11 source; a
  twenty-Thrum Spoken Working about 56; the whole novice pool about 230, a little under half a source gem. Compare the
  apparatus, which draws hundreds to thousands per craft: SOURCE tops up a jar before a craft, it does not replace
  sourcelinks.
- **Resonate.** Default cost 50 mana (Ars's per-glyph config file, `config/vocanacompat/glyph_vocana_thrum.toml`,
  owns it; the module reads the live value) returns `50 / 5 x 0.65 = 6.5` Thrum. The cooldown of 100 ticks is the
  real cap: at most 1.3 Thrum a second whatever the caster's mana, and sustaining it costs 10 mana a second, twice a
  fresh caster's regeneration. A novice who spends all their mana on Resonate roughly doubles their Thrum income and
  casts no Ars spells meanwhile. No augments: Amplify would have to be priced into the exchange.
- **Catalysts.** The Enchanter's Sword needs a diamond sword, gold blocks and source gem blocks on the apparatus, far
  more than a Tuning Chime; as a Catalyst it does what the Chime does with one more Capacity, less than the first
  staff's two, so it is a convenience for an Ars player who also chants, never the better Catalyst. Spell books are off
  by default because a novice book is cheap and would let a player skip the Chime altogether.

## Config

| Key (`[section]`) | Default | Range | Meaning |
|---|---|---|---|
| `balance.arsCostFactor` | 1.15 | 1.0-2.0 | Multiplier on a player's Ars spell cost. |
| `balance.applyToMachines` | false | | Also apply it to turrets, runes and other machine casters. |
| `balance.arsResourceFactor` | 1.15 | 1.0-2.0 | Divides the source a SOURCE Working may give (see above). |
| `balance.vocanaCostFactor` | 1.0 | 1.0-2.0 | Multiplier on Vocana's Thrum at the Seal. |
| `balance.vocanaStaffArsDiscount` | 0.95 | 0.8-1.0 | Multiplier on an Ars spell's cost with a Vocana staff in the off hand. |
| `balance.manaPerThrum` | 5.0 | 0.1-100 | Par rate: mana one Thrum is worth. |
| `balance.sourcePerMana` | 1.0 | 0.1-100 | Par rate: source one mana is worth. |
| `balance.conversionLoss` | 0.35 | 0.05-0.95 | Share of par lost on every conversion. |
| `source.sourcePerCast` | 40 | 1-10000 | Source per touch of a block, times the Working's power, within the budget. |
| `source.sourceReach` | 3 | 1-8 | Manhattan distance from the touched block to the jars filled. |
| `source.touchMana` | 10 | 0-1000 | Mana per touch of an Ars caster at intensity 1, within the budget. |
| `source.strikeMana` | 3 | 0-100 | Mana the holder of SOURCE-imbued gear regains per strike or blow taken, per unit of strength. |
| `resonate.enabled` | true | | Whether Resonate restores Thrum when it resolves. |
| `resonate.cooldownTicks` | 100 | 0-72000 | Ticks between two Resonates that answer the same caster. |
| `gear.enchantersSwordCatalyst` | true | | The Enchanter's Sword is a Catalyst. |
| `gear.enchantersSwordCapacity` | 1 | 0-3 | Its Capacity bonus. |
| `gear.spellBookCatalyst` | false | | Spell books are Catalysts. |
| `gear.spellBookCapacity` | 0 | 0-3 | Their Capacity bonus. |
| `general.verboseLogging` | false | | Log at INFO instead of DEBUG. |

The glyph's own mana cost, tier, per-spell limit and enabled flag are Ars's per-glyph config,
`config/vocanacompat/glyph_vocana_thrum.toml`, written by `GlyphRegistry.registerSpell` (cost 50, tier 2 by default).

## The bridge spells

### SOURCE (`nauspi`, NOWS-pee, Essence, Thaumaturgy, chapter Thaumaturgy)

Heard from source jars, magebloom, sourceberry bushes and archwood logs, and from Ars's magical creatures.

- **Into jars.** Vocana never lets a Working edit a block that has a block entity (`Permits.editable`), and Ars's jars
  are block entities, so the jar itself is never the touched block: aim at the **floor beside the jar**. `OPEN SOURCE
  IMBUE SEAL` looking at that block, or `OPEN SOURCE BECOME SEAL` (BECOME at a block changes the gaze block), reaches
  `SourceEssence.block`, which fills every `ISourceTile` within `sourceReach` of it, nearest first, through
  `ISourceTile.addSource(int)`. Any Ars source holder qualifies (jars, the imbuement chamber, relays, sourcelinks).
- **Into a caster.** A Working that touches a player with Ars mana (`OPEN SOURCE FORTH SEAL` at them) gives them
  `touchMana` x intensity mana, within the budget.
- **As a state.** `OPEN SOURCE IMBUE OTHER SEAL` lays Ars's Mana Regeneration effect for the Working's duration; a
  Greater Working lays level II.
- **Found.** As a Bearer or an Aim, SOURCE selects creatures in the module's bearer tag (`vocanacompat:ars_nouveau/
  source`: starbuncles, whirlisprigs, drygmies, bookwyrms, wixies, amethyst golems, alakarkinos and their familiars)
  and any creature whose mana is not empty. Blocks have no bearer hook in `VocanaApi`, so jars are not "found".

### Resonate (`vocanacompat:glyph_vocana_thrum`)

Tier two, 50 mana, no augments, no school. Learned from its glyph at the Scribes Table: a manipulation essence, two
source gems, two amethyst shards and a note block, 63 experience (recipe id `vocanacompat:ars_nouveau/
glyph_vocana_thrum`). `Self -> Resonate` (or any spell that resolves anywhere) returns 6.5 Thrum to the caster if they
have tuned a Chime, with a status line; it answers at most once per `cooldownTicks` and never a fake player, so turrets
and runes cannot farm it. The page appears in the Worn Notebook's tier-two glyph category.

### Imbue Bench recipes (`data/vocanacompat/vocana_imbue/ars_nouveau/`)

| Id | Lays | Into | Materials | Levels | Thrum | Complexity |
|---|---|---|---|---|---|---|
| `enchanters_sword_essence` | any essence (shades +3 / +15 / +2) | `ars_nouveau:enchanters_sword` | 2 source gems, 2 magebloom fiber | 5 | 30 | 6 |
| `source_gear` | SOURCE (on hit / when struck) | any gear | 1 source gem, 1 magebloom, 1 amethyst shard | 4 | 25 | 5 |
| `source_staff` | SOURCE (affinity, +1 Capacity) | staffs | 1 source gem, 2 magebloom fiber | 4 | 25 | 4 |

A source gem costs an amethyst shard and 500 source at the imbuement chamber, so these are dearer than Vocana's
amethyst-and-lapis recipes (`priority` 1 makes the sword recipe win over `vocana:essence_weapon`). SOURCE-imbued gear
returns `strikeMana` x strength mana to its holder per strike; a staff gains SOURCE as an affinity (a fifth cheaper
and stronger SOURCE Workings) and one more Capacity, as any imbued staff does.

## Mana <-> Thrum guard

Three conversions exist and all lose: Resonate (mana -> Thrum, x0.65 at par), SOURCE into jars (Thrum -> source,
x0.65 / 1.15) and SOURCE into a caster or imbued gear (Thrum or bench price -> mana). Source never becomes Thrum, and
mana becomes Thrum only through Resonate, whose cooldown bounds it to 1.3 Thrum a second. `Conversion.roundTrip`
is below one for every allowed `conversionLoss`; the unit tests walk the chain mana -> Thrum -> source at the defaults
and the GameTests assert the defaults (factor >= 1.1, loss >= 0.3, `arsCostFactor x discount >= 1`,
`vocanaCostFactor` = 1) on every run.

## Without Ars Nouveau

The build merges every module's words into one lexicon, so SOURCE exists in a game without Ars, blank in the
Grimoire: nothing teaches it (its `vocana:hears` tags are behind `neoforge:mod_loaded` and
`vocanacompat:module_enabled`) and it has no behaviour (`VocanaApi.essence("SOURCE")` is null), which the GameTests
check. The glyph recipe, the imbue recipes and the tags all carry both conditions.

## Testing

Without Ars: `./gradlew test` (`ConversionTest`) and `./gradlew runGameTestServer` (`ArsNouveauGameTests`: the word,
the defaults, the exchange, and that nothing of the module is registered).

In game, with Ars Nouveau + GeckoLib + Curios (`./gradlew runClient -Pcompat.run=ars_nouveau,curios`):

1. `config/vocanacompat-ars_nouveau.toml` and `config/vocanacompat/glyph_vocana_thrum.toml` are written; the log says
   `ars_nouveau module: loaded for Ars Nouveau`.
2. Cast a 20-mana spell from a novice book: 23 mana leaves the bar; with a Vocana staff in the off hand, 22. Place a
   spell turret casting the same spell beside a jar: it draws 20 source (machines untouched by default).
3. Vocana prices unchanged: inscribe `OPEN FIRE FORTH SEAL` before and after enabling the module.
4. Stand by a source jar and sourceberry bush with the Chime: SOURCE is heard (the Grimoire's Thaumaturgy chapter);
   near a starbuncle, LIGHT; near a Wilden hunter, SHADOW and DEATH; near a drygmy, LIFE.
5. `OPEN SOURCE IMBUE SEAL` looking at the floor beside an empty jar: the jar's fill rises by about 11 (a Whisper);
   `OPEN GREATER SOURCE IMBUE SEAL` more; a second jar within three blocks also fills once the first is full. Aim at
   the jar itself: nothing (expected; Vocana refuses block entities).
6. `OPEN SOURCE FORTH SEAL` at another player holding a spell book: their mana rises (the status is a particle burst).
7. Learn Resonate at the Scribes Table (recipe shown in the Worn Notebook, tier-two glyphs). Cast `Self -> Resonate`
   with a tuned Chime in the inventory: 50 mana leaves, "Resonate: 6.5 Thrum returns" appears; cast again at once:
   "still ringing". Cast from a turret: nothing.
8. Imbue an Enchanter's Sword with FIRE at the Imbue Bench with two source gems and two magebloom fiber on it; strike
   a mob: it burns. Imbue an iron sword with SOURCE: strikes return 3 mana. Hold the Enchanter's Sword alone and
   inscribe a chant: it casts (Capacity +1 in the F8 panel).
9. Set `modules.ars_nouveau.enabled = false`: no cost change, no SOURCE behaviour, no glyph recipe, no tags.
