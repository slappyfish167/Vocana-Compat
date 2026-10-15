package dev.vocana.compat.iceandfire;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Settings of the Ice and Fire module, {@code config/vocanacompat-iceandfire.toml}. Every number the module applies
 * lives here: the siren switches, the dragonsteel rebalance (one block of offsets per dragonsteel kind, laid over
 * netherite's figures by {@link DragonsteelBalance}) and the wyrmsteel figures. The boolean switches are also
 * reachable from data through the core's {@code vocanacompat:module_flag} condition ({@code "module":"iceandfire"})
 * by the key names in {@link #FLAGS}, so a data file can follow a finer toggle than the module's own. Values are read
 * lazily, after the config has loaded; {@link #get} answers the default before then.
 */
public final class IceAndFireConfig {
    private IceAndFireConfig() {}
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue verboseLogging;

    // ---- sirens ----
    public static final ModConfigSpec.BooleanValue removeSirens, removeSirenIslands;

    // ---- dragonsteel ----
    public static final ModConfigSpec.BooleanValue rebalanceDragonsteel;
    /** The per-kind offsets, in the order {@link DragonsteelBalance.Metal} declares the kinds (fire, ice, lightning, wyrmsteel). */
    public static final Map<DragonsteelBalance.Metal, Offsets> OFFSETS = new LinkedHashMap<>();
    public static final ModConfigSpec.IntValue iceFrostResistancePieces;

    /** One dragonsteel kind's offsets, each a config value; {@link #tuning} reads them into the pure record the helper takes. */
    public record Offsets(ModConfigSpec.DoubleValue damage, ModConfigSpec.DoubleValue attackSpeed, ModConfigSpec.DoubleValue armor,
                          ModConfigSpec.DoubleValue toughness, ModConfigSpec.DoubleValue knockbackResistance, ModConfigSpec.DoubleValue movementSpeed) {
        public DragonsteelBalance.Tuning tuning() {
            return new DragonsteelBalance.Tuning(get(damage), get(attackSpeed), get(armor), get(toughness), get(knockbackResistance), get(movementSpeed));
        }
    }

    /** The boolean switches by the key a {@code vocanacompat:module_flag} condition names. */
    public static final Map<String, ModConfigSpec.BooleanValue> FLAGS = new LinkedHashMap<>();

    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.iceandfire").push("general");
        verboseLogging = b.comment("Log what the Ice and Fire module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.iceandfire.verboseLogging").define("verboseLogging", false);
        b.pop();

        b.comment("Sirens. Ice and Fire adds sirens two ways: as a night-time beach spawn (its own 'iceandfire:siren_spawns'",
                  "biome modifier, off unless its siren.beachSpawns is turned on) and placed by hand on siren islands (the",
                  "'iceandfire:siren_island' structure). removeSirens drops the beach spawn through a 'neoforge:remove_spawns'",
                  "biome modifier and, in code, refuses every siren that is not placed by a spawn egg or a command, so the",
                  "islands still generate but stand empty. removeSirenIslands also empties the biome tag the island structure",
                  "generates in, so no new island appears at all (islands already generated stay).")
            .translation("vocanacompat.configuration.iceandfire.sirens").push("sirens");
        removeSirens = flag(b, IceAndFireSwitches.REMOVE_SIRENS, true, "Keep sirens out of the world (spawn eggs and /summon still work).");
        removeSirenIslands = flag(b, IceAndFireSwitches.REMOVE_SIREN_ISLANDS, false, "Also stop siren islands from generating (requires removeSirens).");
        b.pop();

        b.comment("Dragonsteel. Ice and Fire's dragonsteel reads its figures from its own config (iaf-common.json, 'armors': 25 base",
                  "damage and 8000 durability by default), far above netherite. With rebalance on, this module rewrites the",
                  "attribute modifiers of every dragonsteel tool, weapon and armour piece (and of wyrmsteel gear) at the",
                  "moment they are read: netherite's figures plus the offsets below, so each kind is a netherite-class",
                  "alternative with its own lean. Durability is not an attribute and stays Ice and Fire's; lower",
                  "dragonSteelBaseDurability there if you want netherite's 2031. Tool offsets add to netherite's attack damage",
                  "(sword 8, axe 9, shovel 6.5, pickaxe 5, hoe 1) and attack speed (sword -2.4, pickaxe -2.8, axe and shovel -3,",
                  "hoe 0, all relative to the player's 4). Armour offsets add to netherite's 3/8/6/3 per piece (never below 0);",
                  "toughness and knockback resistance replace netherite's 3 and 0.1 per piece; movementSpeed is a fraction of",
                  "the wearer's speed per piece worn (-0.02 is two percent slower per piece).")
            .translation("vocanacompat.configuration.iceandfire.dragonsteel").push("dragonsteel");
        rebalanceDragonsteel = flag(b, IceAndFireSwitches.REBALANCE, true, "Rewrite dragonsteel (and wyrmsteel) attributes towards netherite's figures with the offsets below.");
        OFFSETS.put(DragonsteelBalance.Metal.FIRE, offsets(b, "fire", "Fire dragonsteel: hits harder, swings slower, holds its ground.",
            2.0, -0.2, 0.0, 3.0, 0.15, 0.0));
        OFFSETS.put(DragonsteelBalance.Metal.ICE, offsets(b, "ice", "Ice dragonsteel: tougher armour, a little heavier; its wearer sheds freezing (see iceFrostResistancePieces).",
            1.0, -0.3, 0.0, 4.0, 0.1, -0.02));
        OFFSETS.put(DragonsteelBalance.Metal.LIGHTNING, offsets(b, "lightning", "Lightning dragonsteel: quick to swing and quick to move, thinner armour.",
            0.0, 0.4, -1.0, 2.0, 0.05, 0.02));
        OFFSETS.put(DragonsteelBalance.Metal.WYRMSTEEL, offsets(b, "wyrmsteel", "Wyrmsteel (the netherite-dragonsteel alloy, this module's own gear): the hardest edge, netherite's pace.",
            3.0, 0.0, 1.0, 4.0, 0.15, 0.0));
        iceFrostResistancePieces = b.comment("How many ice dragonsteel armour pieces a player must wear to shed freezing (powder snow) entirely; 0 turns it off.")
            .translation("vocanacompat.configuration.iceandfire.dragonsteel.iceFrostResistancePieces").defineInRange("iceFrostResistancePieces", 2, 0, 4);
        b.pop();
        SPEC = b.build();
    }

    private static ModConfigSpec.BooleanValue flag(ModConfigSpec.Builder b, String key, boolean fallback, String comment) {
        if (!IceAndFireSwitches.ALL.contains(key)) throw new IllegalArgumentException("switch " + key + " is not listed in IceAndFireSwitches");
        var v = b.comment(comment).translation("vocanacompat.configuration.iceandfire." + key).define(key, fallback);
        FLAGS.put(key, v);
        return v;
    }
    private static Offsets offsets(ModConfigSpec.Builder b, String kind, String comment, double damage, double speed, double armor, double toughness, double knockback, double movement) {
        b.comment(comment).translation("vocanacompat.configuration.iceandfire.dragonsteel." + kind).push(kind);
        var o = new Offsets(
            b.comment("Added to netherite's attack damage of the same tool.").translation("vocanacompat.configuration.iceandfire.dragonsteel.damage").defineInRange("damage", damage, -8.0, 32.0),
            b.comment("Added to netherite's attack speed of the same tool (positive is faster).").translation("vocanacompat.configuration.iceandfire.dragonsteel.attackSpeed").defineInRange("attackSpeed", speed, -3.0, 4.0),
            b.comment("Added to netherite's armour of the same piece.").translation("vocanacompat.configuration.iceandfire.dragonsteel.armor").defineInRange("armor", armor, -8.0, 8.0),
            b.comment("Armour toughness per piece (netherite: 3).").translation("vocanacompat.configuration.iceandfire.dragonsteel.toughness").defineInRange("toughness", toughness, 0.0, 12.0),
            b.comment("Knockback resistance per piece (netherite: 0.1).").translation("vocanacompat.configuration.iceandfire.dragonsteel.knockbackResistance").defineInRange("knockbackResistance", knockback, 0.0, 1.0),
            b.comment("Movement speed per piece worn, as a fraction of the wearer's speed.").translation("vocanacompat.configuration.iceandfire.dragonsteel.movementSpeed").defineInRange("movementSpeed", movement, -0.25, 0.25));
        b.pop();
        return o;
    }

    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
    /** A boolean switch by its condition key; null for a key that is not one. */
    public static Boolean flag(String key) {
        var v = FLAGS.get(key);
        return v == null ? null : get(v);
    }
    /** The offsets of one kind as the pure helper reads them. */
    public static DragonsteelBalance.Tuning tuning(DragonsteelBalance.Metal metal) { return OFFSETS.get(metal).tuning(); }
}
