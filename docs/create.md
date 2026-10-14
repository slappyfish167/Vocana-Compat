# The Create module

Create's machines join Vocana's language. The module loads only when `create` (6.0.x) is installed and
`modules.create.enabled` is on; its own settings are in `config/vocanacompat-create.toml`. Code lives in
`dev.vocana.compat.create`, words in `src/main/lexicon/create.json`, data under `data/vocanacompat/<kind>/create/`.

## Words

| Word | Form | Class | Chapter | Heard from |
|---|---|---|---|---|
| **KINETIC** | `krenshu` (KREN-shoo) | Essence, Transmutation | Matter | shafts, cogwheels, presses, fans, bearings, every block Create turns; moving contraptions |
| **SPIN** | `kirsau` (KEER-sow) | Deed, Force frame, Elementalism | Shapes & Force | hand cranks, valve handles, water wheels, windmill and mechanical bearings, turntables, cogwheels |
| **BRASS** | `orichalcum` (or-ih-KAL-kum) | Elder shade of METAL | Matter (beside METAL) | brass blocks and casings, brass-cased machines; METAL must be known first |

**KINETIC** is the essence of turning things. Made and loosed, its contact whirls a creature sideways, hurts a little
(`kinetic.touchDamage`) and leaves it reeling with Slowness; IMBUE lays a wound-down state (Slowness and Mining
Fatigue). A weapon imbued with it whirls what it strikes. A tool imbued with it digs `kinetic.toolHasteFactor` times
faster while any kinetic block within `kinetic.toolHasteRadius` is turning. Create's contraption entities *bear* it,
so found Aims and conditions can name them. On a bare block it only shows the machines around it turning: Vocana
never hands a block entity to an essence, and every kinetic block has one, so changing machines is SPIN's work.

**SPIN** sets what it finds turning. Creatures and dropped items are whirled aside (players are dizzied for a moment);
a **hand crank** (or valve handle) it reaches cranks itself for `spin.crankSeconds` seconds, scaled by the Working's
extent, driving whatever it is attached to; a **blaze burner** is stoked by the essence the chant carries: FIRE kindles
it as coal would, LIGHTNING makes it seethe as a blaze cake would, for Create's own burn time; other kinetic blocks
show their rotation indicators. Everything goes through Create's own entry points (`HandCrankBlockEntity.turn`,
`BlazeBurnerBlock.tryInsert` with `doNotConsume`), so Create's rules hold: a burner already seething refuses more,
normal fuel never replaces special fuel, an overstressed network still does not turn.

**BRASS** is data only: an Elder shade of METAL with the `encase` trait (what it strikes is clamped still a moment),
slightly stronger and longer than plain METAL, drawn in brass. It is a common word, not a deep one, so brass casings
teach it anywhere.

Create's blocks also teach core words: its metals METAL, the blaze burner FIRE, fans and sails WIND, water wheels and
fluid machines WATER, the steam engine LIGHTNING (`data/vocanacompat/tags/block/create/*.json`, referenced once each
from `data/vocana/tags/block/hears/*.json`).

## Imbue Bench recipes (`data/vocanacompat/vocana_imbue/create/`)

