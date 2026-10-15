package dev.vocana.compat;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Map;

/**
 * The module toggles ({@code config/vocanacompat-modules.toml}): one {@code modules.<name>.enabled} per
 * {@link Module}, all on by default. The file is registered as a {@code STARTUP} config because the toggles decide
 * what is constructed and registered, which happens before a {@code COMMON} config would be read. Modules keep
 * their own settings in their own file ({@link Module#configFile()}); nothing else belongs here.
 */
public final class CompatConfig {
    private CompatConfig() {}

    public static final ModConfigSpec SPEC;
    private static final Map<Module, ModConfigSpec.BooleanValue> ENABLED = new EnumMap<>(Module.class);

    static {
        var b = new ModConfigSpec.Builder();
        b.comment("Which compatibility modules may load. A module also needs its mod installed; a module that is off",
                  "registers nothing and keeps its data (recipes, tags, advancements) out of the game; its words stay in the lexicon as unheard slots.")
            .translation("vocanacompat.configuration.modules").push("modules");
        for (var m : Module.values()) {
            b.translation("vocanacompat.configuration.modules." + m.id).push(m.id);
            ENABLED.put(m, b.comment("Load the " + m.displayName + " module when '" + m.modId + "' is installed.")
                .translation("vocanacompat.configuration.modules." + m.id + ".enabled").define("enabled", true));
            b.pop();
        }
        b.pop();
        SPEC = b.build();
    }

    /** Whether the module's toggle is on; its default (on) before the config has loaded, for example in a test. */
    public static boolean enabled(Module m) {
        var v = ENABLED.get(m);
        try { return v.get(); } catch (IllegalStateException e) { return v.getDefault(); }
    }
}
