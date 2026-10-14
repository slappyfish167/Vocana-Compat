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
    private static final String MOD_LOADED = "neoforge:mod_loaded", MODULE = "vocanacompat:module_enabled", CONFIG = "vocanacompat:iceandfire/config";
    private static final Set<String> ITEMS = Set.of("netherite_dragonsteel_ingot", "wyrmsteel_staff", "wyrmsteel_sword", "wyrmsteel_pickaxe",
        "incomplete_wyrmsteel_staff", "incomplete_wyrmsteel_sword", "incomplete_wyrmsteel_pickaxe");

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
            if (p.toString().contains("/recipe/")) assertTrue(types.contains(MOD_LOADED + ":create"), p + ": wyrmsteel is made on Create machines only");
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
        for (String line : List.of("wyrmsteel_staff", "wyrmsteel_sword", "wyrmsteel_pickaxe")) {
            var r = read(RES.resolve("data/vocanacompat/recipe/iceandfire/" + line + ".json"));
            assertEquals("create:sequenced_assembly", r.get("type").getAsString(), line);
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
        try (Stream<Path> s = Files.list(RES.resolve("data/vocanacompat/recipe/iceandfire"))) {
            for (Path p : s.toList()) assertNotEquals("minecraft:crafting_shaped", read(p).get("type").getAsString(), p + ": no crafting-table recipe for wyrmsteel");
        }
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
            assertTrue(lang.has("item.vocanacompat." + item), item + " has a name");
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