| Id | Lays | Into | Materials | Levels | Thrum | Complexity |
|---|---|---|---|---|---|---|
| `create/kinetic_tool` | KINETIC (on use, while held) | tools | 1 precision mechanism, 2 andesite alloy | 4 | 20 | 5 |
| `create/kinetic_weapon` | KINETIC (on hit) | weapons | 1 precision mechanism, 2 brass sheets | 5 | 25 | 6 |
| `create/kinetic_armor` | KINETIC (when struck, when equipped) | armour | 1 precision mechanism, 4 andesite alloy | 5 | 25 | 6 |
| `create/brass_any` | BRASS (named directly; plain METAL keeps the core's deep price) | anything | 2 brass ingots, 1 amethyst shard | 6 | 30 | 6 |

## Create recipes (`data/vocanacompat/recipe/create/`)

- `create/torc`: a **Torc** on the assembly line. A gold ingot is pressed, then a deployer sets copper and an amethyst
  shard, twice round; most of the time a Torc comes off (a golden or copper sheet, a shard, a nugget now and then).
  The half-made piece is `vocanacompat:create/incomplete_torc`, a Create `SequencedAssemblyItem` registered only when
  the module loads, so the progress bar shows.
- `create/tuning_chime_mixing`: a **Tuning Chime** alloyed in a basin over a *seething* blaze burner from copper,
  brass and an amethyst shard.

Templates for other modules (Ice and Fire's dragonsteel line) are in `docs/create_recipes.md`; the builders in
`CreateRecipes` write the same shapes.

## Immunity

Create's structural and contraption parts that have no block entity (chassis, poles, stickers, tracks, casings,
girders, sails, contacts, brackets) are in `vocana:immune` through `#vocanacompat:create/immune`, so a BLAST crater or
a temporary wall never opens a machine or a moving contraption. Blocks with block entities (every kinetic block, the
burner, the basin) are already refused by Vocana's `Permits.editable`.

## Settings (`config/vocanacompat-create.toml`)

| Key | Default | Meaning |
|---|---|---|
| `general.verboseLogging` | false | Log cranks turned and burners stoked at INFO |
| `kinetic.touchDamage` | 3.0 | Contact damage per point of intensity |
| `kinetic.touchShove` | 1.0 | Sideways whirl of the contact per point of intensity |
| `kinetic.touchSlownessTicks` | 60 | Slowness after the contact, per point of intensity (capped at two) |
| `kinetic.imbueSlownessAmplifier` | 1 | Slowness level of the IMBUE state (Mining Fatigue I comes with it) |
| `kinetic.gearShove` | 0.6 | Whirl of a KINETIC-imbued weapon per point of imbue strength |
| `kinetic.toolHaste` | true | KINETIC-imbued tools dig faster near turning machinery |
| `kinetic.toolHasteFactor` | 1.5 | That dig-speed multiplier |
| `kinetic.toolHasteRadius` | 6 | How far a turning kinetic block counts as nearby |
| `kinetic.toolHasteRescanTicks` | 20 | How often a digging player's surroundings are rescanned |
| `spin.spinShove` | 0.9 | Whirl SPIN gives creatures and items per point of intensity |
| `spin.spinCranks` | true | SPIN turns hand cranks and valve handles |
| `spin.crankSeconds` | 6 | How long a spun crank keeps turning (× the Working's extent) |
| `spin.maxCranks` | 64 | Cranks magic may turn at once, server-wide |
| `spin.spinStokesBurners` | true | SPIN with FIRE kindles a blaze burner, with LIGHTNING makes it seethe |
| `spin.spinIndicators` | true | Draw Create's rotation indicators on reached kinetic blocks |

## In-game checklist (needs Create installed: `./gradlew runClient -Pcompat.run=create`)

1. Log shows `create module: loaded for Create` and `Create module: KINETIC and SPIN registered`.
2. Hold the Chime near a turning cogwheel: `krenshu` is heard; near a hand crank: `kirsau`; near brass casing after
   METAL is known: `orichalcum`. A moving contraption (a bearing with blocks) teaches `krenshu` too.
3. Cast `open krenshu close` at a cow: it is whirled aside, takes 3 damage, has Slowness I for 3 s.
4. Cast `open kirsau close` at a hand crank attached to a millstone: the crank turns for about 6 s and the millstone
   runs. The `spin.maxCranks` cap refuses the 65th crank.
5. Cast `open saunta kirsau close` at an empty-heat blaze burner: it kindles. `open ... LIGHTNING form ... kirsau` at
   it: it seethes (blue flame) for 160 s; recasting while it seethes does nothing (Create's threshold).
6. Imbue a pickaxe with KINETIC at the bench (`create/kinetic_tool`): mining beside a running press is 1.5× faster,
   and stops being so when the network stalls.
7. Imbue a sword with KINETIC: struck mobs are whirled.
8. Put gold ingots on a depot under a press with a deployer line: the Torc assembles; the incomplete Torc shows a
   progress bar and is named "Incomplete Torc".
9. Basin over a seething burner: copper + brass + amethyst shard → Tuning Chime.
10. BLAST at a mechanical piston's extension poles: the poles stand; nearby stone craters.
11. Turn `modules.create.enabled` off: none of the words are heard from machines, recipes and imbues are gone, no
    `vocanacompat:create/incomplete_torc` in the creative search.
