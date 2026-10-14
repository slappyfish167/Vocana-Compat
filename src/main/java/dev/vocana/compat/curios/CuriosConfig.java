package dev.vocana.compat.curios;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Curios module, {@code config/vocanacompat-curios.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class CuriosConfig {
    private CuriosConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.curios").push("general");
        verboseLogging = b.comment("Log what the Curios module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.curios.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
