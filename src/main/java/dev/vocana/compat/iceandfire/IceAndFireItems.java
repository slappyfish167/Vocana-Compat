package dev.vocana.compat.iceandfire;

import dev.vocana.compat.VocanaCompat;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The module's own items, registered under {@code vocanacompat} only when the module loads (the register is bound to
 * the mod bus from {@link IceAndFireCompat#init}, which runs during mod construction, before the registry events).
 * Wyrmsteel is the netherite-dragonsteel alloy: the ingot comes only from a superheated Create mixer, the staff, sword
 * and pickaxe only from Create sequenced assembly, each through its own incomplete piece (the transitional item of its
 * line). The tier gives the tools their mining level, durability and repair material; their attack figures are
 * rewritten from the config by {@link DragonsteelRebalance} like dragonsteel's, so the tier's own damage bonus is only
 * what shows with the rebalance turned off (netherite's 4).
 */
public final class IceAndFireItems {
    private IceAndFireItems() {}
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VocanaCompat.ID);

    /** Wyrmsteel's durability (netherite's 2031 and a half again) and mining: it digs what netherite digs. Fixed at registration, before configs load. */
    public static final int DURABILITY = 3000;
    public static final Tier TIER = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, DURABILITY, 10f, 4f, 20, () -> Ingredient.of(IceAndFireItems.INGOT.get()));

    public static final DeferredItem<Item> INGOT = ITEMS.register("netherite_dragonsteel_ingot", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> STAFF = ITEMS.register("wyrmsteel_staff", () -> new WyrmsteelStaffItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> SWORD = ITEMS.register("wyrmsteel_sword", () -> new SwordItem(TIER, new Item.Properties().fireResistant().rarity(Rarity.EPIC)
        .attributes(SwordItem.createAttributes(TIER, 3, -2.4f))));
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("wyrmsteel_pickaxe", () -> new PickaxeItem(TIER, new Item.Properties().fireResistant().rarity(Rarity.EPIC)
        .attributes(PickaxeItem.createAttributes(TIER, 1f, -2.8f))));
    /** The pieces an assembly line carries between its steps; plain items, one per line so Create never confuses the lines. */
    public static final DeferredItem<Item> INCOMPLETE_STAFF = ITEMS.register("incomplete_wyrmsteel_staff", () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));
    public static final DeferredItem<Item> INCOMPLETE_SWORD = ITEMS.register("incomplete_wyrmsteel_sword", () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));
    public static final DeferredItem<Item> INCOMPLETE_PICKAXE = ITEMS.register("incomplete_wyrmsteel_pickaxe", () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(INGOT);
            else if (e.getTabKey() == CreativeModeTabs.COMBAT) e.accept(SWORD);
            else if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) { e.accept(PICKAXE); e.accept(STAFF); }
        });
    }
}
