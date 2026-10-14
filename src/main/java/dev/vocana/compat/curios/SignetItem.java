package dev.vocana.compat.curios;

import dev.vocana.Vocana;
import dev.vocana.bench.ResidueItem;
import dev.vocana.engine.Vocabulary;
import dev.vocana.server.CastService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Locale;

/**
 * The Signet of the Elements: a ring that drinks an Essence Residue (what the Imbue Bench leaves when an essence imbue
 * is stripped) and then carries that essence in Vocana's own {@code vocana:word} component, as a residue or a Whispering
 * Relic does. Worn in a ring slot it makes Workings of that essence cheaper ({@code CuriosConfig.signetCostFactor},
 * applied through {@code VocanaApi.registerCostFactor}; see {@link CurioSlots#ringEssences}). To attune it, use the
 * Signet with a residue in the other hand: the residue is spent and its essence taken, replacing any earlier one. The
 * bench itself does not take rings (it imbues weapons, armour, tools and staffs only), which is why the Signet drinks.
 */
public final class SignetItem extends VocanaCurioItem {
    public SignetItem(Properties props) { super(props, "item.vocanacompat.signet_of_the_elements.tooltip", () -> CuriosConfig.get(CuriosConfig.signetCostFactor)); }

    /** The essence a Signet carries (a gloss), or empty. */
    public static String essence(ItemStack stack) { return stack.getOrDefault(Vocana.WORD.get(), ""); }
    /** The base essence a Signet carries (an Elder shade answers as the essence it shades), or empty. */
    public static String base(ItemStack stack) { String e = essence(stack); return e.isEmpty() ? "" : Vocabulary.INSTANCE.base(e); }

    private static ItemStack residueIn(Player p, InteractionHand hand) {
        var other = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return other.is(Vocana.RESIDUE.get()) && !ResidueItem.essence(other).isEmpty() ? other : ItemStack.EMPTY;
    }

    /** With a residue in the other hand the use attunes instead of equipping, so Curios must stand aside. */
    @Override public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player p && (!residueIn(p, InteractionHand.MAIN_HAND).isEmpty() || !residueIn(p, InteractionHand.OFF_HAND).isEmpty())) return false;
        return super.canEquipFromUse(slotContext, stack);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var signet = player.getItemInHand(hand);
        var residue = residueIn(player, hand);
        if (residue.isEmpty()) return super.use(level, player, hand);
        if (player instanceof ServerPlayer sp) {
            String gloss = ResidueItem.essence(residue);
            signet.set(Vocana.WORD.get(), gloss);
            if (!sp.isCreative()) residue.shrink(1);
            var w = Vocabulary.INSTANCE.byGloss(gloss);
            sp.serverLevel().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1f, .8f);
            sp.serverLevel().sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.2, sp.getZ(), 16, .3, .3, .3, .4);
            CastService.tell(sp, "The signet drinks the residue: it carries " + (w == null ? gloss.toLowerCase(Locale.ROOT) : w.form()) + ".");
            sp.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.sidedSuccess(signet, level.isClientSide);
    }

    @Override public boolean isFoil(ItemStack stack) { return !essence(stack).isEmpty(); }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String e = essence(stack);
        if (e.isEmpty()) tooltip.add(Component.translatable("item.vocanacompat.signet_of_the_elements.empty").withStyle(ChatFormatting.GRAY));
        else {
            var w = Vocabulary.INSTANCE.byGloss(e);
            tooltip.add(Component.translatable("item.vocanacompat.signet_of_the_elements.carries", w == null ? e.toLowerCase(Locale.ROOT) : w.form()).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
