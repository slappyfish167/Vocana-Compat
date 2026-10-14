package dev.vocana.compat;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The per-module lexicon and Grimoire files, and what the build merged from them. Runs without Minecraft: the files
 * are read from {@code src/main/<kind>} (the working directory is the project) and the merged resources from the
 * test classpath, which also carries the Vocana jar, so module words can be checked against the core lexicon.
 */
final class LexiconExtensionTest {
    /** The groups Vocana's {@code Vocabulary.load} reads from an extension. */
    private static final Set<String> GROUPS = Set.of("roots", "particles", "measures", "primes", "elder", "callings", "derived_forms", "blends");
    private static final Set<String> MODULES = Set.of("create", "wyrmroost", "iceandfire", "emi", "curios", "hexcasting", "ars_nouveau");

    private static List<Path> files(String kind) throws IOException {
        try (Stream<Path> s = Files.list(Path.of("src/main/" + kind))) { return s.filter(p -> p.toString().endsWith(".json")).sorted().toList(); }
    }
    private static JsonObject read(Path p) throws IOException {
        try (var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static JsonObject resource(String name) throws IOException {
        var in = LexiconExtensionTest.class.getResourceAsStream(name);
        assertNotNull(in, name + " is on the classpath");
        try (var r = new InputStreamReader(in, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static String name(Path p) { String n = p.getFileName().toString(); return n.substring(0, n.length() - ".json".length()); }

    @Test void everyModuleHasALexiconAndAGrimoireFileAndNothingElseDoes() throws IOException {
        for (String kind : List.of("lexicon", "grimoire")) {
            var names = files(kind).stream().map(LexiconExtensionTest::name).collect(java.util.stream.Collectors.toSet());
            assertEquals(MODULES, names, "src/main/" + kind + " holds exactly one file per module");
        }
    }

    @Test void lexiconFilesHoldOnlyKnownGroupsOfWellFormedWords() throws IOException {
        for (Path p : files("lexicon")) {
            var o = read(p);
            for (var e : o.entrySet()) {
                assertTrue(GROUPS.contains(e.getKey()), p + ": unknown group '" + e.getKey() + "' (Vocana reads " + GROUPS + ")");
                assertTrue(e.getValue().isJsonArray(), p + ": '" + e.getKey() + "' must be an array");
                for (JsonElement w : e.getValue().getAsJsonArray()) {
                    var word = w.getAsJsonObject();
                    assertTrue(word.has("form") && word.has("gloss"), p + ": every word needs 'form' and 'gloss': " + word);
                    if (!e.getKey().equals("callings")) {
                        assertTrue(word.has("respelling"), p + ": '" + word.get("form").getAsString() + "' needs 'respelling' (Vocana syllabifies it)");
                        assertTrue(word.has("chapter"), p + ": '" + word.get("form").getAsString() + "' needs 'chapter' so the Grimoire can place it");
                    }
                    if (e.getKey().equals("elder")) assertTrue(word.has("variant_of"), p + ": an Elder word needs 'variant_of'");
                    else if (!e.getKey().equals("callings")) assertTrue(word.has("class"), p + ": '" + word.get("form").getAsString() + "' needs 'class'");
                }
            }
        }
    }

    @Test void formsAndGlossesAreUniqueAcrossModulesAndTheCoreLexicon() throws IOException {
        Map<String, String> owner = new HashMap<>();
        var core = LexiconExtensionTest.class.getResourceAsStream("/data/vocana/lexicon.json");
        if (core != null) try (var r = new InputStreamReader(core, StandardCharsets.UTF_8)) {
            var o = JsonParser.parseReader(r).getAsJsonObject();
            for (String g : GROUPS) if (o.has(g)) for (JsonElement w : o.getAsJsonArray(g)) claim(owner, w.getAsJsonObject(), "vocana");
        }
        for (Path p : files("lexicon")) {
            var o = read(p);
            for (String g : GROUPS) if (o.has(g)) for (JsonElement w : o.getAsJsonArray(g)) claim(owner, w.getAsJsonObject(), name(p));
        }
    }
    private static void claim(Map<String, String> owner, JsonObject word, String by) {
        for (String key : List.of("form", "gloss")) {
            if (!word.has(key)) continue;
            String id = key + "=" + word.get(key).getAsString();
            String other = owner.putIfAbsent(id, by);
            assertTrue(other == null || other.equals(by), by + " reuses " + id + " of " + other);
        }
    }

    @Test void theBuildMergedEveryModuleIntoTheExtensionFiles() throws IOException {
        var lexicon = resource("/META-INF/vocana/lexicon.json");
        int words = 0;
        for (String g : GROUPS) if (lexicon.has(g)) words += lexicon.getAsJsonArray(g).size();
        int expected = 0;
        for (Path p : files("lexicon")) { var o = read(p); for (String g : GROUPS) if (o.has(g)) expected += o.getAsJsonArray(g).size(); }
        assertEquals(expected, words, "the merged lexicon carries every module word once");
        var groups = resource("/META-INF/vocana/grimoire_groups.json");
        assertTrue(groups.has("groups") && groups.get("groups").isJsonArray(), "grimoire_groups.json has a 'groups' array");
        JsonArray all = groups.getAsJsonArray("groups");
        int expectedGroups = 0;
        for (Path p : files("grimoire")) expectedGroups += read(p).getAsJsonArray("groups").size();
        assertEquals(expectedGroups, all.size(), "the merged Grimoire groups carry every module group once");
    }
}
