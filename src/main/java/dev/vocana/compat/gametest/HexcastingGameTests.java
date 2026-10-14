package dev.vocana.compat.gametest;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.Vocabulary;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Hex Casting module without Hex Casting: the module stays unloaded, its words are in the merged lexicon with the
 * frames and chapter the Grimoire needs, no Deed or Catalyst was registered for them, and its conditioned data stayed
 * out. Nothing here touches a Hex class or the module package (so {@code -Pcompat.skip=hexcasting} still compiles);
 * the media arithmetic has its unit test in {@code src/test}. Dev-only.
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class HexcastingGameTests {
    private HexcastingGameTests() {}

    @GameTest(template = "test_empty")
    public static void theModuleStaysOutWithoutHex(GameTestHelper h) {
        h.assertFalse(ModList.get().isLoaded("hexcasting"), "hexcasting is not on the GameTest classpath");
        h.assertFalse(VocanaCompat.loaded(Module.HEXCAST), "the module did not load");
        h.assertTrue(VocanaApi.deed("DRAW") == null, "DRAW has no behaviour without Hex");
        h.assertTrue(VocanaApi.deed("AWAKEN") == null, "AWAKEN has no behaviour without Hex");
        h.assertFalse(VocanaApi.catalyst(new ItemStack(Items.STICK)), "no Catalyst registration leaked");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theWordsAreInTheLexiconWithTheirFramesAndChapter(GameTestHelper h) {
        var draw = Vocabulary.INSTANCE.byGloss("DRAW");
        var awaken = Vocabulary.INSTANCE.byGloss("AWAKEN");
        h.assertTrue(draw != null && draw.form().equals("hainsu"), "DRAW is hainsu");
        h.assertTrue(awaken != null && awaken.form().equals("rauspe"), "AWAKEN is rauspe");
        h.assertValueEqual(draw.category(), "Deed", "DRAW is a Deed");
        h.assertValueEqual(draw.frame(), "State", "DRAW uses the State frame, which VocanaApi deeds may");
        h.assertValueEqual(awaken.frame(), "Force", "AWAKEN uses the Force frame");
        h.assertValueEqual(Chapter.of(draw), Chapter.ARCANA, "DRAW sits in the Thaumaturgy chapter");
        h.assertValueEqual(Chapter.of(awaken), Chapter.ARCANA, "AWAKEN sits in the Thaumaturgy chapter");
        h.assertTrue(Vocabulary.INSTANCE.byForm("hainsu") == draw, "the form resolves to the word");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theModuleDataStaysOutWithoutHex(GameTestHelper h) {
        // The conditioned tag files are dropped, so the shared optional references resolve to nothing: Hex's amethyst
        // blocks are not anchors here and no block teaches DRAW. The core anchors are untouched.
        var anchors = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getTag(dev.vocana.server.Rituals.ANCHORS);
        h.assertTrue(anchors.isPresent() && anchors.get().size() > 0, "the core ritual anchors still load");
        var draw = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getTag(net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("vocana", "hears/draw")));
        h.assertTrue(draw.isEmpty() || draw.get().size() == 0, "nothing teaches DRAW without Hex");
        h.succeed();
    }
}
