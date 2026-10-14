package dev.vocana.compat.create;

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
 * The recipe builders write the shapes Create's codecs read (checked against Create's generated data on
 * {@code mc1.21.1/dev}), and the module's shipped data files follow them: every file carries both conditions, every
 * sequenced-assembly step keeps the transitional item, the lexicon's pronunciations are Sphinx phones. No Minecraft
 * here; the files are read from {@code src/main/resources}.
 */
final class CreateRecipesTest {
    private static final Path DATA = Path.of("src/main/resources/data/vocanacompat");
    /** The CMU/Sphinx phone set the recognizer's dictionary uses (stress digits are not written in the lexicon). */
    private static final Set<String> PHONES = Set.of("AA","AE","AH","AO","AW","AY","B","CH","D","DH","EH","ER","EY","F","G","HH","IH","IY","JH","K","L","M","N","NG","OW","OY","P","R","S","SH","T","TH","UH","UW","V","W","Y","Z","ZH");

    private static JsonObject read(Path p) throws IOException {
        try (var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
    }
    private static List<Path> files(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) { return s.filter(p -> p.toString().endsWith(".json")).sorted().toList(); }
    }
    private static boolean conditioned(JsonObject o) {
        if (!o.has("neoforge:conditions")) return false;
        boolean mod = false, module = false;
        for (JsonElement c : o.getAsJsonArray("neoforge:conditions")) {
            var x = c.getAsJsonObject(); String type = x.get("type").getAsString();
            mod |= type.equals("neoforge:mod_loaded") && "create".equals(x.get("modid").getAsString());
            module |= type.equals("vocanacompat:module_enabled") && "create".equals(x.get("module").getAsString());
        }
        return mod && module;
    }

    @Test void mixingMatchesCreatesBrassIngotShape() {
        var o = CreateRecipes.mixing(CreateRecipes.HEATED, List.of(CreateRecipes.ingredient("#c:ingots/copper"), CreateRecipes.ingredient("#c:ingots/zinc")), CreateRecipes.output("create:brass_ingot", 2, 1));
        assertEquals("create:mixing", o.get("type").getAsString());
        assertEquals("heated", o.get("heat_requirement").getAsString());
        assertEquals("c:ingots/copper", o.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("tag").getAsString());
        var result = o.getAsJsonArray("results").get(0).getAsJsonObject();
        assertEquals("create:brass_ingot", result.get("id").getAsString());
        assertEquals(2, result.get("count").getAsInt());
        assertFalse(result.has("chance"), "a sure result writes no chance, as Create's data does");
        assertFalse(CreateRecipes.mixing(CreateRecipes.NONE, List.of(), CreateRecipes.output("x:y")).has("heat_requirement"), "no heat, no field");
        assertEquals("superheated", CreateRecipes.mixing(CreateRecipes.SUPERHEATED, List.of(), CreateRecipes.output("x:y")).get("heat_requirement").getAsString());
    }

