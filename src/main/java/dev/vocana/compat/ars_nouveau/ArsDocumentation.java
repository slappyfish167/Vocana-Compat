package dev.vocana.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.documentation.ReloadDocumentationEvent;
import com.hollingsworth.arsnouveau.api.documentation.builder.DocEntryBuilder;
import com.hollingsworth.arsnouveau.api.documentation.entry.GlyphEntry;
import com.hollingsworth.arsnouveau.api.registry.DocumentationRegistry;
import com.hollingsworth.arsnouveau.setup.registry.Documentation;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The Resonate glyph's page in the Worn Notebook. Ars posts {@code ReloadDocumentationEvent.AddEntries} on the game
 * bus every time a player joins a world, after its own entries, and {@code DocumentationRegistry.registerEntry}
 * overwrites by id, so answering it each time is what the event's Javadoc asks for. The page is built the way Ars
 * builds its own glyph pages ({@code Documentation.addGlyphEntries}) and the example addon builds its: the tier's
 * category, the glyph item as icon, the glyph entry, and the Scribes Table recipe looked up by the id this module ships.
 */
final class ArsDocumentation {
    private ArsDocumentation() {}

    static void register() { NeoForge.EVENT_BUS.addListener(ArsDocumentation::addEntries); }

    private static void addEntries(ReloadDocumentationEvent.AddEntries event) {
        var glyph = ResonateEffect.INSTANCE;
        if (glyph.glyphItem == null) return;
        var builder = new DocEntryBuilder(VocanaCompat.ID, Documentation.glyphCategory(glyph.getConfigTier()), glyph.getLocalizationKey(), glyph.getRegistryName())
            .withIcon(glyph.glyphItem)
            .withPage(GlyphEntry.create(glyph))
            .withCraftingPages(ResonateEffect.RECIPE, glyph.glyphItem);
        DocumentationRegistry.registerEntry(builder.category, builder.build()).withSearchTag(Component.translatable("ars_nouveau.keyword.glyph"));
    }
}
