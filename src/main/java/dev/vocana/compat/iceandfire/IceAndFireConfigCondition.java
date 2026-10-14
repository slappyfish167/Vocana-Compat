package dev.vocana.compat.iceandfire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * The data condition {@code {"type":"vocanacompat:iceandfire/config","key":"removeSirens"}}: true when that boolean
 * switch of {@link IceAndFireConfig} is on. It lets one data file (the siren biome modifier, the island tag) follow a
 * finer toggle than the module's own {@code vocanacompat:module_enabled}; both are still written side by side, since
 * this type only exists once the module has loaded (a file naming an unregistered condition type fails to parse and
 * is skipped with a log line, which the module condition in front of it avoids). An unknown key is false, with a warning.
 */
public record IceAndFireConfigCondition(String key) implements ICondition {
    public static final MapCodec<IceAndFireConfigCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.STRING.fieldOf("key").forGetter(IceAndFireConfigCondition::key)
    ).apply(i, IceAndFireConfigCondition::new));

    @Override public boolean test(IContext context) {
        Boolean v = IceAndFireConfig.flag(key);
        if (v == null) { VocanaCompat.LOG.warn("vocanacompat:iceandfire/config: no switch named '{}' (one of {})", key, IceAndFireConfig.FLAGS.keySet()); return false; }
        return v;
    }

    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
