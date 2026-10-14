package dev.vocana.compat.wyrmroost;

import dev.vocana.api.VocanaApi;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.magic.Essence;
import dev.vocana.magic.Permits;
import dev.vocana.magic.Working;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * The Essence WYRM: the dragon-kind as a word. Found, it is any Wyrmroost dragon (and an egg), so {@code WYRM NEAREST},
 * {@code WYRM HOSTILE} and {@code SELF WARD WYRM} work like any essence head. Made, it is a dragon's weight: a heavy
 * strike that throws a creature back and slows it, or, reaching one of the caster's own dragons, a mending touch.
 * Laid as a state (IMBUE) on the caster or an ally it is scaled hide: resistance to blows and to fire; on a foe it is
 * the burden of scales. Imbued gear strikes with the same weight.
 */
final class WyrmEssence implements VocanaApi.EssenceBehaviour {
    /** The module's own list of dragon kinds (data: {@code data/vocanacompat/tags/entity_type/wyrmroost/wyrm.json}). */
    static final TagKey<EntityType<?>> KIN = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "wyrmroost/wyrm"));
    private static final ParticleOptions SCALE = new DustParticleOptions(new Vector3f(.80f, .42f, .14f), 1.3f);

    private static boolean on() { return WyrmroostConfig.get(WyrmroostConfig.wyrmWord); }

    @Override public boolean bears(Entity e) {
        return on() && (Dragons.isDragon(e) || e.getType().is(KIN));
    }

    @Override public boolean touch(Working w, LivingEntity target, double intensity, Vec3 from) {
        if (!on()) return false;
        var caster = w.caster();
        if (Dragons.isDragon(target) && Taming.sameSide(target, caster)) {
            // One's own dragon is never struck by the word that names it: it is mended instead (or left alone).
            if (!WyrmroostConfig.get(WyrmroostConfig.wyrmMendsOwnDragons) || !Permits.help(target)) return false;
            target.heal((float) (WyrmroostConfig.get(WyrmroostConfig.wyrmMendAmount) * intensity));
            Essence.puff(w.level, target.getBoundingBox().getCenter(), List.of("LIFE"), 10, .5);
            return true;
        }
        if (Taming.strangers(target, caster)) return false;   // a stranger's tamed dragon is not ours to strike
        float f = (float) intensity;
        w.hurt(target, (float) (WyrmroostConfig.get(WyrmroostConfig.wyrmTouchDamage) * f));
        knock(target, from, WyrmroostConfig.get(WyrmroostConfig.wyrmTouchKnockback) * intensity);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (40 * Math.max(.5, intensity)), 0));
        return true;
    }

    @Override public boolean imbue(Working w, LivingEntity target, double intensity, int ticks) {
        if (!on()) return false;
        int amp = Math.max(0, Math.min(2, (int) Math.round(intensity) - 1));
        if (target == w.caster() || Taming.sameSide(target, w.caster())) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, amp));
            target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks, 0));
            return true;
        }
        if (Taming.strangers(target, w.caster())) return false;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1 + amp));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0));
        return true;
    }

    @Override public void gear(LivingEntity source, LivingEntity target, float strength) {
        if (!on() || target == null || !target.isAlive()) return;
        if (source instanceof net.minecraft.server.level.ServerPlayer p && Taming.sameSide(target, p)) return;
        knock(target, source.position(), .5 * strength);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (30 * Math.max(.5, strength)), 0));
    }

    /** Throws a creature away from a point, as Vocana's own essences do. */
    static void knock(LivingEntity l, Vec3 from, double strength) {
        Vec3 d = l.position().subtract(from).multiply(1, 0, 1);
        if (d.lengthSqr() < 1e-6 || strength <= 0) return;
        l.knockback(strength, -d.x, -d.z);
        l.hurtMarked = true;
    }

    @Override public ParticleOptions particle() { return SCALE; }
    @Override public SoundEvent sound() { return SoundEvents.ENDER_DRAGON_FLAP; }
    @Override public int colour() { return 0xcc6b24; }
}
