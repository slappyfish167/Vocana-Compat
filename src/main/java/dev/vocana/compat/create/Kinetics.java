package dev.vocana.compat.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/**
 * The module's one window on Create's kinetics: every reference to a Create class that touches the world sits here, so
 * the essence, the deed and the events read as Vocana code. Everything is read-mostly; the two writes (turning a hand
 * crank, feeding a blaze burner) go through Create's own public entry points, never its fields.
 */
final class Kinetics {
    private Kinetics() {}

    /** The kinetic block entity at a position, or null. Every block Create turns has one ({@code KineticBlockEntity}). */
    static KineticBlockEntity kinetic(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof KineticBlockEntity k ? k : null;
    }
    /** Whether a kinetic block is actually turning: {@code getSpeed()} is 0 when idle, overstressed or with the tick rate frozen. */
    static boolean turning(KineticBlockEntity k) { return k != null && k.getSpeed() != 0; }

    /**
     * Whether any kinetic block within {@code radius} of a position is turning. Walks the block entities of the loaded
     * chunks the cube touches (a handful of map lookups) rather than every block in it; unloaded chunks are not loaded.
     */
    static boolean machineryTurning(Level level, BlockPos center, int radius) {
        int r2 = radius * radius;
        for (int cx = SectionPos.blockToSectionCoord(center.getX() - radius); cx <= SectionPos.blockToSectionCoord(center.getX() + radius); cx++)
            for (int cz = SectionPos.blockToSectionCoord(center.getZ() - radius); cz <= SectionPos.blockToSectionCoord(center.getZ() + radius); cz++) {
                if (!(level.getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk)) continue;
                for (var be : chunk.getBlockEntities().values())
                    if (be instanceof KineticBlockEntity k && k.getBlockPos().distSqr(center) <= r2 && turning(k)) return true;
            }
        return false;
    }

    /**
     * Draws Create's own rotation indicator (the ring of coloured motes goggles show) on a kinetic block, as
     * {@code KineticEffectHandler.spawnRotationIndicators} does: colour and mote speed from {@code IRotate.SpeedLevel}
     * of the block's speed, radii from the {@code KineticBlock}, ten ticks of life, twenty motes. An idle block shows a
     * slow grey ghost of a turn so the word is seen to land.
     */
    static void indicate(ServerLevel level, BlockPos pos, KineticBlockEntity k) {
        var state = k.getBlockState();
        if (!(state.getBlock() instanceof KineticBlock kb)) return;
        Direction.Axis axis = kb.getRotationAxis(state);
        if (axis == null) return;
        float speed = k.getSpeed();
        var speedLevel = IRotate.SpeedLevel.of(speed);
        int colour = speed == 0 ? 0x8a8a8a : speedLevel.getColor();
        float moteSpeed = speed == 0 ? 6 : speedLevel.getParticleSpeed() * Math.signum(speed);
        var data = new RotationIndicatorParticleData(colour, moteSpeed, kb.getParticleInitialRadius(), kb.getParticleTargetRadius(), 10, axis);
        Vec3 c = Vec3.atCenterOf(pos);
        level.sendParticles(data, c.x, c.y, c.z, 20, 0, 0, 0, 1);
    }

    /**
     * Turns a hand crank (a valve handle is one too) as a player's hand would: {@code HandCrankBlockEntity.turn(back)}
     * sets {@code inUse = 10} ticks and starts its generated rotation. Called again before those ticks run out it keeps
     * turning. Returns whether a crank stood there.
     */
    static boolean crank(ServerLevel level, BlockPos pos, boolean back) {
        if (!(level.getBlockEntity(pos) instanceof HandCrankBlockEntity c)) return false;
        c.turn(back);
        return true;
    }
    static boolean isCrank(Level level, BlockPos pos) { return level.getBlockEntity(pos) instanceof HandCrankBlockEntity; }

    /** Whether a blaze burner stands here (the empty burner is the same block at heat NONE; the flint-lit one is another block and is left alone). */
    static boolean isBurner(Level level, BlockPos pos) { return level.getBlockState(pos).getBlock() instanceof BlazeBurnerBlock; }

    /**
     * Feeds a blaze burner without spending an item, through {@code BlazeBurnerBlock.tryInsert(state, level, pos, stack,
     * doNotConsume, forceOverflow, simulate)}: a blaze cake (Create's {@code superheated_blaze_burner_fuels} data map,
     * 3200 ticks) makes it seethe, coal (a furnace fuel, 1600 ticks) kindles it. Create's own rules apply: a burner
     * already seething past its insertion threshold refuses, and normal fuel never replaces special fuel. Returns
     * whether the burner took the fuel.
     */
    static boolean stoke(ServerLevel level, BlockPos pos, boolean seethe) {
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlazeBurnerBlock)) return false;
        ItemStack fuel = new ItemStack(seethe ? AllItems.BLAZE_CAKE.get() : Items.COAL);
        return BlazeBurnerBlock.tryInsert(state, level, pos, fuel, true, false, false).getResult().consumesAction();
    }

    /** Whether an entity is one of Create's moving contraptions (bearing, piston, pulley, gantry, cart or train). */
    static boolean isContraption(Entity e) { return e instanceof AbstractContraptionEntity; }

    /**
     * Whirls an entity: a shove at right angles to the line from {@code from}, a little away from it and a little up,
     * so the target is spun aside rather than thrown back. Mobs are turned on the spot too; a player's own facing is
     * theirs to keep. {@code hurtMarked} sends the new motion to the client.
     */
    static void whirl(Entity e, Vec3 from, double strength) {
        if (strength <= 0 || e == null || !e.isAlive()) return;
        Vec3 d = e.position().subtract(from).multiply(1, 0, 1);
        if (d.lengthSqr() < 1e-6) d = new Vec3(1, 0, 0);
        d = d.normalize();
        Vec3 tangent = new Vec3(-d.z, 0, d.x);
        double s = Math.min(3, strength);
        e.setDeltaMovement(e.getDeltaMovement().add(tangent.scale(s)).add(d.scale(s * .4)).add(0, .15 * Math.min(1, s), 0));
        e.hurtMarked = true;
        if (e instanceof LivingEntity l && !(e instanceof net.minecraft.world.entity.player.Player)) {
            float yaw = l.getYRot() + (float) (120 * s);
            l.setYRot(yaw); l.setYBodyRot(yaw); l.setYHeadRot(yaw);
        }
    }
}
