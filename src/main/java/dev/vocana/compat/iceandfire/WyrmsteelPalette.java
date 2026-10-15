package dev.vocana.compat.iceandfire;

/**
 * Wyrmsteel's colours, and the recolouring that turns Ice and Fire's netherite dragon armour into wyrmsteel's. The
 * module ships none of Ice and Fire's art: the client reads Ice and Fire's own netherite texture from the installed mod
 * and maps each pixel's brightness onto this ramp ({@link IceAndFireClient}). Free of Minecraft so it can be
 * unit-tested; pixels are in the {@code 0xAABBGGRR} order {@code NativeImage} uses.
 */
public final class WyrmsteelPalette {
    private WyrmsteelPalette() {}

    /** From dark to light, as {@code tools/textures_iceandfire.py} draws the item sprites: edge, dark, steel, sheen, glint. */
    static final int[][] RAMP = {{20, 16, 24}, {31, 26, 36}, {58, 50, 66}, {96, 82, 112}, {150, 134, 170}};

    /** One pixel recoloured: its brightness picks a point on the ramp, its alpha is kept, a clear pixel stays clear. */
    public static int recolourAbgr(int abgr) {
        int a = abgr >>> 24;
        if (a == 0) return abgr;
        int r = abgr & 0xFF, g = (abgr >> 8) & 0xFF, b = (abgr >> 16) & 0xFF;
        double t = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        double pos = t * (RAMP.length - 1);
        int i = Math.min(RAMP.length - 2, (int) pos);
        double f = pos - i;
        int nr = mix(RAMP[i][0], RAMP[i + 1][0], f), ng = mix(RAMP[i][1], RAMP[i + 1][1], f), nb = mix(RAMP[i][2], RAMP[i + 1][2], f);
        return a << 24 | nb << 16 | ng << 8 | nr;
    }

    private static int mix(int from, int to, double f) { return (int) Math.round(from + (to - from) * f); }
}
