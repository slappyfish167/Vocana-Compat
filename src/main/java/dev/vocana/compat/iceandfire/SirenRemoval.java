package dev.vocana.compat.iceandfire;

import dev.vocana.compat.VocanaCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Keeps sirens out of the world in code, behind the data that already does most of it. Ice and Fire (branch
 * {@code 1.21.1}) brings sirens two ways: {@code world/siren/SirenSpawnBiomeModifier} adds a beach spawn in the ADD
 * phase (only with its {@code siren.beachSpawns} on), which our {@code neoforge:remove_spawns} modifier takes away
 * again in the REMOVE phase; and {@code world/structure/SirenIslandStructure}'s piece builds one to three
 * {@code SirenEntity} by hand in {@code postProcess} and calls {@code addFreshEntity} without {@code finalizeSpawn},
 * so no spawn event sees them. Hence two hooks: {@code FinalizeSpawnEvent} refuses the world's own spawns (natural,
 * chunk generation, structure, patrol, reinforcement) and marks the deliberate ones (spawn egg, command, spawner,
 * dispenser, bucket, summoning) with a scoreboard tag; {@code EntityJoinLevelEvent} then turns away every fresh siren
 * that carries no such mark, which catches the island's. Sirens loaded from disk are left alone, so a world that had
 * them keeps them until they die. Sirens are told apart by entity id, never by class.
 */
public final class SirenRemoval {
    private SirenRemoval() {}
    public static final ResourceLocation SIREN = ResourceLocation.fromNamespaceAndPath("iceandfire", "siren");
    /** The scoreboard tag a siren someone asked for carries, so the join hook lets it through. */
    public static final String ALLOWED = "vocanacompat.siren_allowed";
    /** The spawn reasons that are the world's own doing. Everything else is someone asking for a siren. */
    static final Set<MobSpawnType> UNASKED = EnumSet.of(MobSpawnType.NATURAL, MobSpawnType.CHUNK_GENERATION, MobSpawnType.STRUCTURE, MobSpawnType.PATROL, MobSpawnType.REINFORCEMENT);

    static boolean siren(Entity e) { return e != null && SIREN.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType())); }
    static boolean on() { return IceAndFireConfig.get(IceAndFireConfig.removeSirens); }

    /** NeoForge 21.1: {@code net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent}; cancelling the event alone would not block the spawn, {@code setSpawnCancelled} does. */
    public static void onFinalizeSpawn(FinalizeSpawnEvent e) {
        if (!on() || !siren(e.getEntity())) return;
        if (UNASKED.contains(e.getSpawnType())) { e.setSpawnCancelled(true); log("refused a siren spawn ({})", e.getSpawnType()); }
        else e.getEntity().addTag(ALLOWED);
    }

    public static void onJoin(EntityJoinLevelEvent e) {
        if (!on() || e.getLevel().isClientSide() || e.loadedFromDisk() || !siren(e.getEntity())) return;
        if (e.getEntity().getTags().contains(ALLOWED)) return;
        e.setCanceled(true);
        log("turned away a siren placed at {} (siren island or another mod)", e.getEntity().blockPosition());
    }

    private static void log(String message, Object arg) {
        if (IceAndFireConfig.get(IceAndFireConfig.verboseLogging)) VocanaCompat.LOG.info("iceandfire: " + message, arg); else VocanaCompat.LOG.debug("iceandfire: " + message, arg);
    }
}
