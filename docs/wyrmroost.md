# The Wyrmroost module

Wyrmroost (`wyrmroost` 1.4.x, the NeoForge 1.21.1 port by NordAct) adds ten dragon kinds, their eggs, geodes and
platinum. This module lets Vocana hear words from them, gives the tongue three words for dragons, lays Wyrmroost's
materials into gear at the Imbue Bench, and makes a caster's magic treat their own dragon as their own. It loads only
when Wyrmroost is installed and `modules.wyrmroost.enabled` is on (`config/vocanacompat-modules.toml`); its own
settings are in `config/vocanacompat-wyrmroost.toml`.

Wyrmroost is All Rights Reserved: the module compiles against its jar, copies nothing from it, and touches its classes
in one file only (`Dragons.java`, see "Verified API" below). Everything else goes through vanilla `TamableAnimal`
(every Wyrmroost dragon is one) and through tags.

## Hearing words from dragons

Vocana teaches a word from any creature or block in the `vocana:hears/<word>` tag. The module owns one tag per word,
`vocanacompat:wyrmroost/<word>` (`data/vocanacompat/tags/entity_type/wyrmroost/`, `tags/block/wyrmroost/`), and the
shared `vocana:hears/<word>` files in this repository hold one optional line per module pointing at it. All of it is
behind `neoforge:mod_loaded` and `vocanacompat:module_enabled`.

