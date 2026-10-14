package dev.vocana.compat.iceandfire;

import dev.vocana.StaffItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;

/**
 * The Wyrmsteel Staff: an Archmage's Staff reforged in the netherite-dragonsteel alloy on a Create assembly line
 * ({@code data/vocanacompat/recipe/iceandfire/wyrmsteel_staff.json}; there is no crafting-table recipe). It is a
 * {@link StaffItem}, so Vocana already treats it as a Catalyst: Archmage capacity (6) and the affinity of all three
 * dragon essences at once, which no core staff has. The figures are constructor constants because {@code StaffItem}
 * reads them from the item, not per stack; the Imbue Bench recipe that lays dragon essences into it cheaply is data.
 */
public final class WyrmsteelStaffItem extends StaffItem {
    public static final int CAPACITY = 6;
    public static final Set<String> AFFINITY = Set.of("FIRE", "FROST", "LIGHTNING");

    public WyrmsteelStaffItem(Properties props) { super(props, CAPACITY, AFFINITY); }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.vocanacompat.wyrmsteel_staff.tooltip").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
