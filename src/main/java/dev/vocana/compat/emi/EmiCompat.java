package dev.vocana.compat.emi;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * EMI <-> Vocana. Loaded only when the mod {@code emi} is installed and {@code modules.emi.enabled} is on. Only
 * classes in this package may import {@code dev.emi.emi}. EMI finds plugins by scanning every mod jar for classes
 * annotated {@code @EmiEntrypoint} that implement {@code EmiPlugin} (EmiAgnosNeoForge.getPluginsAgnos), so the
 * plugin class belongs in this package too; EMI only instantiates it when EMI itself is present, but the class
 * must still not be referenced from outside. Settings live in {@link EmiConfig} ({@code config/vocanacompat-emi.toml});
 * data under {@code data/vocanacompat/<kind>/emi/}. See docs/adding_a_module.md.
 */
public final class EmiCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.EMI, EmiConfig.SPEC);
    }
}
