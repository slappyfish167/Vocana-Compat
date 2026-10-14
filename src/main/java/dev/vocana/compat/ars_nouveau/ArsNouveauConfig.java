package dev.vocana.compat.ars_nouveau;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Ars Nouveau module, {@code config/vocanacompat-ars_nouveau.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class ArsNouveauConfig {
    private ArsNouveauConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.ars_nouveau").push("general");
        verboseLogging = b.comment("Log what the Ars Nouveau module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.ars_nouveau.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
