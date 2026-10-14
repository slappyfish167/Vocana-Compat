package dev.vocana.compat.curios;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Curios API <-> Vocana. Loaded only when the mod {@code curios} is installed and {@code modules.curios.enabled} is
 * on. Only classes in this package may import {@code top.theillusivec4.curios}. The expected work: a slot for the
 * Torc ({@code data/vocanacompat/curios/slots}, {@code data/vocanacompat/curios/entities}) and
 * {@code VocanaApi.registerWorn} so a Torc in that slot counts as worn. Settings live in {@link CuriosConfig}
 * ({@code config/vocanacompat-curios.toml}). See docs/adding_a_module.md.
 */
public final class CuriosCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.CURIOS, CuriosConfig.SPEC);
    }
}
