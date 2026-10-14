package dev.vocana.compat.create;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the Create module, {@code config/vocanacompat-create.toml}: every number a behaviour uses and a toggle
 * per feature. Add values here, never to the shared {@code CompatConfig}. Read through {@link #get}, which answers
 * the default before the file has loaded (the config is {@code COMMON} and loads after module construction).
 */
public final class CreateConfig {
    private CreateConfig() {}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue verboseLogging;

    // KINETIC, the essence
    public static final ModConfigSpec.DoubleValue touchDamage, touchShove, gearShove;
    public static final ModConfigSpec.IntValue touchSlownessTicks, imbueSlownessAmplifier;
    public static final ModConfigSpec.BooleanValue toolHaste;
    public static final ModConfigSpec.DoubleValue toolHasteFactor;
    public static final ModConfigSpec.IntValue toolHasteRadius, toolHasteRescanTicks;

    // SPIN, the deed
    public static final ModConfigSpec.DoubleValue spinShove;
    public static final ModConfigSpec.BooleanValue spinCranks, spinStokesBurners, spinIndicators;
    public static final ModConfigSpec.IntValue crankSeconds, maxCranks;

    static {
        var b = new ModConfigSpec.Builder();
        b.translation("vocanacompat.configuration.create").push("general");
        verboseLogging = b.comment("Log what the Create module does (words registered, cranks turned, burners stoked) at INFO instead of DEBUG.")
            .translation("vocanacompat.configuration.create.verboseLogging").define("verboseLogging", false);
        b.pop();

        b.comment("KINETIC (krenshu): the essence of turning things.").translation("vocanacompat.configuration.create.kinetic").push("kinetic");
        touchDamage = b.comment("Damage of KINETIC's natural contact, per point of intensity (a plain bolt is intensity 1).")
            .translation("vocanacompat.configuration.create.kinetic.touchDamage").defineInRange("touchDamage", 3.0, 0.0, 40.0);
        touchShove = b.comment("Strength of the sideways whirl KINETIC's contact gives a creature, per point of intensity.")
            .translation("vocanacompat.configuration.create.kinetic.touchShove").defineInRange("touchShove", 1.0, 0.0, 5.0);
        touchSlownessTicks = b.comment("How long (ticks) a creature reels with Slowness after KINETIC's contact, per point of intensity (capped at two).")
            .translation("vocanacompat.configuration.create.kinetic.touchSlownessTicks").defineInRange("touchSlownessTicks", 60, 0, 1200);
        imbueSlownessAmplifier = b.comment("Slowness level (0 = I) of the wound-up state IMBUE lays with KINETIC; Mining Fatigue I comes with it.")
            .translation("vocanacompat.configuration.create.kinetic.imbueSlownessAmplifier").defineInRange("imbueSlownessAmplifier", 1, 0, 3);
        gearShove = b.comment("Whirl strength of a weapon imbued with KINETIC, per point of the imbue's strength.")
            .translation("vocanacompat.configuration.create.kinetic.gearShove").defineInRange("gearShove", 0.6, 0.0, 5.0);
        toolHaste = b.comment("A tool imbued with KINETIC (any trigger) digs faster while Create machinery turns nearby.")
            .translation("vocanacompat.configuration.create.kinetic.toolHaste").define("toolHaste", true);
        toolHasteFactor = b.comment("The dig-speed multiplier of such a tool among running machinery.")
            .translation("vocanacompat.configuration.create.kinetic.toolHasteFactor").defineInRange("toolHasteFactor", 1.5, 1.0, 5.0);
        toolHasteRadius = b.comment("How far (blocks) a turning kinetic block counts as nearby.")
            .translation("vocanacompat.configuration.create.kinetic.toolHasteRadius").defineInRange("toolHasteRadius", 6, 1, 16);
        toolHasteRescanTicks = b.comment("How often (ticks) the surroundings of a digging player are checked for turning machinery.")
            .translation("vocanacompat.configuration.create.kinetic.toolHasteRescanTicks").defineInRange("toolHasteRescanTicks", 20, 1, 200);
        b.pop();

        b.comment("SPIN (kirsau): the Force Deed that sets things turning.").translation("vocanacompat.configuration.create.spin").push("spin");
        spinShove = b.comment("Whirl strength SPIN gives creatures and dropped items, per point of intensity.")
            .translation("vocanacompat.configuration.create.spin.spinShove").defineInRange("spinShove", 0.9, 0.0, 5.0);
        spinCranks = b.comment("SPIN at a hand crank (or a valve handle) turns it by itself for a while, as if a hand were on it.")
            .translation("vocanacompat.configuration.create.spin.spinCranks").define("spinCranks", true);
        crankSeconds = b.comment("How long (seconds, scaled by the Working's extent) a spun hand crank keeps turning.")
            .translation("vocanacompat.configuration.create.spin.crankSeconds").defineInRange("crankSeconds", 6, 1, 120);
        maxCranks = b.comment("How many hand cranks may be turning by magic at once on the server; further SPINs are refused.")
            .translation("vocanacompat.configuration.create.spin.maxCranks").defineInRange("maxCranks", 64, 1, 1024);
        spinStokesBurners = b.comment("SPIN at a blaze burner with FIRE in the chant kindles it (as coal would); with LIGHTNING it seethes (as a blaze cake would, for Create's own 160 s).")
            .translation("vocanacompat.configuration.create.spin.spinStokesBurners").define("spinStokesBurners", true);
        spinIndicators = b.comment("SPIN and KINETIC draw Create's rotation indicator particles on the kinetic blocks they reach.")
            .translation("vocanacompat.configuration.create.spin.spinIndicators").define("spinIndicators", true);
        b.pop();
        SPEC = b.build();
    }

    /** The configured value, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        try { return value.get(); } catch (IllegalStateException e) { return value.getDefault(); }
    }
}
