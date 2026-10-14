package dev.vocana.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.vocana.Imbue;
import dev.vocana.Trigger;
import dev.vocana.Vocana;
import dev.vocana.bench.ImbueRecipe;
import dev.vocana.bench.ImbueRules;
import dev.vocana.bench.ResidueItem;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.server.ImbueRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * One Imbue Bench recipe as an EMI page: the gear it takes (a slot cycling through what its target stands for), an
 * arrow, a preview of the piece once imbued (its tooltip is Vocana's own, read from the {@code vocana:imbues}
 * component), the materials laid on the bench below, the ritual's catalyst blocks at the right as catalysts, then the
 * text: what it lays in (named only when heard, drawn at render time from the snapshot), the price in levels, Thrum,
 * strength and complexity, the gear kind and triggers, the Elder-shade surcharge, the catalysts' counts and radius,
 * and whether the chant must be channelled. The bench is the category's workstation.
 */
final class ImbueEmiRecipe implements EmiRecipe {
    static final int WIDTH = 160, LINE = 10, INK = 0xFF404040;
    private final EmiRecipeCategory category;
    private final ImbueRecipe recipe;
    private final ResourceLocation id;
    private final ImbueRules.Kind kind;
    private final Trigger trigger;
    private final EmiIngredient target, preview;
    private final List<EmiIngredient> inputs, catalysts;
    private final List<EmiStack> outputs;
    private final List<FormattedCharSequence> lines;

