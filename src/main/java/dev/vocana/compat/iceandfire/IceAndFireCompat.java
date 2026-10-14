package dev.vocana.compat.iceandfire;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Ice and Fire: Community Edition <-> Vocana. Loaded only when the mod {@code iceandfire} is installed and
 * {@code modules.iceandfire.enabled} is on. Only classes in this package may import {@code com.iafenvoy.*}
 * (Ice and Fire, Uranus, Jupiter). Settings live in {@link IceAndFireConfig}
 * ({@code config/vocanacompat-iceandfire.toml}); words in {@code src/main/lexicon/iceandfire.json}; data under
 * {@code data/vocanacompat/<kind>/iceandfire/}. See docs/adding_a_module.md.
 */
public final class IceAndFireCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.ICE_AND_FIRE, IceAndFireConfig.SPEC);
    }
}
