package dev.vocana.compat.hexcasting;

/**
 * The arithmetic of the media <-> Thrum bridge, kept free of Minecraft and Hex Casting so it can be unit-tested and
 * exercised by the GameTests that run without Hex. Media is Hex's integer currency (one amethyst dust is 10 000 by
 * default); Thrum is Vocana's floating pool.
 */
public final class MediaMath {
    private MediaMath() {}

    /** The media that buys this much Thrum at the configured rate; never less than one unit for a positive amount. */
    public static long mediaFor(double thrum, long mediaPerThrum) {
        if (thrum <= 0 || mediaPerThrum <= 0) return 0;
        return Math.max(1, (long) Math.ceil(thrum * mediaPerThrum));
    }

    /** The Thrum this much media is worth. */
    public static double thrumFor(long media, long mediaPerThrum) {
        if (media <= 0 || mediaPerThrum <= 0) return 0;
        return (double) media / mediaPerThrum;
    }

    /**
     * How much Thrum one DRAW may restore: the room left in the pool, capped per cast. {@code intensity} is the Deed's
     * intensity (a measure such as TWO raises it); it scales the cap between one and two times.
     */
    public static double room(double thrum, double maxThrum, double capPerCast, double intensity) {
        double cap = capPerCast * Math.max(1, Math.min(2, intensity));
        return Math.max(0, Math.min(cap, maxThrum - thrum));
    }

    /**
     * How much to ask of one media holder. Raw amethyst comes in whole items ({@code unit} is one item's worth, as a
     * simulated withdrawal of 1 reveals), so the request is rounded down to whole units; when nothing has been drawn
     * yet and even one unit exceeds the need, one unit is taken anyway and the surplus is wasted, as Hex itself wastes
     * it. A holder that gives any amount ({@code unit} of 1 or less) is asked for exactly the need.
     */
    public static long quanta(long need, long unit, boolean first) {
        if (need <= 0) return 0;
        if (unit <= 1) return need;
        long whole = need / unit * unit;
        if (whole > 0) return whole;
        return first ? unit : 0;
    }
}
