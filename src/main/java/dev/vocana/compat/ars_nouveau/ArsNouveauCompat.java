package dev.vocana.compat.ars_nouveau;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Ars Nouveau <-> Vocana. Loaded only when the mod {@code ars_nouveau} is installed and
 * {@code modules.ars_nouveau.enabled} is on. Only classes in this package may import
 * {@code com.hollingsworth.arsnouveau}, GeckoLib or Nuggets. Settings live in {@link ArsNouveauConfig}
 * ({@code config/vocanacompat-ars_nouveau.toml}); words in {@code src/main/lexicon/ars_nouveau.json}; data under
 * {@code data/vocanacompat/<kind>/ars_nouveau/}. See docs/adding_a_module.md.
 */
public final class ArsNouveauCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.ARS_NOUVEAU, ArsNouveauConfig.SPEC);
    }
}
