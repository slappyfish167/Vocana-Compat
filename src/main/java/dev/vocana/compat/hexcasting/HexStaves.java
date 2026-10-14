package dev.vocana.compat.hexcasting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Hex staves as Vocana Catalysts. Hex recognises its staves by the item tag {@code hexcasting:staves}
 * ({@code at.petrak.hexcasting.api.mod.HexTags.Items.STAVES}; the client casting screen checks that tag, not the
 * class), and every wood, the edified, quenched and mindsplice staves are in it, so the tag is the contract and the
 * module needs no Hex class to read it. Registered through {@code VocanaApi.registerCatalyst}, which makes a held staff
 * open the Chime screen, carry a spoken chant, tune a Chorus and serve as a Bound Catalyst for KEEP, with the
 * configured Capacity on top of the caster's own.
 */
final class HexStaves {
    private HexStaves() {}
    /** The same id as HexTags.Items.STAVES (verified in both HexCast NEOFORGED and HexMod 1.21). */
    static final TagKey<Item> STAVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("hexcasting", "staves"));

    static boolean isStaff(ItemStack stack) {
        return HexcastingConfig.get(HexcastingConfig.stavesAsCatalysts) && !stack.isEmpty() && stack.is(STAVES);
    }
    static int capacityBonus(ItemStack stack) {
        return HexcastingConfig.get(HexcastingConfig.staffCapacityBonus);
    }
}
