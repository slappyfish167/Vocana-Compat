package dev.vocana.compat.iceandfire;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Wyrmsteel turns dragon breath. Damage of a type in {@code #vocanacompat:iceandfire/dragon_breath} (Ice and Fire's
 * {@code dragon_fire}, {@code dragon_ice} and {@code dragon_lightning}, and vanilla's fire, freezing and lightning tags)
 * is lessened for whoever wears wyrmsteel: a share per piece of wyrmsteel dragon armour on a dragon, a smaller share
 * per piece of wyrmsteel armour on anyone else ({@link WyrmsteelBreath}; the shares in {@link IceAndFireConfig}).
 * {@code LivingIncomingDamageEvent} (NeoForge 21.1) fires before armour and protection enchantments, so those still
 * apply to what is left.
 */
public final class WyrmsteelWard {
    private WyrmsteelWard() {}
    public static final TagKey<DamageType> DRAGON_BREATH = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("vocanacompat", "iceandfire/dragon_breath"));

    public static void onIncomingDamage(LivingIncomingDamageEvent e) {
        if (!e.getSource().is(DRAGON_BREATH)) return;
        LivingEntity target = e.getEntity();
        int dragonArmour = 0, armour = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.getItem() instanceof WyrmsteelDragonArmorItem) dragonArmour++;
            else {
                var t = DragonsteelRebalance.target(stack);
                if (t != null && t.metal() == DragonsteelBalance.Metal.WYRMSTEEL && t.piece().armour()) armour++;
            }
        }
        if (dragonArmour == 0 && armour == 0) return;
        double factor = WyrmsteelBreath.factor(dragonArmour, IceAndFireConfig.get(IceAndFireConfig.dragonArmorBreathResistance),
                                               armour, IceAndFireConfig.get(IceAndFireConfig.armorBreathResistance));
        e.setAmount((float) (e.getAmount() * factor));
    }
}
