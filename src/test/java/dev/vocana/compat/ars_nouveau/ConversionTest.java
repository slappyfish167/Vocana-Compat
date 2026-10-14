package dev.vocana.compat.ars_nouveau;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The exchange arithmetic of the Ars Nouveau bridge, at the defaults and at the edges. Runs without Minecraft or Ars. */
class ConversionTest {
    static final double MANA_PER_THRUM = 5, SOURCE_PER_MANA = 1, LOSS = .35, RESOURCES = 1.15;

    @Test void arsSpellsCostMoreAndAStaffGivesALittleBack() {
        assertEquals(115, Conversion.arsCost(100, 1.15, .95, false));
        assertEquals(109, Conversion.arsCost(100, 1.15, .95, true), "1.15 x 0.95 = 1.0925, rounded");
        assertEquals(0, Conversion.arsCost(0, 1.15, .95, true));
        assertEquals(0, Conversion.arsCost(-5, 1.15, .95, false), "never negative");
        assertEquals(1, Conversion.arsCost(1, 1.15, .95, false), "1.15 rounds to 1: small spells stay small");
        assertEquals(2, Conversion.arsCost(2, 1.15, .95, false), "2.3 rounds to 2");
        assertEquals(20, Conversion.arsCost(20, 1.0, 1.0, true), "factors of one change nothing");
    }

    @Test void resonateReturnsLessThanParAndNothingForNothing() {
        assertEquals(6.5, Conversion.thrumForMana(50, MANA_PER_THRUM, LOSS), 1e-9, "50 mana at 5 per Thrum is 10 Thrum at par, 6.5 after the loss");
        assertEquals(0, Conversion.thrumForMana(0, MANA_PER_THRUM, LOSS));
        assertEquals(0, Conversion.thrumForMana(50, 0, LOSS), "a zero rate gives nothing rather than infinity");
        assertEquals(10, Conversion.thrumForMana(50, MANA_PER_THRUM, 0), 1e-9, "no loss is par");
        assertEquals(0, Conversion.thrumForMana(50, MANA_PER_THRUM, 1), 1e-9, "total loss is nothing");
        assertEquals(0, Conversion.thrumForMana(50, MANA_PER_THRUM, 7), 1e-9, "a loss past one is clamped, never negative");
    }

    @Test void aWorkingsSourceBudgetFollowsThePaidThrum() {
        double budget = Conversion.sourceBudget(4, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, RESOURCES);
        assertEquals(4 * 5 * 1 * .65 / 1.15, budget, 1e-9, "4 Thrum paid: 20 source at par, 13 after the loss, 11.3 at Ars's dearer prices");
        assertEquals(0, Conversion.sourceBudget(0, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, RESOURCES), "a free Working gives nothing");
        assertEquals(0, Conversion.sourceBudget(-1, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, RESOURCES));
        assertEquals(Conversion.sourceBudget(4, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, 1), Conversion.sourceBudget(4, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, .5), 1e-9, "a resource factor under one is read as one");
    }

    @Test void aTouchIsBoundedByTheCastAmountTheWorkingsPowerAndItsBudget() {
        assertEquals(40, Conversion.sourceForTouch(40, 1, 1000));
        assertEquals(60, Conversion.sourceForTouch(40, 1.5, 1000), "a stronger Working gives more");
        assertEquals(11, Conversion.sourceForTouch(40, 1, 11.3), "never past what is left, whole points only");
        assertEquals(0, Conversion.sourceForTouch(40, 1, 0));
        assertEquals(0, Conversion.sourceForTouch(40, 0, 1000), "a powerless Working gives nothing");
        assertEquals(0, Conversion.sourceForTouch(40, 1, -3));
    }

    @Test void manaAndSourceConvertBothWaysAtTheParRate() {
        assertEquals(10, Conversion.manaForSource(10, 1), 1e-9);
        assertEquals(5, Conversion.manaForSource(10, 2), 1e-9);
        assertEquals(0, Conversion.manaForSource(10, 0), 1e-9);
        assertEquals(20, Conversion.sourceForMana(10, 2), 1e-9);
        assertEquals(0, Conversion.sourceForMana(-10, 2), 1e-9);
    }

    @Test void noRoundTripGainsAndTheDefaultsKeepTheirPromises() {
        assertEquals(.65 * .65, Conversion.roundTrip(LOSS), 1e-9);
        assertTrue(Conversion.roundTrip(LOSS) < .5, "a round trip at the defaults keeps under half");
        for (double loss = .05; loss <= .95; loss += .05) assertTrue(Conversion.roundTrip(loss) < 1, "no loss setting lets a round trip gain: " + loss);
        // The full chain at the defaults: 50 mana -> 6.5 Thrum (Resonate) -> source budget of a Working that paid it all.
        double thrum = Conversion.thrumForMana(50, MANA_PER_THRUM, LOSS);
        double source = Conversion.sourceBudget(thrum, MANA_PER_THRUM, SOURCE_PER_MANA, LOSS, RESOURCES);
        assertTrue(source < 50 * SOURCE_PER_MANA * Conversion.roundTrip(LOSS) + 1e-9, "50 mana comes back as at most " + 50 * Conversion.roundTrip(LOSS) + " source, got " + source);
        assertTrue(Conversion.guardsHold(1.15, .95, 1.0, .35));
        assertFalse(Conversion.guardsHold(1.0, .95, 1.0, .35), "a cost factor of one with the staff discount would make Ars cheaper than parity");
        assertFalse(Conversion.guardsHold(1.15, .95, .9, .35), "Vocana may never be cheaper than its own numbers");
        assertFalse(Conversion.guardsHold(1.15, .95, 1.0, .2), "a conversion must lose at least three tenths");
    }

    @Test void theCooldownCountsDownToZeroAndNoFurther() {
        assertEquals(100, Conversion.cooldownLeft(1000, 1000, 100));
        assertEquals(1, Conversion.cooldownLeft(1099, 1000, 100));
        assertEquals(0, Conversion.cooldownLeft(1100, 1000, 100));
        assertEquals(0, Conversion.cooldownLeft(5000, 1000, 100));
        assertEquals(0, Conversion.cooldownLeft(1000, 1000, 0), "no cooldown answers at once");
        assertEquals(0, Conversion.cooldownLeft(1000, 1000, -5), "a negative cooldown is none");
    }
}
