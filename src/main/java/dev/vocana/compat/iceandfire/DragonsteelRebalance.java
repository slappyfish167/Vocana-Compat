package dev.vocana.compat.iceandfire;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies {@link DragonsteelBalance} in the game. {@code ItemAttributeModifierEvent} (NeoForge 21.1,
 * {@code net.neoforged.neoforge.event}) fires whenever a stack's attribute modifiers are read, on both sides, so the
 * rewrite is per stack, needs no patch of Ice and Fire's tiers (its {@code IafTiers} is an enum implementing
 * {@code Tier}, not a holder that could be wrapped, and it reads its figures from Ice and Fire's own config) and is
 * undone by turning the toggle off. For a tool the vanilla base ids {@code minecraft:base_attack_damage} and
 * {@code base_attack_speed} are kept, so the tooltip still prints the green totals; for armour the ids follow vanilla's
 * {@code minecraft:armor.<piece>} shape. The frost resistance of ice dragonsteel is not an attribute: a player wearing
 * enough pieces simply never accrues freezing ticks.
 */
public final class DragonsteelRebalance {
    private DragonsteelRebalance() {}
    private static final Map<Item, Optional<DragonsteelBalance.Target>> TARGETS = new ConcurrentHashMap<>();
    private static final ResourceLocation BASE_DAMAGE = ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                          BASE_SPEED = ResourceLocation.withDefaultNamespace("base_attack_speed");

    /** The piece a stack is, cached per item (ids never change after registration). */
    static DragonsteelBalance.Target target(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return TARGETS.computeIfAbsent(stack.getItem(), item -> Optional.ofNullable(DragonsteelBalance.classify(BuiltInRegistries.ITEM.getKey(item).toString()))).orElse(null);
    }

    public static void onAttributes(ItemAttributeModifierEvent e) {
        if (!IceAndFireConfig.get(IceAndFireConfig.rebalanceDragonsteel)) return;
        var t = target(e.getItemStack());
        if (t == null) return;
        var n = DragonsteelBalance.numbers(t, IceAndFireConfig.tuning(t.metal()));
        if (t.piece().tool()) {
            replace(e, Attributes.ATTACK_DAMAGE, BASE_DAMAGE, n.attackDamage(), EquipmentSlotGroup.MAINHAND, AttributeModifier.Operation.ADD_VALUE);
            replace(e, Attributes.ATTACK_SPEED, BASE_SPEED, n.attackSpeed(), EquipmentSlotGroup.MAINHAND, AttributeModifier.Operation.ADD_VALUE);
            return;
        }
        String piece = t.piece().suffix();
        var group = EquipmentSlotGroup.bySlot(slot(t.piece()));
        replace(e, Attributes.ARMOR, ResourceLocation.withDefaultNamespace("armor." + piece), n.armor(), group, AttributeModifier.Operation.ADD_VALUE);
        replace(e, Attributes.ARMOR_TOUGHNESS, ResourceLocation.withDefaultNamespace("armor." + piece), n.toughness(), group, AttributeModifier.Operation.ADD_VALUE);
        replace(e, Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.withDefaultNamespace("armor." + piece), n.knockbackResistance(), group, AttributeModifier.Operation.ADD_VALUE);
        replace(e, Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("vocanacompat", "dragonsteel_" + piece), n.movementSpeed(), group, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    /** Drops whatever the item (and anyone before us) set for the attribute and lays our one amount; zero leaves none. */
    private static void replace(ItemAttributeModifierEvent e, Holder<Attribute> attribute, ResourceLocation id, double amount, EquipmentSlotGroup group, AttributeModifier.Operation op) {
        e.removeAllModifiersFor(attribute);
        if (amount != 0) e.addModifier(attribute, new AttributeModifier(id, amount, op), group);
    }

    static EquipmentSlot slot(DragonsteelBalance.Piece p) {
        return switch (p) { case HELMET -> EquipmentSlot.HEAD; case CHESTPLATE -> EquipmentSlot.CHEST; case LEGGINGS -> EquipmentSlot.LEGS; case BOOTS -> EquipmentSlot.FEET; default -> EquipmentSlot.MAINHAND; };
    }

    /** Ice dragonsteel's frost resistance: with enough pieces on, a player's freezing never builds (server side; the client is told as usual). */
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        Player p = e.getEntity();
        if (p.level().isClientSide() || !IceAndFireConfig.get(IceAndFireConfig.rebalanceDragonsteel)) return;
        int needed = IceAndFireConfig.get(IceAndFireConfig.iceFrostResistancePieces);
        if (needed <= 0 || p.getTicksFrozen() <= 0) return;
        int worn = 0;
        for (var slot : EquipmentSlot.values()) if (slot.isArmor() && DragonsteelBalance.shedsFrost(target(p.getItemBySlot(slot)))) worn++;
        if (worn >= needed) p.setTicksFrozen(0);
    }
}
