# Create recipe templates (for sibling modules)

The shapes Create 6.0.x (`mc1.21.1/dev`) reads, as JSON any module can copy, with the conditions every file in this
repository carries. The Java builders in `dev.vocana.compat.create.CreateRecipes` write the same shapes (pure Gson, no
Minecraft, so a unit test can use them); the shipped examples are `data/vocanacompat/recipe/create/torc.json` and
`tuning_chime_mixing.json`. Verified against Create's generated data and codecs: `ProcessingRecipeParams`
(`ingredients`, `results`, `processing_time`, `heat_requirement`), `ProcessingOutput` (`id`, `count`, `chance`,
`components`), `SequencedAssemblyRecipeSerializer` (`ingredient`, `transitional_item`, `sequence`, `results`, `loops`),
`ItemApplicationRecipeParams` (`keep_held_item`), `HeatCondition` (`none`, `heated`, `superheated`).

## Conditions

Put this first in every file. A module that also needs its own mod adds a third `neoforge:mod_loaded`; a file that
belongs to the Ice and Fire module uses `"module": "iceandfire"` in place of `create` *and* keeps the `create`
`mod_loaded` (the recipe type does not exist without Create).

```json
"neoforge:conditions": [
  { "type": "neoforge:mod_loaded", "modid": "create" },
  { "type": "neoforge:mod_loaded", "modid": "iceandfire" },
  { "type": "vocanacompat:module_enabled", "module": "iceandfire" }
]
```

## `create:mixing` — alloying in a basin

`heat_requirement`: omit (or `"none"`) for a cold mix, `"heated"` for any lit burner (kindled or seething; a
*smouldering* one is not enough), `"superheated"` for a seething burner only (blaze cake). Ingredients are items
(`item`/`tag`) or fluids (`{"type": "neoforge:single", "amount": <mB>, "fluid": "<id>"}`); results likewise. `count`
and `chance` default to 1; `processing_time` is optional (mixing ignores it).

Netherite dragonsteel, superheated, from Create's brass example (`mixing/brass_ingot.json`):

```json
{
  "neoforge:conditions": [
    { "type": "neoforge:mod_loaded", "modid": "create" },
    { "type": "neoforge:mod_loaded", "modid": "iceandfire" },
    { "type": "vocanacompat:module_enabled", "module": "iceandfire" }
  ],
  "type": "create:mixing",
  "heat_requirement": "superheated",
  "ingredients": [
    { "tag": "c:ingots/netherite" },
    { "item": "iceandfire:dragonsteel_fire_ingot" },
    { "item": "iceandfire:dragonsteel_fire_ingot" }
  ],
  "results": [
    { "id": "iceandfire:<netherite_dragonsteel_ingot>", "count": 2 }
  ]
}
```

(Replace the Ice and Fire ids with the real ones from `IafItems`; the recipe file name is free, e.g.
`data/vocanacompat/recipe/iceandfire/netherite_dragonsteel_mixing.json`.)

## `create:pressing` — one item under a mechanical press

```json
{ "type": "create:pressing", "ingredients": [ { "tag": "c:ingots/brass" } ], "results": [ { "id": "create:brass_sheet" } ] }
```

## `create:deploying` — a deployer applies an item to another

The first ingredient is the item on the belt or depot, the second the one in the deployer's hand (consumed unless
`"keep_held_item": true`, which is how sand paper and tools are used).

```json
{
  "type": "create:deploying",
  "ingredients": [ { "item": "minecraft:iron_ingot" }, { "item": "create:brass_sheet" } ],
  "results": [ { "id": "<result>" } ]
}
```

## `create:item_application` — the player (or a deployer) applies an item to a placed block

```json
{
  "type": "create:item_application",
  "ingredients": [ { "tag": "c:stripped_logs" }, { "item": "create:andesite_alloy" } ],
  "results": [ { "id": "create:andesite_casing" } ]
}
```

## `create:sequenced_assembly` — an assembly line

`ingredient` starts the line and becomes `transitional_item`; the `sequence` runs in order `loops` times (default 1);
then one of `results` is drawn by weight (`chance` values are weights here, not probabilities). **Every step's first
ingredient and its result must be the transitional item.** Steps may be `create:pressing`, `create:deploying`,
`create:filling` (a fluid as second ingredient) or `create:cutting`. The transitional item should be an item of
Create's `SequencedAssemblyItem` class so the progress bar and step name show: register one in your module package
(`DeferredRegister.Items`, `new SequencedAssemblyItem(new Item.Properties())`) with a model that borrows the finished
item's texture, as `CreateItems.INCOMPLETE_TORC` does. Any plain item works without the tooltip.

A dragonsteel tool line, from Create's `sequenced_assembly/precision_mechanism.json` and `sturdy_sheet.json`:

```json
{
  "neoforge:conditions": [
    { "type": "neoforge:mod_loaded", "modid": "create" },
    { "type": "neoforge:mod_loaded", "modid": "iceandfire" },
    { "type": "vocanacompat:module_enabled", "module": "iceandfire" }
  ],
  "type": "create:sequenced_assembly",
  "ingredient": { "item": "iceandfire:<netherite_dragonsteel_ingot>" },
  "loops": 3,
  "results": [
    { "chance": 40.0, "id": "iceandfire:<netherite_dragonsteel_sword>" },
    { "chance": 3.0, "id": "iceandfire:<netherite_dragonsteel_ingot>" },
    { "id": "minecraft:netherite_scrap" }
  ],
  "sequence": [
    {
      "type": "create:pressing",
      "ingredients": [ { "item": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" } ],
      "results": [ { "id": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" } ]
    },
    {
      "type": "create:deploying",
      "ingredients": [ { "item": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" }, { "item": "iceandfire:<dragon_bone>" } ],
      "results": [ { "id": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" } ]
    },
    {
      "type": "create:filling",
      "ingredients": [
        { "item": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" },
        { "type": "neoforge:single", "amount": 250, "fluid": "minecraft:lava" }
      ],
      "results": [ { "id": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" } ]
    }
  ],
  "transitional_item": { "id": "vocanacompat:iceandfire/incomplete_dragonsteel_blade" }
}
```

With the builders, the same file is

```java
var t = "vocanacompat:iceandfire/incomplete_dragonsteel_blade";
JsonObject recipe = CreateRecipes.conditioned(CreateRecipes.sequencedAssembly("iceandfire:<ingot>", t, 3,
    List.of(CreateRecipes.pressingStep(t), CreateRecipes.deployingStep(t, "iceandfire:<dragon_bone>"), CreateRecipes.fillingStep(t, "minecraft:lava", 250)),
    List.of(CreateRecipes.output("iceandfire:<sword>", 1, 40), CreateRecipes.output("iceandfire:<ingot>", 1, 3), CreateRecipes.output("minecraft:netherite_scrap"))),
    "iceandfire");
```

(`conditioned` writes the `create` module's conditions; an Ice and Fire file edits the `module` value to `iceandfire`
or writes its own conditions array.)

## Heat for the Ice and Fire module

Fire dragon's fire on a burner is Create's business, not ours: a seething burner needs a blaze cake (or
`create:superheated_blaze_burner_fuels`). The Create module's SPIN with LIGHTNING in the chant seethes a burner for
Create's own 160 s, which is one way for a caster to run a superheated alloying step without cake. Ice dragons have
no equivalent; `"heated"` recipes accept any kindled burner.
