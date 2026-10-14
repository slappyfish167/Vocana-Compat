package dev.vocana.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.vocana.Imbue;
import dev.vocana.Trigger;
import dev.vocana.Vocana;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The three pages of the ritual category, built as {@link TextPage}s: the ritual figure (anchors as catalysts: the
 * {@code vocana:ritual_anchors} block tag, +8 Capacity and 5% less Strain gain per anchor, at most ten, Ritual tier,
 * from {@code dev.vocana.server.Rituals}), the bench's channel step (the bench and four anchors as catalysts, as the
 * shipped chant recipe asks; the figures are the server's defaults, named as such), and stripping an imbue (an imbued
 * piece in, an Essence Residue and the clean piece out, for {@code bench.stripXp} levels). Text lives in the lang file.
 */
final class RitualPages {
    private RitualPages() {}
    static final String ANCHORS = "vocana:ritual_anchors";

    static List<TextPage> all(EmiRecipeCategory category) {
        EmiStack bench = EmiStack.of(Vocana.IMBUE_BENCH_ITEM.get());
        var figure = new TextPage(category, id("figure"), List.of(), List.of(anchors(3)), List.of(),
            Component.translatable("vocanacompat.emi.ritual.figure.title"),
            List.of(Component.translatable("vocanacompat.emi.ritual.figure.text"), Component.translatable("vocanacompat.emi.ritual.figure.text2")));
        var channel = new TextPage(category, id("channel"), List.of(), List.of(bench, anchors(4)), List.of(),
            Component.translatable("vocanacompat.emi.ritual.channel.title"),
            List.of(Component.translatable("vocanacompat.emi.ritual.channel.text"), Component.translatable("vocanacompat.emi.ritual.channel.text2")));
        var sword = new ItemStack(Items.IRON_SWORD);
        var imbued = Gear.imbued(sword, new Imbue(Component.translatable("vocanacompat.emi.imbue.any_essence_short").getString(), Trigger.ON_HIT, 1f, 6, false));
        var strip = new TextPage(category, id("strip"), List.of(EmiStack.of(imbued)), List.of(bench), List.of(EmiStack.of(Vocana.RESIDUE.get()), EmiStack.of(sword)),
            Component.translatable("vocanacompat.emi.ritual.strip.title"),
            List.of(Component.translatable("vocanacompat.emi.ritual.strip.text", 3), Component.translatable("vocanacompat.emi.ritual.strip.text2")));
        return List.of(figure, channel, strip);
    }
    private static ResourceLocation id(String page) { return ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "ritual/" + page); }
    /** The anchor blocks as one cycling ingredient of {@code count}; the tag itself when EMI knows it, else what it resolves to. */
    private static EmiIngredient anchors(int count) {
        var items = Gear.itemsOfBlockTag(ANCHORS, count);
        return items.isEmpty() ? EmiStack.of(Items.AMETHYST_BLOCK, count) : EmiIngredient.of(items, count);
    }
}