    @Test void sequencedAssemblyMatchesCreatesPrecisionMechanismShape() {
        String t = "create:incomplete_precision_mechanism";
        var o = CreateRecipes.sequencedAssembly("#c:plates/gold", t, 5,
            List.of(CreateRecipes.deployingStep(t, "create:cogwheel"), CreateRecipes.deployingStep(t, "create:large_cogwheel"), CreateRecipes.deployingStep(t, "#c:nuggets/iron")),
            List.of(CreateRecipes.output("create:precision_mechanism", 1, 120), CreateRecipes.output("minecraft:clock")));
        assertEquals("create:sequenced_assembly", o.get("type").getAsString());
        assertEquals("c:plates/gold", o.getAsJsonObject("ingredient").get("tag").getAsString());
        assertEquals(5, o.get("loops").getAsInt());
        assertEquals(t, o.getAsJsonObject("transitional_item").get("id").getAsString());
        assertEquals(120.0, o.getAsJsonArray("results").get(0).getAsJsonObject().get("chance").getAsDouble());
        JsonArray steps = o.getAsJsonArray("sequence");
        assertEquals(3, steps.size());
        var first = steps.get(0).getAsJsonObject();
        assertEquals("create:deploying", first.get("type").getAsString());
        assertEquals(t, first.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString());
        assertEquals("create:cogwheel", first.getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString());
        assertEquals(t, first.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
        var pressing = CreateRecipes.pressingStep(t);
        assertEquals(1, pressing.getAsJsonArray("ingredients").size(), "a pressing step has the transitional item alone");
        var filling = CreateRecipes.fillingStep(t, "minecraft:lava", 500);
        var fluid = filling.getAsJsonArray("ingredients").get(1).getAsJsonObject();
        assertEquals("neoforge:single", fluid.get("type").getAsString());
        assertEquals(500, fluid.get("amount").getAsInt());
    }

    @Test void deployingAndConditions() {
        var d = CreateRecipes.deploying("minecraft:iron_ingot", "create:brass_sheet", CreateRecipes.output("x:y"), true);
        assertTrue(d.get("keep_held_item").getAsBoolean());
        assertFalse(CreateRecipes.deploying("a:b", "c:d", CreateRecipes.output("x:y"), false).has("keep_held_item"));
        var c = CreateRecipes.conditioned(d, "iceandfire");
        assertTrue(conditioned(c));
        assertEquals(3, c.getAsJsonArray("neoforge:conditions").size(), "the module's two conditions plus one for the other mod");
        assertEquals("neoforge:conditions", c.entrySet().iterator().next().getKey(), "conditions come first");
        assertEquals("create:item_application", CreateRecipes.itemApplication("#c:stripped_logs", "create:andesite_alloy", CreateRecipes.output("create:andesite_casing")).get("type").getAsString());
    }

    @Test void everyShippedDataFileCarriesBothConditions() throws IOException {
        int n = 0;
        for (Path dir : List.of(DATA.resolve("recipe/create"), DATA.resolve("vocana_imbue/create"), DATA.resolve("tags/block/create"), DATA.resolve("tags/entity_type/create")))
            for (Path p : files(dir)) { assertTrue(conditioned(read(p)), p + " carries neoforge:mod_loaded create and vocanacompat:module_enabled create"); n++; }
        assertTrue(n >= 10, "the module ships its data (" + n + " files)");
    }

    @Test void theShippedCreateRecipesFollowTheShapes() throws IOException {
        var torc = read(DATA.resolve("recipe/create/torc.json"));
        assertEquals("create:sequenced_assembly", torc.get("type").getAsString());
        String t = torc.getAsJsonObject("transitional_item").get("id").getAsString();
        assertEquals("vocanacompat:create/incomplete_torc", t, "the transitional item is the module's own (CreateItems)");
        for (JsonElement s : torc.getAsJsonArray("sequence")) {
            var step = s.getAsJsonObject();
            assertEquals(t, step.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString(), "a step starts from the transitional item");
            assertEquals(t, step.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString(), "a step gives it back");
            assertTrue(Set.of("create:pressing", "create:deploying", "create:filling", "create:cutting").contains(step.get("type").getAsString()));
        }
        assertEquals("vocana:torc", torc.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
        var chime = read(DATA.resolve("recipe/create/tuning_chime_mixing.json"));
        assertEquals("create:mixing", chime.get("type").getAsString());
        assertEquals("superheated", chime.get("heat_requirement").getAsString());
        assertEquals("vocana:tuning_chime", chime.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
    }

    @Test void theImbueRecipesNameTheModulesWords() throws IOException {
        for (Path p : files(DATA.resolve("vocana_imbue/create"))) {
            var o = read(p);
            String essence = o.get("essence").getAsString();
            assertTrue(Set.of("KINETIC", "BRASS").contains(essence), p + " lays a Create word");
            assertTrue(o.has("target") && o.has("materials") && o.has("xp_levels") && o.has("thrum") && o.has("complexity"), p + " has the bench's fields");
            for (JsonElement m : o.getAsJsonArray("materials")) assertTrue(m.getAsJsonObject().has("item") || m.getAsJsonObject().has("tag"), p + ": a material is an item or a tag");
            if (essence.equals("BRASS")) assertFalse(o.get("shades").getAsBoolean(), "BRASS is named directly; plain METAL keeps the core's deep price");
        }
    }

    @Test void theWordsPronounceInSphinxPhones() throws IOException {
        var lexicon = read(Path.of("src/main/lexicon/create.json"));
        int words = 0;
        for (String group : List.of("roots", "elder")) for (JsonElement w : lexicon.getAsJsonArray(group)) {
            var o = w.getAsJsonObject(); words++;
            assertTrue(o.has("arpabet"), o.get("form").getAsString() + " gives the recognizer its phones");
            for (String phone : o.get("arpabet").getAsString().split(" ")) assertTrue(PHONES.contains(phone), o.get("form").getAsString() + ": '" + phone + "' is no Sphinx phone");
            assertEquals(o.get("respelling").getAsString().split("-").length >= 2, true, o.get("form").getAsString() + " has at least two syllables, as every root does");
        }
        assertEquals(3, words, "KINETIC, SPIN and BRASS");
    }
}
