package dev.vocana.compat.create;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Builders for Create's processing recipes as JSON, for the module's own data and for sibling modules (Ice and Fire
 * builds its dragonsteel alloy and tool lines with these shapes; {@code docs/create_recipes.md} shows the output).
 * Pure Java and Gson, no Minecraft, no Create: the shapes were read from Create's generated data and codecs on
 * {@code mc1.21.1/dev} ({@code ProcessingRecipeParams}: {@code ingredients}, {@code results}, {@code processing_time},
 * {@code heat_requirement}; {@code ProcessingOutput}: {@code id}, {@code count}, {@code chance}, {@code components};
 * {@code SequencedAssemblyRecipeSerializer}: {@code ingredient}, {@code transitional_item}, {@code sequence},
 * {@code results}, {@code loops}; {@code ItemApplicationRecipeParams}: {@code keep_held_item}). Nothing here is used at
 * runtime; the unit test keeps the shipped files in step with it.
 */
public final class CreateRecipes {
    private CreateRecipes() {}
    public static final String MIXING = "create:mixing", PRESSING = "create:pressing", DEPLOYING = "create:deploying",
        ITEM_APPLICATION = "create:item_application", SEQUENCED_ASSEMBLY = "create:sequenced_assembly", FILLING = "create:filling", CUTTING = "create:cutting";
    /** {@code heat_requirement} values ({@code HeatCondition}, serialised lower case): none, heated (any lit burner but a smouldering one), superheated (a seething one). */
    public static final String NONE = "none", HEATED = "heated", SUPERHEATED = "superheated";

    /** The conditions every data file of this module carries, plus a {@code mod_loaded} for each further mod named. */
    public static JsonArray conditions(String... alsoLoaded) {
        var a = new JsonArray();
        a.add(condition("neoforge:mod_loaded", "modid", "create"));
        a.add(condition("vocanacompat:module_enabled", "module", "create"));
        for (String mod : alsoLoaded) a.add(condition("neoforge:mod_loaded", "modid", mod));
        return a;
    }
    private static JsonObject condition(String type, String key, String value) { var o = new JsonObject(); o.addProperty("type", type); o.addProperty(key, value); return o; }

    /** An ingredient: {@code #c:ingots/gold} is a tag, anything else an item id. */
    public static JsonObject ingredient(String idOrTag) {
        var o = new JsonObject();
        if (idOrTag.startsWith("#")) o.addProperty("tag", idOrTag.substring(1)); else o.addProperty("item", idOrTag);
        return o;
    }
    /** A fluid ingredient of a {@code create:filling} step or a mixing recipe: NeoForge's {@code neoforge:single} sized fluid ingredient. */
    public static JsonObject fluid(String fluidId, int amountMb) {
        var o = new JsonObject(); o.addProperty("type", "neoforge:single"); o.addProperty("amount", amountMb); o.addProperty("fluid", fluidId); return o;
    }
    /** A result; {@code count} 1 and {@code chance} 1 are left out as Create's data does. In a sequenced assembly's result pool chances are weights. */
    public static JsonObject output(String id, int count, double chance) {
        var o = new JsonObject();
        if (chance != 1) o.addProperty("chance", chance);
        o.addProperty("id", id);
        if (count != 1) o.addProperty("count", count);
        return o;
    }
    public static JsonObject output(String id) { return output(id, 1, 1); }

    /** A processing recipe of any single-step type: ingredients in, results out, an optional heat and time. */
    public static JsonObject processing(String type, String heat, int processingTime, List<JsonObject> ingredients, List<JsonObject> results) {
        var o = new JsonObject();
        o.addProperty("type", type);
        if (heat != null && !heat.equals(NONE)) o.addProperty("heat_requirement", heat);
        if (processingTime > 0) o.addProperty("processing_time", processingTime);
        o.add("ingredients", array(ingredients));
        o.add("results", array(results));
        return o;
    }
    /** {@code create:mixing} in a basin under a mechanical mixer; {@code heat} is {@link #HEATED} or {@link #SUPERHEATED} for alloying over a blaze burner. */
    public static JsonObject mixing(String heat, List<JsonObject> ingredients, JsonObject... results) { return processing(MIXING, heat, 0, ingredients, List.of(results)); }
    /** {@code create:pressing}: one item under a mechanical press. */
    public static JsonObject pressing(String ingredient, JsonObject result) { return processing(PRESSING, NONE, 0, List.of(ingredient(ingredient)), List.of(result)); }
    /** {@code create:deploying}: a deployer presses {@code applied} onto {@code target}; {@code keepHeldItem} leaves the applied item in the deployer's hand. */
    public static JsonObject deploying(String target, String applied, JsonObject result, boolean keepHeldItem) {
        var o = processing(DEPLOYING, NONE, 0, List.of(ingredient(target), ingredient(applied)), List.of(result));
        if (keepHeldItem) o.addProperty("keep_held_item", true);
        return o;
    }
    /** {@code create:item_application}: the player applies {@code applied} to the block {@code target} by hand (a deployer can too). */
    public static JsonObject itemApplication(String target, String applied, JsonObject result) {
        return processing(ITEM_APPLICATION, NONE, 0, List.of(ingredient(target), ingredient(applied)), List.of(result));
    }

    /**
     * {@code create:sequenced_assembly}: {@code ingredient} becomes {@code transitional} (an item of Create's
     * {@code SequencedAssemblyItem} class shows the progress), the {@code steps} run in order {@code loops} times, then one
     * of {@code results} is drawn by weight. Every step's first ingredient must be the transitional item and its result
     * the transitional item again; {@link #step} writes that for you.
     */
    public static JsonObject sequencedAssembly(String ingredient, String transitional, int loops, List<JsonObject> steps, List<JsonObject> results) {
        var o = new JsonObject();
        o.addProperty("type", SEQUENCED_ASSEMBLY);
        o.add("ingredient", ingredient(ingredient));
        if (loops != 1) o.addProperty("loops", loops);
        o.add("results", array(results));
        o.add("sequence", array(steps));
        o.add("transitional_item", output(transitional));
        return o;
    }
    /** One step of a sequenced assembly: a pressing ({@code applied} null), deploying or filling of the transitional item. */
    public static JsonObject step(String type, String transitional, JsonObject applied) {
        var ingredients = applied == null ? List.of(ingredient(transitional)) : List.of(ingredient(transitional), applied);
        return processing(type, NONE, 0, ingredients, List.of(output(transitional)));
    }
    public static JsonObject pressingStep(String transitional) { return step(PRESSING, transitional, null); }
    public static JsonObject deployingStep(String transitional, String applied) { return step(DEPLOYING, transitional, ingredient(applied)); }
    public static JsonObject fillingStep(String transitional, String fluidId, int amountMb) { return step(FILLING, transitional, fluid(fluidId, amountMb)); }

    /** Puts the module's conditions on a recipe (first, so a reader sees them before the recipe). */
    public static JsonObject conditioned(JsonObject recipe, String... alsoLoaded) {
        var o = new JsonObject();
        o.add("neoforge:conditions", conditions(alsoLoaded));
        for (var e : recipe.entrySet()) o.add(e.getKey(), e.getValue());
        return o;
    }
    private static JsonArray array(List<JsonObject> items) { var a = new JsonArray(); for (var i : items) a.add(i); return a; }
}
