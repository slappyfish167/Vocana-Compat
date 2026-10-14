package dev.vocana.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import dev.vocana.Vocana;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.server.Casting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resonate, the Ars glyph {@code vocanacompat:glyph_vocana_thrum}: a tier-two effect that returns a little Thrum to a
 * caster who has tuned a Chime, paid for in the mana the glyph costs. Ars charges the mana itself (the glyph's cost is
 * its per-glyph setting, {@code config/vocanacompat/glyph_vocana_thrum.toml}, which {@code GlyphRegistry.registerSpell}
 * writes from {@link #getDefaultManaCost}); this class only converts that cost at the par rate less the conversion
 * loss ({@link Conversion#thrumForMana}) and refunds it through the Seal ({@code Casting.refund}), which clamps at the
 * caster's pool. Only the caster is answered, whatever the spell hit, and never a fake player, so turrets and runes
 * gain nothing; a cooldown per caster caps how fast mana can become Thrum however deep the caster's mana.
 *
 * <p>Registration follows the example addon: the {@code INSTANCE} is handed to {@code GlyphRegistry.registerSpell}
 * during mod construction, and Ars's own {@code ItemsRegistry.onItemRegistry} later turns every entry of
 * {@code GlyphRegistry.getGlyphItemMap()} into a glyph item under the part's registry name.
 */
public final class ResonateEffect extends AbstractEffect {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "glyph_vocana_thrum");
    /** The Scribes Table recipe this module ships for the glyph, which the documentation page shows. */
    public static final ResourceLocation RECIPE = ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "ars_nouveau/glyph_vocana_thrum");
    public static final ResonateEffect INSTANCE = new ResonateEffect();

    /** The game time each caster last Resonated, for the cooldown; pruned when it grows. */
    private final Map<UUID, Long> last = new HashMap<>();

    private ResonateEffect() { super(ID, "Resonate"); }

    @Override public int getDefaultManaCost() { return Conversion.RESONATE_MANA; }
    @Override public SpellTier defaultTier() { return SpellTier.TWO; }
    /** No augments: Amplify would have to be priced into the exchange, and the cooldown is the balance lever instead. */
    @Override public @NotNull Set<AbstractAugment> getCompatibleAugments() { return augmentSetOf(); }
    @Override public String getBookDescription() {
        return "Returns a little of Vocana's Thrum to a caster who has tuned a Chime, paid for in the mana this glyph costs. The Seal keeps a share of every exchange, and it will not answer the same caster twice within a few seconds.";
    }

    @Override public void onResolve(HitResult hit, Level world, @NotNull LivingEntity shooter, SpellStats stats, SpellContext context, SpellResolver resolver) {
        if (!(world instanceof ServerLevel level) || !(shooter instanceof ServerPlayer p) || p instanceof FakePlayer) return;
        if (!ArsNouveauConfig.get(ArsNouveauConfig.resonateEnabled)) return;
        var lore = p.getData(Vocana.LORE);
        if (lore.heard.isEmpty()) { p.displayClientMessage(Component.translatable("vocanacompat.ars_nouveau.resonate.untuned"), true); return; }
        long now = level.getGameTime();
        Long previous = last.get(p.getUUID());
        long wait = previous == null ? 0 : Conversion.cooldownLeft(now, previous, ArsNouveauConfig.get(ArsNouveauConfig.resonateCooldownTicks));
        if (wait > 0) { p.displayClientMessage(Component.translatable("vocanacompat.ars_nouveau.resonate.cooling", number(wait / 20.0, 1)), true); return; }
        double thrum = Conversion.thrumForMana(getCastingCost(), ArsNouveauConfig.get(ArsNouveauConfig.manaPerThrum), ArsNouveauConfig.get(ArsNouveauConfig.conversionLoss));
        if (thrum <= 0) return;
        double before = lore.thrum;
        Casting.refund(p, thrum);
        last.put(p.getUUID(), now);
        if (last.size() > 256) last.values().removeIf(t -> now - t > 20 * 600);
        level.sendParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 1.4, p.getZ(), 8, .4, .3, .4, 1);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, .8f, 1.3f);
        p.displayClientMessage(Component.translatable("vocanacompat.ars_nouveau.resonate.restored", number(lore.thrum - before, 1), number(lore.thrum, 0), number(lore.maxThrum(), 0)), true);
    }

    private static String number(double v, int decimals) { return String.format(Locale.ROOT, "%." + decimals + "f", v); }
}
