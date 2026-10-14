package dev.vocana.compat.ars_nouveau;

/**
 * The arithmetic of the bridge between Ars Nouveau's mana and source and Vocana's Thrum, kept pure (no Minecraft, no
 * Ars) so the unit tests and the GameTests, which run without Ars, can hold it to its promises.
 *
 * <p>The par rates: Ars's own machines price one point of mana as one point of source (a spell turret draws its
 * spell's mana cost from the jars beside it: {@code TileCaster.expendMana} calls {@code SourceUtil.takeSourceMultiple}
 * with the spell's mana cost), and one Thrum is worth about five mana, weighed by regeneration rather than by pool: a
 * fresh Ars caster regains 5 mana a second from a pool of 100 ({@code ServerConfig.INIT_MANA_REGEN},
 * {@code INIT_MAX_MANA}) while a novice Chime regains 0.725 Thrum a second from a pool of 82 ({@code Resonance}). Both
 * rates are config. Every conversion then loses {@code conversionLoss} of its par value, so no chain of conversions
 * can come back with more than it started with: mana to Thrum (Resonate) and Thrum to source or mana (SOURCE) each
 * keep {@code 1 - loss}, a round trip {@code (1 - loss)^2}.
 */
public final class Conversion {
    private Conversion() {}
    /** The default mana cost of the Resonate glyph, the value its own Ars config file starts from. */
    public static final int RESONATE_MANA = 50;

    /**
     * An Ars spell's mana cost after the bridge: dearer by the cost factor, a little cheaper again when a Vocana staff
     * is in the off hand. Rounded to the integer Ars spends, never negative.
     */
    public static int arsCost(int cost, double costFactor, double staffDiscount, boolean staffInOffHand) {
        double c = Math.max(0, cost) * Math.max(0, costFactor);
        if (staffInOffHand) c *= Math.max(0, Math.min(1, staffDiscount));
        return (int) Math.max(0, Math.round(c));
    }

    /** The Thrum a Resonate cast returns for the mana it cost: the par value less the conversion loss. */
    public static double thrumForMana(double mana, double manaPerThrum, double loss) {
        if (mana <= 0 || manaPerThrum <= 0) return 0;
        return mana / manaPerThrum * (1 - loss(loss));
    }

    /**
     * How much source (or its mana equivalent) one Working may give out in all: the Thrum it paid at par, less the
     * conversion loss, divided by the resource factor because Ars's resources are the dearer ones.
     */
    public static double sourceBudget(double thrumPaid, double manaPerThrum, double sourcePerMana, double loss, double resourceFactor) {
        if (thrumPaid <= 0) return 0;
        return thrumPaid * Math.max(0, manaPerThrum) * Math.max(0, sourcePerMana) * (1 - loss(loss)) / Math.max(1, resourceFactor);
    }

    /** What one touch of the ground near a jar gives: the per-cast amount scaled by the Working's power, never past what the Working has left. */
    public static int sourceForTouch(int sourcePerCast, double power, double remainingBudget) {
        long want = Math.round(Math.max(0, sourcePerCast) * Math.max(0, power));
        long left = (long) Math.floor(Math.max(0, remainingBudget));
        return (int) Math.max(0, Math.min(want, left));
    }

    public static double manaForSource(double source, double sourcePerMana) { return sourcePerMana <= 0 ? 0 : Math.max(0, source) / sourcePerMana; }
    public static double sourceForMana(double mana, double sourcePerMana) { return Math.max(0, mana) * Math.max(0, sourcePerMana); }

    /** The share of its value a unit keeps after going out and coming back (Thrum to mana-equivalent to Thrum): below one, always. */
    public static double roundTrip(double loss) { double kept = 1 - loss(loss); return kept * kept; }

    /**
     * The invariants the defaults promise: Ars never cheaper than Vocana parity even with the staff discount, Vocana
     * never cheaper than its own numbers, at least three tenths lost on every conversion.
     */
    public static boolean guardsHold(double arsCostFactor, double staffDiscount, double vocanaCostFactor, double loss) {
        return arsCostFactor * staffDiscount >= 1 && vocanaCostFactor >= 1 && loss >= 0.3;
    }

    /** Ticks left before Resonate may answer the same caster again; zero when it may. */
    public static long cooldownLeft(long now, long last, int cooldownTicks) { return Math.max(0, last + Math.max(0, cooldownTicks) - now); }

    static double loss(double loss) { return Math.max(0, Math.min(1, loss)); }
}
