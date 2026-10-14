package dev.vocana.compat.emi;

import com.google.gson.JsonArray;
import dev.vocana.bench.ImbueRecipe;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.server.ImbueRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.Collection;
import java.util.List;

/**
 * Hands the Imbue Bench's recipes to clients. Vocana loads them through a server-side reload listener
 * ({@link ImbueRecipes}) and shows the bench what it needs on request, so a client on a dedicated server never holds the
 * list EMI wants to display; in single player the integrated server fills the same static map and the client reads it
 * directly. This payload closes the gap when this module runs on the server too: on every {@link OnDatapackSyncEvent}
 * (a player joining, or {@code /reload}) the recipes go out as the JSON {@link ImbueJson} writes, which NeoForge sends
 * before the vanilla recipe packet that makes EMI reload, so EMI sees them on its first pass. The payload is optional:
 * a client without the module, or a server without it, is simply not sent to.
 */
public final class ImbueRecipeSync {
    private ImbueRecipeSync() {}
    /** The longest string one payload carries, under the 1 MiB custom-payload limit; a pack past it is sent truncated, with a warning. */
    static final int MAX_CHARS = 900_000;

    /** The recipes, as JSON written by {@link ImbueJson#writeAll}. */
    public record Payload(String json) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "imbue_recipes"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of((buf, p) -> buf.writeUtf(p.json, MAX_CHARS), buf -> new Payload(buf.readUtf(MAX_CHARS)));
        @Override public Type<Payload> type() { return TYPE; }
    }

    /** What the server last sent this client, or null when nothing came (single player reads the server's map instead). */
    private static volatile List<ImbueRecipe> synced;

    static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("1").optional().playToClient(Payload.TYPE, Payload.CODEC, (p, ctx) -> receive(p.json()));
    }

    /** Server side: sends the loaded recipes to the players the event names, when the config allows and the client listens. */
    static void datapackSync(OnDatapackSyncEvent e) {
        if (!EmiConfig.get(EmiConfig.syncRecipes)) return;
        String json = encode(ImbueRecipes.all());
        e.getRelevantPlayers().forEach(p -> send(p, json));
    }
    static void send(ServerPlayer p, String json) {
        if (p.connection == null || !NetworkRegistry.hasChannel(p.connection, Payload.TYPE.id())) return;
        PacketDistributor.sendToPlayer(p, new Payload(json));
    }
    /** The recipes as one JSON array, cut to the payload limit at a recipe boundary when a pack is enormous. */
    static String encode(Collection<ImbueRecipe> recipes) {
        var all = ImbueJson.writeAll(recipes);
        String json = all.toString();
        if (json.length() <= MAX_CHARS) return json;
        var kept = new JsonArray();
        int length = 2;
        for (var e : all) { String s = e.toString(); if (length + s.length() + 1 > MAX_CHARS) break; kept.add(e); length += s.length() + 1; }
        VocanaCompat.LOG.warn("emi module: {} imbue recipes do not fit one payload; sending the first {}", all.size(), kept.size());
        return kept.toString();
    }

    /** Client side: keeps what the server sent. */
    static void receive(String json) {
        var list = List.copyOf(ImbueJson.readAll(json));
        synced = list;
        if (EmiConfig.get(EmiConfig.verboseLogging)) VocanaCompat.LOG.info("emi module: received {} imbue recipes from the server", list.size());
        else VocanaCompat.LOG.debug("emi module: received {} imbue recipes from the server", list.size());
    }
    /** Client side: forgets the last server's recipes (on logout), so another server's list is never shown in their place. */
    static void clear() { synced = null; }
    /** The recipes the server sent this client, or null when none came. */
    public static List<ImbueRecipe> synced() { return synced; }
}
