package dev.vocana.compat;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Vocana Compat: soft-dependency bridges between Vocana and other mods. The mod itself does almost nothing; each
 * {@link Module} is a package that is only class-loaded when its target mod is present and its toggle in
 * {@link CompatConfig} is on. Words come from {@code META-INF/vocana/lexicon.json} (assembled by the build from
 * {@code src/main/lexicon/<module>.json}), behaviour from {@code dev.vocana.api.VocanaApi}, data from
 * {@code data/vocanacompat} behind the {@code vocanacompat:module_enabled} condition.
 */
@Mod(VocanaCompat.ID)
public final class VocanaCompat {
    public static final String ID = "vocanacompat";
    public static final Logger LOG = LogUtils.getLogger();

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, ID);
    static {
        CONDITIONS.register("module_enabled", () -> ModuleEnabledCondition.CODEC);
        CONDITIONS.register("module_flag", () -> ModuleFlagCondition.CODEC);
    }

    private static ModContainer container;
    private static final Map<Module, CompatModule> LOADED = new EnumMap<>(Module.class);
    /** The boolean switches each module exposes to data through {@code vocanacompat:module_flag}. */
    private static final Map<Module, Map<String, BooleanSupplier>> FLAGS = new EnumMap<>(Module.class);

    public VocanaCompat(IEventBus modBus, ModContainer container) {
        VocanaCompat.container = container;
        // STARTUP configs are read as soon as they are registered, so the toggles are known before the modules load.
        container.registerConfig(ModConfig.Type.STARTUP, CompatConfig.SPEC, "vocanacompat-modules.toml");
        CONDITIONS.register(modBus);
        for (var m : Module.values()) load(m, modBus);
        LOG.info("Vocana Compat {}: {} module(s) loaded {}", container.getModInfo().getVersion(), LOADED.size(), LOADED.keySet());
    }

    /** Loads one module when its mod is installed, its toggle is on and this build compiled it. Failures stay local to the module. */
    private static void load(Module m, IEventBus modBus) {
        if (!ModList.get().isLoaded(m.modId)) { LOG.debug("{} module: '{}' is not installed", m.id, m.modId); return; }
        if (!CompatConfig.enabled(m)) { LOG.info("{} module: disabled in vocanacompat-modules.toml", m.id); return; }
        try {
            // Initialising the class here means a static initialiser that touches the target mod fails inside this try too.
            Class<?> type = Class.forName(m.className(), true, VocanaCompat.class.getClassLoader());
            var module = (CompatModule) type.getDeclaredConstructor().newInstance();
            module.init(modBus);
            LOADED.put(m, module);
            LOG.info("{} module: loaded for {}", m.id, m.displayName);
        } catch (ClassNotFoundException e) {
            LOG.warn("{} module: '{}' is installed but this build of Vocana Compat has no {} (built with compat.skip?)", m.id, m.modId, m.className());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.error("{} module failed to load; Vocana keeps working without it", m.id, e);
        }
    }

    /** The modules that actually loaded in this game: mod present, toggle on, init succeeded. */
    public static Set<Module> loaded() { return Collections.unmodifiableSet(LOADED.keySet()); }
    public static boolean loaded(Module m) { return LOADED.containsKey(m); }
    /**
     * Exposes one of a module's boolean switches to data as {@code {"type":"vocanacompat:module_flag","module":"<id>",
     * "key":"<key>"}}. Call it from {@link CompatModule#init}; the supplier is read at every data reload, so a config
     * value works (read it lazily: a COMMON config is not loaded during {@code init}).
     */
    public static void registerFlag(Module m, String key, BooleanSupplier value) {
        FLAGS.computeIfAbsent(m, k -> new java.util.LinkedHashMap<>()).put(key, value);
    }
    /** A module's switch by key: its value, or null when the module registered no such key. */
    public static Boolean flag(Module m, String key) {
        var s = FLAGS.getOrDefault(m, Map.of()).get(key);
        return s == null ? null : s.getAsBoolean();
    }
    /** The {@code vocanacompat} mod container, for modules that register configs or need their mod info. */
    public static ModContainer container() { return container; }

    /**
     * Registers a module's own settings file, {@code config/vocanacompat-<module>.toml}, as a {@code COMMON} config.
     * Call it from {@link CompatModule#init}. A spec without values is skipped by NeoForge, which is fine for a stub.
     */
    public static void registerModuleConfig(Module m, ModConfigSpec spec) {
        container.registerConfig(ModConfig.Type.COMMON, spec, m.configFile());
    }
}
