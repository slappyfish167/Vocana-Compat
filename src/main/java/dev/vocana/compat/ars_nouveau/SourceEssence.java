package dev.vocana.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import dev.vocana.api.VocanaApi;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.magic.Working;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The essence SOURCE: Ars Nouveau's raw magic as a Vocana word. Touching a plain block pours source into the jars (any
 * {@code ISourceTile}) within reach of it, touching an Ars caster gives them mana, laid as a state it hastens mana
 * regeneration, and gear imbued with it returns mana to its holder on a strike. Every gift is paid for by the Thrum the
 * Working cost: a Working may give out at most its paid Thrum at the par rate less the conversion loss
 * ({@link Conversion#sourceBudget}), whatever its shape or how many blocks it reaches.
 *
 * <p>Vocana never lets a Working edit a block that has a block entity ({@code Permits.editable}), so the jar itself is
 * never the touched block: the caster aims IMBUE or BECOME at the floor beside the jar and the source flows into every
 * jar within {@code sourceReach}. Source is added through {@code ISourceTile.addSource(int)}, which
 * {@code AbstractSourceMachine} clamps to the jar's capacity and follows with a block update.
 */
final class SourceEssence implements VocanaApi.EssenceBehaviour {
    static final String GLOSS = "SOURCE";
    /** Creatures that bear source by kind (the module's entity tag, which also feeds {@code vocana:hears/source}). */
    static final TagKey<EntityType<?>> BEARERS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "ars_nouveau/source"));
    /** Source (or its mana equivalent) each Working has given so far, by Working id. Bounded: Workings are short-lived. */
    private final Map<UUID, Double> given = new LinkedHashMap<>(64, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, Double> eldest) { return size() > 512; }
    };

    private static double cfg(ModConfigSpec.DoubleValue v) { return ArsNouveauConfig.get(v); }
    private static int cfg(ModConfigSpec.IntValue v) { return ArsNouveauConfig.get(v); }

    private double remaining(Working w) {
        double budget = Conversion.sourceBudget(w.paid, cfg(ArsNouveauConfig.manaPerThrum), cfg(ArsNouveauConfig.sourcePerMana),
            cfg(ArsNouveauConfig.conversionLoss), cfg(ArsNouveauConfig.arsResourceFactor));
        return Math.max(0, budget - given.getOrDefault(w.id, 0d));
    }
    private void spent(Working w, double source) { given.merge(w.id, source, Double::sum); }

    @Override public boolean block(Working w, BlockPos pos, int ticks) {
        var level = w.level;
        int reach = cfg(ArsNouveauConfig.sourceReach);
        int left = Conversion.sourceForTouch(cfg(ArsNouveauConfig.sourcePerCast), w.power, remaining(w));
        if (left <= 0) return false;
        boolean any = false;
        // Nearest first, the way Ars's own SourceUtil.canGiveSource walks the neighbourhood; a full jar is passed over.
        for (BlockPos b : BlockPos.withinManhattan(pos, reach, reach, reach)) {
            if (left <= 0) break;
            if (!level.isLoaded(b) || !(level.getBlockEntity(b) instanceof ISourceTile tile) || !tile.canAcceptSource()) continue;
            int before = tile.getSource();
            tile.addSource(left);
            int added = tile.getSource() - before;
            if (added <= 0) continue;
            left -= added; any = true;
            spent(w, added);
            Vec3 at = Vec3.atCenterOf(b);
            level.sendParticles(ParticleTypes.ENCHANT, at.x, at.y + .3, at.z, 10 + added / 4, .25, .3, .25, .4);
            level.playSound(null, b, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, .6f, 1.2f);
        }
        return any;
    }

    @Override public boolean touch(Working w, LivingEntity target, double intensity, Vec3 from) {
        if (!(target instanceof Player)) return false;
        var mana = CapabilityRegistry.getMana(target);
        if (mana == null) return false;
        double sourcePerMana = cfg(ArsNouveauConfig.sourcePerMana);
        double want = Math.min(cfg(ArsNouveauConfig.touchMana) * Math.max(0, intensity), Conversion.manaForSource(remaining(w), sourcePerMana));
        want = Math.min(want, mana.getMaxMana() - mana.getCurrentMana());
        if (want < 1) return false;
        mana.addMana(want);
        spent(w, Conversion.sourceForMana(want, sourcePerMana));
        if (target instanceof ServerPlayer sp) mana.syncToClient(sp);
        w.level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1, target.getZ(), 16, .3, .5, .3, .5);
        return true;
    }

    /** Laid as a state, SOURCE is Ars's own mana-regeneration effect for the Working's duration; a stronger Working lays its second level. */
    @Override public boolean imbue(Working w, LivingEntity target, double intensity, int ticks) {
        if (CapabilityRegistry.getMana(target) == null) return false;
        return target.addEffect(new MobEffectInstance(ModPotions.MANA_REGEN_EFFECT, Math.max(20, ticks), intensity >= 2 ? 1 : 0));
    }

    /** Imbued gear: the striker (a weapon's wielder, or armour's wearer when struck) regains a little mana. */
    @Override public void gear(LivingEntity source, LivingEntity target, float strength) {
        if (source == null) return;
        var mana = CapabilityRegistry.getMana(source);
        if (mana == null) return;
        double give = cfg(ArsNouveauConfig.strikeMana) * Math.max(0, strength);
        if (give <= 0) return;
        mana.addMana(give);
        if (source instanceof ServerPlayer sp) mana.syncToClient(sp);
    }

    /** Source is borne by Ars's magical creatures and by anyone whose mana is not empty. */
    @Override public boolean bears(Entity e) {
        if (e.getType().is(BEARERS)) return true;
        if (!(e instanceof LivingEntity l)) return false;
        var mana = CapabilityRegistry.getMana(l);
        return mana != null && mana.getCurrentMana() > 0;
    }

    @Override public ParticleOptions particle() { return ParticleTypes.ENCHANT; }
    @Override public SoundEvent sound() { return SoundEvents.AMETHYST_BLOCK_RESONATE; }
    @Override public int colour() { return 0x8c52d9; }
}
