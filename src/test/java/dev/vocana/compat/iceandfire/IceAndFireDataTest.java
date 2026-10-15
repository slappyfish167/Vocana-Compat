package dev.vocana.compat.iceandfire;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The module's data files, read from {@code src/main/resources} (the working directory is the project): every file the
 * module ships carries both load conditions, the Create recipes have the shapes Create's own generated data uses, the
 * imbue recipes the fields Vocana's bench reads, every item the data names has a model, and the lexicon words are
 * whole. Pure file checks; Minecraft is not involved.
 */
final class IceAndFireDataTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final String MOD_LOADED = "neoforge:mod_loaded", MODULE = "vocanacompat:module_enabled", CONFIG = "vocanacompat:module_flag", NOT = "neoforge:not";
    /** The wyrmsteel set: every piece made on its own Create assembly line, and what each line starts from. */
    private static final Map<String, String> LINES = Map.ofEntries(
        Map.entry("wyrmsteel_staff", "vocana:archmage_staff"), Map.entry("wyrmsteel_sword", "minecraft:netherite_sword"),
        Map.entry("wyrmsteel_pickaxe", "minecraft:netherite_pickaxe"), Map.entry("wyrmsteel_axe", "minecraft:netherite_axe"),
        Map.entry("wyrmsteel_shovel", "minecraft:netherite_shovel"), Map.entry("wyrmsteel_hoe", "minecraft:netherite_hoe"),
        Map.entry("wyrmsteel_helmet", "minecraft:netherite_helmet"), Map.entry("wyrmsteel_chestplate", "minecraft:netherite_chestplate"),
        Map.entry("wyrmsteel_leggings", "minecraft:netherite_leggings"), Map.entry("wyrmsteel_boots", "minecraft:netherite_boots"));
    private static final List<String> DRAGON_ARMOR = List.of("dragonarmor_wyrmsteel_head", "dragonarmor_wyrmsteel_neck", "dragonarmor_wyrmsteel_body", "dragonarmor_wyrmsteel_tail");
    private static final Set<String> ITEMS = items();
    private static Set<String> items() {
        var out = new java.util.TreeSet<String>(Set.of("netherite_dragonsteel_ingot"));
        for (String line : LINES.keySet()) { out.add(line); out.add("incomplete_" + line); }
        out.addAll(DRAGON_ARMOR);
        return out;
    }

    private static JsonObject read(Path p) throws IOException {
        try (var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static List<Path> moduleFiles() throws IOException {
        try (Stream<Path> s = Files.walk(RES.resolve("data"))) {
            return s.filter(p -> p.toString().endsWith(".json") && (p.toString().contains("/iceandfire/") || p.toString().contains("/data/iceandfire/"))).sorted().toList();
        }
    }
    private static Set<String> conditionTypes(JsonObject o, String modid) {
        var types = new java.util.HashSet<String>();
        if (!o.has("neoforge:conditions")) return types;
        for (JsonElement c : o.getAsJsonArray("neoforge:conditions")) {
            var co = c.getAsJsonObject();
            String type = co.get("type").getAsString();
            if (type.equals(MOD_LOADED)) types.add(type + ":" + co.get("modid").getAsString());
            else if (type.equals(NOT)) {
                var inner = co.getAsJsonObject("value");
                assertEquals(MOD_LOADED, inner.get("type").getAsString(), "only a missing mod is negated");
                types.add(NOT + ":" + inner.get("modid").getAsString());
            }
            else if (type.equals(MODULE)) { assertEquals("iceandfire", co.get("module").getAsString()); types.add(type); }
            else if (type.equals(CONFIG)) types.add(type + ":" + co.get("key").getAsString());
            else fail("unknown condition " + type);
        }
        return types;
    }

    @Test void everyModuleDataFileCarriesBothConditions() throws IOException {
        var files = moduleFiles();
        assertFalse(files.isEmpty());
        for (Path p : files) {
            var types = conditionTypes(read(p), "iceandfire");
            assertTrue(types.contains(MOD_LOADED + ":iceandfire"), p + " needs neoforge:mod_loaded iceandfire");
            assertTrue(types.contains(MODULE), p + " needs vocanacompat:module_enabled iceandfire");
            if (p.toString().contains("/recipe/")) {
                String name = p.getFileName().toString();
                if (p.toString().contains("/recipe/iceandfire/dragonforge/")) {
                    assertTrue(types.contains(CONFIG + ":dragonForge"), p + " follows the dragon forge switch");
                    assertFalse(types.contains(MOD_LOADED + ":create"), p + ": the dragon forge needs no Create");
                } else if (p.toString().contains("/recipe/iceandfire/smithing/")) {
                    assertTrue(types.contains(NOT + ":create") && types.contains(CONFIG + ":withoutCreate"), p + " is the fallback for a game without Create");
                } else if (name.startsWith("dragonarmor_") || name.equals("netherite_dragonsteel_ingot_from_block.json")) {
                    assertFalse(types.contains(MOD_LOADED + ":create"), p + " works with or without Create");
                } else assertTrue(types.contains(MOD_LOADED + ":create"), p + ": made on Create machines");
            }
            if (p.toString().contains("/data/iceandfire/")) assertTrue(types.contains(CONFIG + ":removeSirenIslands"), p + " follows the island switch");
        }
    }

    @Test void theConfigConditionOnlyNamesSwitchesTheConfigHas() throws IOException {
        var keys = IceAndFireSwitches.ALL;
        for (Path p : moduleFiles()) for (String t : conditionTypes(read(p), "iceandfire"))
            if (t.startsWith(CONFIG + ":")) assertTrue(keys.contains(t.substring(CONFIG.length() + 1)), p + " names a switch that is not in IceAndFireConfig: " + t);
    }

    @Test void theSirenDataTargetsTheSirenAndTheIslandTag() throws IOException {
        var remove = read(RES.resolve("data/vocanacompat/neoforge/biome_modifier/iceandfire/remove_siren_spawns.json"));
        assertEquals("neoforge:remove_spawns", remove.get("type").getAsString());
        assertEquals("iceandfire:siren", remove.get("entity_types").getAsString());
        assertEquals("#minecraft:is_overworld", remove.get("biomes").getAsString());
        var island = read(RES.resolve("data/iceandfire/tags/worldgen/biome/structure_gen/siren_island.json"));
        assertTrue(island.get("replace").getAsBoolean() && island.getAsJsonArray("values").isEmpty(), "the island tag is emptied");
    }

    @Test void theCreateRecipesHaveCreatesShapes() throws IOException {
        var mixing = read(RES.resolve("data/vocanacompat/recipe/iceandfire/netherite_dragonsteel_ingot.json"));
        assertEquals("create:mixing", mixing.get("type").getAsString());
        assertEquals("superheated", mixing.get("heat_requirement").getAsString());
        assertEquals(2, mixing.getAsJsonArray("ingredients").size(), "netherite and one dragonsteel ingot");
        assertEquals("vocanacompat:netherite_dragonsteel_ingot", mixing.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
        for (var entry : LINES.entrySet()) {
            String line = entry.getKey();
            var r = read(RES.resolve("data/vocanacompat/recipe/iceandfire/" + line + ".json"));
            assertEquals("create:sequenced_assembly", r.get("type").getAsString(), line);
            assertEquals(entry.getValue(), r.getAsJsonObject("ingredient").get("item").getAsString(), line + " is reforged from its counterpart");
            String transitional = r.getAsJsonObject("transitional_item").get("id").getAsString();
            assertEquals("vocanacompat:incomplete_" + line, transitional, line + " rides its own incomplete piece");
            assertTrue(r.get("loops").getAsInt() >= 2 && r.get("loops").getAsInt() <= 3, line + " loops two or three times");
            assertEquals("vocanacompat:" + line, r.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
            JsonArray steps = r.getAsJsonArray("sequence");
            assertTrue(steps.size() >= 3, line + " has at least three steps");
            boolean pressed = false, alloyed = false;
            for (JsonElement s : steps) {
                var step = s.getAsJsonObject();
                String type = step.get("type").getAsString();
                assertTrue(Set.of("create:deploying", "create:pressing").contains(type), line + ": " + type);
                var ingredients = step.getAsJsonArray("ingredients");
                assertEquals(transitional, ingredients.get(0).getAsJsonObject().get("item").getAsString(), line + ": every step starts from the incomplete piece");
                assertEquals(transitional, step.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString(), line + ": every step hands it on");
                if (type.equals("create:pressing")) { pressed = true; assertEquals(1, ingredients.size()); }
                else { assertEquals(2, ingredients.size()); var extra = ingredients.get(1); if (extra.isJsonObject() && "vocanacompat:netherite_dragonsteel_ingot".equals(extra.getAsJsonObject().get("item").getAsString())) alloyed = true; }
            }
            assertTrue(pressed && alloyed, line + " is pressed and takes the alloy");
        }
        var block = read(RES.resolve("data/vocanacompat/recipe/iceandfire/wyrmsteel_block.json"));
        assertEquals("create:compacting", block.get("type").getAsString(), "the block is packed on a press");
        assertEquals(9, block.getAsJsonArray("ingredients").size());
        for (JsonElement i : block.getAsJsonArray("ingredients")) assertEquals("vocanacompat:netherite_dragonsteel_ingot", i.getAsJsonObject().get("item").getAsString());
        assertEquals("vocanacompat:wyrmsteel_block", block.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
        try (Stream<Path> s = Files.list(RES.resolve("data/vocanacompat/recipe/iceandfire"))) {
            for (Path p : s.filter(f -> f.toString().endsWith(".json")).toList()) {
                var r = read(p);
                if (!r.get("type").getAsString().startsWith("minecraft:crafting")) continue;
                // The one crafting-table recipe unpacks the block: it makes no wyrmsteel that was not already there.
                assertEquals("minecraft:crafting_shapeless", r.get("type").getAsString(), p + ": no crafting-table recipe makes wyrmsteel");
                assertEquals(1, r.getAsJsonArray("ingredients").size(), p.toString());
                assertEquals("vocanacompat:wyrmsteel_block", r.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString(), p.toString());
                assertEquals("vocanacompat:netherite_dragonsteel_ingot", r.getAsJsonObject("result").get("id").getAsString(), p.toString());
                assertEquals(9, r.getAsJsonObject("result").get("count").getAsInt(), p.toString());
            }
        }
    }

    @Test void theDragonForgeAlloysWyrmsteelInEachKindOfForge() throws IOException {
        for (String kind : List.of("fire", "ice", "lightning")) {
            var r = read(RES.resolve("data/vocanacompat/recipe/iceandfire/dragonforge/netherite_dragonsteel_ingot_" + kind + ".json"));
            assertEquals("iceandfire:dragonforge", r.get("type").getAsString());
            assertEquals(kind, r.get("dragonType").getAsString(), "Ice and Fire's forge ids are fire, ice and lightning");
            assertTrue(r.get("cookTime").getAsInt() > 1000, "slower than a dragonsteel ingot (1000)");
            assertEquals("c:ingots/netherite", r.getAsJsonObject("input").get("tag").getAsString());
            assertEquals("iceandfire:dragonsteel_" + kind + "_ingot", r.getAsJsonObject("blood").get("item").getAsString(), "the forge's own kind of dragonsteel");
            var result = r.getAsJsonObject("result");
            assertEquals("vocanacompat:netherite_dragonsteel_ingot", result.get("id").getAsString());
            assertFalse(result.has("count") && result.get("count").getAsInt() != 1, "one ingot; the Create mixer gives two");
        }
    }

    @Test void withoutCreateTheSmithingTableMakesTheSet() throws IOException {
        for (var entry : LINES.entrySet()) {
            var r = read(RES.resolve("data/vocanacompat/recipe/iceandfire/smithing/" + entry.getKey() + ".json"));
            assertEquals("minecraft:smithing_transform", r.get("type").getAsString());
            assertEquals("minecraft:netherite_upgrade_smithing_template", r.getAsJsonObject("template").get("item").getAsString());
            assertEquals(entry.getValue(), r.getAsJsonObject("base").get("item").getAsString(), entry.getKey() + " upgrades the piece its assembly line starts from");
            assertEquals("vocanacompat:netherite_dragonsteel_ingot", r.getAsJsonObject("addition").get("item").getAsString());
            assertEquals("vocanacompat:" + entry.getKey(), r.getAsJsonObject("result").get("id").getAsString());
        }
        var block = read(RES.resolve("data/vocanacompat/recipe/iceandfire/smithing/wyrmsteel_block.json"));
        assertEquals("minecraft:crafting_shaped", block.get("type").getAsString(), "without a press the crafting table packs the block");
        assertEquals("vocanacompat:wyrmsteel_block", block.getAsJsonObject("result").get("id").getAsString());
        assertEquals("vocanacompat:netherite_dragonsteel_ingot", block.getAsJsonObject("key").getAsJsonObject("#").get("item").getAsString());
        assertEquals(List.of("###", "###", "###"), block.getAsJsonArray("pattern").asList().stream().map(JsonElement::getAsString).toList());
    }

    @Test void wyrmsteelDragonArmourIsSmithedFromDragonsteelDragonArmour() throws IOException {
        var lang = read(RES.resolve("assets/vocanacompat/lang/en_us.json"));
        assertTrue(lang.has("item.vocanacompat.dragonarmor_wyrmsteel") && lang.has("item.vocanacompat.dragonarmor_wyrmsteel.tooltip"), "one name for the four parts, and the tooltip");
        for (String part : List.of("head", "neck", "body", "tail")) {
            var r = read(RES.resolve("data/vocanacompat/recipe/iceandfire/dragonarmor_wyrmsteel_" + part + ".json"));
            assertEquals("minecraft:smithing_transform", r.get("type").getAsString());
            var bases = new java.util.HashSet<String>();
            for (JsonElement b : r.getAsJsonArray("base")) bases.add(b.getAsJsonObject().get("item").getAsString());
            assertEquals(Set.of("iceandfire:dragonarmor_dragon_steel_fire_" + part, "iceandfire:dragonarmor_dragon_steel_ice_" + part, "iceandfire:dragonarmor_dragon_steel_lightning_" + part), bases,
                part + " starts from any dragonsteel dragon armour of the same part (Ice and Fire's ids)");
            assertEquals("vocanacompat:wyrmsteel_block", r.getAsJsonObject("addition").get("item").getAsString());
            assertEquals("vocanacompat:dragonarmor_wyrmsteel_" + part, r.getAsJsonObject("result").get("id").getAsString());
        }
        var breath = ids(read(RES.resolve("data/vocanacompat/tags/damage_type/iceandfire/dragon_breath.json")));
        for (String id : List.of("iceandfire:dragon_fire", "iceandfire:dragon_ice", "iceandfire:dragon_lightning", "#minecraft:is_fire", "#minecraft:is_freezing", "#minecraft:is_lightning"))
            assertTrue(breath.contains(id), "dragon breath includes " + id);
    }

    @Test void theBlockHasItsLootModelsAndTags() throws IOException {
        var loot = read(RES.resolve("data/vocanacompat/loot_table/blocks/wyrmsteel_block.json"));
        var types = conditionTypes(loot, "iceandfire");
        assertTrue(types.contains(MOD_LOADED + ":iceandfire") && types.contains(MODULE), "the loot table is the module's");
        assertEquals("vocanacompat:wyrmsteel_block", loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
        var state = read(RES.resolve("assets/vocanacompat/blockstates/wyrmsteel_block.json"));
        assertEquals("vocanacompat:block/wyrmsteel_block", state.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        var model = read(RES.resolve("assets/vocanacompat/models/block/wyrmsteel_block.json"));
        assertEquals("vocanacompat:block/iceandfire/wyrmsteel_block", model.getAsJsonObject("textures").get("all").getAsString());
        assertTrue(Files.exists(RES.resolve("assets/vocanacompat/textures/block/iceandfire/wyrmsteel_block.png")));
        assertEquals("vocanacompat:block/wyrmsteel_block", read(RES.resolve("assets/vocanacompat/models/item/wyrmsteel_block.json")).get("parent").getAsString());
        assertTrue(read(RES.resolve("assets/vocanacompat/lang/en_us.json")).has("block.vocanacompat.wyrmsteel_block"));
        for (String tag : List.of("minecraft/tags/block/mineable/pickaxe", "minecraft/tags/block/needs_diamond_tool", "minecraft/tags/block/beacon_base_blocks",
                                  "vocanacompat/tags/block/iceandfire/ritual_anchors", "vocanacompat/tags/block/iceandfire/metal"))
            assertTrue(ids(read(RES.resolve("data/" + tag + ".json"))).contains("vocanacompat:wyrmsteel_block"), tag + " holds the block");
        assertTrue(ids(read(RES.resolve("data/vocana/tags/block/ritual_anchors.json"))).contains("#vocanacompat:iceandfire/ritual_anchors"), "the anchors reach Vocana's tag");
    }

    @Test void theGearIsInTheVanillaTagsThatMakeItEnchantable() throws IOException {
        var expected = Map.of("swords", "sword", "pickaxes", "pickaxe", "axes", "axe", "shovels", "shovel", "hoes", "hoe",
            "head_armor", "helmet", "chest_armor", "chestplate", "leg_armor", "leggings", "foot_armor", "boots");
        for (var e : expected.entrySet()) {
            var tag = read(RES.resolve("data/minecraft/tags/item/" + e.getKey() + ".json"));
            assertTrue(ids(tag).contains("vocanacompat:wyrmsteel_" + e.getValue()), "minecraft:" + e.getKey() + " holds the wyrmsteel " + e.getValue());
        }
        var trims = ids(read(RES.resolve("data/minecraft/tags/item/trimmable_armor.json")));
        for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) assertTrue(trims.contains("vocanacompat:wyrmsteel_" + piece), piece + " takes armour trims");
        // Shared tags carry no module condition (other modules may add to them): every line is optional instead, and
        // the module's items exist only when it loaded.
        for (String dir : List.of("data/minecraft/tags", "data/c/tags")) {
            try (Stream<Path> s = Files.walk(RES.resolve(dir))) {
                for (Path p : s.filter(f -> f.toString().endsWith(".json")).toList()) {
                    var o = read(p);
                    assertFalse(o.get("replace").getAsBoolean(), p + " never replaces the tag");
                    for (JsonElement v : o.getAsJsonArray("values")) assertFalse(v.getAsJsonObject().get("required").getAsBoolean(), p + ": every line is optional");
                }
            }
        }
    }

    private static Set<String> ids(JsonObject tag) {
        var out = new java.util.HashSet<String>();
        for (JsonElement v : tag.getAsJsonArray("values")) out.add(v.isJsonObject() ? v.getAsJsonObject().get("id").getAsString() : v.getAsString());
        return out;
    }

    @Test void theImbueRecipesHaveTheBenchsFields() throws IOException {
        try (Stream<Path> s = Files.list(RES.resolve("data/vocanacompat/vocana_imbue/iceandfire"))) {
            var files = s.toList();
            assertTrue(files.size() >= 6);
            for (Path p : files) {
                var o = read(p);
                assertTrue(o.has("target") && o.has("essence") && o.has("materials") && o.has("xp_levels") && o.has("thrum") && o.has("complexity"), p + " has the bench's fields");
                assertTrue(Set.of("weapon", "armor", "tool", "staff", "any").contains(o.getAsJsonObject("target").get("kind").getAsString()), p + ": known kind");
                for (JsonElement m : o.getAsJsonArray("materials")) { var mo = m.getAsJsonObject(); assertTrue(mo.has("item") || mo.has("tag"), p + ": a material names an item or a tag"); }
                if (o.has("triggers")) for (JsonElement t : o.getAsJsonArray("triggers")) assertTrue(Set.of("ON_HIT", "ON_HURT", "PASSIVE", "ON_USE", "ON_EQUIP").contains(t.getAsString()), p + ": known trigger");
            }
        }
    }

    @Test void everyItemHasAModelATextureAndAName() throws IOException {
        var lang = read(RES.resolve("assets/vocanacompat/lang/en_us.json"));
        for (String item : ITEMS) {
            var model = read(RES.resolve("assets/vocanacompat/models/item/" + item + ".json"));
            String texture = model.getAsJsonObject("textures").get("layer0").getAsString();
            assertEquals("vocanacompat:item/iceandfire/" + item, texture);
            assertTrue(Files.exists(RES.resolve("assets/vocanacompat/textures/item/iceandfire/" + item + ".png")), item + " has its texture");
            assertTrue(lang.has("item.vocanacompat." + (item.startsWith("dragonarmor_wyrmsteel_") ? "dragonarmor_wyrmsteel" : item)), item + " has a name");
        }
    }

    @Test void theSharedHearsTagsOnlyAddOneOptionalLineEach() throws IOException {
        for (String kind : List.of("entity_type", "block")) {
            try (Stream<Path> s = Files.list(RES.resolve("data/vocana/tags/" + kind + "/hears"))) {
                for (Path p : s.toList()) {
                    var o = read(p);
                    assertFalse(o.get("replace").getAsBoolean(), p + " never replaces Vocana's tag");
                    for (JsonElement v : o.getAsJsonArray("values")) {
                        var vo = v.getAsJsonObject();
                        assertFalse(vo.get("required").getAsBoolean(), p + ": module references are optional");
                        String id = vo.get("id").getAsString();
                        if (id.startsWith("#vocanacompat:iceandfire/")) {
                            String gloss = id.substring("#vocanacompat:iceandfire/".length());
                            assertTrue(Files.exists(RES.resolve("data/vocanacompat/tags/" + kind + "/iceandfire/" + gloss + ".json")), p + " points at a module tag that exists");
                        }
                    }
                }
            }
        }
    }

    @Test void theLexiconWordsAreWholeElderShades() throws IOException {
        var o = read(Path.of("src/main/lexicon/iceandfire.json"));
        var elder = o.getAsJsonArray("elder");
        assertEquals(2, elder.size());
        for (JsonElement w : elder) {
            var word = w.getAsJsonObject();
            for (String key : List.of("form", "gloss", "class", "variant_of", "respelling", "ipa", "chapter", "profile", "meaning", "beginner_clue")) assertTrue(word.has(key), word.get("gloss") + " has " + key);
            assertEquals("Elder", word.get("class").getAsString());
            assertEquals(word.get("variant_of").getAsString(), word.get("chapter").getAsString(), "a shade sits in its base's chapter");
            assertTrue(Set.of("FROST", "LIGHTNING").contains(word.get("variant_of").getAsString()));
            var profile = word.getAsJsonObject("profile");
            assertTrue(profile.has("power") && profile.has("trait") && profile.has("particle") && profile.has("sound"));
            String gloss = word.get("gloss").getAsString().toLowerCase();
            assertTrue(Files.exists(RES.resolve("data/vocanacompat/tags/entity_type/iceandfire/" + gloss + ".json")), gloss + " can be heard from a creature");
            assertTrue(Files.exists(RES.resolve("data/vocana/tags/entity_type/hears/" + gloss + ".json")), gloss + " is referenced from Vocana's hears tag");
        }
    }
}
