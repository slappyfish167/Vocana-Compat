package dev.vocana.compat.iceandfire;

import dev.vocana.compat.iceandfire.DragonsteelBalance.Metal;
import dev.vocana.compat.iceandfire.DragonsteelBalance.Piece;
import dev.vocana.compat.iceandfire.DragonsteelBalance.Target;
import dev.vocana.compat.iceandfire.DragonsteelBalance.Tuning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The rebalance arithmetic, without Minecraft: which ids are gear, and what netherite-plus-offsets comes to. */
final class DragonsteelBalanceTest {
    private static final Tuning NONE = new Tuning(0, 0, 0, DragonsteelBalance.NETHERITE_TOUGHNESS, DragonsteelBalance.NETHERITE_KNOCKBACK_RESISTANCE, 0);

    @Test void classifiesIceAndFireDragonsteelGearByIdAndNothingElse() {
        assertEquals(new Target(Metal.FIRE, Piece.SWORD), DragonsteelBalance.classify("iceandfire:dragonsteel_fire_sword"));
        assertEquals(new Target(Metal.ICE, Piece.CHESTPLATE), DragonsteelBalance.classify("iceandfire:dragonsteel_ice_chestplate"));
        assertEquals(new Target(Metal.LIGHTNING, Piece.HOE), DragonsteelBalance.classify("iceandfire:dragonsteel_lightning_hoe"));
        assertEquals(new Target(Metal.WYRMSTEEL, Piece.PICKAXE), DragonsteelBalance.classify("vocanacompat:wyrmsteel_pickaxe"));
        for (Piece p : Piece.values())
            assertEquals(new Target(Metal.WYRMSTEEL, p), DragonsteelBalance.classify("vocanacompat:wyrmsteel_" + p.suffix()), "the whole wyrmsteel set is rebalanced: " + p);
        assertNull(DragonsteelBalance.classify("vocanacompat:wyrmsteel_block"), "the block is not gear");
        assertNull(DragonsteelBalance.classify("vocanacompat:incomplete_wyrmsteel_axe"), "an incomplete piece is not gear");
        assertNull(DragonsteelBalance.classify("iceandfire:dragonsteel_fire_ingot"), "ingots are not gear");
        assertNull(DragonsteelBalance.classify("iceandfire:dragonsteel_fire_block"));
        assertNull(DragonsteelBalance.classify("iceandfire:dragonarmor_dragonsteel_fire_head"), "dragon armour is not player gear");
        assertNull(DragonsteelBalance.classify("vocanacompat:wyrmsteel_staff"), "the staff keeps Vocana's figures");
        assertNull(DragonsteelBalance.classify("minecraft:netherite_sword"));
        assertNull(DragonsteelBalance.classify("vocanacompat:dragonsteel_fire_sword"), "the namespace matters");
        assertNull(DragonsteelBalance.classify("nonsense"));
        assertNull(DragonsteelBalance.classify(null));
    }

    @Test void zeroOffsetsReproduceNetherite() {
        var sword = DragonsteelBalance.numbers(new Target(Metal.FIRE, Piece.SWORD), NONE);
        assertEquals(7, sword.attackDamage(), "netherite sword: 1 + 7 = 8");
        assertEquals(-2.4, sword.attackSpeed(), 1e-9);
        assertEquals(0, sword.armor());
        var axe = DragonsteelBalance.numbers(new Target(Metal.ICE, Piece.AXE), NONE);
        assertEquals(8, axe.attackDamage()); assertEquals(-3.0, axe.attackSpeed(), 1e-9);
        assertEquals(5.5, DragonsteelBalance.numbers(new Target(Metal.ICE, Piece.SHOVEL), NONE).attackDamage(), 1e-9);
        assertEquals(0, DragonsteelBalance.numbers(new Target(Metal.ICE, Piece.HOE), NONE).attackDamage(), 1e-9);
        var chest = DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.CHESTPLATE), NONE);
        assertEquals(8, chest.armor()); assertEquals(3, chest.toughness()); assertEquals(0.1, chest.knockbackResistance(), 1e-9);
        assertEquals(0, chest.attackDamage()); assertEquals(0, chest.movementSpeed());
        assertEquals(3, DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.HELMET), NONE).armor());
        assertEquals(6, DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.LEGGINGS), NONE).armor());
        assertEquals(3, DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.BOOTS), NONE).armor());
    }

    @Test void offsetsLayOverNetheriteAndArmourNeverGoesBelowZero() {
        var fire = new Tuning(2, -0.2, 0, 3, 0.15, 0);
        var sword = DragonsteelBalance.numbers(new Target(Metal.FIRE, Piece.SWORD), fire);
        assertEquals(9, sword.attackDamage(), 1e-9, "a fire dragonsteel sword hits for 10");
        assertEquals(-2.6, sword.attackSpeed(), 1e-9);
        var lightning = new Tuning(0, 0.4, -1, 2, 0.05, 0.02);
        var boots = DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.BOOTS), lightning);
        assertEquals(2, boots.armor()); assertEquals(2, boots.toughness()); assertEquals(0.02, boots.movementSpeed(), 1e-9);
        var stripped = DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.BOOTS), new Tuning(0, 0, -8, -1, 2, 0));
        assertEquals(0, stripped.armor(), "armour is floored at zero");
        assertEquals(0, stripped.toughness(), "toughness is floored at zero");
        assertEquals(1, stripped.knockbackResistance(), "knockback resistance is capped at one");
        assertEquals(-2.4 + 0.4, DragonsteelBalance.numbers(new Target(Metal.LIGHTNING, Piece.SWORD), lightning).attackSpeed(), 1e-9);
    }

    @Test void onlyIceArmourShedsFrost() {
        assertTrue(DragonsteelBalance.shedsFrost(new Target(Metal.ICE, Piece.HELMET)));
        assertFalse(DragonsteelBalance.shedsFrost(new Target(Metal.ICE, Piece.SWORD)));
        assertFalse(DragonsteelBalance.shedsFrost(new Target(Metal.FIRE, Piece.HELMET)));
        assertFalse(DragonsteelBalance.shedsFrost(null));
    }

    @Test void everyPieceHasAnIdSuffixAndAKind() {
        for (Piece p : Piece.values()) {
            assertEquals(p.name().toLowerCase(), p.suffix());
            assertNotEquals(p.tool(), p.armour());
            if (p.armour()) assertTrue(p.armor > 0); else assertEquals(0, p.armor);
        }
    }
}
