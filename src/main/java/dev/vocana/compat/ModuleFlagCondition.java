package dev.vocana.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * The data condition {@code {"type":"vocanacompat:module_flag","module":"<module id>","key":"<switch>"}}: true only
 * when that module loaded and the boolean switch it registered under that key ({@link VocanaCompat#registerFlag}) is
 * on. It lets one data file follow a finer toggle than {@code vocanacompat:module_enabled}. The type is registered by
 * the core, never by a module: NeoForge decodes a file's whole condition list before testing any of it, so a type that
 * only existed once its module had loaded would make the file unreadable (and a datapack registry entry such as a
 * biome modifier fatal) in every game without that mod. An unknown module or key is false, with a warning.
 */
public record ModuleFlagCondition(String module, String key) implements ICondition {
    public static final MapCodec<ModuleFlagCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.STRING.fieldOf("module").forGetter(ModuleFlagCondition::module),
        Codec.STRING.fieldOf("key").forGetter(ModuleFlagCondition::key)
    ).apply(i, ModuleFlagCondition::new));

    @Override public boolean test(IContext context) {
        var m = Module.byId(module);
        if (m == null) { VocanaCompat.LOG.warn("vocanacompat:module_flag: no module named '{}'", module); return false; }
        if (!VocanaCompat.loaded(m)) return false;
        Boolean v = VocanaCompat.flag(m, key);
        if (v == null) { VocanaCompat.LOG.warn("vocanacompat:module_flag: the {} module has no switch named '{}'", module, key); return false; }
        return v;
    }

    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
