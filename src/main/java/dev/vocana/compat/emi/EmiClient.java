package dev.vocana.compat.emi;

import dev.vocana.bench.ImbueRecipe;
import dev.vocana.server.ImbueRecipes;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/**
 * The module's client half, reached only from {@link EmiCompat#init} on the client. It forgets the synced recipes on
 * logout and decides where the EMI plugin reads the Imbue Bench's recipes from: what the server sent
 * ({@link ImbueRecipeSync}) when anything came, else the integrated server's own map in single player (the same JVM
 * runs the reload listener), else nothing: a dedicated server without this module keeps its recipes to itself, and
 * Vocana's static map would otherwise still hold the last single-player world's list.
 */
final class EmiClient {
    private EmiClient() {}
    static void init() {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> ImbueRecipeSync.clear());
    }
    /** The imbue recipes this client may show, in id order. */
    static List<ImbueRecipe> recipes() {
        var synced = ImbueRecipeSync.synced();
        if (synced != null) return synced;
        return Minecraft.getInstance().hasSingleplayerServer() ? List.copyOf(ImbueRecipes.all()) : List.of();
    }
}
