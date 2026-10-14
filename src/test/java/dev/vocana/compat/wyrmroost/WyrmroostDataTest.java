package dev.vocana.compat.wyrmroost;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The module's data, read from {@code src/main/resources} without Minecraft: every file is valid JSON with both
 * conditions, names only ids verified against Wyrmroost's registries ({@link WyrmroostIds}), and every shared
 * {@code vocana:hears} reference has the module tag it points at. Glosses are checked against Vocana's core lexicon
 * when the Vocana jar is on the test classpath, and against the module's own words otherwise.
 */
final class WyrmroostDataTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final Path DATA = RES.resolve("data/vocanacompat");

    private static JsonObject read(Path p) throws IOException {
        try (var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static List<Path> files(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> s = Files.walk(dir)) { return s.filter(p -> p.toString().endsWith(".json")).sorted().toList(); }
    }
    private static List<Path> moduleFiles() throws IOException {
        var out = new java.util.ArrayList<Path>();
        for (String kind : List.of("tags/entity_type", "tags/block", "vocana_imbue", "recipe")) out.addAll(files(DATA.resolve(kind).resolve("wyrmroost")));
        return out;
    }
    private static void assertConditions(Path p, JsonObject o) {
        var c = o.getAsJsonArray("neoforge:conditions");
        assertNotNull(c, p + " carries neoforge:conditions");
        Set<String> types = new HashSet<>();
        for (JsonElement e : c) {
            var cond = e.getAsJsonObject(); types.add(cond.get("type").getAsString());
            if (cond.get("type").getAsString().equals("neoforge:mod_loaded")) assertEquals("wyrmroost", cond.get("modid").getAsString(), p.toString());
            if (cond.get("type").getAsString().equals("vocanacompat:module_enabled")) assertEquals("wyrmroost", cond.get("module").getAsString(), p.toString());
        }
        assertEquals(Set.of("neoforge:mod_loaded", "vocanacompat:module_enabled"), types, p + " carries both conditions");
    }
    private static String idOf(JsonElement value) { return value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString(); }

    @Test void everyModuleFileIsConditionalOnTheModAndTheModule() throws IOException {
        var all = moduleFiles();
        assertFalse(all.isEmpty(), "the module ships data");
        for (Path p : all) assertConditions(p, read(p));
    }

    @Test void tagsNameOnlyVerifiedWyrmroostIdsAndMarkThemOptional() throws IOException {
        for (Path p : files(DATA.resolve("tags/entity_type/wyrmroost"))) checkTag(p, WyrmroostIds.ENTITIES);
        for (Path p : files(DATA.resolve("tags/block/wyrmroost"))) checkTag(p, WyrmroostIds.BLOCKS);
    }
    private static void checkTag(Path p, Set<String> universe) throws IOException {
        var o = read(p);
        assertFalse(o.get("replace").getAsBoolean(), p + " never replaces");
        JsonArray values = o.getAsJsonArray("values");
        assertTrue(values.size() > 0, p + " lists something");
        for (JsonElement v : values) {
            assertTrue(v.isJsonObject() && !v.getAsJsonObject().get("required").getAsBoolean(), p + ": every entry is optional: " + v);
            String id = idOf(v);
            assertTrue(id.startsWith("wyrmroost:"), p + ": " + id + " is a Wyrmroost id");
            assertTrue(universe.contains(id.substring("wyrmroost:".length())), p + ": " + id + " was verified in Wyrmroost's registries");
        }
    }

    @Test void recipesNameOnlyVerifiedItems() throws IOException {
        Set<String> items = new HashSet<>(WyrmroostIds.ITEMS); items.addAll(WyrmroostIds.BLOCKS);
        for (Path p : files(DATA.resolve("vocana_imbue/wyrmroost"))) {
            var o = read(p);
            assertTrue(o.has("target") && (o.has("essence") || o.has("chant")), p + " has a target and an essence or chant");
            for (JsonElement m : o.getAsJsonArray("materials")) if (m.getAsJsonObject().has("item")) checkItem(p, m.getAsJsonObject().get("item").getAsString(), items);
        }
        for (Path p : files(DATA.resolve("recipe/wyrmroost"))) {
            var o = read(p);
            assertEquals("minecraft:crafting_shapeless", o.get("type").getAsString(), p.toString());
            for (JsonElement i : o.getAsJsonArray("ingredients")) checkItem(p, i.getAsJsonObject().get("item").getAsString(), items);
            assertTrue(o.getAsJsonObject("result").get("id").getAsString().startsWith("vocana:"), p + " makes a Vocana staff");
        }
    }
    private static void checkItem(Path p, String id, Set<String> items) {
        if (id.startsWith("wyrmroost:")) assertTrue(items.contains(id.substring("wyrmroost:".length())), p + ": " + id + " was verified in WRItems/WRBlocks");
        else assertTrue(id.startsWith("minecraft:") || id.startsWith("vocana:"), p + ": " + id + " comes from Minecraft or Vocana");
    }

    @Test void everySharedHearsReferenceHasItsModuleTagAndAKnownGloss() throws IOException {
        Set<String> glosses = glosses();
        for (String kind : List.of("entity_type", "block")) {
            var shared = RES.resolve("data/vocana/tags/" + kind + "/hears");
            int refs = 0;
            for (Path p : files(shared)) {
                var o = read(p);
                assertFalse(o.get("replace").getAsBoolean(), p + " never replaces the core tag");
                String gloss = p.getFileName().toString().replace(".json", "");
                for (JsonElement v : o.getAsJsonArray("values")) {
                    String id = idOf(v);
                    if (!id.equals("#vocanacompat:wyrmroost/" + gloss)) continue;   // another module's line
                    refs++;
                    assertFalse(v.getAsJsonObject().get("required").getAsBoolean(), p + ": the module reference is optional");
                    assertTrue(Files.exists(DATA.resolve("tags/" + kind + "/wyrmroost/" + gloss + ".json")), p + " points at a module tag that exists");
                    if (glosses != null) assertTrue(glosses.contains(gloss.toUpperCase(java.util.Locale.ROOT)), p + ": '" + gloss + "' is a word Vocana or this module knows");
                }
            }
            assertTrue(refs > 0, "the module references its " + kind + " tags from vocana:hears");
            for (Path p : files(DATA.resolve("tags/" + kind + "/wyrmroost"))) {
                String gloss = p.getFileName().toString().replace(".json", "");
                if (gloss.equals("ritual_anchors")) continue;
                assertTrue(Files.exists(shared.resolve(gloss + ".json")), p + " is referenced from vocana:hears/" + gloss);
            }
        }
    }
    /** The glosses of the core lexicon (from the Vocana jar, when present) plus the module's own; null when the core is absent. */
    private static Set<String> glosses() throws IOException {
        Set<String> out = new HashSet<>();
        var lex = read(Path.of("src/main/lexicon/wyrmroost.json"));
        for (String g : List.of("roots", "elder")) for (JsonElement w : lex.getAsJsonArray(g)) out.add(w.getAsJsonObject().get("gloss").getAsString());
        var core = WyrmroostDataTest.class.getResourceAsStream("/data/vocana/lexicon.json");
        if (core == null) return null;
        try (var r = new InputStreamReader(core, StandardCharsets.UTF_8)) {
            var o = JsonParser.parseReader(r).getAsJsonObject();
            for (String g : List.of("roots", "elder", "particles", "measures")) if (o.has(g)) for (JsonElement w : o.getAsJsonArray(g)) out.add(w.getAsJsonObject().get("gloss").getAsString());
        }
        return out;
    }

    @Test void theWordsArePronouncedAndKinWithVerifiedEntities() throws IOException {
        var lex = read(Path.of("src/main/lexicon/wyrmroost.json"));
        Set<String> glosses = new HashSet<>();
        for (String g : List.of("roots", "elder")) for (JsonElement e : lex.getAsJsonArray(g)) {
            var w = e.getAsJsonObject(); glosses.add(w.get("gloss").getAsString());
            for (String key : List.of("form", "respelling", "ipa", "arpabet", "meaning", "beginner_clue", "chapter")) assertTrue(w.has(key), w.get("gloss") + " has " + key);
            assertTrue(w.get("arpabet").getAsString().matches("[A-Z]{1,2}( [A-Z]{1,2})*"), w.get("gloss") + ": arpabet is space-separated Sphinx phones");
            if (w.has("kin_entities")) for (JsonElement k : w.getAsJsonArray("kin_entities")) {
                String id = k.getAsString();
                assertTrue(id.startsWith("wyrmroost:") && WyrmroostIds.ENTITIES.contains(id.substring("wyrmroost:".length())), w.get("gloss") + ": kin " + id + " was verified");
            }
        }
        assertEquals(Set.of("WYRM", "ROOST", "DRAGONFIRE"), glosses);
        var roost = lex.getAsJsonArray("roots").get(1).getAsJsonObject();
        assertEquals("State", roost.get("frame").getAsString(), "an extension Deed must use the Force, State or Ruin frame");
        var fire = lex.getAsJsonArray("elder").get(0).getAsJsonObject();
        assertEquals(WyrmroostIds.FIRE_BREATH_SOUND, fire.getAsJsonObject("profile").get("sound").getAsString());
        assertEquals("FIRE", fire.get("variant_of").getAsString());
    }
}
