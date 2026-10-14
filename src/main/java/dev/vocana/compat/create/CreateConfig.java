package dev.vocana.compat.create;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings of the Create module, {@code config/vocanacompat-create.toml}. Add values here, never to the shared {@code CompatConfig}. */
public final class CreateConfig {
    private CreateConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.create").push("general");
        verboseLogging = b.comment("Log what the Create module does (recipes injected, words taught) at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.create.verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
