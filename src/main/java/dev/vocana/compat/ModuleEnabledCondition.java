package dev.vocana.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * The data condition {@code {"type":"vocanacompat:module_enabled","module":"<module id>"}}: true only when that
 * module loaded in this game (mod installed, toggle on, init succeeded). Put it, together with
 * {@code neoforge:mod_loaded}, in the {@code neoforge:conditions} of every recipe, tag, loot modifier and
 * advancement a module ships, so a disabled module's data stays out of the game. Unknown module ids are false.
 */
public record ModuleEnabledCondition(String module) implements ICondition {
    public static final MapCodec<ModuleEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.STRING.fieldOf("module").forGetter(ModuleEnabledCondition::module)
    ).apply(i, ModuleEnabledCondition::new));

    public static ModuleEnabledCondition of(Module m) { return new ModuleEnabledCondition(m.id); }

    @Override public boolean test(IContext context) {
        var m = Module.byId(module);
        if (m == null) { VocanaCompat.LOG.warn("vocanacompat:module_enabled: no module named '{}'", module); return false; }
        return VocanaCompat.loaded(m);
    }

    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
