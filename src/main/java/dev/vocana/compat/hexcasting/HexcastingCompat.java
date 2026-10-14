package dev.vocana.compat.hexcasting;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Hex Casting (HexCast NEOFORGED) <-> Vocana. Loaded only when the mod {@code hexcasting} is installed and
 * {@code modules.hexcasting.enabled} is on. Only classes in this package may import {@code at.petrak.hexcasting},
 * PAUCAL, Patchouli, Caelus or Kotlin types, and this module keeps to {@code at.petrak.hexcasting.api} (see
 * {@code docs/hexcasting.md} for what was verified where). What it adds:
 * <ul>
 * <li>Hex staves ({@code hexcasting:staves}) are Vocana Catalysts with a configurable Capacity ({@link HexStaves});</li>
 * <li>the Deed DRAW turns media the caster carries into Thrum through Hex's media holders ({@link MediaDraw});</li>
 * <li>the Deed AWAKEN casts a packaged Hex held in the caster's hand ({@link HexAwaken});</li>
 * <li>data: imbue recipes paid in amethyst dust and charged amethyst, Hex's amethyst and edified blocks as ritual
 *     anchors, and {@code vocana:hears} tags so Hex's blocks teach words.</li>
 * </ul>
 * Settings live in {@link HexcastingConfig} ({@code config/vocanacompat-hexcasting.toml}); words in
 * {@code src/main/lexicon/hexcasting.json}; data under {@code data/vocanacompat/<kind>/hexcasting/}.
 */
public final class HexcastingCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.HEXCAST, HexcastingConfig.SPEC);
        // Each registration reads its toggle when consulted, so a setting changed in the file applies without a restart.
        VocanaApi.registerCatalyst(HexStaves::isStaff, HexStaves::capacityBonus);
        VocanaApi.registerDeed(MediaDraw.GLOSS, MediaDraw::act);
        VocanaApi.registerDeed(HexAwaken.GLOSS, HexAwaken::act);
        VocanaCompat.LOG.debug("hexcasting module: staves as Catalysts, Deeds {} and {} registered", MediaDraw.GLOSS, HexAwaken.GLOSS);
    }
}
