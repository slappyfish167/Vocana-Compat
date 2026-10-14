package dev.vocana.compat.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.vocana.api.VocanaApi;
import dev.vocana.bench.ImbueRecipe;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.ChapterGroup;
import dev.vocana.engine.Vocabulary;
import dev.vocana.server.ImbueRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The Create module without Create: its words are in the lexicon (the extension merged and loaded), the Grimoire
 * places them, its imbue recipes parse with Vocana's own loader yet stay out of the game, and so do its Create recipes,
 * item and tags. Nothing here names a Create class; the module's behaviour (KINETIC, SPIN, the cranks, the burners)
 * needs Create installed and is checked by hand ({@code docs/create.md}, "In-game checklist").
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class CreateGameTests {
    private CreateGameTests() {}
    private static final List<String> IMBUE_RECIPES = List.of("kinetic_tool", "kinetic_weapon", "kinetic_armor", "brass_any");

    @GameTest(template = "test_empty")
    public static void theWordsAreInTheLexicon(GameTestHelper h) {
        var kinetic = Vocabulary.INSTANCE.byGloss("KINETIC");
        h.assertTrue(kinetic != null, "KINETIC is in the lexicon");
        h.assertTrue(kinetic.category().equals("Essence") && kinetic.form().equals("krenshu") && kinetic.discipline().equals("Transmutation"), "KINETIC is an Essence of Transmutation, krenshu");
        h.assertTrue(Vocabulary.ROOTS.contains("KINETIC"), "KINETIC counts as a root");
        h.assertTrue("K R EH N SH UW".equals(Vocabulary.INSTANCE.pronunciation("krenshu")), "the recognizer gets KINETIC's phones");
        var spin = Vocabulary.INSTANCE.byGloss("SPIN");
        h.assertTrue(spin != null && spin.category().equals("Deed") && spin.frame().equals("Force") && spin.form().equals("kirsau"), "SPIN is a Force Deed, kirsau");
        var brass = Vocabulary.INSTANCE.byGloss("BRASS");
        h.assertTrue(brass != null && brass.elder() && brass.base().equals("METAL"), "BRASS is an Elder shade of METAL");
        h.assertTrue(brass.shade().trait().equals("encase") && brass.tongue().equals("common"), "BRASS clamps (encase) and is a common word, not a deep one");
        h.assertTrue(Vocabulary.ELDER.contains("BRASS") && Vocabulary.INSTANCE.shades("METAL").contains(brass), "BRASS sits among METAL's shades");
        h.assertTrue(Vocabulary.INSTANCE.speakable("krenshu") && Vocabulary.INSTANCE.speakable("kirsau") && Vocabulary.INSTANCE.speakable("orichalcum"), "all three can be spoken");
        // Without Create the module never loads, so the glosses have words but no behaviour: the core's default branches return false.
        h.assertFalse(VocanaCompat.loaded(Module.CREATE), "the Create module did not load");
        h.assertTrue(VocanaApi.essence("KINETIC") == null && VocanaApi.deed("SPIN") == null, "no behaviour is registered without Create");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theGrimoirePlacesThem(GameTestHelper h) {
        h.assertTrue(Chapter.of(Vocabulary.INSTANCE.byGloss("KINETIC")) == Chapter.MATTER, "KINETIC is in Matter");
        h.assertTrue(Chapter.of(Vocabulary.INSTANCE.byGloss("SPIN")) == Chapter.SHAPES, "SPIN is in Shapes & Force");
        h.assertTrue(Chapter.of(Vocabulary.INSTANCE.byGloss("BRASS")) == Chapter.MATTER, "BRASS sits beside METAL in Matter");
        h.assertTrue(Chapter.MATTER.words(Vocabulary.INSTANCE).stream().anyMatch(w -> w.gloss().equals("KINETIC")), "Matter lists KINETIC");
        // src/main/grimoire/create.json extends the core 'making' group in place: same ribbon, a blurb that names the machines.
        var making = ChapterGroup.byId("making");
        h.assertTrue(making != null && making.chapters().contains(Chapter.MATTER) && ChapterGroup.of(Chapter.MATTER) == making, "Matter still sits in the core's Matter & Making ribbon");
        h.assertTrue(making.blurb().contains("Create"), "the module's grimoire file reached the ribbon's blurb");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theImbueRecipesParseButStayOut(GameTestHelper h) {
        for (String name : IMBUE_RECIPES) {
            String id = "vocanacompat:create/" + name;
            JsonObject o = resource(h, "/data/vocanacompat/vocana_imbue/create/" + name + ".json");
            h.assertTrue(conditioned(o), id + " carries both conditions");
            ImbueRecipe r = ImbueRecipe.parse(id, o);
            h.assertTrue(r != null && r.id().equals(id), id + " parses with Vocana's loader");
            h.assertTrue(r.essences().contains(name.startsWith("brass") ? "BRASS" : "KINETIC"), id + " lays the module's word");
            h.assertTrue(ImbueRecipes.byId(id) == null, id + " is not loaded while Create is absent");
        }
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theCreateDataStaysOut(GameTestHelper h) {
        var manager = h.getLevel().getServer().getRecipeManager();
        for (String name : List.of("torc", "tuning_chime_mixing")) {
            h.assertTrue(conditioned(resource(h, "/data/vocanacompat/recipe/create/" + name + ".json")), name + " carries both conditions");
            h.assertTrue(manager.byKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "create/" + name)).isEmpty(), name + " is not a loaded recipe");
        }
        h.assertFalse(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "create/incomplete_torc")), "the transitional item is not registered without Create");
        var blocks = h.getLevel().registryAccess().registryOrThrow(Registries.BLOCK);
        for (String gloss : List.of("kinetic", "spin", "brass")) {
            var tag = blocks.getTag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("vocana", "hears/" + gloss)));
            h.assertTrue(tag.isEmpty() || tag.get().size() == 0, "vocana:hears/" + gloss + " names no block here (its only entry is the optional module tag)");
        }
        h.succeed();
    }

    private static JsonObject resource(GameTestHelper h, String name) {
        var in = CreateGameTests.class.getResourceAsStream(name);
        h.assertTrue(in != null, name + " is in the jar");
        try (var r = new InputStreamReader(in, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
        catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }
    /** Both of the module's conditions, in any order. */
    private static boolean conditioned(JsonObject o) {
        if (!o.has("neoforge:conditions")) return false;
        boolean mod = false, module = false;
        for (var c : o.getAsJsonArray("neoforge:conditions")) {
            var x = c.getAsJsonObject();
            String type = x.get("type").getAsString();
            mod |= type.equals("neoforge:mod_loaded") && x.get("modid").getAsString().equals("create");
            module |= type.equals("vocanacompat:module_enabled") && x.get("module").getAsString().equals("create");
        }
        return mod && module;
    }
}
