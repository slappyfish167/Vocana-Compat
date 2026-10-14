package dev.vocana.compat.wyrmroost;

import com.github.wolfshotz.wyrmroost.entities.dragon.AbstractDragonEntity;
import com.github.wolfshotz.wyrmroost.entities.dragon.RoyalRedEntity;
import net.minecraft.world.entity.Entity;

/**
 * The one class of this module that names Wyrmroost's own types. Everything else talks to dragons through vanilla
 * {@code TamableAnimal} (which every Wyrmroost dragon extends) and through entity-type tags, so a change in Wyrmroost's
 * internals touches this file only. Every member below was read from {@code NordAct/Wyrmroost}, branch {@code main}:
 * <ul>
 *   <li>{@code entities/dragon/AbstractDragonEntity.java}: {@code public abstract class AbstractDragonEntity extends
 *       TamableAnimal}; {@code public void setSit(boolean)} (calls {@code setOrderedToSit} and {@code setInSittingPose}
 *       and stops the dragon in place), {@code public boolean isSleeping()}, {@code public void setSleeping(boolean)},
 *       {@code public abstract boolean maySleep()}, {@code public boolean isFlying()}.</li>
 *   <li>{@code entities/dragon/RoyalRedEntity.java}: {@code public boolean isBreathingFire()}.</li>
 * </ul>
 * The Coin Dragon ({@code CoinDragonEntity extends Mob}) and the Lesser Desertwyrm ({@code LDWyrmEntity extends
 * PathfinderMob}) are not {@code AbstractDragonEntity}s: they count as kin through the {@code vocanacompat:wyrmroost/wyrm}
 * tag but cannot be tamed, settled or roused.
 */
final class Dragons {
    private Dragons() {}

    /**
     * Touches the Wyrmroost classes this module compiles against. Called from {@code init} so that a jar whose classes
     * moved fails there, where {@code VocanaCompat} catches the {@code LinkageError} and drops the module, instead of
     * inside a Working.
     */
    static String probe() {
        return AbstractDragonEntity.class.getName() + ", " + RoyalRedEntity.class.getName();
    }

    /** A Wyrmroost dragon proper: tameable, able to sit, perhaps to fly and sleep. */
    static boolean isDragon(Entity e) { return e instanceof AbstractDragonEntity; }

    /** Whether a dragon is at rest: sitting (the staff's "sit") or asleep. */
    static boolean settled(Entity e) {
        return e instanceof AbstractDragonEntity d && (d.isInSittingPose() || d.isSleeping());
    }

    /** Whether a dragon is in the air. */
    static boolean flying(Entity e) { return e instanceof AbstractDragonEntity d && d.isFlying(); }

    /** Sits a dragon down where it is (forgetting its target), and lays it to sleep when asked and its kind sleeps. */
    static void settle(Entity e, boolean sleep) {
        if (!(e instanceof AbstractDragonEntity d)) return;
        d.setTarget(null);
        d.getNavigation().stop();
        d.setSit(true);
        if (sleep && d.maySleep()) d.setSleeping(true);
    }

    /** Wakes and stands a dragon so Vocana's FOLLOW and SUMMON can move it. */
    static void rouse(Entity e) {
        if (!(e instanceof AbstractDragonEntity d)) return;
        d.setSleeping(false);
        d.setSit(false);
    }

    /** A Royal Red in the act of breathing fire: it bears DRAGONFIRE while it does. */
    static boolean breathingFire(Entity e) { return e instanceof RoyalRedEntity r && r.isBreathingFire(); }
}
