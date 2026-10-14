package dev.vocana.compat.curios;

import dev.vocana.Vocana;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.type.ISlotType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A log of what the module wired up, for testing in game. Two moments, because Curios' slots and the item tags are
 * data: at {@code FMLCommonSetupEvent} only the registrations are known (the items, what they expect of the slots, the
 * config); once a server has started ({@code ServerStartedEvent}, after the datapacks and tags are loaded) the player's
 * resolved slot set ({@code CuriosApi.getPlayerSlots(false)}) and whether each item landed in its {@code curios:<slot>}
 * tag can be read and are logged, with a warning for anything missing. {@code verboseLogging} raises the lines to INFO.
 */
final class CuriosSelfCheck {
    private CuriosSelfCheck() {}
    private record Expect(Item item, String slot) {}

    private static List<Expect> expectations() {
        return List.of(new Expect(Vocana.TORC.get(), CurioSlots.NECKLACE), new Expect(Vocana.RELIC.get(), CurioSlots.CHARM),
            new Expect(CuriosItems.RING_OF_FOCUS.get(), CurioSlots.RING), new Expect(CuriosItems.SIGNET_OF_THE_ELEMENTS.get(), CurioSlots.RING),
            new Expect(CuriosItems.AMULET_OF_RESONANCE.get(), CurioSlots.NECKLACE), new Expect(CuriosItems.CHARM_OF_CALM.get(), CurioSlots.CHARM));
    }
    private static void log(String format, Object... args) {
        if (CuriosConfig.get(CuriosConfig.verboseLogging)) VocanaCompat.LOG.info(format, args); else VocanaCompat.LOG.debug(format, args);
    }

    static void commonSetup(FMLCommonSetupEvent e) {
        List<String> items = new ArrayList<>();
        for (var x : expectations()) items.add(BuiltInRegistries.ITEM.getKey(x.item) + " -> " + x.slot);
        log("curios module: registered {} items for Curios slots: {}", items.size(), items);
        log("curios module: Ring of Focus +{} Capacity, Amulet regen x{}, Charm strain x{}, ring-slot essence cost x{}, relic whispers {} after {} s",
            CuriosConfig.get(CuriosConfig.ringCapacity), CuriosConfig.get(CuriosConfig.amuletRegen), CuriosConfig.get(CuriosConfig.charmStrain),
            CuriosConfig.get(CuriosConfig.signetCostFactor), CuriosConfig.get(CuriosConfig.relicWhispers), CuriosConfig.get(CuriosConfig.relicWhisperSeconds));
    }

    static void serverStarted(ServerStartedEvent e) {
        Map<String, ISlotType> slots = CuriosApi.getPlayerSlots(false);
        List<String> have = new ArrayList<>();
        for (var s : slots.values()) have.add(s.getIdentifier() + "x" + s.getSize());
        log("curios module: player slots {}", have);
        for (String id : List.of(CurioSlots.NECKLACE, CurioSlots.RING, CurioSlots.CHARM))
            if (!slots.containsKey(id) || slots.get(id).getSize() <= 0) VocanaCompat.LOG.warn("curios module: players have no '{}' slot; data/vocanacompat/curios/entities/curios/player.json did not apply", id);
        for (var x : expectations()) {
            boolean tagged = new ItemStack(x.item).is(CuriosTags.createItemTag(x.slot));
            if (tagged) log("curios module: {} is in curios:{}", BuiltInRegistries.ITEM.getKey(x.item), x.slot);
            else VocanaCompat.LOG.warn("curios module: {} is not in the item tag curios:{} (data/curios/tags/item/{}.json did not apply)", BuiltInRegistries.ITEM.getKey(x.item), x.slot, x.slot);
        }
    }
}
