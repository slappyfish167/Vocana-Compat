package dev.vocana.compat.curios;

import dev.vocana.Imbue;
import dev.vocana.Imbues;
import dev.vocana.TorcItem;
import dev.vocana.Vocana;
import dev.vocana.engine.Analysis;
import dev.vocana.engine.Token;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * What a player wears in Curios slots, asked the way Vocana asks: at the Seal, on the regen tick, when a Torc is looked
 * for. Every question goes through {@code CuriosApi.getCuriosInventory(LivingEntity)}, which on NeoForge 9.5.1 returns
 * {@code Optional<ICuriosItemHandler>} (empty for an entity with no slots) and {@code ICuriosItemHandler.findCurios /
 * findFirstCurio} ({@code neoforge/.../api/type/capability/ICuriosItemHandler.java}), which only see stacks in
 * functional slots. The slot ids are the Curios presets ({@code common/.../data/curios/curios/slots/*.json}).
 */
public final class CurioSlots {
    private CurioSlots() {}
    public static final String RING = "ring", NECKLACE = "necklace", CHARM = "charm";

    public static Optional<ICuriosItemHandler> inventory(LivingEntity e) { return e == null ? Optional.empty() : CuriosApi.getCuriosInventory(e); }

    /** The stacks matching {@code filter} in any slot. */
    public static List<SlotResult> equipped(LivingEntity e, Predicate<ItemStack> filter) { return inventory(e).map(h -> h.findCurios(filter)).orElse(List.of()); }
    /** The stacks in the slots named, empty ones included. */
    public static List<SlotResult> in(LivingEntity e, String... slots) { return inventory(e).map(h -> h.findCurios(slots)).orElse(List.of()); }
    public static int count(LivingEntity e, Predicate<ItemStack> filter) { return equipped(e, filter).size(); }

    /** Another place a Torc may be worn (registered with {@code VocanaApi.registerWorn}): any Curios slot, the necklace by its tag. */
    public static boolean wearsTorc(Player p) { return inventory(p).map(h -> h.isEquipped(s -> s.getItem() instanceof TorcItem)).orElse(false); }

    // ---- the focus items (VocanaApi caster modifiers) ----
    public static int capacity(ServerPlayer p) { return CurioRules.ringCapacity(count(p, s -> s.is(CuriosItems.RING_OF_FOCUS.get())), CuriosConfig.get(CuriosConfig.ringCapacity)); }
    public static double regen(ServerPlayer p) { return count(p, s -> s.is(CuriosItems.AMULET_OF_RESONANCE.get())) > 0 ? CuriosConfig.get(CuriosConfig.amuletRegen) : 1; }
    public static double strain(ServerPlayer p) { return count(p, s -> s.is(CuriosItems.CHARM_OF_CALM.get())) > 0 ? CuriosConfig.get(CuriosConfig.charmStrain) : 1; }

    /**
     * The base essences carried by what sits in the ring slots: a Signet's drunk essence, every essence imbued into the
     * piece ({@code vocana:imbues}, read through {@code Imbues.view}, which also reads the 2.0 {@code vocana:imbuement}
     * of gear) and a bare 2.0 {@code vocana:imbuement} on something that is not gear. Chant imbues carry no essence.
     */
    public static Set<String> ringEssences(ServerPlayer p) {
        Set<String> out = new LinkedHashSet<>();
        for (var r : in(p, RING)) {
            var stack = r.stack(); if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SignetItem) { String b = SignetItem.base(stack); if (!b.isEmpty()) out.add(b); }
            for (Imbue im : Imbues.view(stack).list()) if (!im.chant()) out.add(im.base());
            var old = stack.get(Vocana.IMBUEMENT.get()); if (old != null) out.add(old.base());
        }
        return out;
    }
    /** The root glosses of a read chant, as {@code Casting.environment} gathers them (an Elder shade answers as its base). */
    public static Set<String> roots(Analysis.Result r) {
        Set<String> roots = new HashSet<>();
        if (r != null && r.working != null) for (Token t : r.working.tokens) if (t.root()) roots.add(t.head());
        return roots;
    }
    /** The Thrum factor at the Seal (registered with {@code VocanaApi.registerCostFactor}). */
    public static double cost(ServerPlayer p, Analysis.Result r) {
        var roots = roots(r); if (roots.isEmpty()) return 1;
        return CurioRules.costFactor(ringEssences(p), roots, CuriosConfig.get(CuriosConfig.signetCostFactor));
    }
}
