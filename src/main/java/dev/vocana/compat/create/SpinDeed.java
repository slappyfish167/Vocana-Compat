package dev.vocana.compat.create;

import dev.vocana.api.VocanaApi;
import dev.vocana.magic.Essence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * SPIN ({@code kirsau}), a Force Deed: sets what it finds turning. Creatures and dropped items are whirled aside (a
 * player is also made dizzy for a moment); a hand crank it finds is cranked by itself for a while, through
 * {@link Spinners}; a blaze burner is stoked by the essence the chant carries (FIRE kindles, LIGHTNING seethes); any
 * other kinetic block shows its turn. The core applies the Bearer's essences to the targets first and calls this
 * after ({@code Acts.extension}); the deed respects the Working's own harm rules.
 */
final class SpinDeed implements VocanaApi.DeedBehaviour {
    static final String GLOSS = "SPIN";

    @Override public boolean act(VocanaApi.DeedContext ctx) {
        var w = ctx.working();
        var level = w.level;
        boolean reached = false;
        double shove = CreateConfig.get(CreateConfig.spinShove) * ctx.intensity();
        for (Entity e : ctx.entities()) {
            if (e instanceof ItemEntity || e instanceof ExperienceOrb) { Kinetics.whirl(e, ctx.point(), shove * .6); reached = true; continue; }
            if (!(e instanceof LivingEntity l) || !w.harms(l)) continue;
            Kinetics.whirl(l, ctx.point(), shove);
            if (l instanceof Player) l.addEffect(new MobEffectInstance(MobEffects.CONFUSION, w.ticks(3), 0));
            reached = true;
        }
        boolean fire = ctx.essences().contains("FIRE"), lightning = ctx.essences().contains("LIGHTNING");
        for (BlockPos pos : ctx.blocks()) {
            var k = Kinetics.kinetic(level, pos);
            if (k == null) {
                // A burner is no kinetic block; it is fed by what the chant carries, never by SPIN alone.
                if ((fire || lightning) && CreateConfig.get(CreateConfig.spinStokesBurners) && Kinetics.isBurner(level, pos) && Kinetics.stoke(level, pos, lightning)) {
                    Essence.puff(w, Vec3.atCenterOf(pos).add(0, .4, 0), List.of(lightning ? "LIGHTNING" : "FIRE"), 12, .3);
                    if (CreateConfig.get(CreateConfig.verboseLogging)) CreateCompat.LOG.info("SPIN stoked the blaze burner at {} ({})", pos, lightning ? "seething" : "kindled");
                    reached = true;
                }
                continue;
            }
            if (CreateConfig.get(CreateConfig.spinCranks) && Kinetics.isCrank(level, pos)) {
                // The caster faces the crank; a clockwise turn for them is a plain turn for the crank. Inverted SPIN is
                // written only, so there is no "backwards" to read; sneaking players turn cranks back by hand instead.
                if (Spinners.start(level, pos, false, w.ticks(CreateConfig.get(CreateConfig.crankSeconds)))) reached = true;
            }
            if (CreateConfig.get(CreateConfig.spinIndicators)) Kinetics.indicate(level, pos, k);
            level.sendParticles(ParticleTypes.CRIT, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 6, .3, .3, .3, .05);
        }
        if (reached) Essence.sound(w, ctx.point(), KineticEssence.GLOSS, .7f);
        return reached;
    }
}
