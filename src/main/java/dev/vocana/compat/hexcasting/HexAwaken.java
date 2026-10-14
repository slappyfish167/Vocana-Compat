package dev.vocana.compat.hexcasting;

import at.petrak.hexcasting.api.item.HexHolderItem;
import dev.vocana.api.VocanaApi;
import dev.vocana.server.CastService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * The Deed AWAKEN (Force frame): a packaged Hex in the caster's hand casts itself, as if used. Hex marks every item
 * that can hold a packaged Hex with the API interface {@code HexHolderItem} (cypher, trinket, artifact; verified in
 * {@code common/items/magic/ItemPackagedHex.java}, whose {@code use} builds a {@code PackagedItemCastEnv}, runs the
 * stored iotas through a {@code CastingVM}, applies the item's cooldown and breaks a spent cypher). There is no public
 * "cast now" method apart from that {@code use}, so the Deed calls the item's own {@code Item.use} with the hand it is
 * held in: Hex's media accounting, cooldowns, mishaps and particles all happen exactly as on a right-click. The item
 * must be in a hand because {@code use} reads {@code player.getItemInHand(hand)}; the caster's other hand holds the
 * Catalyst.
 */
final class HexAwaken {
    private HexAwaken() {}
    static final String GLOSS = "AWAKEN";

    static boolean act(VocanaApi.DeedContext ctx) {
        ServerPlayer p = ctx.caster();
        if (p == null) return false;
        if (!HexcastingConfig.get(HexcastingConfig.awakenEnabled)) { CastService.tell(p, "AWAKEN is closed in this world."); return false; }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = p.getItemInHand(hand);
            if (!(st.getItem() instanceof HexHolderItem holder)) continue;
            String what = st.getHoverName().getString();
            if (!holder.hasHex(st)) { CastService.tell(p, "The " + what + " holds no Hex to wake."); return false; }
            if (p.getCooldowns().isOnCooldown(st.getItem())) { CastService.tell(p, "The " + what + " is still settling."); return false; }
            var level = p.serverLevel();
            var result = st.getItem().use(level, p, hand);
            if (!result.getResult().consumesAction()) { CastService.tell(p, "The " + what + " would not wake."); return false; }
            level.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.2, p.getZ(), 16, .3, .3, .3, .5);
            return true;
        }
        CastService.tell(p, "AWAKEN needs a cypher, trinket or artifact in your hand.");
        return false;
    }
}
