package dev.vocana.compat.wyrmroost;

import java.util.Set;

/**
 * The Wyrmroost registry ids this module's data may name, as read from {@code NordAct/Wyrmroost} (branch {@code main}):
 * entity types from {@code registry/WREntities.java}, items from {@code registry/WRItems.java}, blocks from
 * {@code registry/WRBlocks.java}. Plain strings: the class loads without Wyrmroost, so unit tests can check every tag
 * and recipe against it. Not every id is used; the lists are the verified universe.
 */
public final class WyrmroostIds {
    private WyrmroostIds() {}
    public static final String MOD_ID = "wyrmroost";

    /** The ten dragon-kind species. Coin Dragon and Lesser Desertwyrm are not {@code AbstractDragonEntity}s in Wyrmroost. */
    public static final Set<String> DRAGONS = Set.of("royal_red", "alpine", "butterfly_leviathan", "overworld_drake", "silver_glider",
        "canari_wyvern", "roost_stalker", "dragon_fruit_drake", "coin_dragon", "lesser_desertwyrm");
    /** Every entity type Wyrmroost registers. */
    public static final Set<String> ENTITIES = Set.of("lesser_desertwyrm", "overworld_drake", "silver_glider", "roost_stalker", "butterfly_leviathan",
        "dragon_fruit_drake", "canari_wyvern", "royal_red", "coin_dragon", "alpine", "dragon_egg", "blue_geode_tipped_arrow", "red_geode_tipped_arrow",
        "purple_geode_tipped_arrow", "fire_breath", "wind_gust");
    /** Every block Wyrmroost registers. */
    public static final Set<String> BLOCKS = Set.of("platinum_ore", "platinum_block", "chiseled_platinum_block", "raw_platinum_block",
        "blue_geode_ore", "blue_geode_block", "red_geode_ore", "red_geode_block", "purple_geode_ore", "purple_geode_block");
    /** The items this module's recipes may name (a subset of {@code WRItems}; every block above is also an item). */
    public static final Set<String> ITEMS = Set.of("desert_wyrm", "dragon_egg", "soul_crystal", "dragon_staff", "coin_dragon", "trumpet",
        "blue_geode", "red_geode", "purple_geode", "raw_platinum", "platinum_ingot", "drake_backplate",
        "iron_dragon_armor", "gold_dragon_armor", "diamond_dragon_armor", "platinum_dragon_armor",
        "blue_geode_dragon_armor", "red_geode_dragon_armor", "purple_geode_dragon_armor",
        "platinum_sword", "platinum_pickaxe", "platinum_axe", "platinum_shovel", "platinum_hoe",
        "platinum_helmet", "platinum_chestplate", "platinum_leggings", "platinum_boots",
        "drake_helmet", "drake_chestplate", "drake_leggings", "drake_boots", "jewelled_apple");
    /** Wyrmroost's own entity-type tag of its dragons ({@code WREntities.Tags.DRAGONS}). */
    public static final String DRAGONS_TAG = "wyrmroost:dragons";
    /** The fire-breath sound ({@code WRSounds.FIRE_BREATH}), named by DRAGONFIRE's profile. */
    public static final String FIRE_BREATH_SOUND = "wyrmroost:breathweapon.fire";
}
