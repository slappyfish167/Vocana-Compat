package dev.vocana.compat.hexcasting;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the Hex Casting module, {@code config/vocanacompat-hexcasting.toml}. Every number the module uses and
 * one toggle per feature live here, never in the shared {@code CompatConfig}. The file is a {@code COMMON} config and
 * is read after the module has loaded, so every value is read lazily through {@link #get}, which answers the default
 * until then.
 */
public final class HexcastingConfig {
    private HexcastingConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    /** Hex staves count as Vocana Catalysts (inscribe, chant aloud, tune, hold a Bound Catalyst). */
    public static final ModConfigSpec.BooleanValue stavesAsCatalysts;
    /** The Capacity a Hex staff adds, like a Vocana staff's +2/+4/+6 (Canon §8.2). */
    public static final ModConfigSpec.IntValue staffCapacityBonus;
    /** The Deed DRAW: media the caster carries becomes Thrum. */
    public static final ModConfigSpec.BooleanValue drawEnabled;
    /** Media units per point of Thrum. Hex counts one amethyst dust as 10 000 media, a shard 50 000, a charged crystal 100 000. */
    public static final ModConfigSpec.LongValue mediaPerThrum;
    /** The most Thrum one DRAW may restore (a measure on the Deed scales it, at most twofold). */
    public static final ModConfigSpec.DoubleValue drawMaxThrumPerCast;
    /** Whether DRAW may empty rechargeable holders (phials) as well as raw amethyst. */
    public static final ModConfigSpec.BooleanValue drawFromBatteries;
    /** The Deed AWAKEN: a packaged Hex (cypher, trinket, artifact) in the caster's hand casts itself. */
    public static final ModConfigSpec.BooleanValue awakenEnabled;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.hexcasting").push("general");
        verboseLogging = b.comment("Log what the Hex Casting module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.hexcasting.verboseLogging").define("verboseLogging", false);
        b.pop();
        b.comment("Hex staves as Vocana Catalysts.").translation("vocanacompat.configuration.hexcasting.staves").push("staves");
        stavesAsCatalysts = b.comment("Any item in the hexcasting:staves tag may be held as a Catalyst: inscribe, chant aloud, tune, keep a Bound Catalyst.")
            .translation("vocanacompat.configuration.hexcasting.staves.enabled").define("enabled", true);
        staffCapacityBonus = b.comment("The Capacity a Hex staff adds. Vocana's own staffs add 2, 4 or 6.")
            .translation("vocanacompat.configuration.hexcasting.staves.capacityBonus").defineInRange("capacityBonus", 2, 0, 6);
        b.pop();
        b.comment("The Deed DRAW (hainsu): media the caster carries becomes Thrum.").translation("vocanacompat.configuration.hexcasting.draw").push("draw");
        drawEnabled = b.comment("Whether DRAW works. Off, the word is still in the lexicon but refuses.")
            .translation("vocanacompat.configuration.hexcasting.draw.enabled").define("enabled", true);
        mediaPerThrum = b.comment("Media per point of Thrum. Hex Casting counts one amethyst dust as 10000 media (a shard 50000, charged amethyst 100000),",
                                  "so 2500 makes a dust worth four Thrum; a full pool is 40 to 376 Thrum.")
            .translation("vocanacompat.configuration.hexcasting.draw.mediaPerThrum").defineInRange("mediaPerThrum", 2500L, 1L, 10_000_000L);
        drawMaxThrumPerCast = b.comment("The most Thrum one DRAW restores. A measure on the Deed (TWO, THREE...) raises it, at most to twice this.")
            .translation("vocanacompat.configuration.hexcasting.draw.maxThrumPerCast").defineInRange("maxThrumPerCast", 20.0, 1.0, 400.0);
        drawFromBatteries = b.comment("Whether DRAW also empties rechargeable media holders (phials). Raw amethyst dust, shards and charged amethyst are always taken, dust first, as Hex itself spends them.")
            .translation("vocanacompat.configuration.hexcasting.draw.fromBatteries").define("fromBatteries", false);
        b.pop();
        b.comment("The Deed AWAKEN (rauspe): a cypher, trinket or artifact in the caster's hand casts its Hex.").translation("vocanacompat.configuration.hexcasting.awaken").push("awaken");
        awakenEnabled = b.comment("Whether AWAKEN works. The item's own cooldown and media rules apply as if it were used by hand.")
            .translation("vocanacompat.configuration.hexcasting.awaken.enabled").define("enabled", true);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
