package dev.vocana.compat.iceandfire;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Ice and Fire module, {@code config/vocanacompat-iceandfire.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class IceAndFireConfig {
    private IceAndFireConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.iceandfire").push("general");
        verboseLogging = b.comment("Log what the Ice and Fire module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.iceandfire.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
