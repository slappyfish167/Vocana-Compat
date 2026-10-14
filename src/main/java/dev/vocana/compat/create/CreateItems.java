package dev.vocana.compat.create;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items the module adds, registered only when it loads (a missing item in a recipe is fine: the recipe is behind the
 * module's conditions). The one item is the half-made Torc of the sequenced-assembly recipe: Create's
 * {@code SequencedAssemblyItem} (a plain {@code Item} subclass) draws the assembly's progress bar and names the step.
 * Its id keeps the module's folder, {@code vocanacompat:create/incomplete_torc}, so its model and lang key sit with
 * the module's other assets.
 */
final class CreateItems {
    private CreateItems() {}
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VocanaCompat.ID);
    static final DeferredItem<Item> INCOMPLETE_TORC = ITEMS.register("create/incomplete_torc", () -> new SequencedAssemblyItem(new Item.Properties()));
    static void register(IEventBus modBus) { ITEMS.register(modBus); }
}
