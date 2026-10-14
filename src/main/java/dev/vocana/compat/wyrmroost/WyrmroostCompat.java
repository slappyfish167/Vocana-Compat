package dev.vocana.compat.wyrmroost;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Wyrmroost <-> Vocana. Loaded only when the mod {@code wyrmroost} is installed and {@code modules.wyrmroost.enabled}
 * is on. Only classes in this package may import {@code com.github.wolfshotz.wyrmroost}.
 * Settings live in {@link WyrmroostConfig} ({@code config/vocanacompat-wyrmroost.toml}); words in
 * {@code src/main/lexicon/wyrmroost.json}; data under {@code data/vocanacompat/<kind>/wyrmroost/}. See docs/adding_a_module.md.
 */
public final class WyrmroostCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.WYRMROOST, WyrmroostConfig.SPEC);
    }
}
