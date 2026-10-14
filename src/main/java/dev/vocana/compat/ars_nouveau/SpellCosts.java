package dev.vocana.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import dev.vocana.StaffItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The balance lever on Ars's side: every spell a player casts costs {@code arsCostFactor} times what Ars computed,
 * and a Vocana staff in the off hand gives {@code vocanaStaffArsDiscount} of that back. Both hang on
 * {@code SpellCostCalcEvent}, whose {@code currentCost} is already net of Ars's own discounts (rings, threads, the
 * caster tool: {@code ManaUtil.getPlayerDiscounts}) and which {@code SpellResolver} posts twice per cast on the game
 * bus: {@code Pre} from {@code getResolveCost} (can the caster afford it?) and {@code Post} from
 * {@code getExpendedCost} (what is taken). Both get the same answer, so a spell never passes the check and then costs
 * more. Machines (turrets, runes: {@code CasterType} other than {@code PLAYER}) are left alone unless
 * {@code applyToMachines} says otherwise, because their source is a bulk resource with its own economy.
 */
final class SpellCosts {
    private SpellCosts() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(SpellCosts::pre);
        NeoForge.EVENT_BUS.addListener(SpellCosts::post);
    }

    private static void pre(SpellCostCalcEvent.Pre event) { adjust(event); }
    private static void post(SpellCostCalcEvent.Post event) { adjust(event); }

    static void adjust(SpellCostCalcEvent event) {
        SpellContext context = event.context;
        if (context == null || context.getCaster() == null) return;
        boolean player = context.getCaster().getCasterType() == SpellContext.CasterType.PLAYER;
        if (!player && !ArsNouveauConfig.get(ArsNouveauConfig.applyToMachines)) return;
        boolean staff = player && context.getUnwrappedCaster() instanceof Player p && p.getOffhandItem().getItem() instanceof StaffItem;
        event.currentCost = Conversion.arsCost(event.currentCost, ArsNouveauConfig.get(ArsNouveauConfig.arsCostFactor),
            ArsNouveauConfig.get(ArsNouveauConfig.vocanaStaffArsDiscount), staff);
    }
}
