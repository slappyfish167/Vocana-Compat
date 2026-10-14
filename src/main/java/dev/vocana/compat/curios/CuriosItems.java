package dev.vocana.compat.curios;

import dev.vocana.compat.VocanaCompat;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The items the Curios module adds, registered under {@code vocanacompat} only when the module loads (so they do not
 * exist without Curios; the GameTests check that). Four focus items: the Ring of Focus (Capacity), the Amulet of
 * Resonance (Thrum regeneration), the Charm of Calm (Strain) and the Signet of the Elements (an essence's Workings
 * cheaper). They join Vocana's items in Tools & Utilities. Slots come from the {@code curios:ring/necklace/charm} tags.
 */
public final class CuriosItems {
    private CuriosItems() {}
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VocanaCompat.ID);

    public static final DeferredItem<Item> RING_OF_FOCUS = ITEMS.register("ring_of_focus",
        () -> new VocanaCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "item.vocanacompat.ring_of_focus.tooltip", () -> CuriosConfig.get(CuriosConfig.ringCapacity)));
    public static final DeferredItem<Item> AMULET_OF_RESONANCE = ITEMS.register("amulet_of_resonance",
        () -> new VocanaCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "item.vocanacompat.amulet_of_resonance.tooltip", () -> percent(CuriosConfig.get(CuriosConfig.amuletRegen))));
    public static final DeferredItem<Item> CHARM_OF_CALM = ITEMS.register("charm_of_calm",
        () -> new VocanaCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "item.vocanacompat.charm_of_calm.tooltip", () -> percent(CuriosConfig.get(CuriosConfig.charmStrain))));
    public static final DeferredItem<Item> SIGNET_OF_THE_ELEMENTS = ITEMS.register("signet_of_the_elements",
        () -> new SignetItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    /** A multiplier as the tooltip prints it: "+25 %" or "-15 %". */
    static String percent(double multiplier) { long pct = Math.round((multiplier - 1) * 100); return (pct >= 0 ? "+" : "") + pct + " %"; }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() != CreativeModeTabs.TOOLS_AND_UTILITIES) return;
            e.accept(RING_OF_FOCUS); e.accept(AMULET_OF_RESONANCE); e.accept(CHARM_OF_CALM); e.accept(SIGNET_OF_THE_ELEMENTS);
        });
    }
}
