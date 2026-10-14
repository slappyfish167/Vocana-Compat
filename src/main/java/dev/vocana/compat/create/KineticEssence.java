package dev.vocana.compat.create;

import dev.vocana.api.VocanaApi;
import dev.vocana.magic.Essence;
import dev.vocana.magic.Working;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * KINETIC ({@code krenshu}), the essence of turning things, as the world meets it. Its natural form is a bolt
 * ({@code Essence.natural} gives unknown essences an area; the Working's own Form still shapes it): on contact a
 * creature is whirled sideways, hurt a little and left reeling with Slowness; laid on by IMBUE it winds a creature down
 * (Slowness and Mining Fatigue). On a block it can only show nearby machines turning, because {@code Essence.transmute}
 * never hands a block entity to an extension and every kinetic block has one; the hard work on machines belongs to
 * SPIN. Imbued into a weapon it whirls what is struck; its tool imbue is handled by {@link KineticEvents}. Contraption
 * entities bear it. The core checks harm permissions before any of these are called.
 */
final class KineticEssence implements VocanaApi.EssenceBehaviour {
    static final String GLOSS = "KINETIC";

    @Override public boolean touch(Working w, LivingEntity target, double intensity, Vec3 from) {
        Kinetics.whirl(target, from, CreateConfig.get(CreateConfig.touchShove) * intensity);
        float damage = (float) (CreateConfig.get(CreateConfig.touchDamage) * intensity);
        if (damage > 0) w.hurt(target, damage);
        int reel = (int) (CreateConfig.get(CreateConfig.touchSlownessTicks) * Math.min(2, intensity));
        if (reel > 0) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, reel, 0));
        return true;
    }

    @Override public boolean imbue(Working w, LivingEntity target, double intensity, int ticks) {
        int amp = Math.max(0, Math.min(3, CreateConfig.get(CreateConfig.imbueSlownessAmplifier) + (int) Math.round(intensity) - 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, amp));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 0));
        Essence.puff(w, target.getBoundingBox().getCenter(), List.of(GLOSS), 14, .35);
        return true;
    }

    /** Only plain blocks reach here; the answer is the machines around them showing their turn. */
    @Override public boolean block(Working w, BlockPos pos, int ticks) {
        if (!CreateConfig.get(CreateConfig.spinIndicators)) return false;
        boolean any = false;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            var k = Kinetics.kinetic(w.level, p);
            if (k == null) continue;
            Kinetics.indicate(w.level, p.immutable(), k);
            any = true;
        }
        return any;
    }

    @Override public void gear(LivingEntity source, LivingEntity target, float strength) {
        Kinetics.whirl(target, source.position(), CreateConfig.get(CreateConfig.gearShove) * strength);
    }

    @Override public boolean bears(Entity e) { return Kinetics.isContraption(e); }
    @Override public ParticleOptions particle() { return ParticleTypes.CRIT; }
    @Override public SoundEvent sound() { return SoundEvents.GRINDSTONE_USE; }
    /** Brass and oil: the colour of a running machine. */
    @Override public int colour() { return 0xd0a75a; }
}
