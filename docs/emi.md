# The EMI module

Shows Vocana inside EMI (1.1.24+1.21.1): the Imbue Bench's recipes, the rituals around the bench, and the words of the
language as the player comes to hear them. Package `dev.vocana.compat.emi`; toggle `modules.emi.enabled`; settings in
`config/vocanacompat-emi.toml`. EMI is a client mod, so almost everything here is client-side; the one thing the module
does on a server is hand the bench's recipes to clients (below).

## What it adds

| Category (`emi.category.vocanacompat.*`) | Workstations | Pages |
|---|---|---|
| `vocanacompat:imbue` Imbue Bench | the Imbue Bench | one per `ImbueRecipe` the client can see: the target gear (an item, a tag, or one piece of each kind the recipe takes) → a preview of the piece imbued (its tooltip is Vocana's, read from the `vocana:imbues` component set on the preview); the materials below, ritual catalyst blocks at the right as catalysts; then what it lays in, the price (levels, Thrum, strength, complexity), the gear kind and triggers, the Elder-shade surcharge, each catalyst's count and radius, and the channel step |
| `vocanacompat:ritual` Rituals and the Bench | the Imbue Bench | the ritual figure (anchors of `vocana:ritual_anchors`, +8 Capacity and −5% Strain gain each, Ritual tier), channelling at the bench, stripping an imbue (imbued piece → Essence Residue + the clean piece, `bench.stripXp` levels) |
| `vocanacompat:lexicon` Vocana Lexicon | the Tuning Chime and every staff | a proficiency page (the five levels with this server's thresholds from `masteryCasts`, the Thrum/Strain/voicing factors, the player's practised chants) and one page per word: form and respelling, class, chapter and Grimoire group, meaning when the server reveals meanings, an Elder word's base, the chant bound to it (`Calls: …`) and the practised chants that say it |

Residues are told apart by the essence they carry (`setDefaultComparison` on `vocana:essence_residue` with
`Comparison.compareComponents()`), so a recipe asking for "a residue of fire" shows that residue.

### What the player has heard

EMI builds its recipes once per world join, on its reload thread, before the player has opened anything; Vocana's
client only learns what the player has heard from the snapshot the server sends when the Chime or Grimoire is opened
(and every few ticks while a session is live), never on login. So the lexicon pages do not decide anything at
registration: every word gets a page whose id is its position in the lexicon (`vocanacompat:lexicon/word/<n>`, never
its form), and the page is drawn from `ClientState` each frame:

- before any snapshot: the word's chapter and group, and "Open the Tuning Chime or your Grimoire once…";
- unheard: the chapter and group and the Grimoire's own hint of where such words answer (deep words, grammar words,
  Primes, Callings have their own), exactly as the Grimoire's blank slots do (Canon §7.2);
