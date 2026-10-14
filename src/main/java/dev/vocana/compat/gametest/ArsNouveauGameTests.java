package dev.vocana.compat.gametest;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.compat.ars_nouveau.ArsNouveauConfig;
import dev.vocana.compat.ars_nouveau.Conversion;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.Vocabulary;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Ars Nouveau module without Ars Nouveau: the GameTest server carries only Vocana and this mod, so these tests
 * check what must hold either way (the word is in the merged lexicon with its chapter and school, the balance defaults
 * keep their promises, the exchange arithmetic loses on every trip) and what must stay out without the mod (the
 * essence behaviour, the glyph recipe, the discovery tags). {@code ArsNouveauConfig} and {@code Conversion} import
 * nothing of Ars, which is why they may be touched from here. Dev-only (excluded from the jar).
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class ArsNouveauGameTests {
    private ArsNouveauGameTests() {}

    @GameTest(template = "test_empty")
    public static void theSourceWordIsInTheLexicon(GameTestHelper h) {
        var word = Vocabulary.INSTANCE.byGloss("SOURCE");
        h.assertTrue(word != null, "SOURCE is in the merged lexicon");
        h.assertValueEqual(word.form(), "nauspi", "its form");
        h.assertValueEqual(word.category(), "Essence", "its class");
        h.assertValueEqual(word.discipline(), "Thaumaturgy", "its school");
        h.assertTrue(Chapter.of(word) == Chapter.ARCANA, "the Grimoire places it in Thaumaturgy, found " + Chapter.of(word));
        h.assertTrue(word.syllables() == 2 && !word.ipa().isEmpty(), "it has a respelling of two syllables and an IPA");
        h.assertValueEqual(Vocabulary.INSTANCE.form("SOURCE"), "nauspi", "the gloss resolves to the form");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void nothingOfTheModuleIsRegisteredWithoutArs(GameTestHelper h) {
        h.assertFalse(VocanaCompat.loaded(Module.ARS_NOUVEAU), "the module did not load");
        h.assertTrue(VocanaApi.essence("SOURCE") == null, "no behaviour was registered for SOURCE");
        var recipes = h.getLevel().getRecipeManager();
        h.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "ars_nouveau/glyph_vocana_thrum")).isEmpty(), "the glyph recipe stayed out (its conditions failed)");
        var blocks = h.getLevel().registryAccess().registryOrThrow(Registries.BLOCK);
        int hears = blocks.getTag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("vocana", "hears/source"))).map(t -> t.size()).orElse(0);
        h.assertTrue(hears == 0, "no block teaches SOURCE without Ars, found " + hears);
        var entities = h.getLevel().registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        int bearers = entities.getTag(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "ars_nouveau/source"))).map(t -> t.size()).orElse(0);
        h.assertTrue(bearers == 0, "the module's bearer tag stayed out, found " + bearers);
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theBalanceDefaultsAreSane(GameTestHelper h) {
        double ars = ArsNouveauConfig.get(ArsNouveauConfig.arsCostFactor), staff = ArsNouveauConfig.get(ArsNouveauConfig.vocanaStaffArsDiscount);
        double vocana = ArsNouveauConfig.get(ArsNouveauConfig.vocanaCostFactor), loss = ArsNouveauConfig.get(ArsNouveauConfig.conversionLoss);
        h.assertTrue(ars >= 1.1 && ars <= 1.25, "Ars is 10-25 % dearer by default: " + ars);
        h.assertTrue(ArsNouveauConfig.get(ArsNouveauConfig.arsResourceFactor) >= 1.1, "Ars resources are dearer by default");
        h.assertTrue(vocana == 1.0, "Vocana keeps its own prices by default");
        h.assertTrue(loss >= .3, "every conversion loses at least three tenths: " + loss);
        h.assertTrue(ars * staff >= 1, "the staff discount never makes Ars cheaper than parity: " + ars * staff);
        h.assertTrue(Conversion.guardsHold(ars, staff, vocana, loss), "the guards hold together");
        h.assertFalse(ArsNouveauConfig.get(ArsNouveauConfig.applyToMachines), "machines are left alone by default");
        h.assertTrue(ArsNouveauConfig.get(ArsNouveauConfig.resonateCooldownTicks) >= 40, "Resonate has a cooldown");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theExchangeLosesOnEveryTrip(GameTestHelper h) {
        double mana = ArsNouveauConfig.get(ArsNouveauConfig.manaPerThrum), source = ArsNouveauConfig.get(ArsNouveauConfig.sourcePerMana);
        double loss = ArsNouveauConfig.get(ArsNouveauConfig.conversionLoss), resources = ArsNouveauConfig.get(ArsNouveauConfig.arsResourceFactor);
        double thrum = Conversion.thrumForMana(Conversion.RESONATE_MANA, mana, loss);
        h.assertTrue(thrum > 0 && thrum * mana < Conversion.RESONATE_MANA * (1 - .3), "Resonate returns less than par: " + thrum + " Thrum for " + Conversion.RESONATE_MANA + " mana");
        double budget = Conversion.sourceBudget(thrum, mana, source, loss, resources);
        h.assertTrue(budget < Conversion.RESONATE_MANA * source * Conversion.roundTrip(loss) + 1e-9, "mana to Thrum to source keeps under (1 - loss)^2 of the start: " + budget);
        h.assertTrue(Conversion.sourceForTouch(ArsNouveauConfig.get(ArsNouveauConfig.sourcePerCast), 1, budget) <= budget, "a touch never exceeds the budget");
        h.assertTrue(Conversion.arsCost(100, ArsNouveauConfig.get(ArsNouveauConfig.arsCostFactor), ArsNouveauConfig.get(ArsNouveauConfig.vocanaStaffArsDiscount), true) >= 100, "a staff in the off hand still leaves an Ars spell at least its own price");
        h.succeed();
    }
}
