package dev.vocana.compat.gametest;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatConfig;
import dev.vocana.compat.Module;
import dev.vocana.compat.ModuleEnabledCondition;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Vocabulary;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Smoke tests for the skeleton, run by {@code runGameTestServer} with only Vocana and this mod present: the mod and
 * its toggles load, Vocana's extension API and lexicon are reachable, and no module loads without its mod. Dev-only
 * (excluded from the jar). Module agents add their own classes here; a module's GameTests can only exercise code
 * that needs no target mod, since none is on the GameTest classpath.
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class CompatGameTests {
    private CompatGameTests() {}

    @GameTest(template = "test_empty")
    public static void theModLoadsWithItsToggles(GameTestHelper h) {
        h.assertTrue(ModList.get().isLoaded(VocanaCompat.ID), "vocanacompat is loaded");
        h.assertTrue(ModList.get().isLoaded("vocana"), "Vocana is loaded beside it");
        h.assertTrue(VocanaCompat.container() != null, "the mod container is kept");
        h.assertTrue(CompatConfig.SPEC.isLoaded(), "vocanacompat-modules.toml is loaded");
        for (var m : Module.values()) h.assertTrue(CompatConfig.enabled(m), "modules." + m.id + ".enabled defaults to true");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void vocanaApiAndLexiconAreReachable(GameTestHelper h) {
        h.assertTrue(VocanaApi.essences() != null, "VocanaApi answers");
        h.assertTrue(VocanaApi.essence("VOCANACOMPAT_PROBE") == null, "an unregistered essence is unknown");
        h.assertTrue(!Vocabulary.INSTANCE.words().isEmpty(), "Vocana's lexicon is loaded");
        h.assertTrue(VocanaCompat.class.getResource("/META-INF/vocana/lexicon.json") != null, "the merged lexicon extension is in the resources");
        h.assertTrue(VocanaCompat.class.getResource("/META-INF/vocana/grimoire_groups.json") != null, "the merged Grimoire groups are in the resources");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void noModuleLoadsWithoutItsMod(GameTestHelper h) {
        for (var m : Module.values()) {
            h.assertFalse(ModList.get().isLoaded(m.modId), m.modId + " is not on the GameTest classpath");
            h.assertFalse(VocanaCompat.loaded(m), m.id + " did not load");
        }
        h.assertTrue(VocanaCompat.loaded().isEmpty(), "no module loaded");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theModuleEnabledConditionIsRegisteredAndFalseHere(GameTestHelper h) {
        var key = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "module_enabled");
        h.assertTrue(NeoForgeRegistries.CONDITION_SERIALIZERS.containsKey(key), "vocanacompat:module_enabled is registered");
        h.assertFalse(new ModuleEnabledCondition("create").test(ICondition.IContext.EMPTY), "an unloaded module's data stays out");
        h.assertFalse(new ModuleEnabledCondition("nonsense").test(ICondition.IContext.EMPTY), "an unknown module is false, not an error");
        h.succeed();
    }
}
