package dev.vocana.compat.gametest;

import com.google.gson.JsonParser;
import dev.vocana.api.VocanaApi;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.Vocabulary;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The Wyrmroost module without Wyrmroost: its words are in the merged lexicon with a chapter and a pronunciation, its
 * behaviours stay unregistered (the module did not load), and its data parses. Nothing here touches
 * {@code dev.vocana.compat.wyrmroost.Dragons}, the one class that names Wyrmroost's types.
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class WyrmroostGameTests {
    private WyrmroostGameTests() {}

    @GameTest(template = "test_empty")
    public static void theWordsAreInTheLexiconWithChapters(GameTestHelper h) {
        var lex = Vocabulary.INSTANCE;
        var wyrm = lex.byGloss("WYRM"); var roost = lex.byGloss("ROOST"); var fire = lex.byGloss("DRAGONFIRE");
        h.assertTrue(wyrm != null && roost != null && fire != null, "WYRM, ROOST and DRAGONFIRE are loaded from META-INF/vocana/lexicon.json");
        h.assertTrue(wyrm.category().equals("Essence"), "WYRM is an Essence");
        h.assertTrue(roost.category().equals("Deed") && roost.frame().equals("State"), "ROOST is a Deed of the State frame");
        h.assertTrue(fire.elder() && fire.base().equals("FIRE"), "DRAGONFIRE shades FIRE");
        h.assertTrue(fire.shade().trait().equals("inferno") && fire.shade().weight() == 1, "DRAGONFIRE carries its profile");
        h.assertTrue(Chapter.of(wyrm) == Chapter.SUMMONING && Chapter.of(roost) == Chapter.SUMMONING, "WYRM and ROOST sit in the Summoning chapter");
        h.assertTrue(Chapter.of(fire) == Chapter.FIRE, "DRAGONFIRE sits beside FIRE");
        for (var w : List.of(wyrm, roost, fire)) {
            h.assertTrue(lex.pronunciation(w.form()) != null && !lex.pronunciation(w.form()).isBlank(), w.form() + " has an ARPAbet pronunciation");
            h.assertTrue(lex.speakable(w.form()), w.form() + " is speakable");
        }
        h.assertTrue(lex.form("WYRM").equals("mespau") && lex.form("ROOST").equals("hespon") && lex.form("DRAGONFIRE").equals("drakon"), "the forms are as documented");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theBehavioursStayOutWithoutTheMod(GameTestHelper h) {
        h.assertFalse(VocanaCompat.loaded(Module.WYRMROOST), "the Wyrmroost module did not load (no Wyrmroost here)");
        h.assertTrue(VocanaApi.essence("WYRM") == null, "WYRM has no behaviour without the module");
        h.assertTrue(VocanaApi.deed("ROOST") == null, "ROOST has no behaviour without the module");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theDataParsesAndCarriesBothConditions(GameTestHelper h) {
        for (String path : List.of("/data/vocanacompat/tags/entity_type/wyrmroost/wyrm.json",
                                   "/data/vocanacompat/tags/entity_type/wyrmroost/dragonfire.json",
                                   "/data/vocanacompat/vocana_imbue/wyrmroost/red_geode_fire.json",
                                   "/data/vocanacompat/recipe/wyrmroost/ember_staff_from_red_geode.json")) {
            var in = WyrmroostGameTests.class.getResourceAsStream(path);
            h.assertTrue(in != null, path + " is in the jar");
            try (var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                var o = JsonParser.parseReader(r).getAsJsonObject();
                var conditions = o.getAsJsonArray("neoforge:conditions");
                h.assertTrue(conditions != null && conditions.size() == 2, path + " carries both conditions");
            } catch (java.io.IOException e) { h.fail(path + ": " + e); }
        }
        var shared = WyrmroostGameTests.class.getResourceAsStream("/data/vocana/tags/entity_type/hears/wyrm.json");
        h.assertTrue(shared != null, "the shared vocana:hears/wyrm tag references the module's tag");
        h.succeed();
    }
}
