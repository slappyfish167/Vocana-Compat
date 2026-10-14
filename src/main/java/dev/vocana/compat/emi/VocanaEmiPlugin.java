package dev.vocana.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.vocana.Vocana;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Vocabulary;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;

/**
 * The EMI plugin. EMI finds it by its annotation in this jar's scan data and instantiates it on the client when EMI
 * reloads (on every world join, after recipes and tags are synced), on EMI's reload thread
 * ({@code EmiReloadManager.ReloadWorker}, branch 1.21); {@link #register} then adds three categories:
 * <ul>
 * <li>{@code vocanacompat:imbue}: one {@link ImbueEmiRecipe} per Imbue Bench recipe the client can see
 *     ({@link EmiClient#recipes}), the bench as workstation, or one page saying why there are none;</li>
 * <li>{@code vocanacompat:ritual}: the {@link RitualPages} (figure, channel, strip), the bench as workstation;</li>
 * <li>{@code vocanacompat:lexicon}: the {@link ProficiencyEmiRecipe} and one {@link WordEmiRecipe} per word, with the
 *     Tuning Chime and the staffs as workstations, sorted by Grimoire group, chapter and lexicon order.</li>
 * </ul>
 * Residues are told apart by their essence ({@code vocana:word}) through a default comparison on the item. Each
 * category answers to a toggle in {@link EmiConfig}. Category names come from {@code emi.category.vocanacompat.<id>}
 * (EmiRecipeCategory.getName via EmiUtil.translateId).
 */
@EmiEntrypoint
public final class VocanaEmiPlugin implements EmiPlugin {
    public static final ResourceLocation IMBUE_ID = id("imbue"), RITUAL_ID = id("ritual"), LEXICON_ID = id("lexicon");
    public static final EmiRecipeCategory IMBUE = new EmiRecipeCategory(IMBUE_ID, EmiStack.of(Vocana.IMBUE_BENCH_ITEM.get()));
    public static final EmiRecipeCategory RITUAL = new EmiRecipeCategory(RITUAL_ID, EmiStack.of(Items.AMETHYST_BLOCK));
    public static final EmiRecipeCategory LEXICON = new EmiRecipeCategory(LEXICON_ID, EmiStack.of(Vocana.CHIME.get()), EmiStack.of(Vocana.CHIME.get()), lexiconOrder());

    static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, path); }

    /** The proficiency page first, then the words by group, chapter and lexicon order, as the Grimoire turns its pages. */
    private static Comparator<EmiRecipe> lexiconOrder() {
        return (a, b) -> {
            boolean wa = a instanceof WordEmiRecipe, wb = b instanceof WordEmiRecipe;
            if (wa != wb) return wa ? 1 : -1;
            if (!wa) return 0;
            WordEmiRecipe x = (WordEmiRecipe) a, y = (WordEmiRecipe) b;
            int c = Integer.compare(x.groupIndex(), y.groupIndex());
            if (c == 0) c = Integer.compare(x.group.chapters().indexOf(x.chapter), y.group.chapters().indexOf(y.chapter));
            return c != 0 ? c : Integer.compare(x.index, y.index);
        };
    }

    @Override public void register(EmiRegistry registry) {
        EmiStack bench = EmiStack.of(Vocana.IMBUE_BENCH_ITEM.get());
        registry.setDefaultComparison(Vocana.RESIDUE.get(), Comparison.compareComponents());
        if (EmiConfig.get(EmiConfig.imbueCategory)) {
            registry.addCategory(IMBUE);
            registry.addWorkstation(IMBUE, bench);
            var recipes = EmiClient.recipes();
            if (recipes.isEmpty()) {
                registry.addRecipe(new TextPage(IMBUE, id("imbue/none"), List.of(), List.of(bench), List.of(),
                    Component.translatable("vocanacompat.emi.imbue.none.title"), List.of(Component.translatable("vocanacompat.emi.imbue.none.text"))));
            } else for (var r : recipes) {
                try { registry.addRecipe(new ImbueEmiRecipe(IMBUE, r)); }
                catch (RuntimeException e) { VocanaCompat.LOG.warn("emi module: imbue recipe {} is not shown: {}", r.id(), e.toString()); }
            }
            log("imbue category: {} recipe(s)", recipes.size());
        }
        if (EmiConfig.get(EmiConfig.ritualCategory)) {
            registry.addCategory(RITUAL);
            registry.addWorkstation(RITUAL, bench);
            for (var page : RitualPages.all(RITUAL)) registry.addRecipe(page);
        }
        if (EmiConfig.get(EmiConfig.lexiconCategory)) {
            registry.addCategory(LEXICON);
            registry.addWorkstation(LEXICON, EmiStack.of(Vocana.CHIME.get()));
            for (var staff : Vocana.STAFFS.values()) registry.addWorkstation(LEXICON, EmiStack.of(staff.get()));
            if (EmiConfig.get(EmiConfig.proficiencyPage)) registry.addRecipe(new ProficiencyEmiRecipe());
            int i = 0;
            for (Vocabulary.Word w : Vocabulary.INSTANCE.words()) registry.addRecipe(new WordEmiRecipe(w, i++));
            log("lexicon category: {} word page(s)", i);
        }
    }
    private static void log(String message, Object... args) {
        if (EmiConfig.get(EmiConfig.verboseLogging)) VocanaCompat.LOG.info("emi module: " + message, args);
        else VocanaCompat.LOG.debug("emi module: " + message, args);
    }
}
