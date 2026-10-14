package dev.vocana.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.common.items.EnchantersSword;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import dev.vocana.api.VocanaApi;
import dev.vocana.compat.CompatModule;
import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import net.neoforged.bus.api.IEventBus;

/**
 * Ars Nouveau <-> Vocana. Loaded only when the mod {@code ars_nouveau} is installed and
 * {@code modules.ars_nouveau.enabled} is on. Only classes in this package may import
 * {@code com.hollingsworth.arsnouveau}, GeckoLib or Nuggets. Settings live in {@link ArsNouveauConfig}
 * ({@code config/vocanacompat-ars_nouveau.toml}); the word SOURCE in {@code src/main/lexicon/ars_nouveau.json}; data
 * under {@code data/vocanacompat/<kind>/ars_nouveau/}. See {@code docs/ars_nouveau.md}.
 *
 * <p>What it wires, all during mod construction: on Vocana's side the essence SOURCE ({@link SourceEssence}), a Thrum
 * cost factor that defaults to one, and Ars's Enchanter's Sword (and, if the owner wants, spell books) as Catalysts;
 * on Ars's side the spell cost factor and the staff discount ({@link SpellCosts}), the Resonate glyph
 * ({@link ResonateEffect}) and its page in the Worn Notebook ({@link ArsDocumentation}). The glyph must be registered
 * here, not at common setup, because Ars's item registration walks {@code GlyphRegistry.getGlyphItemMap()} and
 * {@code GlyphRegistry.registerSpell} files the glyph's config with the active mod container, which is this mod only
 * while it is being constructed.
 */
public final class ArsNouveauCompat implements CompatModule {
    @Override public void init(IEventBus modBus) {
        VocanaCompat.registerModuleConfig(Module.ARS_NOUVEAU, ArsNouveauConfig.SPEC);

        VocanaApi.registerEssence(SourceEssence.GLOSS, new SourceEssence());
        VocanaApi.registerCostFactor((player, reading) -> ArsNouveauConfig.get(ArsNouveauConfig.vocanaCostFactor));
        // The toggles are read per call, so the config may be changed after the registrations are made.
        VocanaApi.registerCatalyst(stack -> ArsNouveauConfig.get(ArsNouveauConfig.enchantersSwordCatalyst) && stack.getItem() instanceof EnchantersSword,
            stack -> ArsNouveauConfig.get(ArsNouveauConfig.enchantersSwordCapacity));
        VocanaApi.registerCatalyst(stack -> ArsNouveauConfig.get(ArsNouveauConfig.spellBookCatalyst) && stack.getItem() instanceof SpellBook,
            stack -> ArsNouveauConfig.get(ArsNouveauConfig.spellBookCapacity));

        SpellCosts.register();
        GlyphRegistry.registerSpell(ResonateEffect.INSTANCE);
        ArsDocumentation.register();
        VocanaCompat.LOG.debug("ars_nouveau module: SOURCE essence, cost bridge and the Resonate glyph {} registered", ResonateEffect.ID);
    }
}
