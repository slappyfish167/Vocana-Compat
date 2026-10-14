package dev.vocana.compat.wyrmroost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

/** Whose creature a tamed animal is, as this module sees it: the owner's, or (by config) their scoreboard team's. */
final class Taming {
    private Taming() {}

    /** Whether a tamed animal is on the caster's side: owned by them, or by someone on their team when teams count. */
    static boolean sameSide(Entity e, ServerPlayer caster) {
        if (caster == null || !(e instanceof TamableAnimal t) || !t.isTame()) return false;
        if (t.isOwnedBy(caster)) return true;
        if (!WyrmroostConfig.get(WyrmroostConfig.respectTeams)) return false;
        var owner = t.getOwner();   // null while the owner is offline: then only ownership itself counts
        return owner != null && caster.getTeam() != null && caster.isAlliedTo(owner);
    }

    /** A tamed animal that belongs to someone else than the caster's side. */
    static boolean strangers(Entity e, ServerPlayer caster) {
        return e instanceof TamableAnimal t && t.isTame() && !sameSide(t, caster);
    }
}
