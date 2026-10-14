package dev.vocana.compat.hexcasting;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import dev.vocana.Vocana;
import dev.vocana.api.VocanaApi;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.server.CastService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Deed DRAW (State frame): the caster draws Thrum out of the media they carry. Media is found the way Hex finds
 * it for a cast, through {@code HexAPI.instance().findMediaHolder(stack)} (a Java default method of the API interface,
 * implemented by {@code HexAPIImpl}, which on NeoForge answers for amethyst dust, shards, charged amethyst, quenched
 * shards and every {@code MediaHolderItem}; verified in
 * {@code Forge/src/main/java/at/petrak/hexcasting/forge/xplat/ForgeXplatImpl.java}), and spent through
 * {@code ADMediaHolder.withdrawMedia}, which for raw amethyst shrinks the stack by whole items and returns the media
 * actually taken. Holders are drained in Hex's own order, highest consumption priority first (dust, then shards, then
 * charged amethyst), packaged Hexes never, phials only when the config allows. The conversion rate, the per-cast cap
 * and the toggle are in {@link HexcastingConfig}.
 */
final class MediaDraw {
    private MediaDraw() {}
    static final String GLOSS = "DRAW";

    static boolean act(VocanaApi.DeedContext ctx) {
        ServerPlayer p = ctx.caster();
        if (p == null) return false;
        if (!HexcastingConfig.get(HexcastingConfig.drawEnabled)) { CastService.tell(p, "DRAW is closed in this world."); return false; }
        // DRAW works on the caster's own stores; aimed at another creature it has nothing to take from.
        for (Entity e : ctx.entities()) if (e instanceof LivingEntity && e != p) { CastService.tell(p, "DRAW draws only from the media you carry."); return false; }
        var lore = p.getData(Vocana.LORE);
        long perThrum = HexcastingConfig.get(HexcastingConfig.mediaPerThrum);
        double room = MediaMath.room(lore.thrum, lore.maxThrum(), HexcastingConfig.get(HexcastingConfig.drawMaxThrumPerCast), ctx.intensity());
        if (room < .5) { CastService.tell(p, "Your Thrum is already full."); return false; }
        List<ADMediaHolder> holders = holders(p);
        if (holders.isEmpty()) { CastService.tell(p, "You carry no media to draw on: amethyst dust, shards or charged amethyst."); return false; }
        long need = MediaMath.mediaFor(room, perThrum), drawn = 0;
        for (ADMediaHolder h : holders) {
            if (need <= 0) break;
            // A simulated withdrawal of 1 reveals the holder's smallest step: one item's worth for raw amethyst, 1 for a phial.
            long unit = h.withdrawMedia(1, true);
            if (unit <= 0) continue;
            long take = MediaMath.quanta(need, unit, drawn == 0);
            if (take <= 0) continue;
            long got = h.withdrawMedia(take, false);
            if (got <= 0) continue;
            drawn += got; need -= got;
        }
        if (drawn <= 0) { CastService.tell(p, "The media would not yield."); return false; }
        double gain = Math.min(lore.maxThrum() - lore.thrum, MediaMath.thrumFor(drawn, perThrum));
        lore.thrum += gain;
        var level = p.serverLevel();
        level.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.2, p.getZ(), 24, .4, .4, .4, .6);
        level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 8, .3, .3, .3, .02);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1f, 1.3f);
        CastService.tell(p, String.format(Locale.ROOT, "Drew %.1f Thrum from %.1f dust of media.", gain, drawn / (double) MediaConstants.DUST_UNIT));
        if (HexcastingConfig.get(HexcastingConfig.verboseLogging)) VocanaCompat.LOG.info("hexcasting DRAW: {} drew {} media for {} Thrum", p.getScoreboardName(), drawn, gain);
        return true;
    }

    /** The caster's media holders that DRAW may take from, in Hex's consumption order (highest priority first). */
    static List<ADMediaHolder> holders(ServerPlayer p) {
        boolean batteries = HexcastingConfig.get(HexcastingConfig.drawFromBatteries);
        List<ADMediaHolder> out = new ArrayList<>();
        var inv = p.getInventory();
        List<ItemStack> stacks = new ArrayList<>(inv.items);
        stacks.addAll(inv.offhand);
        for (ItemStack st : stacks) {
            if (st.isEmpty() || st.getItem() instanceof HexHolderItem) continue;
            ADMediaHolder h = HexAPI.instance().findMediaHolder(st);
            if (h == null || !h.canProvide()) continue;
            if (h.canRecharge() && !batteries) continue;
            if (h.withdrawMedia(-1, true) <= 0) continue;
            out.add(h);
        }
        out.sort((a, b) -> Integer.compare(b.getConsumptionPriority(), a.getConsumptionPriority()));
        return out;
    }
}
