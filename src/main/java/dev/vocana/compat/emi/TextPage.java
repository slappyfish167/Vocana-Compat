package dev.vocana.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * A page of explanation in one of the module's categories: a row of stacks (inputs, then outputs, with catalysts at the
 * right edge), a title and a body wrapped to the page. EMI's own {@code EmiInfoRecipe} does much the same but only in
 * its Information category; this one sits where the explanation belongs (the ritual category, or the empty Imbue
 * Bench category). The body is wrapped once, when EMI builds the recipe on its reload thread, as EmiInfoRecipe does
 * ({@code EmiInfoRecipe} constructor, branch 1.21), so the height is known before the page is laid out.
 */
final class TextPage implements EmiRecipe {
    static final int WIDTH = 160, LINE = 10, TITLE = 0xFF202020, INK = 0xFF404040;
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs, catalysts;
    private final List<EmiStack> outputs;
    private final FormattedCharSequence title;
    private final List<FormattedCharSequence> body;

    TextPage(EmiRecipeCategory category, ResourceLocation id, List<EmiIngredient> inputs, List<EmiIngredient> catalysts, List<EmiStack> outputs, Component title, List<Component> body) {
        this.category = category; this.id = id;
        this.inputs = List.copyOf(inputs); this.catalysts = List.copyOf(catalysts); this.outputs = List.copyOf(outputs);
        var font = Minecraft.getInstance().font;
        this.title = title.getVisualOrderText();
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component c : body) lines.addAll(font.split(c, WIDTH - 2));
        this.body = List.copyOf(lines);
    }
    private boolean hasStacks() { return !inputs.isEmpty() || !outputs.isEmpty() || !catalysts.isEmpty(); }
    private int textTop() { return hasStacks() ? 22 : 0; }

    @Override public EmiRecipeCategory getCategory() { return category; }
    @Override public ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return inputs; }
    @Override public List<EmiIngredient> getCatalysts() { return catalysts; }
    @Override public List<EmiStack> getOutputs() { return outputs; }
    @Override public boolean supportsRecipeTree() { return false; }
    @Override public RecipeHolder<?> getBackingRecipe() { return null; }
    @Override public int getDisplayWidth() { return WIDTH; }
    @Override public int getDisplayHeight() { return textTop() + LINE + 2 + body.size() * LINE + 2; }

    @Override public void addWidgets(WidgetHolder w) {
        int x = 0;
        for (var in : inputs) { w.addSlot(in, x, 0); x += 18; }
        if (!inputs.isEmpty() && !outputs.isEmpty()) { w.addTexture(EmiTexture.EMPTY_ARROW, x + 2, 0); x += 28; }
        for (var out : outputs) { w.addSlot(out, x, 0).recipeContext(this); x += 18; }
        int cx = WIDTH - 18 * catalysts.size();
        for (var c : catalysts) { w.addSlot(c, cx, 0).catalyst(true); cx += 18; }
        int y = textTop();
        w.addText(title, 0, y, TITLE, false);
        y += LINE + 2;
        for (var line : body) { w.addText(line, 0, y, INK, false); y += LINE; }
    }
}
