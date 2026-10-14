package dev.vocana.compat.wyrmroost;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Wyrmroost module, {@code config/vocanacompat-wyrmroost.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class WyrmroostConfig {
    private WyrmroostConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.wyrmroost").push("general");
        verboseLogging = b.comment("Log what the Wyrmroost module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.wyrmroost.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