    ImbueEmiRecipe(EmiRecipeCategory category, ImbueRecipe r) {
        this.category = category; this.recipe = r;
        this.id = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "imbue/" + EmiText.slug(r.id()));
        this.kind = ImbueRules.Kind.of(r.kind());
        // The preview acts on the first trigger the recipe allows that the gear's kind accepts.
        Trigger t = r.triggers().isEmpty() ? Trigger.PASSIVE : r.triggers().get(0);
        if (kind != null) for (Trigger c : r.triggers()) if (ImbueRules.accepts(kind, c)) { t = c; break; }
        this.trigger = t;
        List<ItemStack> gear = Gear.representatives(kind, r.item(), r.tag());
        var tagKey = r.tag() == null ? null : ResourceLocation.tryParse(r.tag());
        this.target = r.item() == null && tagKey != null ? EmiIngredient.of(TagKey.create(Registries.ITEM, tagKey))
            : gear.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(gear.stream().map(EmiStack::of).toList());
        var imbue = new Imbue(laid(r), trigger, r.strength(), r.complexity(), r.chant());
        List<EmiStack> previews = new ArrayList<>();
        for (ItemStack g : gear) previews.add(EmiStack.of(Gear.imbued(g, imbue)));
        this.outputs = List.copyOf(previews);
        this.preview = previews.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(previews);
        List<EmiIngredient> in = new ArrayList<>();
        in.add(target);
        for (var m : r.materials()) { var ing = material(m); if (!ing.isEmpty()) in.add(ing); }
        this.inputs = List.copyOf(in);
        List<EmiIngredient> cats = new ArrayList<>();
        for (var c : r.catalysts()) {
            var ing = c.block() != null ? Gear.blockItem(c.block(), c.count()) : EmiIngredient.of(Gear.itemsOfBlockTag(c.tag(), c.count()), c.count());
            if (!ing.isEmpty()) cats.add(ing);
        }
        this.catalysts = List.copyOf(cats);
        this.lines = List.copyOf(text(r));
    }

    /** What the preview says it carries: the first named essence, "any essence", or the chant's limit. */
    private static String laid(ImbueRecipe r) {
        if (r.chant()) return Component.translatable("vocanacompat.emi.imbue.chant", r.maxCp()).getString();
        String first = r.essences().isEmpty() ? ImbueRecipe.ANY : r.essences().get(0);
        return first.equals(ImbueRecipe.ANY) ? Component.translatable("vocanacompat.emi.imbue.any_essence_short").getString() : first;
    }
    /** A material as a slot: the item, the tag, or for a residue of the essence being laid a residue of it (when one essence is named). */
    private EmiIngredient material(ImbueRecipe.Material m) {
        if (m.ofEssence() && !recipe.chant() && recipe.essences().size() == 1 && !recipe.essences().get(0).equals(ImbueRecipe.ANY))
            return EmiStack.of(ResidueItem.of(recipe.essences().get(0), m.count()));
        if (m.item() != null) { var i = Gear.item(m.item()); return i == null ? EmiStack.EMPTY : EmiStack.of(i, m.count()); }
        var key = ResourceLocation.tryParse(m.tag());
        if (key == null) return EmiStack.EMPTY;
        if (m.ofEssence() && m.tag().equals("vocana:residues")) return EmiStack.of(Vocana.RESIDUE.get(), m.count());
        return EmiIngredient.of(TagKey.create(Registries.ITEM, key), m.count());
    }
    /** The static lines, wrapped once on EMI's reload thread as EmiInfoRecipe wraps its text. */
    private List<FormattedCharSequence> text(ImbueRecipe r) {
        var font = Minecraft.getInstance().font;
        List<Component> out = new ArrayList<>();
        out.add(Component.translatable(r.chant() ? "vocanacompat.emi.imbue.price_chant" : "vocanacompat.emi.imbue.price",
            r.xpLevels(), EmiText.number(r.thrum()), EmiText.number(r.strength()), r.complexity()));
        List<String> acts = new ArrayList<>();
        for (Trigger t : r.triggers()) if (kind == null || ImbueRules.accepts(kind, t)) acts.add(t.label);
        out.add(Component.translatable("vocanacompat.emi.imbue.into", Component.translatable("vocanacompat.emi.kind." + (kind == null ? "any" : kind.key())), EmiText.prose(acts)));
        if (!r.chant() && r.shades() && !ImbueRecipe.Surcharge.NONE.equals(r.shade()))
            out.add(Component.translatable("vocanacompat.emi.imbue.surcharge", r.shade().xpLevels(), EmiText.number(r.shade().thrum()), r.shade().complexity()));
        for (var c : r.catalysts()) out.add(Component.translatable("vocanacompat.emi.imbue.catalyst", c.count(), ImbueRecipes.name(c), c.radius()));
        if (r.channel()) out.add(Component.translatable(r.chant() ? "vocanacompat.emi.imbue.channel_chant" : "vocanacompat.emi.imbue.channel_essence"));
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component c : out) lines.addAll(font.split(c, WIDTH - 2));
        return lines;
    }
    /** What the recipe lays in, as the player may hear it now: forms once heard, glosses as plain words otherwise. */
    private Component laysLine() {
        if (recipe.chant()) return Component.translatable("vocanacompat.emi.imbue.lays", Component.translatable("vocanacompat.emi.imbue.chant", recipe.maxCp()));
        if (recipe.essences().contains(ImbueRecipe.ANY)) return Component.translatable("vocanacompat.emi.imbue.lays", Component.translatable("vocanacompat.emi.imbue.any_essence"));
        var what = Component.empty();
        for (int i = 0; i < recipe.essences().size(); i++) {
            if (i > 0) what.append(i == recipe.essences().size() - 1 ? " and " : ", ");
            what.append(Heard.essence(recipe.essences().get(i)));
        }
        if (recipe.shades()) what.append(Component.translatable("vocanacompat.emi.imbue.shades"));
        return Component.translatable("vocanacompat.emi.imbue.lays", what);
    }

    @Override public EmiRecipeCategory getCategory() { return category; }
    @Override public ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return inputs; }
    @Override public List<EmiIngredient> getCatalysts() { return catalysts; }
    @Override public List<EmiStack> getOutputs() { return outputs; }
    @Override public RecipeHolder<?> getBackingRecipe() { return null; }
    @Override public int getDisplayWidth() { return WIDTH; }
    /** The slot rows, two lines for what is laid in (it may wrap), then the static lines. */
    @Override public int getDisplayHeight() { return 44 + 2 * LINE + lines.size() * LINE + 2; }

    @Override public void addWidgets(WidgetHolder w) {
        w.addSlot(target, 0, 0);
        w.addTexture(EmiTexture.EMPTY_ARROW, 22, 0);
        w.addSlot(preview, 50, 0).recipeContext(this).appendTooltip(Component.translatable("vocanacompat.emi.imbue.preview"));
        int x = 0;
        for (int i = 1; i < inputs.size(); i++) {
            var slot = w.addSlot(inputs.get(i), x, 22);
            var m = recipe.materials().get(i - 1);
            if (m.ofEssence()) slot.appendTooltip(Component.translatable("vocanacompat.emi.imbue.residue_of"));
            x += 18;
        }
        int cx = WIDTH - 18 * catalysts.size();
        for (var c : catalysts) { w.addSlot(c, cx, 22).catalyst(true); cx += 18; }
        // What is laid in depends on what the player has heard, so it is drawn from the snapshot each frame.
        w.addDrawable(0, 44, WIDTH, 2 * LINE, (g, mx, my, delta) -> {
            var font = Minecraft.getInstance().font;
            var wrapped = font.split(laysLine(), WIDTH - 2);
            for (int i = 0; i < Math.min(2, wrapped.size()); i++) g.drawString(font, wrapped.get(i), 0, i * LINE, INK, false);
        });
        int y = 44 + 2 * LINE;
        for (var line : lines) { w.addText(line, 0, y, INK, false); y += LINE; }
    }
}
