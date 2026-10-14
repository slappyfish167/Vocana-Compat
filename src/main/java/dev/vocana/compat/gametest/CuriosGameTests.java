package dev.vocana.compat.gametest;

import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * The Curios module seen from a game without Curios (the only kind {@code runGameTestServer} runs): the module did not
 * load, so none of its items exist and none of its data (recipes behind {@code neoforge:mod_loaded} +
 * {@code vocanacompat:module_enabled}) reached the game. No Curios class is named here; the module's own classes are
 * not loaded either, which is the point. The behaviour behind the slots can only be tried in game (docs/curios.md has
 * the checklist); its arithmetic is unit-tested in {@code CurioRulesTest}.
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class CuriosGameTests {
    private CuriosGameTests() {}
    private static final List<String> ITEMS = List.of("ring_of_focus", "amulet_of_resonance", "charm_of_calm", "signet_of_the_elements");

    @GameTest(template = "test_empty")
    public static void curiosItemsDoNotExistWithoutCurios(GameTestHelper h) {
        h.assertFalse(ModList.get().isLoaded(Module.CURIOS.modId), "curios is not on the GameTest classpath");
        h.assertFalse(VocanaCompat.loaded(Module.CURIOS), "the Curios module did not load");
        for (String id : ITEMS)
            h.assertFalse(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, id)), "vocanacompat:" + id + " is not registered without Curios");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void curiosRecipesStayOutWithoutCurios(GameTestHelper h) {
        var recipes = h.getLevel().getServer().getRecipeManager();
        for (String id : ITEMS)
            h.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "curios/" + id)).isEmpty(), "recipe vocanacompat:curios/" + id + " was dropped by its conditions");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void vocanaItemsKeepWorkingWithoutCurios(GameTestHelper h) {
        // The Torc and relic exist as Vocana made them; nothing of this module hangs on them when Curios is absent.
        h.assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("vocana", "torc")), "Vocana's Torc is registered");
        h.assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("vocana", "whispering_relic")), "Vocana's relic is registered");
        h.assertFalse(dev.vocana.api.VocanaApi.worn(h.makeMockServerPlayerInLevel()), "no registered worn-Torc predicate answers");
        h.succeed();
    }
}
