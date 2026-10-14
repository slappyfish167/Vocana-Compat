package dev.vocana.compat.iceandfire;

import com.mojang.serialization.MapCodec;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Ice and Fire: Community Edition <-> Vocana. Loaded only when the mod {@code iceandfire} is installed and
 * {@code modules.iceandfire.enabled} is on. The module touches Ice and Fire by ids and data alone (entity and item
 * ids from its {@code IafEntities}/{@code IafItems} on branch {@code 1.21.1}), so nothing here imports
 * {@code com.iafenvoy.*}; what it does:
 * <ul>
 * <li>sirens gone ({@link SirenRemoval} and {@code data/vocanacompat/neoforge/biome_modifier/iceandfire/});</li>
 * <li>dragonsteel brought to netherite's class with a lean per kind ({@link DragonsteelRebalance}, numbers in {@link IceAndFireConfig});</li>
 * <li>wyrmsteel, the netherite-dragonsteel alloy, and its staff, sword and pickaxe, made only on Create machines ({@link IceAndFireItems}, {@code data/vocanacompat/recipe/iceandfire/});</li>
 * <li>words: dragons teach FIRE, FROST, LIGHTNING and SKY, and the Elder shades DRAGONFROST and DRAGONBOLT ({@code src/main/lexicon/iceandfire.json}, {@code vocana:hears} tags);</li>
 * <li>Imbue Bench recipes from dragon materials ({@code data/vocanacompat/vocana_imbue/iceandfire/}).</li>
 * </ul>
 * Settings live in {@link IceAndFireConfig} ({@code config/vocanacompat-iceandfire.toml}); see docs/iceandfire.md.
 */
public final class IceAndFireCompat implements CompatModule {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, VocanaCompat.ID);
    static { CONDITIONS.register("iceandfire/config", () -> IceAndFireConfigCondition.CODEC); }

    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.ICE_AND_FIRE, IceAndFireConfig.SPEC);
        CONDITIONS.register(modBus);
        IceAndFireItems.register(modBus);
        NeoForge.EVENT_BUS.addListener(DragonsteelRebalance::onAttributes);
        NeoForge.EVENT_BUS.addListener(DragonsteelRebalance::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SirenRemoval::onFinalizeSpawn);
        NeoForge.EVENT_BUS.addListener(SirenRemoval::onJoin);
    }
}
