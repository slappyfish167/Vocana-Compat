package dev.vocana.compat.emi;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the EMI module, {@code config/vocanacompat-emi.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class EmiConfig {
    private EmiConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.emi").push("general");
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
