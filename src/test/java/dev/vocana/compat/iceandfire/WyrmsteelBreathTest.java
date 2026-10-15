package dev.vocana.compat.iceandfire;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Wyrmsteel's breath resistance and the dragon armour recolouring, without Minecraft. */
final class WyrmsteelBreathTest {
    @Test void eachPieceTakesItsShareAndTheSharesAdd() {
        assertEquals(1.0, WyrmsteelBreath.factor(0, 0.15, 0, 0.05), 1e-9, "no wyrmsteel, no change");
        assertEquals(0.40, WyrmsteelBreath.factor(4, 0.15, 0, 0.05), 1e-9, "a dragon in four pieces takes 40 %");
        assertEquals(0.80, WyrmsteelBreath.factor(0, 0.15, 4, 0.05), 1e-9, "a player in the full set takes 80 %");
        assertEquals(0.80, WyrmsteelBreath.factor(1, 0.15, 1, 0.05), 1e-9, "shares add");
    }

    @Test void theFactorStaysBetweenZeroAndOne() {
        assertEquals(0.0, WyrmsteelBreath.factor(4, 0.25, 4, 0.25), 1e-9, "never below zero");
        assertEquals(1.0, WyrmsteelBreath.factor(-3, 0.15, -1, 0.05), 1e-9, "negative counts are none");
        assertEquals(1.0, WyrmsteelBreath.factor(4, 0, 4, 0), 1e-9, "zero shares turn it off");
    }

    @Test void theRecolourKeepsAlphaAndBrightnessOrderAndLandsOnTheRamp() {
        assertEquals(0, WyrmsteelPalette.recolourAbgr(0), "a clear pixel stays clear");
        assertEquals(0x00FFFFFF, WyrmsteelPalette.recolourAbgr(0x00FFFFFF), "any fully transparent pixel is left as it is");
        int black = WyrmsteelPalette.recolourAbgr(0xFF000000), white = WyrmsteelPalette.recolourAbgr(0xFFFFFFFF);
        assertEquals(abgr(WyrmsteelPalette.RAMP[0]), black, "black is the ramp's darkest");
        assertEquals(abgr(WyrmsteelPalette.RAMP[WyrmsteelPalette.RAMP.length - 1]), white, "white is its lightest");
        assertEquals(0x80, WyrmsteelPalette.recolourAbgr(0x80336699) >>> 24, "alpha is kept");
        int last = -1;
        for (int v = 0; v <= 255; v += 5) {
            int px = WyrmsteelPalette.recolourAbgr(0xFF000000 | v << 16 | v << 8 | v);
            int lum = (px & 0xFF) + ((px >> 8) & 0xFF) + ((px >> 16) & 0xFF);
            assertTrue(lum >= last, "brighter stays brighter at " + v);
            last = lum;
        }
    }

    private static int abgr(int[] rgb) { return 0xFF000000 | rgb[2] << 16 | rgb[1] << 8 | rgb[0]; }
}
