package dev.vocana.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.vocana.client.ClientState;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.ChapterGroup;
import dev.vocana.engine.Vocabulary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * One word of the language as an EMI page, drawn like a leaf of the Grimoire: its form and respelling, class, chapter
 * and group, its meaning when the server tells meanings, an Elder word's base (named only when heard too), the chant
 * bound to it and the practised chants that say it. All of it is read from the snapshot at draw time, so a page that
 * was a blank slot becomes the word the moment the Chime hears it; before any snapshot it says how to get one. The
 * page's id is the word's position in the lexicon, never its form, and an unheard word is shown only as its chapter and
 * the Grimoire's hint of where such words answer, as the Grimoire's blank slots are (Canon §7.2).
 */
final class WordEmiRecipe implements EmiRecipe {
    static final int WIDTH = 160, HEIGHT = 76, LINE = 10, TITLE = 0xFF202020, INK = 0xFF404040, FAINT = 0xFF707070;
    final Vocabulary.Word word;
    final int index;
    final Chapter chapter;
    final ChapterGroup group;
    private final ResourceLocation id;
    private final EmiStack icon;

    WordEmiRecipe(Vocabulary.Word word, int index) {
        this.word = word; this.index = index;
        this.chapter = Chapter.of(word);
        this.group = ChapterGroup.of(chapter);
        this.id = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "lexicon/word/" + index);
        this.icon = Gear.icon(group.icon(), Items.BOOK);
    }
    /** The group's place among the ribbons, for sorting. */
    int groupIndex() { return ChapterGroup.all().indexOf(group); }

    @Override public EmiRecipeCategory getCategory() { return VocanaEmiPlugin.LEXICON; }
    @Override public ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return List.of(); }
    @Override public List<EmiStack> getOutputs() { return List.of(); }
    @Override public boolean supportsRecipeTree() { return false; }
    @Override public RecipeHolder<?> getBackingRecipe() { return null; }
    @Override public int getDisplayWidth() { return WIDTH; }
    @Override public int getDisplayHeight() { return HEIGHT; }

    @Override public void addWidgets(WidgetHolder w) {
        w.addSlot(icon, 0, 0).drawBack(false);
        w.addDrawable(0, 0, WIDTH, HEIGHT, (g, mx, my, delta) -> draw(g));
    }

    private void draw(GuiGraphics g) {
        var font = Minecraft.getInstance().font;
        Component title; List<Component> body = new ArrayList<>(); int colour = INK;
        String place = chapter.title() + " · " + group.title();
        if (!Heard.known()) {
            title = Component.literal(place);
            body.add(Component.translatable("vocanacompat.emi.lexicon.no_snapshot"));
            colour = FAINT;
        } else if (!Heard.heard(word.form())) {
            title = Component.literal(place);
            body.add(Component.translatable(unheardKey()));
            colour = FAINT;
        } else {
            title = Component.literal(word.form() + "  [" + word.say() + "]");
            String cls = word.category() + (word.frame().isEmpty() ? "" : " (" + word.frame() + ")");
            body.add(Component.literal(cls + " · " + place));
            if (ClientState.meanings()) {
                if (!word.meaning().isEmpty()) body.add(Component.literal(word.meaning()));
                if (word.elder()) body.add(elderLine());
            }
            String bound = ClientState.bound(word.form());
            if (bound != null) body.add(Component.translatable("vocanacompat.emi.lexicon.calls", bound));
            body.addAll(Heard.practisedWith(word.form(), 2));
        }
        g.drawString(font, font.plainSubstrByWidth(title.getString(), WIDTH - 22), 22, 4, TITLE, false);
        int y = 16, left = (HEIGHT - y) / LINE;
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component c : body) lines.addAll(font.split(c, WIDTH - 2));
        for (int i = 0; i < Math.min(left, lines.size()); i++) g.drawString(font, lines.get(i), 0, y + i * LINE, colour, false);
    }
    /** An Elder word's base, named only when the reader has heard it, and what the shade costs. */
    private Component elderLine() {
        var base = Vocabulary.INSTANCE.byGloss(word.base());
        Component name = base != null && Heard.heard(base.form()) ? Component.literal(base.form()) : Component.translatable("vocanacompat.emi.lexicon.elder_unheard_base");
        int weight = word.shade() == null ? 0 : word.shade().weight();
        return Component.translatable("vocanacompat.emi.lexicon.elder", name, weight > 0 ? Component.translatable("vocanacompat.emi.lexicon.elder_cost", weight) : Component.empty());
    }
    /** Where an unheard word may be found, without naming it: the Grimoire's own hints. */
    private String unheardKey() {
        if (word.category().equals("Prime")) return "vocanacompat.emi.lexicon.unheard_prime";
        if ("deep".equals(word.tongue())) return "vocanacompat.emi.lexicon.unheard_deep";
        if (word.category().equals("Particle") || word.category().equals("Measure")) return "vocanacompat.emi.lexicon.unheard_grammar";
        if (word.calling()) return "vocanacompat.emi.lexicon.unheard_calling";
        return "vocanacompat.emi.lexicon.unheard";
    }
}
