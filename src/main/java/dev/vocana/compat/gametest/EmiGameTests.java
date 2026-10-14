package dev.vocana.compat.gametest;

import com.google.gson.JsonParser;
import dev.vocana.bench.ImbueRecipe;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.compat.emi.EmiText;
import dev.vocana.compat.emi.ImbueJson;
import dev.vocana.server.ImbueRecipes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The EMI module on the bare GameTest server: EMI is absent, so the module must not load, and nothing here may touch
 * an EMI class. What can be tested is the EMI-free part of the package: the recipe JSON writer round-trips every
 * shipped Imbue Bench recipe through Vocana's own parser (that is what the sync sends), and the formatting helper agrees
 * with the server's proficiency arithmetic. Dev-only (excluded from the jar).
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class EmiGameTests {
    private EmiGameTests() {}

    @GameTest(template = "test_empty")
    public static void theEmiModuleDoesNotLoadWithoutEmi(GameTestHelper h) {
        h.assertFalse(ModList.get().isLoaded(Module.EMI.modId), "emi is not on the GameTest classpath");
        h.assertFalse(VocanaCompat.loaded(Module.EMI), "the EMI module did not load");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void imbueRecipesSurviveTheSyncRoundTrip(GameTestHelper h) {
        var all = ImbueRecipes.all();
        h.assertFalse(all.isEmpty(), "Vocana's shipped imbue recipes are loaded");
        var back = ImbueJson.readAll(ImbueJson.writeAll(all).toString());
        h.assertTrue(back.size() == all.size(), "every recipe reads back (" + back.size() + " of " + all.size() + ")");
        int i = 0;
        for (ImbueRecipe r : all) {
            ImbueRecipe b = back.get(i++);
            h.assertTrue(r.equals(b), "recipe " + r.id() + " is unchanged by write+parse: " + b);
        }
        // A recipe with every optional field, written by hand, keeps them all.
        var full = ImbueRecipe.parse("vocanacompat:test", JsonParser.parseString("""
            {"target":{"kind":"weapon","item":"minecraft:iron_sword","tag":"minecraft:swords"},"essence":["FIRE","FROST"],"shades":false,
             "triggers":["ON_HIT"],"materials":[{"item":"minecraft:blaze_powder","count":2},{"tag":"vocana:residues","count":1,"essence":true}],
             "xp_levels":4,"thrum":25.5,"strength":1.5,"complexity":5,"shade":{"xp_levels":3,"thrum":15,"complexity":2},
             "ritual":{"catalysts":[{"tag":"vocana:ritual_anchors","count":4,"radius":5},{"block":"minecraft:lodestone","count":1,"radius":3}],"channel":true},"priority":2}
            """).getAsJsonObject());
        var again = ImbueJson.readAll("[" + ImbueJson.write(full) + "]");
        h.assertTrue(again.size() == 1 && full.equals(again.get(0)), "a recipe with every field round-trips: " + again);
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theFormattingHelperAgreesWithTheServer(GameTestHelper h) {
        int casts = dev.vocana.server.Mastery.masteryCasts();
        for (int level = 0; level <= 4; level++) {
            h.assertTrue(EmiText.threshold(level, casts) == dev.vocana.server.Mastery.threshold(level), "threshold of level " + level);
            h.assertTrue(Math.abs(EmiText.thrumFactor(level) - dev.vocana.server.Mastery.thrumFactor(level)) < 1e-9, "Thrum factor of level " + level);
            h.assertTrue(Math.abs(EmiText.strainFactor(level) - dev.vocana.server.Mastery.strainFactor(level)) < 1e-9, "Strain factor of level " + level);
        }
        for (int c : new int[]{0, 1, 4, 11, 12, 24, 39, 40, 500}) h.assertTrue(EmiText.level(c, casts) == dev.vocana.server.Mastery.level(c), "level of " + c + " releases");
        h.succeed();
    }
}
