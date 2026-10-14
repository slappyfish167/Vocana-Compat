package dev.vocana.compat.ars_nouveau;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the Ars Nouveau module, {@code config/vocanacompat-ars_nouveau.toml}. Every number the bridge uses is
 * here and nowhere in the code: the balance factors between the two systems, the exchange rates behind the SOURCE
 * word and the Resonate glyph, and which Ars gear counts as a Vocana Catalyst. The class imports nothing of Ars, so
 * the GameTests, which run without it, may read the defaults; before the config has loaded {@link #get} answers with
 * them. The rationale behind each default is in {@code docs/ars_nouveau.md}.
 */
public final class ArsNouveauConfig {
    private ArsNouveauConfig() {}
    public static final ModConfigSpec SPEC;

    // ---- balance between the two systems ----
    /** Multiplier on every Ars spell's mana cost, after Ars's own discounts (rings, armour threads). */
    public static final ModConfigSpec.DoubleValue arsCostFactor;
    /** Whether the cost factor also applies to spells cast by turrets, runes and other machines. */
    public static final ModConfigSpec.BooleanValue applyToMachines;
    /** Multiplier on Ars resources where this module prices them at run time (the source a SOURCE Working gives). */
    public static final ModConfigSpec.DoubleValue arsResourceFactor;
    /** Multiplier on Vocana's own Thrum at the Seal; never below one, so Vocana is never cheaper than its own numbers. */
    public static final ModConfigSpec.DoubleValue vocanaCostFactor;
    /** Multiplier on an Ars spell's cost while a Vocana staff is held in the off hand (the one place Vocana gear helps Ars). */
    public static final ModConfigSpec.DoubleValue vocanaStaffArsDiscount;
    /** The par rate: how many points of Ars mana one Thrum is worth. */
    public static final ModConfigSpec.DoubleValue manaPerThrum;
    /** The par rate: how many points of source one point of mana is worth (Ars's own turrets draw one source per mana). */
    public static final ModConfigSpec.DoubleValue sourcePerMana;
    /** The share of par value lost on every conversion between the systems, so no loop can gain. */
    public static final ModConfigSpec.DoubleValue conversionLoss;

    // ---- the SOURCE word ----
    /** Source a SOURCE Working pours into the jars near the block it touches, per touch, before the Working's budget. */
    public static final ModConfigSpec.IntValue sourcePerCast;
    /** How far (Manhattan blocks) from the touched block the Working looks for jars. */
    public static final ModConfigSpec.IntValue sourceReach;
    /** Mana a SOURCE Working gives an Ars caster it touches, per touch at full intensity, before the budget. */
    public static final ModConfigSpec.IntValue touchMana;
    /** Mana the wielder of SOURCE-imbued gear regains on a strike (or a wearer when struck), per unit of strength. */
    public static final ModConfigSpec.IntValue strikeMana;

    // ---- the Resonate glyph ----
    public static final ModConfigSpec.BooleanValue resonateEnabled;
    /** Ticks a caster must wait between two Resonates that answer; the real cap on how fast mana becomes Thrum. */
    public static final ModConfigSpec.IntValue resonateCooldownTicks;

    // ---- Ars gear at the Chime ----
    public static final ModConfigSpec.BooleanValue enchantersSwordCatalyst, spellBookCatalyst;
    public static final ModConfigSpec.IntValue enchantersSwordCapacity, spellBookCapacity;

    public static final ModConfigSpec.BooleanValue verboseLogging;

    static {
        var b = new ModConfigSpec.Builder();
        String t = "vocanacompat.configuration.ars_nouveau.";

        b.comment("How dear each system is next to the other. Ars Nouveau is 10-25 % dearer than Vocana by default; nothing here can",
                  "make Vocana cheaper than its own numbers.").translation(t + "balance").push("balance");
        arsCostFactor = b.comment("Multiplier on the mana cost of every Ars spell cast by a player, applied after Ars's own discounts.")
            .translation(t + "arsCostFactor").defineInRange("arsCostFactor", 1.15, 1.0, 2.0);
        applyToMachines = b.comment("Apply arsCostFactor to spell turrets, runes and other machine casters as well as players.")
            .translation(t + "applyToMachines").define("applyToMachines", false);
        arsResourceFactor = b.comment("Multiplier on Ars resources wherever this module prices them at run time: the source a SOURCE Working may",
                                      "give is divided by it. Ars's own recipes are data in its datapack and are not changed (see docs/ars_nouveau.md).")
            .translation(t + "arsResourceFactor").defineInRange("arsResourceFactor", 1.15, 1.0, 2.0);
        vocanaCostFactor = b.comment("Multiplier on Vocana's Thrum at the Seal while this module is loaded. 1 leaves Vocana at its own prices;",
                                     "owners who want Vocana dearer than Ars instead raise this and lower arsCostFactor.")
            .translation(t + "vocanaCostFactor").defineInRange("vocanaCostFactor", 1.0, 1.0, 2.0);
        vocanaStaffArsDiscount = b.comment("Multiplier on an Ars spell's cost while the caster holds a Vocana staff in the off hand. With the default cost",
                                           "factor the two together still leave Ars dearer than Vocana (1.15 x 0.95 = 1.09).")
            .translation(t + "vocanaStaffArsDiscount").defineInRange("vocanaStaffArsDiscount", 0.95, 0.8, 1.0);
        manaPerThrum = b.comment("Par rate: Ars mana one Thrum is worth. 5 weighs the two by regeneration (5 mana a second from a pool of 100",
                                 "against 0.725 Thrum a second from a pool of 82 for a fresh caster of each).")
            .translation(t + "manaPerThrum").defineInRange("manaPerThrum", 5.0, 0.1, 100.0);
        sourcePerMana = b.comment("Par rate: source one mana is worth. Ars's spell turrets draw exactly one source per point of mana their spell costs.")
            .translation(t + "sourcePerMana").defineInRange("sourcePerMana", 1.0, 0.1, 100.0);
        conversionLoss = b.comment("Share of the par value lost on every conversion between the systems (Resonate: mana to Thrum; SOURCE: Thrum to",
                                   "source or mana). At 0.35 a round trip keeps 42 % of what it started with; the module's tests expect at least 0.3.")
            .translation(t + "conversionLoss").defineInRange("conversionLoss", 0.35, 0.05, 0.95);
        b.pop();

        b.comment("The SOURCE word (Thaumaturgy). A Working that touches a plain block pours source into the jars within reach of it;",
                  "a Working that touches an Ars caster gives them mana; both draw on a budget set by the Thrum the Working paid.")
            .translation(t + "source").push("source");
        sourcePerCast = b.comment("Source given per touch of a block, scaled by the Working's power and capped by its budget.")
            .translation(t + "sourcePerCast").defineInRange("sourcePerCast", 40, 1, 10000);
        sourceReach = b.comment("How many blocks (Manhattan distance) from the touched block jars are filled.")
            .translation(t + "sourceReach").defineInRange("sourceReach", 3, 1, 8);
        touchMana = b.comment("Mana given per touch of an Ars caster at intensity 1, capped by the Working's budget.")
            .translation(t + "touchMana").defineInRange("touchMana", 10, 0, 1000);
        strikeMana = b.comment("Mana the holder of SOURCE-imbued gear regains per strike (weapons) or per blow taken (armour), per unit of strength.")
            .translation(t + "strikeMana").defineInRange("strikeMana", 3, 0, 100);
        b.pop();

        b.comment("The Resonate glyph (vocanacompat:glyph_vocana_thrum), an Ars effect that returns a little Thrum to a caster who has",
                  "tuned a Chime. Its mana cost is Ars's per-glyph setting in config/vocanacompat/glyph_vocana_thrum.toml; the Thrum it",
                  "returns is that cost at the par rate minus the conversion loss.").translation(t + "resonate").push("resonate");
        resonateEnabled = b.comment("Whether the glyph does anything when it resolves (it stays craftable and learnable either way).")
            .translation(t + "resonateEnabled").define("enabled", true);
        resonateCooldownTicks = b.comment("Ticks between two Resonates that answer the same caster. 100 (five seconds) caps the glyph at about",
                                          "1.3 Thrum a second however much mana the caster has.")
            .translation(t + "resonateCooldownTicks").defineInRange("cooldownTicks", 100, 0, 72000);
        b.pop();

        b.comment("Ars gear held as a Vocana Catalyst (inscribe, chant aloud, tune), with its Capacity bonus.").translation(t + "gear").push("gear");
        enchantersSwordCatalyst = b.comment("The Enchanter's Sword counts as a Catalyst.")
            .translation(t + "enchantersSwordCatalyst").define("enchantersSwordCatalyst", true);
        enchantersSwordCapacity = b.comment("Capacity the Enchanter's Sword adds as a Catalyst (a Tuning Chime adds 0, the first staff 2).")
            .translation(t + "enchantersSwordCapacity").defineInRange("enchantersSwordCapacity", 1, 0, 3);
        spellBookCatalyst = b.comment("Ars spell books count as Catalysts.")
            .translation(t + "spellBookCatalyst").define("spellBookCatalyst", false);
        spellBookCapacity = b.comment("Capacity a spell book adds as a Catalyst.")
            .translation(t + "spellBookCapacity").defineInRange("spellBookCapacity", 0, 0, 3);
        b.pop();

        b.translation(t + "general").push("general");
        verboseLogging = b.comment("Log what the Ars Nouveau module does at INFO instead of DEBUG.")
            .translation(t + "verboseLogging").define("verboseLogging", false);
        b.pop();
        SPEC = b.build();
    }

    /** The configured value, or its default before the config has loaded (in a test, or during mod construction). */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
