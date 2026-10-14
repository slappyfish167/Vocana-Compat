package dev.vocana.compat.wyrmroost;

import org.junit.jupiter.api.Test;

import static dev.vocana.compat.wyrmroost.RoostRules.Outcome.*;
import static org.junit.jupiter.api.Assertions.*;

/** The pure rules of ROOST: who is settled, roused, made to hesitate or left alone. */
final class RoostRulesTest {
    @Test void ownDragonsToggleBetweenRestAndReadiness() {
        assertEquals(SETTLE, RoostRules.forDragon(true, true, false, true));
        assertEquals(ROUSE, RoostRules.forDragon(true, true, true, true));
        assertEquals(SETTLE, RoostRules.forDragon(true, true, false, false), "control is not needed over one's own dragon");
    }
    @Test void aStrangersTamedDragonIsNeverTouched() {
        assertEquals(NONE, RoostRules.forDragon(true, false, false, true));
        assertEquals(NONE, RoostRules.forDragon(true, false, true, true));
    }
    @Test void wildDragonsHesitateOnlyWhenTheWorkingMayControlThem() {
        assertEquals(HESITATE, RoostRules.forDragon(false, false, false, true));
        assertEquals(NONE, RoostRules.forDragon(false, false, false, false), "a boss or a spared dragon is left alone");
        assertEquals(HESITATE, RoostRules.forDragon(false, true, false, true), "casterSide is meaningless for a wild dragon");
    }
    @Test void petsSitAndStandButNeverHesitate() {
        assertEquals(SETTLE, RoostRules.forPet(true, true, false));
        assertEquals(ROUSE, RoostRules.forPet(true, true, true));
        assertEquals(NONE, RoostRules.forPet(true, false, false));
        assertEquals(NONE, RoostRules.forPet(false, false, false));
    }
    @Test void hesitationScalesWithTheClauseAndIsClamped() {
        assertEquals(100, RoostRules.hesitateTicks(5, 1));
        assertEquals(150, RoostRules.hesitateTicks(5, 1.5));
        assertEquals(300, RoostRules.hesitateTicks(5, 9), "clamped at three times");
        assertEquals(50, RoostRules.hesitateTicks(5, .1), "never below half");
        assertEquals(0, RoostRules.hesitateTicks(0, 2), "zero seconds turns hesitation off");
    }
    @Test void sleepNeedsTheConfiguredIntensity() {
        assertFalse(RoostRules.sleeps(1, 1.5));
        assertTrue(RoostRules.sleeps(1.5, 1.5));
        assertTrue(RoostRules.sleeps(2.25, 1.5));
    }
}
