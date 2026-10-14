package dev.vocana.compat.curios;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the Curios module, {@code config/vocanacompat-curios.toml}. Every number the module applies lives here:
 * what the focus items grant, how long a worn relic takes to whisper, and whether a curio goes on by use. Add values
 * here, never to the shared {@code CompatConfig}. The file is a {@code COMMON} config, read after the module has loaded,
 * so callers read values through {@link #get} (the default before load).
 */
public final class CuriosConfig {
    private CuriosConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;
    /** Capacity each Ring of Focus adds while worn (through {@code VocanaApi.registerCapacity}). */
    public static final ModConfigSpec.IntValue ringCapacity;
    /** The Thrum regeneration multiplier of a worn Amulet of Resonance. */
    public static final ModConfigSpec.DoubleValue amuletRegen;
    /** The Strain multiplier at the Seal of a worn Charm of Calm. */
    public static final ModConfigSpec.DoubleValue charmStrain;
    /** The Thrum multiplier on a Working whose essence a ring-slot item carries (a Signet's essence, an imbued piece). */
    public static final ModConfigSpec.DoubleValue signetCostFactor;
    /** Whether a Whispering Relic worn in a charm slot whispers its word, and after how many seconds of wear. */
    public static final ModConfigSpec.BooleanValue relicWhispers;
    public static final ModConfigSpec.IntValue relicWhisperSeconds;
    /** Whether using (right-clicking) a Vocana curio puts it straight into a free slot. */
    public static final ModConfigSpec.BooleanValue equipFromUse;
    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.curios").push("general");
        verboseLogging = b.comment("Log what the Curios module does at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.curios.verboseLogging").define("verboseLogging", false);
        equipFromUse = b.comment("Using (right-clicking) a Ring of Focus, Amulet of Resonance, Charm of Calm, Signet, Torc or Whispering Relic puts it into a free Curios slot.")
            .translation("vocanacompat.configuration.curios.equipFromUse").define("equipFromUse", true);
        b.pop();
        b.comment("What the focus items grant while worn. Set a multiplier to 1 (or a bonus to 0) to switch an item's effect off.")
            .translation("vocanacompat.configuration.curios.items").push("items");
        ringCapacity = b.comment("Capacity each worn Ring of Focus adds (Vocana's Capacity is 8 + 2.5 × Focus plus the Catalyst's bonus).")
            .translation("vocanacompat.configuration.curios.ringCapacity").defineInRange("ringCapacity", 2, 0, 20);
        amuletRegen = b.comment("Thrum regeneration multiplier while an Amulet of Resonance is worn.")
            .translation("vocanacompat.configuration.curios.amuletRegen").defineInRange("amuletRegen", 1.25, 0.1, 5.0);
        charmStrain = b.comment("Strain multiplier at the Seal while a Charm of Calm is worn.")
            .translation("vocanacompat.configuration.curios.charmStrain").defineInRange("charmStrain", 0.85, 0.1, 2.0);
        signetCostFactor = b.comment("Thrum multiplier on a Working of an essence carried by an item in a ring slot: a Signet of the Elements that drank",
                                     "a residue of that essence, or any essence-imbued piece a pack maker tags into curios:ring.")
            .translation("vocanacompat.configuration.curios.signetCostFactor").defineInRange("signetCostFactor", 0.9, 0.1, 2.0);
        b.pop();
        b.comment("Vocana's own items in Curios slots.").translation("vocanacompat.configuration.curios.relics").push("relics");
        relicWhispers = b.comment("A Whispering Relic worn in a charm slot whispers its word to its wearer after a while, as using it would.")
            .translation("vocanacompat.configuration.curios.relicWhispers").define("relicWhispers", true);
        relicWhisperSeconds = b.comment("Seconds a relic must be worn before it whispers (counted anew each time it is put on).")
            .translation("vocanacompat.configuration.curios.relicWhisperSeconds").defineInRange("relicWhisperSeconds", 30, 1, 600);
        b.pop();
        SPEC = b.build();
    }
    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
