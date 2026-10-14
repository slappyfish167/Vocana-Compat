package dev.vocana.compat.create;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Create <-> Vocana. Loaded only when the mod {@code create} is installed and {@code modules.create.enabled} is on.
 * Only classes in this package may import {@code com.simibubi.create}, Flywheel, Ponder or Registrate.
 * Settings live in {@link CreateConfig} ({@code config/vocanacompat-create.toml}); words in
 * {@code src/main/lexicon/create.json}; data under {@code data/vocanacompat/<kind>/create/}. See docs/adding_a_module.md.
 */
public final class CreateCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.CREATE, CreateConfig.SPEC);
        // The module's work starts here: VocanaApi registrations, DeferredRegisters on modBus, NeoForge.EVENT_BUS listeners.
    }
}
