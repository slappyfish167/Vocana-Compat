package dev.vocana.compat.hexcasting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The media <-> Thrum arithmetic, which runs without Minecraft or Hex. */
final class MediaMathTest {
    @Test void mediaAndThrumConvertAtTheRate() {
        assertEquals(10_000L, MediaMath.mediaFor(4, 2500));
        assertEquals(1L, MediaMath.mediaFor(0.0001, 2500), "a positive need costs at least one media");
        assertEquals(0L, MediaMath.mediaFor(0, 2500));
        assertEquals(0L, MediaMath.mediaFor(5, 0), "a zero rate buys nothing");
        assertEquals(4.0, MediaMath.thrumFor(10_000, 2500), 1e-9);
        assertEquals(0.0, MediaMath.thrumFor(-5, 2500), 1e-9);
        assertEquals(0.0, MediaMath.thrumFor(5, 0), 1e-9);
    }

    @Test void roomIsTheSmallerOfTheGapAndTheScaledCap() {
        assertEquals(20.0, MediaMath.room(30, 100, 20, 1), 1e-9);
        assertEquals(5.0, MediaMath.room(95, 100, 20, 1), 1e-9);
        assertEquals(0.0, MediaMath.room(100, 100, 20, 1), 1e-9);
        assertEquals(0.0, MediaMath.room(120, 100, 20, 1), 1e-9, "an overfull pool has no room");
        assertEquals(40.0, MediaMath.room(30, 100, 20, 2), 1e-9);
        assertEquals(40.0, MediaMath.room(30, 100, 20, 9), 1e-9, "the measure scales at most twofold");
        assertEquals(20.0, MediaMath.room(30, 100, 20, 0.3), 1e-9, "a weak Deed still draws the full cap");
    }

    @Test void quantaRoundToWholeItemsAndAllowOneOvershootOnly() {
        assertEquals(20_000L, MediaMath.quanta(25_000, 10_000, false));
        assertEquals(20_000L, MediaMath.quanta(25_000, 10_000, true));
        assertEquals(10_000L, MediaMath.quanta(2_500, 10_000, true));
        assertEquals(0L, MediaMath.quanta(2_500, 10_000, false));
        assertEquals(2_500L, MediaMath.quanta(2_500, 1, false), "a holder with a unit of 1 gives any amount");
        assertEquals(2_500L, MediaMath.quanta(2_500, 0, true), "a holder with no unit gives any amount");
        assertEquals(0L, MediaMath.quanta(0, 10_000, true));
        assertEquals(0L, MediaMath.quanta(-1, 10_000, true));
    }
}
