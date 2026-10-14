package dev.vocana.compat.emi;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the EMI module, {@code config/vocanacompat-emi.toml}. The category toggles are read on the client when EMI
 * reloads (a change needs a rejoin or an EMI reload); {@code syncRecipes} is read on the server when a player joins.
 * Add values here, never to the shared {@code CompatConfig}.
 */
public final class EmiConfig {
    private EmiConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue imbueCategory, ritualCategory, lexiconCategory, proficiencyPage, syncRecipes, verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.comment("Which EMI pages the module adds (client side; EMI rebuilds them when you join a world).")
            .translation("vocanacompat.configuration.emi.categories").push("categories");
        imbueCategory = b.comment("The Imbue Bench category: one entry per imbue recipe, with its target, materials, price and ritual.")
            .translation("vocanacompat.configuration.emi.categories.imbue").define("imbue", true);
        ritualCategory = b.comment("The ritual category: the ritual figure, the bench's channel step and stripping an imbue.")
            .translation("vocanacompat.configuration.emi.categories.ritual").define("ritual", true);
        lexiconCategory = b.comment("The lexicon category: one page per word of the language, named only once you have heard it.")
            .translation("vocanacompat.configuration.emi.categories.lexicon").define("lexicon", true);
        proficiencyPage = b.comment("The proficiency page in the lexicon category: the five levels, their thresholds and your practised chants.")
            .translation("vocanacompat.configuration.emi.categories.proficiency").define("proficiency", true);
        b.pop();
        b.translation("vocanacompat.configuration.emi").push("general");
        syncRecipes = b.comment("Server side: send the Imbue Bench's recipes to joining clients so EMI can list them (clients without the mod ignore it).")
            .translation("vocanacompat.configuration.emi.syncRecipes").define("syncRecipes", true);
        verboseLogging = b.comment("Log what the EMI module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.emi.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
