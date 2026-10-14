package dev.vocana.compat.hexcasting;

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
 * The module's data and words, checked without Minecraft: every file under a {@code hexcasting/} data folder carries
 * both conditions, every shared {@code vocana:} tag only references the module's tag optionally, the imbue recipes
 * have the shape {@code dev.vocana.bench.ImbueRecipe} parses, and the lexicon words are Deeds in a frame the
 * extension API serves, with a chapter and a pronunciation.
 */
final class HexcastingDataTest {
    private static final Path DATA = Path.of("src/main/resources/data");

    private static JsonObject read(Path p) throws IOException {
        try (var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static List<Path> under(Path root, String needle) throws IOException {
        try (Stream<Path> s = Files.walk(root)) { return s.filter(p -> p.toString().endsWith(".json") && p.toString().contains(needle)).sorted().toList(); }
    }
    private static boolean hasCondition(JsonArray conditions, String type, String key, String value) {
        for (JsonElement c : conditions) {
            var o = c.getAsJsonObject();
            if (o.get("type").getAsString().equals(type) && o.get(key).getAsString().equals(value)) return true;
        }
        return false;
    }

    @Test void everyModuleDataFileCarriesBothConditions() throws IOException {
        var files = under(DATA.resolve("vocanacompat"), "/hexcasting/");
        assertFalse(files.isEmpty(), "the module ships data");
        for (Path p : files) {
            var o = read(p);
            assertTrue(o.has("neoforge:conditions"), p + " has neoforge:conditions");
            var c = o.getAsJsonArray("neoforge:conditions");
            assertTrue(hasCondition(c, "neoforge:mod_loaded", "modid", "hexcasting"), p + " requires the mod");
            assertTrue(hasCondition(c, "vocanacompat:module_enabled", "module", "hexcasting"), p + " requires the module");
        }
    }

    @Test void moduleTagsListOnlyOptionalHexIds() throws IOException {
        var files = under(DATA.resolve("vocanacompat/tags"), "/hexcasting/");
        assertFalse(files.isEmpty());
        for (Path p : files) {
            var o = read(p);
            assertFalse(o.get("replace").getAsBoolean(), p + " never replaces");
            for (JsonElement v : o.getAsJsonArray("values")) {
                var e = v.getAsJsonObject();
                assertTrue(e.get("id").getAsString().startsWith("hexcasting:"), p + " lists a Hex id: " + e);
                assertFalse(e.get("required").getAsBoolean(), p + " lists ids optionally: " + e);
            }
        }
    }

    @Test void sharedVocanaTagsOnlyReferenceTheModuleTagOptionally() throws IOException {
        var files = under(DATA.resolve("vocana/tags"), "");
        assertFalse(files.isEmpty());
        for (Path p : files) {
            var o = read(p);
            assertFalse(o.get("replace").getAsBoolean(), p + " never replaces the core tag");
            for (JsonElement v : o.getAsJsonArray("values")) {
                var e = v.getAsJsonObject();
                if (!e.get("id").getAsString().contains("hexcasting")) continue;   // another module's line
                // The module tag mirrors the shared file's name: hears/light.json -> #vocanacompat:hexcasting/light.
                String name = p.getFileName().toString().replace(".json", "");
                assertEquals("#vocanacompat:hexcasting/" + name, e.get("id").getAsString(), p + " references the module tag of the same name");
                assertFalse(e.get("required").getAsBoolean(), p + " references it optionally");
                String kind = p.getParent().getFileName().toString().equals("hears") ? p.getParent().getParent().getFileName().toString() : p.getParent().getFileName().toString();
                assertTrue(Files.exists(DATA.resolve("vocanacompat/tags/" + kind + "/hexcasting/" + name + ".json")), p + " has its module tag file");
            }
        }
    }

    @Test void imbueRecipesHaveTheBenchShape() throws IOException {
        var files = under(DATA.resolve("vocanacompat/vocana_imbue/hexcasting"), "");
        assertEquals(3, files.size(), "three imbue recipes");
        for (Path p : files) {
            var o = read(p);
            assertTrue(o.has("essence") ^ o.has("chant"), p + " lays an essence or a chant, not both");
            assertTrue(o.has("target") && o.getAsJsonObject("target").has("kind"), p + " names a target kind");
            assertTrue(Set.of("any", "weapon", "armor", "tool", "staff").contains(o.getAsJsonObject("target").get("kind").getAsString()), p + " kind is one the bench knows");
            var materials = o.getAsJsonArray("materials");
            assertTrue(materials.size() > 0, p + " costs something");
            boolean hexMaterial = false;
            for (JsonElement m : materials) {
                var mo = m.getAsJsonObject();
                assertTrue(mo.has("item") || mo.has("tag"), p + " material has an item or a tag");
                String id = mo.has("item") ? mo.get("item").getAsString() : mo.get("tag").getAsString();
                hexMaterial |= id.startsWith("hexcasting:");
            }
            assertTrue(hexMaterial, p + " is paid in Hex Casting's media or wood");
            if (o.has("triggers")) for (JsonElement t : o.getAsJsonArray("triggers"))
                assertTrue(Set.of("ON_HIT", "ON_HURT", "PASSIVE", "ON_USE", "ON_EQUIP").contains(t.getAsString()), p + " trigger " + t);
        }
    }

    @Test void theWordsAreExtensionDeedsWithChapterAndSound() throws IOException {
        var lexicon = read(Path.of("src/main/lexicon/hexcasting.json"));
        var roots = lexicon.getAsJsonArray("roots");
        assertEquals(2, roots.size(), "DRAW and AWAKEN");
        for (JsonElement w : roots) {
            var o = w.getAsJsonObject();
            assertEquals("Deed", o.get("class").getAsString());
            assertTrue(Set.of("Force", "State", "Ruin").contains(o.get("frame").getAsString()), "VocanaApi deeds use Force, State or Ruin: " + o.get("gloss"));
            assertEquals("ARCANA", o.get("chapter").getAsString());
            for (String key : List.of("form", "gloss", "respelling", "ipa", "meaning", "beginner_clue", "discipline"))
                assertTrue(o.has(key) && !o.get(key).getAsString().isBlank(), o.get("gloss") + " has " + key);
            assertTrue(o.get("respelling").getAsString().contains("-"), "the respelling is syllabified");
            assertTrue(o.get("ipa").getAsString().startsWith("/") && o.get("ipa").getAsString().endsWith("/"), "IPA is slashed");
            assertTrue(o.get("form").getAsString().matches("[a-z]+"), "the form is plain lower-case letters");
        }
        var glosses = Set.of(roots.get(0).getAsJsonObject().get("gloss").getAsString(), roots.get(1).getAsJsonObject().get("gloss").getAsString());
        assertEquals(Set.of("DRAW", "AWAKEN"), glosses);
        // Each new word can be heard somewhere in Hex's world.
        for (String g : List.of("draw", "awaken")) assertTrue(Files.exists(DATA.resolve("vocanacompat/tags/block/hexcasting/" + g + ".json")), g + " has a hears tag");
    }
}
