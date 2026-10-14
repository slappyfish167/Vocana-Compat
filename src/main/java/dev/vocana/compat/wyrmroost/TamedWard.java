package dev.vocana.compat.wyrmroost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * A caster's words never burn their own mount. Vocana deals its damage as indirect magic credited to the caster
 * ({@code Working.hurt}); when that reaches a dragon tamed by the caster or (by config) a team-mate, the hit is refused.
 * Only magic damage is refused: a sword, an arrow or a fall still hurts, as they do in Wyrmroost. Vanilla pets are left
 * to Vocana's own rules; this module speaks for dragons.
 */
final class TamedWard {
    private TamedWard() {}

    static void onIncomingDamage(LivingIncomingDamageEvent e) {
        if (!WyrmroostConfig.get(WyrmroostConfig.protectTamedFromCasterMagic)) return;
        var victim = e.getEntity();
        if (!Dragons.isDragon(victim)) return;
        var source = e.getSource();
        if (!(source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.MAGIC))) return;
        if (!(source.getEntity() instanceof ServerPlayer caster)) return;
        if (Taming.sameSide(victim, caster)) e.setCanceled(true);
    }
}