- heard: the leaf described above. Meanings follow `ClientState.meanings()` (the server's `grimoire.revealMeanings`).

The same goes for an imbue recipe's "Lays in" line: an essence is named by its form and respelling once heard, else by
its gloss as plain words ("fire") marked as a form not yet heard. The gloss is the recipe's own data (`"essence":
"FIRE"`), so nothing beyond what the recipe file says is revealed. The pages catch up the moment a new snapshot
arrives; no EMI reload is needed.

### Where the imbue recipes come from

Vocana loads `data/<ns>/vocana_imbue/*.json` in `dev.vocana.server.ImbueRecipes`, a **server** reload listener
(`AddReloadListenerEvent`), and never sends the list to clients: the bench screen only receives what the server
decides about the piece on the bench. So:

- **Single player (or hosting a LAN world)**: the integrated server fills `ImbueRecipes.all()` in the same JVM, and the
  plugin reads it directly.
- **Dedicated server with this module installed there too** (EMI must be present on the server for the module to load;
  EMI runs fine server-side): on every `OnDatapackSyncEvent` (a player joining, `/reload`) the server sends the loaded
  recipes as JSON (`ImbueJson` writes the exact shape `ImbueRecipe.parse` reads; the GameTests check the round trip)
  in the optional payload `vocanacompat:imbue_recipes`. NeoForge fires that event before the vanilla recipe packet that
  makes EMI reload, so EMI sees the list on its first pass. `syncRecipes` in the server's config turns it off.
- **Dedicated server without it**: the category holds one page saying so. The client never shows another server's
  list: the synced recipes are dropped on logout, and Vocana's static map is read only when a local server runs.

Everything a recipe needs is in its JSON (item and tag ids, catalyst blocks), so the client resolves them against its
own registries and tags; a recipe whose target tag is empty on the client falls back to its kind's pieces.

## Config (`config/vocanacompat-emi.toml`)

| Key | Default | Side | Meaning |
|---|---|---|---|
| `categories.imbue` | true | client | the Imbue Bench category |
| `categories.ritual` | true | client | the ritual category |
| `categories.lexicon` | true | client | the lexicon category |
| `categories.proficiency` | true | client | the proficiency page inside it |
| `general.syncRecipes` | true | server | send imbue recipes to joining clients |
| `general.verboseLogging` | false | both | log at INFO |

The category toggles are read when EMI reloads (rejoin the world after changing them).

## Code map

- `EmiCompat`: entry; registers the config, the sync payload and its server listener; calls `EmiClient.init()` on the
  client only (`FMLEnvironment.dist`). Nothing in it, `EmiConfig`, `ImbueJson`, `ImbueRecipeSync` or `EmiText` imports EMI.
- `VocanaEmiPlugin` (`@EmiEntrypoint`): EMI finds it in this jar's annotation scan data
  (`EmiAgnosNeoForge.getPluginsAgnos`, EMI branch `1.21`) and instantiates it on the client; `register` builds the
  three categories.
- `ImbueEmiRecipe`, `WordEmiRecipe`, `ProficiencyEmiRecipe`, `TextPage` (+ `RitualPages`): the pages. `Heard`: the
  snapshot index; `Gear`: representative stacks, imbued previews, tag resolution.
- `gametest/EmiGameTests`: the module does not load without EMI; the JSON round trip; `EmiText` agrees with
  `Mastery`. `src/test/.../EmiTextTest`: the pure formatting.

## EMI API used (all read from `emilyploszaj/emi`, branch `1.21`, `xplat/src/main/java/dev/emi/emi/api/`)

`EmiPlugin.register(EmiRegistry)`; `EmiRegistry.addCategory`, `addWorkstation(EmiRecipeCategory, EmiIngredient)`,
`addRecipe`, `setDefaultComparison(Object key, Comparison)`; `EmiRecipeCategory(Identifier, EmiRenderable[,
EmiRenderable, Comparator<EmiRecipe>])` with an `EmiStack` as the renderable, named by `emi.category.<ns>.<path>`;
`EmiRecipe` (`getCategory`, `getId`, `getInputs`, `getCatalysts`, `getOutputs`, `getDisplayWidth/Height`,
`addWidgets`, `supportsRecipeTree`, `getBackingRecipe`); `EmiStack.of(ItemStack)`, `of(ItemLike[, long])`,
`EmiStack.EMPTY`; `EmiIngredient.of(TagKey)`, `of(List, long)`, `isEmpty`; `Comparison.compareComponents()`;
`WidgetHolder.addSlot`, `addTexture(EmiTexture, x, y)`, `addText(FormattedCharSequence, x, y, colour, shadow)`,
`addDrawable`; `SlotWidget.catalyst`, `drawBack`, `recipeContext`, `appendTooltip(Component)`;
`EmiTexture.EMPTY_ARROW`. The NeoForge artifact is remapped to Mojang names, so EMI's `Identifier`/`Text`/
`DrawContext`/`OrderedText` are `ResourceLocation`/`Component`/`GuiGraphics`/`FormattedCharSequence` here. The `:api`
jar holds `dev/emi/emi/api/**` only; nothing outside it (such as `EmiRenderHelper`) is referenced.
