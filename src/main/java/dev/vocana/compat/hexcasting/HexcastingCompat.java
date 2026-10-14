package dev.vocana.compat.hexcasting;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Hex Casting (HexCast NEOFORGED) <-> Vocana. Loaded only when the mod {@code hexcasting} is installed and
 * {@code modules.hexcasting.enabled} is on. Only classes in this package may import {@code at.petrak.hexcasting},
 * PAUCAL, Patchouli, Caelus or Kotlin types. Settings live in {@link HexcastingConfig}
 * ({@code config/vocanacompat-hexcasting.toml}); words in {@code src/main/lexicon/hexcasting.json}; data under
 * {@code data/vocanacompat/<kind>/hexcasting/}. See docs/adding_a_module.md.
 */
public final class HexcastingCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.HEXCAST, HexcastingConfig.SPEC);
    }
}
