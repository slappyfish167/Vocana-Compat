package dev.vocana.compat.iceandfire;

/**
 * Wyrmsteel's resistance to dragon breath, kept free of Minecraft so it can be unit-tested: each piece of wyrmsteel
 * dragon armour a dragon wears and each piece of wyrmsteel armour a player wears takes its share off the damage, and
 * the shares add. {@link WyrmsteelWard} applies it to the damage types in {@code #vocanacompat:iceandfire/dragon_breath}.
 */
public final class WyrmsteelBreath {
    private WyrmsteelBreath() {}

    /** The factor the damage is multiplied by: one less every piece's share, never below zero nor above one. */
    public static double factor(int dragonArmourPieces, double perDragonArmourPiece, int armourPieces, double perArmourPiece) {
        double kept = 1 - Math.max(0, dragonArmourPieces) * perDragonArmourPiece - Math.max(0, armourPieces) * perArmourPiece;
        return kept < 0 ? 0 : Math.min(1, kept);
    }
}
