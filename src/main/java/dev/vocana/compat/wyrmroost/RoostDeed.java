package dev.vocana.compat.wyrmroost;

import dev.vocana.api.VocanaApi;
import dev.vocana.magic.Essence;
import dev.vocana.magic.Permits;
import dev.vocana.magic.States;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;

import java.util.ArrayList;
import java.util.List;

/**
 * The Deed ROOST (State frame): "settle". On a dragon of the caster's own it sits the dragon down where it is (a strong
 * clause also lays it to sleep); on one already at rest it rouses it, so {@code WYRM ROOST} toggles and a roused dragon
 * answers FOLLOW and SUMMON again. A wild dragon the Working may control hesitates for a while: it forgets its target,
 * slowed and weakened, as Vocana's COMMAND does to a will it cannot bend. A stranger's tamed dragon is never touched.
 * The caster's vanilla pets sit and stand the same way (config). Anything else is a harmless no-op.
 */
final class RoostDeed implements VocanaApi.DeedBehaviour {
    @Override public boolean act(VocanaApi.DeedContext ctx) {
        if (!WyrmroostConfig.get(WyrmroostConfig.roostWord)) return false;
        var w = ctx.working();
        var caster = ctx.caster();
        List<Entity> touched = new ArrayList<>();
        for (Entity e : List.copyOf(ctx.entities())) {
            if (!(e instanceof LivingEntity l) || !l.isAlive()) continue;
            RoostRules.Outcome out;
            if (Dragons.isDragon(l) && l instanceof TamableAnimal t) {
                out = RoostRules.forDragon(t.isTame(), Taming.sameSide(t, caster), Dragons.settled(l), w.harms(l) && Permits.control(l));
            } else if (l instanceof TamableAnimal t && WyrmroostConfig.get(WyrmroostConfig.roostVanillaPets)) {
                out = RoostRules.forPet(t.isTame(), Taming.sameSide(t, caster), t.isOrderedToSit() || t.isInSittingPose());
            } else continue;
            switch (out) {
                case SETTLE -> {
                    if (Dragons.isDragon(l)) Dragons.settle(l, RoostRules.sleeps(ctx.intensity(), WyrmroostConfig.get(WyrmroostConfig.sleepAtIntensity)));
                    else if (l instanceof TamableAnimal t) { t.setTarget(null); t.getNavigation().stop(); t.setOrderedToSit(true); }
                    touched.add(l);
                }
                case ROUSE -> {
                    if (Dragons.isDragon(l)) Dragons.rouse(l);
                    else if (l instanceof TamableAnimal t) t.setOrderedToSit(false);
                    touched.add(l);
                }
                case HESITATE -> {
                    if (l instanceof Mob m) {
                        int ticks = RoostRules.hesitateTicks(WyrmroostConfig.get(WyrmroostConfig.hesitateSeconds), ctx.intensity());
                        if (ticks <= 0) continue;
                        m.setTarget(null);
                        m.getNavigation().stop();
                        // The same hold Vocana's COMMAND lays on a mob it cannot turn: it loses its target while the state lasts.
                        States.apply(m, States.Kind.BLINDED, ticks, w.owner, 1);
                        m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1));
                        m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0));
                        touched.add(l);
                    }
                }
                default -> {}
            }
        }
        if (touched.isEmpty()) return false;
        for (Entity e : touched) {
            var at = e.getBoundingBox().getCenter();
            Essence.puff(w.level, at, ctx.essences().isEmpty() ? List.of("WIND") : ctx.essences(), 12, Math.max(.4, e.getBbWidth() * .6));
        }
        var at = touched.getFirst().position();
        w.level.playSound(null, at.x, at.y, at.z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, .6f, 1.3f);
        if (WyrmroostConfig.get(WyrmroostConfig.verboseLogging)) dev.vocana.compat.VocanaCompat.LOG.info("wyrmroost: ROOST touched {} creature(s)", touched.size());
        return true;
    }
}
