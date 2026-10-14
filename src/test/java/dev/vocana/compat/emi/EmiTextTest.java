package dev.vocana.compat.emi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The EMI pages' pure formatting: numbers, proficiency thresholds (as docs/proficiency.md tabulates them), chant words and ids. */
final class EmiTextTest {
    @Test void numbersDropNeedlessFractions() {
        assertEquals("25", EmiText.number(25.0));
        assertEquals("12.5", EmiText.number(12.5));
        assertEquals("0.96", EmiText.number(0.96));
        assertEquals("0.9", EmiText.number(0.9));
        assertEquals("1", EmiText.number(1f));
        assertEquals("0.84", EmiText.number(1 - .04 * 4));
    }
    @Test void thresholdsFollowTheDocumentedTable() {
        assertArrayEquals(new int[]{0, 4, 12, 24, 40}, new int[]{EmiText.threshold(0, 40), EmiText.threshold(1, 40), EmiText.threshold(2, 40), EmiText.threshold(3, 40), EmiText.threshold(4, 40)});
        assertEquals(1, EmiText.threshold(1, 5), "a fraction rounds up, as the server's ceil does");
        assertEquals(0, EmiText.threshold(-1, 40)); assertEquals(40, EmiText.threshold(9, 40));
        assertEquals(0, EmiText.level(3, 40)); assertEquals(1, EmiText.level(4, 40)); assertEquals(2, EmiText.level(12, 40));
        assertEquals(3, EmiText.level(39, 40)); assertEquals(4, EmiText.level(40, 40)); assertEquals(4, EmiText.level(400, 40));
    }
    @Test void factorsMatchTheServersPerLevelSteps() {
        assertEquals(1.0, EmiText.thrumFactor(0), 1e-9); assertEquals(0.84, EmiText.thrumFactor(4), 1e-9);
        assertEquals(0.6, EmiText.strainFactor(4), 1e-9); assertEquals(0.9, EmiText.voicingFactor(1), 1e-9);
    }
    @Test void chantKeysSplitIntoWords() {
        assertEquals(List.of("open", "saunta", "tenhu", "close"), EmiText.words("open saunta tenhu close"));
        assertEquals(List.of(), EmiText.words(null)); assertEquals(List.of(), EmiText.words("   "));
        assertTrue(EmiText.uses("open saunta tenhu close", "saunta"));
        assertTrue(EmiText.uses("open Saunta close", "SAUNTA"), "case does not matter");
        assertFalse(EmiText.uses("open saunta close", "saun"), "a word, not a prefix");
        assertFalse(EmiText.uses("open saunta close", ""));
    }
    @Test void proseJoinsLists() {
        assertEquals("", EmiText.prose(List.of()));
        assertEquals("on hit", EmiText.prose(List.of("on hit")));
        assertEquals("on hit and while held or worn", EmiText.prose(List.of("on hit", "while held or worn")));
        assertEquals("a, b and c", EmiText.prose(List.of("a", "b", "c")));
    }
    @Test void slugsAndGlossesAreSafe() {
        assertEquals("vocana/fire_weapon", EmiText.slug("vocana:fire_weapon"));
        assertEquals("vocanacompat/create/brass_sword", EmiText.slug("vocanacompat:create/brass_sword"));
        assertEquals("odd_id", EmiText.slug("Odd Id")); assertEquals("unnamed", EmiText.slug(""));
        assertEquals("fire", EmiText.glossWord("FIRE")); assertEquals("soul flame", EmiText.glossWord("SOUL_FLAME")); assertEquals("", EmiText.glossWord(null));
    }
}
