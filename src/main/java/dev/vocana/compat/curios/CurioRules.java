package dev.vocana.compat.curios;

import java.util.Set;

/**
 * The module's pure arithmetic, kept free of Minecraft and Curios so it can be unit-tested: how a ring-slot essence
 * prices a Working and when a worn relic whispers. Everything that touches an entity or a slot is in {@link CurioSlots}.
 */
public final class CurioRules {
    private CurioRules() {}

    /**
     * The Thrum factor for a chant whose root glosses are {@code roots} when the caster's ring slots carry the base
     * essences {@code worn}: {@code factor} once when any root is among them (two matching rings do not stack: the
     * discount is for the essence, not the ring), 1 otherwise or when the factor is not a usable number.
     */
    public static double costFactor(Set<String> worn, Set<String> roots, double factor) {
        if (worn == null || roots == null || worn.isEmpty() || roots.isEmpty()) return 1;
        if (!(factor > 0) || Double.isNaN(factor) || Double.isInfinite(factor)) return 1;
        for (String r : roots) if (worn.contains(r)) return factor;
        return 1;
    }

    /** Whether a relic worn for {@code ticks} ticks is due to whisper, with the configured wait in seconds. */
    public static boolean whisperDue(int ticks, int seconds) { return seconds > 0 && ticks >= 20L * seconds; }

    /** Capacity from {@code rings} worn Rings of Focus at {@code each} apiece; never negative. */
    public static int ringCapacity(int rings, int each) { return Math.max(0, rings) * Math.max(0, each); }
}
