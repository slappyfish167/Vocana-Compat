package dev.vocana.compat.wyrmroost;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the Wyrmroost module, {@code config/vocanacompat-wyrmroost.toml}. Every feature has a toggle and every
 * number lives here; the lexicon (word profiles) and the data (tags, recipes) are not configurable because Vocana
 * reads them before any COMMON config loads. Values are read lazily through {@link #get} so a behaviour registered
 * during mod construction sees the configured value once the file has loaded and the default before.
 */
public final class WyrmroostConfig {
    private WyrmroostConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;

    // words
    /** Whether the Essence WYRM has behaviour (selects and touches dragons); off, the word is inert. */
    public static final ModConfigSpec.BooleanValue wyrmWord;
    /** Whether the Deed ROOST has behaviour; off, the word is inert. */
    public static final ModConfigSpec.BooleanValue roostWord;

    // ROOST
    /** Seconds a wild dragon hesitates (drops its target, slowed and weakened) at ROOST, times the clause's grade. */
    public static final ModConfigSpec.DoubleValue hesitateSeconds;
    /** The intensity (clause grade: 1 plain, 1.5 GREATER, ...) from which ROOST also puts a settled dragon to sleep. */
    public static final ModConfigSpec.DoubleValue sleepAtIntensity;
    /** Whether ROOST also sits and rouses the caster's vanilla pets (wolves, cats, parrots). */
    public static final ModConfigSpec.BooleanValue roostVanillaPets;
    /** Whether a dragon tamed by a scoreboard team-mate counts as the caster's own (never harmed, may be roosted). */
    public static final ModConfigSpec.BooleanValue respectTeams;

    // WYRM
    /** Damage of a made WYRM reaching a creature that is no dragon of the caster's (times intensity). */
    public static final ModConfigSpec.DoubleValue wyrmTouchDamage;
    /** Knockback of a made WYRM reaching a creature (times intensity). */
    public static final ModConfigSpec.DoubleValue wyrmTouchKnockback;
    /** Whether a made WYRM reaching one of the caster's own dragons mends it instead of striking it. */
    public static final ModConfigSpec.BooleanValue wyrmMendsOwnDragons;
    /** Health a made WYRM gives one of the caster's own dragons (times intensity). */
    public static final ModConfigSpec.DoubleValue wyrmMendAmount;

    // interplay
    /** Whether a caster's magic damage (the indirect magic Vocana deals) is refused by dragons they or a team-mate tamed. */
    public static final ModConfigSpec.BooleanValue protectTamedFromCasterMagic;

    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.wyrmroost").push("general");
        verboseLogging = b.comment("Log what the Wyrmroost module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.wyrmroost.verboseLogging").define("verboseLogging", false);
        b.pop();

        b.translation("vocanacompat.configuration.wyrmroost.words").push("words");
        wyrmWord = b.comment("Give the Essence WYRM its behaviour: it names and selects Wyrmroost's dragons, and a made WYRM",
                             "strikes with a dragon's weight (or mends the caster's own dragon). Off, the word does nothing.")
            .translation("vocanacompat.configuration.wyrmroost.wyrmWord").define("wyrm", true);
        roostWord = b.comment("Give the Deed ROOST its behaviour: it settles and rouses the caster's dragons and makes wild",
                              "ones hesitate. Off, the word does nothing.")
            .translation("vocanacompat.configuration.wyrmroost.roostWord").define("roost", true);
        b.pop();

        b.translation("vocanacompat.configuration.wyrmroost.roost").push("roost");
        hesitateSeconds = b.comment("How long a wild dragon hesitates at ROOST (drops its target, slowed and weakened), in",
                                    "seconds; a GREATER clause lengthens it.")
            .translation("vocanacompat.configuration.wyrmroost.hesitateSeconds").defineInRange("hesitateSeconds", 5.0, 0.0, 120.0);
        sleepAtIntensity = b.comment("From this clause intensity (1 is plain, each GREATER multiplies by 1.5) ROOST also puts a",
                                     "settled dragon to sleep, when its kind sleeps.")
            .translation("vocanacompat.configuration.wyrmroost.sleepAtIntensity").defineInRange("sleepAtIntensity", 1.5, 1.0, 10.0);
        roostVanillaPets = b.comment("ROOST also sits and rouses the caster's tamed wolves, cats and parrots.")
            .translation("vocanacompat.configuration.wyrmroost.roostVanillaPets").define("vanillaPets", true);
        respectTeams = b.comment("A dragon tamed by a scoreboard team-mate counts as the caster's own: it may be roosted and is",
                                 "never struck or mended as a stranger's would be.")
            .translation("vocanacompat.configuration.wyrmroost.respectTeams").define("respectTeams", true);
        b.pop();

        b.translation("vocanacompat.configuration.wyrmroost.wyrm").push("wyrm");
        wyrmTouchDamage = b.comment("Damage a made WYRM deals to a creature it reaches, times the Working's intensity.")
            .translation("vocanacompat.configuration.wyrmroost.wyrmTouchDamage").defineInRange("touchDamage", 3.0, 0.0, 40.0);
        wyrmTouchKnockback = b.comment("Knockback a made WYRM gives a creature it reaches, times the Working's intensity.")
            .translation("vocanacompat.configuration.wyrmroost.wyrmTouchKnockback").defineInRange("touchKnockback", 0.8, 0.0, 5.0);
        wyrmMendsOwnDragons = b.comment("A made WYRM reaching one of the caster's own dragons mends it instead of striking it.")
            .translation("vocanacompat.configuration.wyrmroost.wyrmMendsOwnDragons").define("mendsOwnDragons", true);
        wyrmMendAmount = b.comment("Health a made WYRM gives one of the caster's own dragons, times the Working's intensity.")
            .translation("vocanacompat.configuration.wyrmroost.wyrmMendAmount").defineInRange("mendAmount", 2.0, 0.0, 40.0);
        b.pop();

        b.translation("vocanacompat.configuration.wyrmroost.interplay").push("interplay");
        protectTamedFromCasterMagic = b.comment("A dragon tamed by the caster (or a team-mate, see roost.respectTeams) refuses the magic",
                                                "damage of that caster's Workings, so a FIRE CONE through one's own mount burns everything but it.")
            .translation("vocanacompat.configuration.wyrmroost.protectTamedFromCasterMagic").define("protectTamedFromCasterMagic", true);
        b.pop();
        SPEC = b.build();
    }

    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
