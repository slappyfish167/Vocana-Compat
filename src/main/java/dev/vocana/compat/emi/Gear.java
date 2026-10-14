package dev.vocana.compat.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.vocana.Imbue;
import dev.vocana.Imbues;
import dev.vocana.Vocana;
import dev.vocana.bench.ImbueRules;
import dev.vocana.server.Imbuements;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Stacks for the EMI pages: the gear a recipe's target stands for (one piece of each kind, as the bench accepts them:
 * {@code dev.vocana.server.Imbuements.kind}), a preview of a piece once imbued (the {@code vocana:imbues} component set
 * on a copy, so Vocana's own tooltip reads it), and tag members as items. Everything here is client-side display; no
 * stack ever enters a world. Registry reads happen on EMI's reload thread, after tags are synced, which is how EMI's
 * own plugins work.
 */
final class Gear {
    private Gear() {}
    /** At most this many members of a tag are shown, so a wide tag does not become a slide show. */
    static final int TAG_LIMIT = 12;

    /** The pieces a kind of gear is shown as; null (any) gives one row of each kind. */
    static List<ItemStack> ofKind(ImbueRules.Kind kind) {
        List<ItemStack> out = new ArrayList<>();
        if (kind == null) { for (var k : ImbueRules.Kind.values()) out.addAll(ofKind(k)); return out; }
        switch (kind) {
            case WEAPON -> { for (Item i : List.of(Items.IRON_SWORD, Items.IRON_AXE, Items.TRIDENT, Items.MACE, Items.BOW, Items.CROSSBOW)) out.add(new ItemStack(i)); }
            case ARMOR -> { for (Item i : List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) out.add(new ItemStack(i)); }
            case TOOL -> { for (Item i : List.of(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.SHEARS, Items.FISHING_ROD)) out.add(new ItemStack(i)); }
            case STAFF -> { for (var staff : Vocana.STAFFS.values()) out.add(new ItemStack(staff.get())); }
        }
        return out;
    }
    /** What a recipe's target stands for: the item it names, else the members of its tag, else its kind's pieces. */
    static List<ItemStack> representatives(ImbueRules.Kind kind, String itemId, String tagId) {
        if (itemId != null) { Item i = item(itemId); if (i != null) return List.of(new ItemStack(i)); }
        if (tagId != null) {
            List<ItemStack> out = new ArrayList<>();
            for (Item i : itemsOfTag(tagId)) {
                if (out.size() >= TAG_LIMIT) break;
                var stack = new ItemStack(i);
                if (kind == null || Imbuements.kind(stack) == kind) out.add(stack);
            }
            if (!out.isEmpty()) return out;
        }
        return ofKind(kind);
    }
    /** A copy of the piece with one imbue laid in, as the bench would leave it. */
    static ItemStack imbued(ItemStack base, Imbue imbue) {
        var s = base.copy();
        Imbues.set(s, new Imbues(List.of(imbue)));
        return s;
    }
    /** An item by id, or null. */
    static Item item(String id) {
        var key = ResourceLocation.tryParse(id);
        return key == null ? null : BuiltInRegistries.ITEM.getOptional(key).orElse(null);
    }
    /** The items of an item tag, in tag order; empty for an unknown or empty tag. */
    static List<Item> itemsOfTag(String tagId) {
        List<Item> out = new ArrayList<>();
        var key = ResourceLocation.tryParse(tagId);
        if (key == null) return out;
        var tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, key));
        if (tag.isPresent()) for (Holder<Item> h : tag.get()) out.add(h.value());
        return out;
    }
    /** The items of a block tag's blocks (those that have one), for catalysts that are placed blocks. */
    static List<EmiStack> itemsOfBlockTag(String tagId, int count) {
        List<EmiStack> out = new ArrayList<>();
        var key = ResourceLocation.tryParse(tagId);
        if (key == null) return out;
        var tag = BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, key));
        if (tag.isPresent()) for (Holder<Block> h : tag.get()) {
            Item i = h.value().asItem();
            if (i != Items.AIR && out.size() < TAG_LIMIT) out.add(EmiStack.of(i, count));
        }
        return out;
    }
    /** A block by id as an item stack of {@code count}, or empty. */
    static EmiStack blockItem(String blockId, int count) {
        var key = ResourceLocation.tryParse(blockId);
        var block = key == null ? null : BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
        return block == null || block.asItem() == Items.AIR ? EmiStack.EMPTY : EmiStack.of(block.asItem(), count);
    }
    /** An item id as a stack, or the fallback for an unknown id (a Grimoire group's icon, for one). */
    static EmiStack icon(String itemId, Item fallback) {
        Item i = itemId == null ? null : item(itemId);
        return EmiStack.of(i == null ? fallback : i);
    }
}
