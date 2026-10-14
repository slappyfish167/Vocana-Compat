package dev.vocana.compat.curios;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The Curios module's pure numbers: the ring-slot essence discount, the relic's wait, the rings' Capacity. No Minecraft, no Curios. */
final class CurioRulesTest {
    @Test void theDiscountAppliesOnceWhenAnyRootMatchesAWornEssence() {
        assertEquals(0.9, CurioRules.costFactor(Set.of("FIRE"), Set.of("FIRE", "FORTH"), 0.9), 1e-9);
        assertEquals(0.9, CurioRules.costFactor(Set.of("FIRE", "FROST"), Set.of("FROST", "FIRE"), 0.9), 1e-9, "two matching essences do not stack");
        assertEquals(1, CurioRules.costFactor(Set.of("FIRE"), Set.of("WATER"), 0.9), 1e-9);
    }
    @Test void nothingWornOrNothingSaidLeavesThePriceAlone() {
        assertEquals(1, CurioRules.costFactor(Set.of(), Set.of("FIRE"), 0.9), 1e-9);
        assertEquals(1, CurioRules.costFactor(Set.of("FIRE"), Set.of(), 0.9), 1e-9);
        assertEquals(1, CurioRules.costFactor(null, null, 0.9), 1e-9);
    }
    @Test void aBrokenFactorIsIgnored() {
        assertEquals(1, CurioRules.costFactor(Set.of("FIRE"), Set.of("FIRE"), 0), 1e-9);
        assertEquals(1, CurioRules.costFactor(Set.of("FIRE"), Set.of("FIRE"), Double.NaN), 1e-9);
        assertEquals(1.2, CurioRules.costFactor(Set.of("FIRE"), Set.of("FIRE"), 1.2), 1e-9, "a surcharge is a legitimate setting");
    }
    @Test void aRelicWhispersAfterTheConfiguredSeconds() {
        assertFalse(CurioRules.whisperDue(599, 30));
        assertTrue(CurioRules.whisperDue(600, 30));
        assertFalse(CurioRules.whisperDue(100000, 0), "zero seconds means never, not at once");
    }
    @Test void ringsAddTheirCapacityEach() {
        assertEquals(4, CurioRules.ringCapacity(2, 2));
        assertEquals(0, CurioRules.ringCapacity(0, 2));
        assertEquals(0, CurioRules.ringCapacity(2, -1));
    }
}