| Kin | Teaches |
|---|---|
| Royal Red, its fire breath | FIRE, DRAGONFIRE; the breath BLAZE; the dragon INFERNO, SKY, SOAR, GREATER, SOUND |
| Alpine | FROST, CHILL, NORTHWIND, WIND, GALE, SKY, SOAR, GREATER, SOUND |
| Butterfly Leviathan | WATER, SEA, LIGHTNING, THUNDER, THUNDERBOLT, GREATER |
| Silver Glider | WIND, ZEPHYR, SKY, SOAR |
| Canari Wyvern | WIND, SKY, DECAY (its bite poisons) |
| Roost Stalker | SHADOW, NIGHT, TAKE (it steals from chests) |
| Overworld Drake | STONE, EARTH, GREATER, SOUND (its roar) |
| Lesser Desertwyrm | STONE, SAND |
| Coin Dragon | GOLD, METAL |
| Dragon Fruit Drake | LIFE, BLOOM, GROW |
| Dragon egg | LIFE, BEGIN |
| Wind gust (the Alpine's) | WIND, GALE |
| Every tameable dragon | FOLLOW |
| Every dragon and the egg | WYRM, ROOST |
| Geode ores | STONE, CRYSTAL; geode blocks CRYSTAL, and red FIRE, blue FROST, purple SHADOW |
| Platinum ore and blocks | METAL (the ore also STONE) |

Elder words answer only to someone who already knows the base word, and the Chime teaches at most three new forms at a
time, as everywhere in Vocana. No dragon is a boss, so none is in `vocana:unmovable`: Vocana's magic moves, binds and
commands them under its ordinary rules (a Royal Red's will is heavy; see below).

Geode blocks and platinum blocks also count as **ritual anchors** for the Imbue Bench (`vocana:ritual_anchors`).

## The words (`src/main/lexicon/wyrmroost.json`)

| Form | Gloss | Class | Chapter | What it does |
|---|---|---|---|---|
| `mespau` | WYRM | Essence | Summoning | **Found:** any Wyrmroost dragon (and an egg): `WYRM NEAREST`, `WYRM HOSTILE`, `SELF WARD WYRM`, `WYRM FOLLOW`. **Made:** a dragon's weight: `touchDamage` (3) × intensity, knockback and a short slow; reaching one of your own dragons it **mends** it (`mendAmount` 2 × intensity) instead. **Laid (IMBUE):** on yourself or an ally, scaled hide (resistance and fire resistance); on a foe, the burden of scales (slowness, weakness). Imbued gear strikes with the same weight. A stranger's tamed dragon is never struck or burdened. |
| `hespon` | ROOST | Deed, State frame | Summoning | **Settle.** On a dragon of your own (or a team-mate's, `roost.respectTeams`): sits it where it is, forgetting its target; a clause at or above `roost.sleepAtIntensity` (1.5, one GREATER) also lays it to sleep when its kind sleeps. On one already at rest: **rouses** it (wakes, stands), so `WYRM ROOST` toggles and a roused dragon answers FOLLOW and SUMMON again. On a **wild** dragon the Working may control: it **hesitates** for `roost.hesitateSeconds` (5) × intensity: drops its target, slowed and weakened, the hold Vocana's COMMAND lays on a will it cannot turn. On a stranger's tamed dragon: nothing. On your own tamed wolves, cats and parrots: sits or stands them (`roost.vanillaPets`). On anything else: a harmless no-op. |
| `drakon` | DRAGONFIRE | Elder shade of FIRE | Fire | Dragon's breath: power 1.3, reach 1.5, speed 1.1, one more CP, the `inferno` trait (it bursts wide and kindles the ground around where it lands, flames that burn out and drop nothing), voiced with Wyrmroost's fire-breath sound. Heard from a Royal Red and its breath, by one who knows FIRE. |

The words are in every build of Vocana Compat (the lexicon extension is merged into the jar unconditionally), but
without Wyrmroost nothing teaches them and WYRM and ROOST have no behaviour, so they stay blank in the Grimoire. The
Grimoire-group file is empty on purpose: the words sit in the Fire and Summoning chapters rather than stealing those
chapters into a ribbon of their own.

## Interplay with Vocana's own words

- **Taming is respected.** A Wyrmroost dragon is a `TamableAnimal`, so Vocana already treats a dragon tamed by the
  caster as consenting: `SUMMON WYRM` fetches it, `WYRM FOLLOW` makes it walk (or fly) after you, `HOSTILE` never
  selects it. On top of that, `interplay.protectTamedFromCasterMagic` (on) makes a dragon tamed by the caster, or by a
  team-mate when `roost.respectTeams` is on, refuse the **magic damage** of that caster's Workings: a `FIRE CONE` through
  your own mount burns everything but it. Swords, arrows and falls still hurt it, as in Wyrmroost. The module's own
  words (WYRM, ROOST) never harm or command a dragon on the caster's side, and never touch a stranger's tamed dragon.
- **Wild dragons** follow Vocana's rules unchanged: `BIND` paid in wisps (`… OF SOUL`) is a soul-binding contest of the
  caster's Authority against the dragon's will (its max health over four), `COMMAND` likewise, and a Royal Red or an
  Alpine is a heavy will. A bound dragon then follows, guards and strikes like any bound being, and falls away when the
  binding ends. ROOST's hesitation is the cheap alternative when the contest is lost.
- **A sitting dragon does not walk.** Vocana's FOLLOW moves a mob by its navigation, which a sitting or sleeping
  Wyrmroost dragon ignores; say `WYRM ROOST` first to rouse it (or use the dragon staff). SUMMON teleports and works
  either way.
- **DRAGONFIRE** is FIRE for every rule: it blends (steam, magma, firestorm), is quenched by water and frost, and a
  Royal Red breathing fire bears FIRE while it burns.

## The Imbue Bench (`data/vocanacompat/vocana_imbue/wyrmroost/`)

Recipes follow Vocana 2.1's `docs/imbue_bench.md`. Materials are Wyrmroost's own; priorities beat the core recipes
when the material lies on the bench.

| Id | Lays | Into | Materials | Levels | Thrum | Complexity | Shade surcharge |
|---|---|---|---|---|---|---|---|
| `red_geode_fire` | FIRE and its shades (DRAGONFIRE too) | anything | 1 red geode | 4 | 25 | 5 | +2 / +10 / +2 |
| `blue_geode_cold` | FROST, WATER, LIGHTNING and shades | anything | 1 blue geode | 4 | 25 | 5 | +2 / +10 / +2 |
| `purple_geode_deep` | METAL, DECAY, VOID, SHADOW and shades | anything | 1 purple geode, 1 amethyst shard | 9 | 45 | 8 | +3 / +15 / +2 |
| `platinum_armor_metal` | METAL, STONE, WYRM and shades | armour | 1 platinum ingot, 1 amethyst shard | 6 | 30 | 6 | +3 / +15 / +2 |
| `soul_crystal_chant` | a chant up to 20 CP | anything | 1 soul crystal, 2 platinum ingots, 1 book; 4 ritual anchors within 5; a channel | 14 | 50 | 4 + CP | — |

Geode and platinum tools and armour are ordinary `SwordItem`/`DiggerItem`/`ArmorItem`s, so the bench takes them as
weapons, tools and armour; Wyrmroost's *dragon* armour (worn by dragons) is none of those and is not imbuable.

## Crafting (`data/vocanacompat/recipe/wyrmroost/`)

No new item: the module adds Wyrmroost alternatives to four of Vocana's elemental staffs, shapeless like the originals:

- Ember Staff: adept staff + red geode + blaze rod
- Rime Staff: adept staff + blue geode + packed ice
- Earth Staff: adept staff + platinum ingot + cobbled deepslate
- Shadow Staff: adept staff + purple geode + ender pearl

## Settings (`config/vocanacompat-wyrmroost.toml`)

| Key | Default | Meaning |
|---|---|---|
| `general.verboseLogging` | false | Log what the module does at INFO. |
| `words.wyrm`, `words.roost` | true | Whether WYRM / ROOST have behaviour (off: inert words). |
| `roost.hesitateSeconds` | 5 | How long a wild dragon hesitates, times intensity (clamped ×0.5–×3); 0 turns it off. |
| `roost.sleepAtIntensity` | 1.5 | From this clause intensity ROOST also puts a settled dragon to sleep. |
| `roost.vanillaPets` | true | ROOST also sits and rouses the caster's tamed wolves, cats and parrots. |
| `roost.respectTeams` | true | A dragon tamed by a scoreboard team-mate counts as the caster's own. |
| `wyrm.touchDamage` / `wyrm.touchKnockback` | 3 / 0.8 | A made WYRM's strike, times intensity. |
| `wyrm.mendsOwnDragons` / `wyrm.mendAmount` | true / 2 | A made WYRM mends the caster's own dragon instead. |
| `interplay.protectTamedFromCasterMagic` | true | Tamed dragons refuse their own caster's magic damage. |

The behaviours read these lazily, so the file can be changed and reloaded without restarting.

## Verified API (read from `NordAct/Wyrmroost`, branch `main`, 2026-10-14)

Only `src/main/java/dev/vocana/compat/wyrmroost/Dragons.java` names Wyrmroost classes:

| Used | Where read |
|---|---|
| `com.github.wolfshotz.wyrmroost.entities.dragon.AbstractDragonEntity extends TamableAnimal` | `entities/dragon/AbstractDragonEntity.java` (class declaration) |
| `setSit(boolean)` (calls `setOrderedToSit`, `setInSittingPose`, stops in place), `isSleeping()`, `setSleeping(boolean)`, `maySleep()` (abstract), `isFlying()` | same file |
| `RoyalRedEntity extends AbstractDragonEntity`, `isBreathingFire()`, spawns `FireBreathEntity` | `entities/dragon/RoyalRedEntity.java` |
| Entity ids `lesser_desertwyrm`, `overworld_drake`, `silver_glider`, `roost_stalker`, `butterfly_leviathan`, `dragon_fruit_drake`, `canari_wyvern`, `royal_red`, `coin_dragon`, `alpine`, `dragon_egg`, `fire_breath`, `wind_gust`, `*_geode_tipped_arrow`; tag `wyrmroost:dragons` (`WREntities.Tags.DRAGONS`) | `registry/WREntities.java` |
| Item ids `blue_geode`, `red_geode`, `purple_geode`, `raw_platinum`, `platinum_ingot`, `soul_crystal`, `dragon_staff`, `dragon_egg`, geode/platinum tools and armour, `*_dragon_armor`; there are **no scale items** | `registry/WRItems.java` |
| Block ids `platinum_ore`, `platinum_block`, `chiseled_platinum_block`, `raw_platinum_block`, `{blue,red,purple}_geode_ore`, `{blue,red,purple}_geode_block` | `registry/WRBlocks.java` |
| Sound `wyrmroost:breathweapon.fire` (`WRSounds.FIRE_BREATH`) | `registry/WRSounds.java` |
| `CoinDragonEntity extends Mob`, `LDWyrmEntity extends PathfinderMob` (not dragons to the API; kin by tag only) | their files |
| Ownership, sitting pose: vanilla `TamableAnimal.isTame/isOwnedBy/getOwner/isInSittingPose/setOrderedToSit` | Minecraft 1.21.1 |

`WyrmroostIds.java` lists the verified ids; the unit tests refuse any tag or recipe that names an id outside them.
The source tree is 1.4.1 while CurseForge serves 1.4.0 (`gradle.properties`); if a class above moved between the two,
`Dragons.probe()` fails in `init` and the loader drops the module without harming Vocana.

## In-game checklist (needs Wyrmroost)

1. Stand near a tamed Overworld Drake with the Chime: FOLLOW, STONE, EARTH, WYRM, ROOST answer (at most three at once).
2. Know FIRE, then listen near a Royal Red: DRAGONFIRE answers; while it breathes, FIRE and BLAZE do too.
3. `open tespe mespau close` (NEAREST WYRM: the selector stands first) with a dragon and a cow in view: the dragon is chosen.
4. `open mespau hespon close` on your own dragon: it sits; again: it stands. With `GREATER`: it sleeps.
5. On a wild Royal Red that targets you: it stops, slowed, for about five seconds, then resumes.
6. On a friend's dragon (no team): nothing happens; on the same scoreboard team: it sits.
7. `FIRE CONE` through your sitting dragon: it takes no damage; a stranger's does.
8. Bench: a red geode on the bench lets FIRE (and DRAGONFIRE, with the surcharge) into a sword; a soul crystal, two
   platinum ingots and a book with four geode blocks around the bench channel a 20-CP chant.
9. Craft an Ember Staff from an adept staff, a red geode and a blaze rod.
10. Turn `words.roost` off and reload: `hespon` does nothing; turn the module off: the words vanish from discovery.
