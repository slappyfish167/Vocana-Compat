package dev.vocana.compat.wyrmroost;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Wyrmroost <-> Vocana. Loaded only when the mod {@code wyrmroost} is installed and {@code modules.wyrmroost.enabled}
 * is on. Only classes in this package may import {@code com.github.wolfshotz.wyrmroost}, and in fact only
 * {@link Dragons} does. What the module adds:
 * <ul>
 *   <li>Discovery: Wyrmroost's dragons, breath, eggs and ores teach Vocana's words through the module's
 *       {@code vocanacompat:wyrmroost/<gloss>} tags, referenced from {@code vocana:hears/<gloss>} (data only).</li>
 *   <li>Words ({@code src/main/lexicon/wyrmroost.json}): the Essence WYRM ({@link WyrmEssence}), the Deed ROOST
 *       ({@link RoostDeed}) and the Elder shade DRAGONFIRE of FIRE, which the core runs itself from its profile.</li>
 *   <li>Interplay: a tamed dragon refuses its own caster's magic damage ({@link TamedWard}); Vocana's FOLLOW, SUMMON,
 *       BIND and COMMAND already treat a tamed dragon as consenting or as a will to contest, since every dragon is a
 *       {@code TamableAnimal}.</li>
 *   <li>Imbue and crafting recipes under {@code data/vocanacompat/.../wyrmroost/}.</li>
 * </ul>
 * Settings live in {@link WyrmroostConfig} ({@code config/vocanacompat-wyrmroost.toml}). See docs/wyrmroost.md.
 */
public final class WyrmroostCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.WYRMROOST, WyrmroostConfig.SPEC);
        // A Wyrmroost build whose classes moved fails here, where the loader drops the module and Vocana keeps running.
        String probed = Dragons.probe();
        // The behaviours check their toggles when they act: the COMMON config is not loaded yet during construction.
        VocanaApi.registerEssence("WYRM", new WyrmEssence());
        VocanaApi.registerDeed("ROOST", new RoostDeed());
        NeoForge.EVENT_BUS.addListener(TamedWard::onIncomingDamage);
        modBus.addListener((FMLCommonSetupEvent e) -> VocanaCompat.LOG.info("wyrmroost module: WYRM and ROOST registered, tamed dragons warded; compiled against {}", probed));
    }
}
