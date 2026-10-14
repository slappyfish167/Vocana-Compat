package dev.vocana.compat.create;

import com.mojang.logging.LogUtils;
import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Create <-> Vocana. Loaded only when the mod {@code create} is installed and {@code modules.create.enabled} is on.
 * Only classes in this package may import {@code com.simibubi.create}, Flywheel, Ponder or Registrate.
 *
 * <p>What it adds: the words KINETIC (an essence, {@link KineticEssence}), SPIN (a Force Deed, {@link SpinDeed}) and
 * BRASS (an Elder shade of METAL, data only) from {@code src/main/lexicon/create.json}, taught by Create's machines
 * through the module's {@code vocana:hears} tags; a KINETIC-imbued tool that digs faster among turning machinery and
 * cranks SPIN keeps turning ({@link KineticEvents}, {@link Spinners}); imbue recipes paid in brass, andesite alloy and
 * precision mechanisms ({@code data/vocanacompat/vocana_imbue/create/}); Create recipes that make Vocana's gear on the
 * assembly line ({@code data/vocanacompat/recipe/create/}, with {@link CreateItems}); and Create's structural blocks in
 * {@code vocana:immune}. Settings live in {@link CreateConfig} ({@code config/vocanacompat-create.toml}).
 */
public final class CreateCompat implements CompatModule {
    static final Logger LOG = LogUtils.getLogger();

    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.CREATE, CreateConfig.SPEC);
        CreateItems.register(modBus);
        VocanaApi.registerEssence(KineticEssence.GLOSS, new KineticEssence());
        VocanaApi.registerDeed(SpinDeed.GLOSS, new SpinDeed());
        NeoForge.EVENT_BUS.register(KineticEvents.class);
        LOG.info("Create module: {} and {} registered; BRASS shades METAL from the lexicon", KineticEssence.GLOSS, SpinDeed.GLOSS);
    }
}
