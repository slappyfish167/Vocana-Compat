package dev.vocana.compat.iceandfire;

import dev.vocana.compat.VocanaCompat;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The module's own items, registered under {@code vocanacompat} only when the module loads (the registers are bound to
 * the mod bus from {@link IceAndFireCompat#init}, which runs during mod construction, before the registry events).
 * Wyrmsteel is the netherite-dragonsteel alloy: with Create the ingot comes from a superheated mixer and the whole
 * set (staff, five tools, four armour pieces) from sequenced assembly, each piece from its netherite counterpart
 * through its own incomplete piece (the transitional item of its line); the block packs nine ingots on a press. A
 * dragon forge alloys the ingot too and, without Create, the smithing table makes the set, all of it data. Wyrmsteel
 * dragon armour ({@link WyrmsteelDragonArmorItem}) is smithed from dragonsteel dragon armour. The tier gives the tools
 * their mining level, durability and repair material, the armour material the armour's; the attack and armour figures
 * are rewritten from the config by {@link DragonsteelRebalance} like
 * dragonsteel's, so the figures built in here are only what shows with the rebalance turned off. They are the
 * rebalance's defaults ({@code [dragonsteel.wyrmsteel]}: damage +3, armour +1, toughness 4, knockback resistance
 * 0.15), so turning it off changes nothing until the offsets are edited.
 */
