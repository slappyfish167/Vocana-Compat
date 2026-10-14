package dev.vocana.compat.emi;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * EMI <-> Vocana. Loaded only when the mod {@code emi} is installed and {@code modules.emi.enabled} is on. Only classes
 * in this package may import {@code dev.emi.emi}, and only the client-side ones do: EMI is a client mod, so everything
 * that touches it ({@link VocanaEmiPlugin} and the recipe classes it builds) is loaded on the client alone. EMI finds the
 * plugin itself: on NeoForge it walks every mod jar's annotation scan data for classes annotated {@code @EmiEntrypoint}
 * that implement {@code EmiPlugin} and instantiates them by their no-arg constructor (EmiAgnosNeoForge.getPluginsAgnos,
 * branch 1.21); nothing in {@code neoforge.mods.toml} is needed.
 * <p>
 * What runs on both sides is small: the module config, and a sync of the Imbue Bench's recipes to clients
 * ({@link ImbueRecipeSync}), because Vocana loads them with a server-side reload listener and never sends them. On a
 * dedicated server the module therefore does nothing but answer that sync; it registers no blocks, items or words.
 * Settings live in {@link EmiConfig} ({@code config/vocanacompat-emi.toml}). See docs/emi.md.
 */
public final class EmiCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.EMI, EmiConfig.SPEC);
        modBus.addListener(ImbueRecipeSync::register);
        NeoForge.EVENT_BUS.addListener(ImbueRecipeSync::datapackSync);
        // The client half is a separate class so a dedicated server never resolves client-only types.
        if (FMLEnvironment.dist.isClient()) EmiClient.init();
    }
}
