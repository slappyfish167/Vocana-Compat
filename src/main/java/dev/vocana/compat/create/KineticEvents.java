package dev.vocana.compat.create;

import dev.vocana.Imbue;
import dev.vocana.Imbues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The module's game-bus listeners: a KINETIC-imbued tool digs faster among turning machinery, and the server tick
 * drives {@link Spinners}. The dig speed is computed on both sides (the client predicts breaking), from the tool's
 * {@code vocana:imbues} component and a per-player scan of the loaded chunks around them, repeated every
 * {@code kinetic.toolHasteRescanTicks}. The class and its listeners are public because the bus registers a class's
 * static listeners through {@code getMethods()}, which sees public methods only.
 */
public final class KineticEvents {
    private KineticEvents() {}
    /** Per player: the tick of the last scan and whether machinery turned then. */
    private static final Map<UUID, long[]> NEARBY = new ConcurrentHashMap<>();

    /** Whether a stack carries KINETIC in any of its imbues (the base of an Elder shade counts, should one ever exist). */
    static boolean imbuedKinetic(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (Imbue i : Imbues.view(stack).list()) if (!i.chant() && KineticEssence.GLOSS.equals(i.base())) return true;
        return false;
    }

    static boolean machineryNear(Player p) {
        long now = p.level().getGameTime();
        long[] c = NEARBY.get(p.getUUID());
        if (c != null && now - c[0] < CreateConfig.get(CreateConfig.toolHasteRescanTicks)) return c[1] != 0;
        boolean near = Kinetics.machineryTurning(p.level(), p.blockPosition(), CreateConfig.get(CreateConfig.toolHasteRadius));
        NEARBY.put(p.getUUID(), new long[]{now, near ? 1 : 0});
        return near;
    }

    @SubscribeEvent public static void breakSpeed(PlayerEvent.BreakSpeed e) {
        if (!CreateConfig.get(CreateConfig.toolHaste)) return;
        var p = e.getEntity();
        if (!imbuedKinetic(p.getMainHandItem()) || !machineryNear(p)) return;
        e.setNewSpeed((float) (e.getOriginalSpeed() * CreateConfig.get(CreateConfig.toolHasteFactor)));
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e) { Spinners.tick(e.getServer()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e) { Spinners.clear(); NEARBY.clear(); }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e) { NEARBY.remove(e.getEntity().getUUID()); }
}
