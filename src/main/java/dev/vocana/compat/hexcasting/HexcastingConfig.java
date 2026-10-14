package dev.vocana.compat.hexcasting;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Hex Casting module, {@code config/vocanacompat-hexcasting.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class HexcastingConfig {
    private HexcastingConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.hexcasting").push("general");
        verboseLogging = b.comment("Log what the Hex Casting module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.hexcasting.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
