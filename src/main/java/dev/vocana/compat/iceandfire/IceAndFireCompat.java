package dev.vocana.compat.iceandfire;

import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ice and Fire: Community Edition <-> Vocana. Loaded only when the mod {@code iceandfire} is installed and
 * {@code modules.iceandfire.enabled} is on. The module touches Ice and Fire by ids and data alone (entity and item
 * ids from its {@code IafEntities}/{@code IafItems} on branch {@code 1.21.1}), so nothing here imports
 * {@code com.iafenvoy.*}; what it does:
 * <ul>
 * <li>sirens gone ({@link SirenRemoval} and {@code data/vocanacompat/neoforge/biome_modifier/iceandfire/});</li>
 * <li>dragonsteel brought to netherite's class with a lean per kind ({@link DragonsteelRebalance}, numbers in {@link IceAndFireConfig});</li>
 * <li>wyrmsteel, the netherite-dragonsteel alloy, and its whole set (staff, five tools, four armour pieces, a block), made only on Create machines ({@link IceAndFireItems}, {@code data/vocanacompat/recipe/iceandfire/});</li>
 * <li>words: dragons teach FIRE, FROST, LIGHTNING and SKY, and the Elder shades DRAGONFROST and DRAGONBOLT ({@code src/main/lexicon/iceandfire.json}, {@code vocana:hears} tags);</li>
 * <li>Imbue Bench recipes from dragon materials ({@code data/vocanacompat/vocana_imbue/iceandfire/}).</li>
 * </ul>
 * Settings live in {@link IceAndFireConfig} ({@code config/vocanacompat-iceandfire.toml}); see docs/iceandfire.md.
 */
public final class IceAndFireCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.ICE_AND_FIRE, IceAndFireConfig.SPEC);
        // The siren switches reach data as vocanacompat:module_flag {"module":"iceandfire","key":...}; the type itself is the core's.
        IceAndFireConfig.FLAGS.forEach((key, value) -> VocanaCompat.registerFlag(Module.ICE_AND_FIRE, key, () -> IceAndFireConfig.get(value)));
        IceAndFireItems.register(modBus);
        NeoForge.EVENT_BUS.addListener(DragonsteelRebalance::onAttributes);
        NeoForge.EVENT_BUS.addListener(DragonsteelRebalance::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SirenRemoval::onFinalizeSpawn);
        NeoForge.EVENT_BUS.addListener(SirenRemoval::onJoin);
    }
}
