package dev.vocana.compat.iceandfire;

import java.util.Locale;

/**
 * The arithmetic of the dragonsteel rebalance, kept free of Minecraft so it can be unit-tested: which item id is a
 * piece of which dragonsteel kind, and what attribute amounts that piece gets. The baseline is netherite as vanilla
 * 1.21.1 builds it ({@code SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4f)} and friends: the tier's +4 damage
 * bonus on each tool's base, and {@code ArmorMaterials.NETHERITE}'s 3/8/6/3 armour, 3 toughness, 0.1 knockback
 * resistance), and each kind lays its {@link Tuning} offsets over it. Item ids are matched by name
 * ({@code iceandfire:dragonsteel_<kind>_<piece>}, as {@code IafItems} registers them on the {@code 1.21.1} branch, and
 * this module's {@code vocanacompat:wyrmsteel_<piece>}), so no Ice and Fire class is ever touched.
 */
public final class DragonsteelBalance {
    private DragonsteelBalance() {}

    /** The three dragonsteel kinds Ice and Fire forges, and this module's alloy. {@code idWord} is the kind's word in the item id. */
    public enum Metal {
        FIRE("iceandfire", "dragonsteel_fire_"), ICE("iceandfire", "dragonsteel_ice_"), LIGHTNING("iceandfire", "dragonsteel_lightning_"),
        WYRMSTEEL("vocanacompat", "wyrmsteel_");
        public final String namespace, prefix;
        Metal(String namespace, String prefix) { this.namespace = namespace; this.prefix = prefix; }
    }

    /** What a piece is, with netherite's figures for it: the attack damage and speed modifiers of a tool, or the armour of a slot. */
    public enum Piece {
        SWORD(7, -2.4), PICKAXE(4, -2.8), AXE(8, -3.0), SHOVEL(5.5, -3.0), HOE(0, 0),
        HELMET(3), CHESTPLATE(8), LEGGINGS(6), BOOTS(3);
        /** Netherite's attack damage modifier (the tool's base damage less the player's own 1, plus the tier's 4). */
        public final double damage;
        /** Netherite's attack speed modifier, relative to the player's 4. */
        public final double attackSpeed;
        /** Netherite's armour for the slot, 0 for a tool. */
        public final double armor;
        private final boolean tool;
        Piece(double damage, double attackSpeed) { this.damage = damage; this.attackSpeed = attackSpeed; this.armor = 0; this.tool = true; }
        Piece(double armor) { this.damage = 0; this.attackSpeed = 0; this.armor = armor; this.tool = false; }
        public boolean tool() { return tool; }
        public boolean armour() { return !tool; }
        /** The piece's suffix in an item id. */
        public String suffix() { return name().toLowerCase(Locale.ROOT); }
    }

    /** A piece of a kind. */
    public record Target(Metal metal, Piece piece) {}

    /** One kind's offsets over netherite, as the config holds them. */
    public record Tuning(double damage, double attackSpeed, double armor, double toughness, double knockbackResistance, double movementSpeed) {}

    /** The attribute amounts a piece gets: ADD_VALUE amounts, except {@code movementSpeed}, which multiplies the total. Zero means no modifier. */
    public record Numbers(double attackDamage, double attackSpeed, double armor, double toughness, double knockbackResistance, double movementSpeed) {}

    /** Netherite's toughness and knockback resistance per piece, for the documentation table and tests. */
    public static final double NETHERITE_TOUGHNESS = 3, NETHERITE_KNOCKBACK_RESISTANCE = 0.1;

    /** The piece an item id names, or null for anything that is not dragonsteel or wyrmsteel gear (ingots, blocks, dragon armour). */
    public static Target classify(String itemId) {
        if (itemId == null) return null;
        int colon = itemId.indexOf(':');
        if (colon < 0) return null;
        String namespace = itemId.substring(0, colon), path = itemId.substring(colon + 1);
        for (Metal m : Metal.values()) {
            if (!m.namespace.equals(namespace) || !path.startsWith(m.prefix)) continue;
            String rest = path.substring(m.prefix.length());
            for (Piece p : Piece.values()) if (rest.equals(p.suffix())) return new Target(m, p);
            return null;
        }
        return null;
    }

    /** The amounts for a piece under a tuning: netherite's figure plus the kind's offset, armour never below zero. */
    public static Numbers numbers(Target t, Tuning u) {
        if (t.piece().tool()) return new Numbers(t.piece().damage + u.damage(), t.piece().attackSpeed + u.attackSpeed(), 0, 0, 0, 0);
        return new Numbers(0, 0, Math.max(0, t.piece().armor + u.armor()), Math.max(0, u.toughness()), clamp(u.knockbackResistance(), 0, 1), u.movementSpeed());
    }

    /** Whether a piece counts towards ice dragonsteel's frost resistance: an ice armour piece. */
    public static boolean shedsFrost(Target t) { return t != null && t.metal() == Metal.ICE && t.piece().armour(); }

    private static double clamp(double v, double lo, double hi) { return v < lo ? lo : Math.min(hi, v); }
}