public final class IceAndFireItems {
    private IceAndFireItems() {}
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VocanaCompat.ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VocanaCompat.ID);
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, VocanaCompat.ID);

    /** Wyrmsteel's durability (netherite's 2031 and a half again) and mining: it digs what netherite digs. Fixed at registration, before configs load. */
    public static final int DURABILITY = 3000;
    public static final Tier TIER = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, DURABILITY, 10f, 4f, 20, () -> Ingredient.of(IceAndFireItems.INGOT.get()));
    /** The damage each tool has over its netherite counterpart (the tier's bonus is netherite's 4): the wyrmsteel offsets' default. */
    public static final int EDGE = 3;

    /** The armour's durability multiplier (netherite's 37; 45 keeps the tools' half again): helmet 495, chestplate 720, leggings 675, boots 585. */
    public static final int ARMOR_DURABILITY_MULTIPLIER = 45;
    /** Armour per piece and the toughness and knockback resistance of each: netherite's 3/8/6/3 plus the wyrmsteel offsets' default one, 4 and 0.15. */
    public static final int HELMET_ARMOR = 4, CHESTPLATE_ARMOR = 9, LEGGINGS_ARMOR = 7, BOOTS_ARMOR = 4, BODY_ARMOR = 11;
    public static final float TOUGHNESS = 4f, KNOCKBACK_RESISTANCE = 0.15f;
    /** The worn look: {@code textures/models/armor/wyrmsteel_layer_1.png} and {@code _layer_2.png}. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMOR_MATERIAL = ARMOR_MATERIALS.register("wyrmsteel", () -> new ArmorMaterial(
        Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.HELMET, HELMET_ARMOR); m.put(ArmorItem.Type.CHESTPLATE, CHESTPLATE_ARMOR);
            m.put(ArmorItem.Type.LEGGINGS, LEGGINGS_ARMOR); m.put(ArmorItem.Type.BOOTS, BOOTS_ARMOR); m.put(ArmorItem.Type.BODY, BODY_ARMOR);
        }),
        15, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(IceAndFireItems.INGOT.get()),
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "wyrmsteel"))), TOUGHNESS, KNOCKBACK_RESISTANCE));

    public static final DeferredItem<Item> INGOT = ITEMS.register("netherite_dragonsteel_ingot", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredBlock<Block> BLOCK = BLOCKS.register("wyrmsteel_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredItem<Item> BLOCK_ITEM = ITEMS.register("wyrmsteel_block", () -> new BlockItem(BLOCK.get(), new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> STAFF = ITEMS.register("wyrmsteel_staff", () -> new WyrmsteelStaffItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> SWORD = ITEMS.register("wyrmsteel_sword", () -> new SwordItem(TIER, gear().attributes(SwordItem.createAttributes(TIER, 3 + EDGE, -2.4f))));
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("wyrmsteel_pickaxe", () -> new PickaxeItem(TIER, gear().attributes(PickaxeItem.createAttributes(TIER, 1f + EDGE, -2.8f))));
    public static final DeferredItem<Item> AXE = ITEMS.register("wyrmsteel_axe", () -> new AxeItem(TIER, gear().attributes(AxeItem.createAttributes(TIER, 5f + EDGE, -3.0f))));
    public static final DeferredItem<Item> SHOVEL = ITEMS.register("wyrmsteel_shovel", () -> new ShovelItem(TIER, gear().attributes(ShovelItem.createAttributes(TIER, 1.5f + EDGE, -3.0f))));
    public static final DeferredItem<Item> HOE = ITEMS.register("wyrmsteel_hoe", () -> new HoeItem(TIER, gear().attributes(HoeItem.createAttributes(TIER, -4f + EDGE, 0f))));
    public static final DeferredItem<Item> HELMET = armor("wyrmsteel_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<Item> CHESTPLATE = armor("wyrmsteel_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<Item> LEGGINGS = armor("wyrmsteel_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<Item> BOOTS = armor("wyrmsteel_boots", ArmorItem.Type.BOOTS);
    /** Wyrmsteel dragon armour, keyed by Ice and Fire's part id: smithed from dragonsteel dragon armour with a Block of Wyrmsteel. */
    public static final Map<String, DeferredItem<Item>> DRAGON_ARMOR = new LinkedHashMap<>();
    static {
        for (String part : List.of("head", "neck", "body", "tail"))
            DRAGON_ARMOR.put(part, ITEMS.register("dragonarmor_wyrmsteel_" + part, () -> WyrmsteelDragonArmorItem.of(part)));
    }

    /** The pieces an assembly line carries between its steps; plain items, one per line so Create never confuses the lines. Keyed by the finished piece's id. */
    public static final Map<String, DeferredItem<Item>> INCOMPLETE = new LinkedHashMap<>();
    public static final DeferredItem<Item> INCOMPLETE_STAFF = incomplete("wyrmsteel_staff");
    public static final DeferredItem<Item> INCOMPLETE_SWORD = incomplete("wyrmsteel_sword");
    public static final DeferredItem<Item> INCOMPLETE_PICKAXE = incomplete("wyrmsteel_pickaxe");
    public static final DeferredItem<Item> INCOMPLETE_AXE = incomplete("wyrmsteel_axe");
    public static final DeferredItem<Item> INCOMPLETE_SHOVEL = incomplete("wyrmsteel_shovel");
    public static final DeferredItem<Item> INCOMPLETE_HOE = incomplete("wyrmsteel_hoe");
    public static final DeferredItem<Item> INCOMPLETE_HELMET = incomplete("wyrmsteel_helmet");
    public static final DeferredItem<Item> INCOMPLETE_CHESTPLATE = incomplete("wyrmsteel_chestplate");
    public static final DeferredItem<Item> INCOMPLETE_LEGGINGS = incomplete("wyrmsteel_leggings");
    public static final DeferredItem<Item> INCOMPLETE_BOOTS = incomplete("wyrmsteel_boots");

    private static Item.Properties gear() { return new Item.Properties().fireResistant().rarity(Rarity.EPIC); }
    private static DeferredItem<Item> armor(String id, ArmorItem.Type type) {
        return ITEMS.register(id, () -> new ArmorItem(ARMOR_MATERIAL, type, gear().durability(type.getDurability(ARMOR_DURABILITY_MULTIPLIER))));
    }
    private static DeferredItem<Item> incomplete(String finished) {
        var item = ITEMS.register("incomplete_" + finished, () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));
        INCOMPLETE.put(finished, item);
        return item;
    }

    static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(INGOT);
            else if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) e.accept(BLOCK_ITEM);
            else if (e.getTabKey() == CreativeModeTabs.COMBAT) {
                e.accept(SWORD); e.accept(AXE); e.accept(HELMET); e.accept(CHESTPLATE); e.accept(LEGGINGS); e.accept(BOOTS);
                for (var piece : DRAGON_ARMOR.values()) e.accept(piece);
            }
            else if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) { e.accept(SHOVEL); e.accept(PICKAXE); e.accept(AXE); e.accept(HOE); e.accept(STAFF); }
        });
    }
}
