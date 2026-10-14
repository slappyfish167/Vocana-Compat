package dev.vocana.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hand cranks that magic keeps turning. {@code HandCrankBlockEntity.turn} gives ten ticks of rotation, so each spun
 * crank is turned again every {@value #RETURN} ticks until its time is up; a crank that is gone, unloaded or no longer
 * a crank is forgotten. The list lives for the server's run only (a restart stills every crank, as it stills every
 * Working), is capped by {@code spin.maxCranks}, and is ticked from {@link KineticEvents}.
 */
final class Spinners {
    private Spinners() {}
    private static final int RETURN = 8;
    private static final class Spin { final ResourceKey<Level> dim; final BlockPos pos; final boolean back; long until; Spin(ResourceKey<Level> dim, BlockPos pos, boolean back, long until) { this.dim = dim; this.pos = pos; this.back = back; this.until = until; } }
    private static final List<Spin> SPINS = new ArrayList<>();

    /** Starts (or extends) a crank's turning for {@code ticks}; false when no crank stands there or the cap is reached. */
    static synchronized boolean start(ServerLevel level, BlockPos pos, boolean back, int ticks) {
        if (!Kinetics.crank(level, pos, back)) return false;
        long until = level.getGameTime() + Math.max(1, ticks);
        for (Spin s : SPINS) if (s.dim == level.dimension() && s.pos.equals(pos)) { s.until = Math.max(s.until, until); return true; }
        if (SPINS.size() >= CreateConfig.get(CreateConfig.maxCranks)) return false;
        SPINS.add(new Spin(level.dimension(), pos.immutable(), back, until));
        if (CreateConfig.get(CreateConfig.verboseLogging)) CreateCompat.LOG.info("SPIN turns the hand crank at {} for {} ticks", pos, ticks);
        return true;
    }

    static synchronized void tick(MinecraftServer server) {
        if (SPINS.isEmpty() || server.getTickCount() % RETURN != 0) return;
        for (Iterator<Spin> it = SPINS.iterator(); it.hasNext(); ) {
            Spin s = it.next();
            ServerLevel level = server.getLevel(s.dim);
            if (level == null || !level.hasChunkAt(s.pos) || level.getGameTime() >= s.until || !Kinetics.crank(level, s.pos, s.back)) it.remove();
        }
    }

    /** How many cranks magic is turning now (tests and the goggles of the curious). */
    static synchronized int turning() { return SPINS.size(); }
    static synchronized void clear() { SPINS.clear(); }
}
