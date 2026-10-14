package dev.vocana.compat.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.function.Supplier;

/**
 * A focus item of this module: a plain item that is also its own curio. Curios 9.5.1 finds the behaviour itself: its
 * item-capability provider ({@code neoforge/.../Curios.java}, {@code registerCapabilities}) takes an {@code ICurioItem}
 * registered through {@code CuriosApi.registerCurio} first and otherwise the item when it implements {@code ICurioItem},
 * so nothing has to be registered for these. Which slot takes an item is data, the {@code curios:<slot>} item tags
 * ({@code data/curios/tags/item/<slot>.json}; the {@code curios:tag} validator every preset slot carries reads them).
 * The effects themselves are not here: Vocana asks {@code VocanaApi} at the Seal and {@link CurioSlots} answers by
 * looking at what is worn, so a stack in a slot needs no tick and no state.
 */
public class VocanaCurioItem extends Item implements ICurioItem {
    /** The tooltip line's key and the number it quotes (a config value), read when the tooltip is drawn. */
    private final String tooltipKey;
    private final Supplier<Object> tooltipValue;

    public VocanaCurioItem(Properties props, String tooltipKey, Supplier<Object> tooltipValue) {
        super(props);
        this.tooltipKey = tooltipKey; this.tooltipValue = tooltipValue;
    }

    /** Right-clicking the item puts it on, when the config allows ({@code ICurioItem.canEquipFromUse} defaults to false). */
    @Override public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) { return CuriosConfig.get(CuriosConfig.equipFromUse); }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey, tooltipValue.get()).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
