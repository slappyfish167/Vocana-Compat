package dev.vocana.compat.curios;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Curios API <-> Vocana. Loaded only when the mod {@code curios} is installed and {@code modules.curios.enabled} is
 * on; only classes in this package import {@code top.theillusivec4.curios}. What it does:
 * <ul>
 * <li><b>Slots for Vocana's items</b> (data): the Torc fits the necklace slot and a Whispering Relic the charm slot
 * ({@code data/curios/tags/item/*.json}); players get the {@code necklace}, {@code ring} and {@code charm} presets
 * ({@code data/vocanacompat/curios/entities/curios/player.json}), since Curios 9.5.1 defines the preset slot types
 * but assigns none to any entity until a mod asks.</li>
 * <li><b>A worn Torc</b>: {@code VocanaApi.registerWorn} with {@link CurioSlots#wearsTorc}, so a Torc in a slot lets
 * its wearer chant hands-free as a carried one does. Vocana's own inventory scan cannot be switched off from here.</li>
 * <li><b>Focus items</b> ({@link CuriosItems}): Capacity, regeneration, Strain and essence cost through the
 * {@code VocanaApi} caster modifiers, every number in {@link CuriosConfig}.</li>
 * <li><b>Vocana's items as curios</b> ({@link VocanaCurios}): the Torc goes on by use; a relic worn long enough
 * whispers its word.</li>
 * </ul>
 * See docs/curios.md.
 */
public final class CuriosCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.CURIOS, CuriosConfig.SPEC);
        CuriosItems.register(modBus);
        // The static registries of VocanaApi take registrations during construction; Vocana consults them at the Seal.
        VocanaApi.registerWorn(CurioSlots::wearsTorc);
        VocanaApi.registerCapacity(CurioSlots::capacity);
        VocanaApi.registerRegen(CurioSlots::regen);
        VocanaApi.registerStrain(CurioSlots::strain);
        VocanaApi.registerCostFactor(CurioSlots::cost);
        modBus.addListener((FMLCommonSetupEvent e) -> { VocanaCurios.register(); CuriosSelfCheck.commonSetup(e); });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> CuriosSelfCheck.serverStarted(e));
    }
}
