package dev.vocana.compat.iceandfire;

import com.iafenvoy.iceandfire.data.DragonArmorMaterial;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.DragonArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

/**
 * Wyrmsteel dragon armour, the one class of the module that names Ice and Fire's own: a dragon's armour slots take only
 * an {@code iceandfire} {@link DragonArmorItem} of the slot's part ({@code screen/slot/DragonArmorSlot}), and the dragon
 * reads its protection from the item's {@link DragonArmorMaterial} ({@code DragonBaseEntity.calculateArmorModifier}).
 * Those three types (branch {@code 1.21.1}, {@code item/DragonArmorItem.java}, {@code data/DragonArmorMaterial.java},
 * {@code data/DragonArmorPart.java}) are unchanged from Ice and Fire 2.0, the jar this module compiles against, to
 * 2.1.4. The class is only loaded with the module, so only where Ice and Fire is installed.
 * <p>
 * The material is named {@code wyrmsteel}, so Ice and Fire's renderer draws
 * {@code iceandfire:textures/entity/dragon_armor/armor_<part>_wyrmsteel.png}; the module ships no such file, and the
 * client makes it from Ice and Fire's netherite texture ({@link IceAndFireClient}). Protection 11 a piece (netherite
 * 7, dragonsteel 10; the dragon's armour stops at vanilla's 30, which a full dragonsteel set already reaches), fire
 * proof. What wyrmsteel adds is breath resistance ({@link WyrmsteelWard}).
 */
public final class WyrmsteelDragonArmorItem extends DragonArmorItem {
    public static final double PROTECTION = 11;
    /** The material's name, which picks the worn texture; a constant, so reading it never loads this class. */
    public static final String MATERIAL_NAME = "wyrmsteel";
    public static final DragonArmorMaterial MATERIAL = new DragonArmorMaterial(MATERIAL_NAME, PROTECTION, true, false);

    public WyrmsteelDragonArmorItem(DragonArmorPart part) { super(MATERIAL, part); }

    /** The piece for one of Ice and Fire's part ids ({@code head}, {@code neck}, {@code body}, {@code tail}), so no other class names {@code DragonArmorPart}. */
    static WyrmsteelDragonArmorItem of(String part) { return new WyrmsteelDragonArmorItem(DragonArmorPart.valueOf(part.toUpperCase(Locale.ROOT))); }

    /** One name for the four parts, as Ice and Fire names its own; Ice and Fire's lookup would point into its namespace. */
    @Override public String getDescriptionId() { return "item.vocanacompat.dragonarmor_wyrmsteel"; }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.vocanacompat.dragonarmor_wyrmsteel.tooltip",
            Math.round(IceAndFireConfig.get(IceAndFireConfig.dragonArmorBreathResistance) * 100)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
