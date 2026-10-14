package dev.vocana.compat.curios;

import dev.vocana.RelicItem;
import dev.vocana.Vocana;
import dev.vocana.engine.Vocabulary;
import dev.vocana.server.CastService;
import dev.vocana.server.Progress;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Curio behaviour for Vocana's own items, which cannot implement {@code ICurioItem} themselves. Registered in common
 * setup with {@code CuriosApi.registerCurio(Item, ICurioItem)}: Curios' item-capability provider looks the registry up
 * each time a stack is asked for its curio ({@code neoforge/.../Curios.java}, {@code CuriosImplMixinHooks.getCurioFromRegistry}),
 * so the moment of registration does not matter as long as it is before a world opens.
 *
 * <ul>
 * <li>The Torc goes on by use and otherwise needs nothing: {@code TorcItem.worn} already asks {@code VocanaApi.worn},
 * and {@link CurioSlots#wearsTorc} answers for the slots.</li>
 * <li>A Whispering Relic worn in a charm slot whispers its word after {@code relicWhisperSeconds} of wear, once per
 * wearing, exactly as using it would ({@code RelicItem.use}: a silent relic takes a random word first, an Elder word
 * is heard as its base by one who does not know that base, {@code Progress.learn} teaches it). The count lives in a
 * weak map keyed by the worn stack itself, as Vocana's {@code Imbuements} keys its cooldowns: a stack taken off and put
 * back is a new object and starts cold.</li>
 * </ul>
 * Spell Crystals and Inscribed Scrolls are not made curios: Vocana's waking words only look at the hands and the
 * inventory ({@code Latents.carried} is package-private and has no extension point), so a vessel in a charm slot would
 * be dormant and the tag would mislead; see docs/curios.md.
 */
final class VocanaCurios {
    private VocanaCurios() {}
    /** Ticks each worn relic has been worn; {@code -1} once it has whispered this wearing. */
    private static final Map<ItemStack, Integer> worn = new WeakHashMap<>();

    static void register() {
        CuriosApi.registerCurio(Vocana.TORC.get(), new ICurioItem() {
            @Override public boolean canEquipFromUse(SlotContext ctx, ItemStack stack) { return CuriosConfig.get(CuriosConfig.equipFromUse); }
        });
        CuriosApi.registerCurio(Vocana.RELIC.get(), new ICurioItem() {
            @Override public boolean canEquipFromUse(SlotContext ctx, ItemStack stack) { return CuriosConfig.get(CuriosConfig.equipFromUse); }
            /** Runs each tick on both sides for a stack in a functional slot ({@code CuriosEventHandler}); only the server counts. */
            @Override public void curioTick(SlotContext ctx, ItemStack stack) {
                if (!(ctx.entity() instanceof ServerPlayer sp) || !CuriosConfig.get(CuriosConfig.relicWhispers)) return;
                int t = worn.getOrDefault(stack, 0);
                if (t < 0) return;
                if (!CurioRules.whisperDue(++t, CuriosConfig.get(CuriosConfig.relicWhisperSeconds))) { worn.put(stack, t); return; }
                worn.put(stack, -1);
                whisper(sp, stack);
            }
            @Override public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) { worn.remove(stack); }
        });
    }

    /** What {@code RelicItem.use} does, from a slot: the relic performs its word for its wearer. */
    static void whisper(ServerPlayer sp, ItemStack stack) {
        String gloss = stack.get(Vocana.WORD.get());
        if (gloss == null || Vocabulary.INSTANCE.byGloss(gloss) == null) { gloss = RelicItem.random(sp.getRandom()).get(Vocana.WORD.get()); stack.set(Vocana.WORD.get(), gloss); }
        var w = Vocabulary.INSTANCE.byGloss(gloss);
        if (w == null) return;
        if (w.elder() && !sp.getData(Vocana.LORE).heard.contains(Vocabulary.INSTANCE.form(w.base()))) { gloss = w.base(); w = Vocabulary.INSTANCE.byGloss(gloss); if (w == null) return; }
        sp.serverLevel().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, .8f, 1.3f);
        sp.serverLevel().sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.6, sp.getZ(), 12, .3, .3, .3, .4);
        boolean fresh = Progress.learn(sp, gloss);
        CastService.tell(sp, "The relic at your breast whispers: " + w.form() + " [" + w.say() + "]" + (fresh ? " · a new form." : ""));
    }
}
