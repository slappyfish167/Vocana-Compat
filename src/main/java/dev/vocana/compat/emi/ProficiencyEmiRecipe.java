package dev.vocana.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.vocana.client.ClientState;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * The proficiency page of the lexicon category: the five levels with the releases each needs on this server
 * ({@code masteryCasts} from the snapshot, 40 by default) and what each takes off Thrum, Strain and voicing, a line on
 * spoken and ritual practice, then the player's own practised chants as the Callings overview lists them. Drawn from
 * the snapshot each frame, like the word pages.
 */
final class ProficiencyEmiRecipe implements EmiRecipe {
    static final int WIDTH = 160, LINE = 10, TITLE = 0xFF202020, INK = 0xFF404040, FAINT = 0xFF707070, CHANTS = 4;
    static final int HEIGHT = 12 + 5 * LINE + 2 * LINE + 4 + LINE + CHANTS * LINE + 2;
    private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "lexicon/proficiency");

    @Override public EmiRecipeCategory getCategory() { return VocanaEmiPlugin.LEXICON; }
    @Override public ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return List.of(); }
    @Override public List<EmiStack> getOutputs() { return List.of(); }
    @Override public boolean supportsRecipeTree() { return false; }
    @Override public RecipeHolder<?> getBackingRecipe() { return null; }
    @Override public int getDisplayWidth() { return WIDTH; }
    @Override public int getDisplayHeight() { return HEIGHT; }

    @Override public void addWidgets(WidgetHolder w) {
        w.addDrawable(0, 0, WIDTH, HEIGHT, (g, mx, my, delta) -> draw(g));
    }
    private void draw(GuiGraphics g) {
        var font = Minecraft.getInstance().font;
        int casts = ClientState.masteryCasts();
        g.drawString(font, Component.translatable("vocanacompat.emi.proficiency.title", casts).getVisualOrderText(), 0, 2, TITLE, false);
        int y = 12;
        for (int level = 0; level < 5; level++) {
            var line = Component.translatable("vocanacompat.emi.proficiency.line", level, Heard.level(level), EmiText.threshold(level, casts),
                EmiText.number(EmiText.thrumFactor(level)), EmiText.number(EmiText.strainFactor(level)), EmiText.number(EmiText.voicingFactor(level)));
            g.drawString(font, font.plainSubstrByWidth(line.getString(), WIDTH), 0, y, INK, false);
            y += LINE;
        }
        List<FormattedCharSequence> gains = font.split(Component.translatable("vocanacompat.emi.proficiency.gains"), WIDTH - 2);
        for (int i = 0; i < Math.min(2, gains.size()); i++) g.drawString(font, gains.get(i), 0, y + i * LINE, FAINT, false);
        y += 2 * LINE + 4;
        var practised = Heard.practised();
        g.drawString(font, Component.translatable(practised.isEmpty() ? (Heard.known() ? "vocanacompat.emi.proficiency.none" : "vocanacompat.emi.lexicon.no_snapshot") : "vocanacompat.emi.proficiency.yours").getVisualOrderText(), 0, y, TITLE, false);
        y += LINE;
        for (int i = 0; i < Math.min(CHANTS, practised.size()); i++) {
            g.drawString(font, font.plainSubstrByWidth(Heard.line(practised.get(i)).getString(), WIDTH), 0, y, INK, false);
            y += LINE;
        }
    }
}
