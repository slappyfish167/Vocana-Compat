package dev.vocana.compat.gametest;

import dev.vocana.compat.Module;
import dev.vocana.compat.VocanaCompat;
import dev.vocana.engine.Chapter;
import dev.vocana.engine.Vocabulary;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The Ice and Fire module on the GameTest server, where Ice and Fire is absent: its words reached Vocana through the
 * merged lexicon and sit in the right chapters, its data files are in the jar, and nothing it registers (items, its
 * config condition) exists because the module did not load. Nothing here names a class of the module's package, so
 * the test never depends on how that package would class-load.
 */
@GameTestHolder(VocanaCompat.ID) @PrefixGameTestTemplate(false)
public final class IceAndFireGameTests {
    private IceAndFireGameTests() {}

    @GameTest(template = "test_empty")
    public static void theDragonShadesAreWordsInTheirChapters(GameTestHelper h) {
        var frost = Vocabulary.INSTANCE.byGloss("DRAGONFROST");
        h.assertTrue(frost != null, "DRAGONFROST is in the lexicon");
        h.assertTrue(frost.elder() && "FROST".equals(frost.base()), "DRAGONFROST is an Elder shade of FROST");
        h.assertTrue(Chapter.of(frost) == Chapter.FROST, "DRAGONFROST sits in the Frost chapter");
        h.assertTrue(Vocabulary.INSTANCE.byForm("drakopagos") == frost, "its form is drakopagos");
        var bolt = Vocabulary.INSTANCE.byGloss("DRAGONBOLT");
        h.assertTrue(bolt != null, "DRAGONBOLT is in the lexicon");
        h.assertTrue(bolt.elder() && "LIGHTNING".equals(bolt.base()), "DRAGONBOLT is an Elder shade of LIGHTNING");
        h.assertTrue(Chapter.of(bolt) == Chapter.LIGHTNING, "DRAGONBOLT sits in the Lightning chapter");
        h.assertTrue(Vocabulary.INSTANCE.shades("FROST").contains(frost), "FROST lists its new shade");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void nothingOfTheModuleIsRegisteredWithoutIceAndFire(GameTestHelper h) {
        h.assertFalse(ModList.get().isLoaded(Module.ICE_AND_FIRE.modId), "iceandfire is not on the GameTest classpath");
        h.assertFalse(VocanaCompat.loaded(Module.ICE_AND_FIRE), "the module did not load");
        for (String id : new String[]{"netherite_dragonsteel_ingot", "wyrmsteel_block", "wyrmsteel_staff", "wyrmsteel_sword", "wyrmsteel_pickaxe", "wyrmsteel_axe",
                                      "wyrmsteel_shovel", "wyrmsteel_hoe", "wyrmsteel_helmet", "wyrmsteel_chestplate", "wyrmsteel_leggings", "wyrmsteel_boots",
                                      "incomplete_wyrmsteel_staff", "incomplete_wyrmsteel_chestplate", "dragonarmor_wyrmsteel_head", "dragonarmor_wyrmsteel_tail"})
            h.assertFalse(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, id)), "vocanacompat:" + id + " is not registered");
        h.assertFalse(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "wyrmsteel_block")), "the wyrmsteel block is not registered");
        h.assertFalse(BuiltInRegistries.ARMOR_MATERIAL.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "wyrmsteel")),
            "the wyrmsteel armour material is not registered");
        // The shared tags name the gear optionally, so without the module they hold nothing of it.
        h.assertFalse(BuiltInRegistries.ITEM.getTag(ItemTags.SWORDS).map(t -> t.stream().anyMatch(i -> BuiltInRegistries.ITEM.getKey(i.value()).getNamespace().equals(VocanaCompat.ID))).orElse(false),
            "minecraft:swords holds no vocanacompat item");
        // The flag condition is the core's, so the siren files still parse in a game without Ice and Fire; their flags read false.
        h.assertTrue(NeoForgeRegistries.CONDITION_SERIALIZERS.containsKey(ResourceLocation.fromNamespaceAndPath(VocanaCompat.ID, "module_flag")), "vocanacompat:module_flag is registered by the core");
        h.assertFalse(new dev.vocana.compat.ModuleFlagCondition("iceandfire", "removeSirens").test(net.neoforged.neoforge.common.conditions.ICondition.IContext.EMPTY), "an unloaded module's flag is false");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void theModuleDataShipsInTheJar(GameTestHelper h) {
        for (String path : new String[]{
            "/data/vocanacompat/neoforge/biome_modifier/iceandfire/remove_siren_spawns.json",
            "/data/vocanacompat/recipe/iceandfire/netherite_dragonsteel_ingot.json",
            "/data/vocanacompat/recipe/iceandfire/wyrmsteel_staff.json",
            "/data/vocanacompat/recipe/iceandfire/wyrmsteel_chestplate.json",
            "/data/vocanacompat/recipe/iceandfire/wyrmsteel_block.json",
            "/data/vocanacompat/loot_table/blocks/wyrmsteel_block.json",
            "/data/minecraft/tags/item/swords.json",
            "/data/vocanacompat/recipe/iceandfire/dragonforge/netherite_dragonsteel_ingot_fire.json",
            "/data/vocanacompat/recipe/iceandfire/smithing/wyrmsteel_sword.json",
            "/data/vocanacompat/recipe/iceandfire/dragonarmor_wyrmsteel_body.json",
            "/data/vocanacompat/tags/damage_type/iceandfire/dragon_breath.json",
            "/assets/vocanacompat/textures/item/iceandfire/dragonarmor_wyrmsteel_body.png",
            "/assets/vocanacompat/textures/models/armor/wyrmsteel_layer_1.png",
            "/assets/vocanacompat/textures/models/armor/wyrmsteel_layer_2.png",
            "/assets/vocanacompat/blockstates/wyrmsteel_block.json",
            "/data/vocanacompat/vocana_imbue/iceandfire/wyrmsteel_staff.json",
            "/data/vocanacompat/tags/entity_type/iceandfire/dragonfrost.json",
            "/data/vocana/tags/entity_type/hears/dragonbolt.json",
            "/assets/vocanacompat/textures/item/iceandfire/wyrmsteel_staff.png"})
            h.assertTrue(VocanaCompat.class.getResource(path) != null, path + " is in the resources");
        h.succeed();
    }
}
