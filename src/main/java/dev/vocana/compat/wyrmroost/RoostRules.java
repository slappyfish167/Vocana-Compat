package dev.vocana.compat.wyrmroost;

/**
 * What the Deed ROOST does to one creature, decided from plain facts so it can be unit-tested without Minecraft or
 * Wyrmroost. The rule: a dragon of the caster's own (theirs or a team-mate's) is settled, or roused if it already rests;
 * a wild dragon the Working may control hesitates; a stranger's tamed dragon is left alone; a tamed vanilla pet of the
 * caster's sits or stands like a dragon but never hesitates.
 */
final class RoostRules {
    private RoostRules() {}

    enum Outcome { SETTLE, ROUSE, HESITATE, NONE }

    /** A Wyrmroost dragon. {@code mayControl} is whether the Working may harm and control it (not a boss, not spared). */
    static Outcome forDragon(boolean tame, boolean casterSide, boolean settled, boolean mayControl) {
        if (tame) return casterSide ? (settled ? Outcome.ROUSE : Outcome.SETTLE) : Outcome.NONE;
        return mayControl ? Outcome.HESITATE : Outcome.NONE;
    }

    /** A vanilla tameable animal: only the caster's own ever answer, and wild ones never hesitate. */
    static Outcome forPet(boolean tame, boolean casterSide, boolean sitting) {
        if (!tame || !casterSide) return Outcome.NONE;
        return sitting ? Outcome.ROUSE : Outcome.SETTLE;
    }

    /** How long a wild dragon hesitates: the configured seconds scaled by the clause's intensity, clamped to 0.5–3. */
    static int hesitateTicks(double seconds, double intensity) {
        double scale = Math.max(.5, Math.min(3, intensity));
        return (int) Math.max(0, Math.round(seconds * 20 * scale));
    }

    /** Whether a settled dragon is also put to sleep: a clause at or above the configured intensity. */
    static boolean sleeps(double intensity, double threshold) { return intensity >= threshold; }
}
