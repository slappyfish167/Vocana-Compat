package dev.vocana.compat;

import net.neoforged.bus.api.IEventBus;

/**
 * One compatibility module. {@link VocanaCompat} instantiates the module's class reflectively during mod
 * construction, only when its target mod is installed and its toggle is on, and calls {@link #init} once. Everything
 * the module does (registering its own config, {@code DeferredRegister}s, event listeners, {@code VocanaApi}
 * behaviours) starts from there.
 *
 * <p>Rule for module code: classes of the target mod may be referenced only from inside the module's own package
 * ({@code dev.vocana.compat.<module>}). Nothing outside it, and nothing in another module, may import them, or a
 * missing mod would crash class loading for everyone.
 */
public interface CompatModule {
    /** Runs during mod construction, on the mod event bus of {@code vocanacompat}. */
    void init(IEventBus modBus);
}
