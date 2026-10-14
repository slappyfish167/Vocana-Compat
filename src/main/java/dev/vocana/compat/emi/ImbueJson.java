package dev.vocana.compat.emi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.vocana.Trigger;
import dev.vocana.bench.ImbueRecipe;
import dev.vocana.compat.VocanaCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Writes an {@link ImbueRecipe} back into the JSON shape {@link ImbueRecipe#parse} reads, so the server can hand its
 * loaded recipes to clients (see {@link ImbueRecipeSync}). Vocana only parses; this is the inverse, kept exact enough
 * that {@code parse(write(r))} equals {@code r} (the GameTests check it). No Minecraft classes are involved.
 */
public final class ImbueJson {
    private ImbueJson() {}

    /** One recipe as its data file would read, plus its {@code id}. */
    public static JsonObject write(ImbueRecipe r) {
        var o = new JsonObject();
        o.addProperty("id", r.id());
        var target = new JsonObject();
        target.addProperty("kind", r.kind());
        if (r.item() != null) target.addProperty("item", r.item());
        if (r.tag() != null) target.addProperty("tag", r.tag());
        o.add("target", target);
        if (r.chant()) { var c = new JsonObject(); c.addProperty("max_cp", r.maxCp()); o.add("chant", c); }
        else { var e = new JsonArray(); for (String g : r.essences()) e.add(g); o.add("essence", e); }
        o.addProperty("shades", r.shades());
        var triggers = new JsonArray(); for (Trigger t : r.triggers()) triggers.add(t.name()); o.add("triggers", triggers);
        var materials = new JsonArray();
        for (var m : r.materials()) {
            var mo = new JsonObject();
            if (m.item() != null) mo.addProperty("item", m.item());
            if (m.tag() != null) mo.addProperty("tag", m.tag());
            mo.addProperty("count", m.count());
            if (m.ofEssence()) mo.addProperty("essence", true);
            materials.add(mo);
        }
        o.add("materials", materials);
        o.addProperty("xp_levels", r.xpLevels()); o.addProperty("thrum", r.thrum());
        o.addProperty("strength", r.strength()); o.addProperty("complexity", r.complexity());
        if (!ImbueRecipe.Surcharge.NONE.equals(r.shade())) {
            var s = new JsonObject();
            s.addProperty("xp_levels", r.shade().xpLevels()); s.addProperty("thrum", r.shade().thrum()); s.addProperty("complexity", r.shade().complexity());
            o.add("shade", s);
        }
        if (r.ritual()) {
            var ritual = new JsonObject(); var catalysts = new JsonArray();
            for (var c : r.catalysts()) {
                var co = new JsonObject();
                if (c.block() != null) co.addProperty("block", c.block());
                if (c.tag() != null) co.addProperty("tag", c.tag());
                co.addProperty("count", c.count()); co.addProperty("radius", c.radius());
                catalysts.add(co);
            }
            ritual.add("catalysts", catalysts); ritual.addProperty("channel", r.channel());
            o.add("ritual", ritual);
        }
        o.addProperty("priority", r.priority());
        return o;
    }

    /** Every recipe, in the order given, as one JSON array. */
    public static JsonArray writeAll(Collection<ImbueRecipe> recipes) {
        var out = new JsonArray();
        for (var r : recipes) out.add(write(r));
        return out;
    }

    /** The recipes of an array written by {@link #writeAll}; an entry that will not parse is logged and skipped. */
    public static List<ImbueRecipe> readAll(String json) {
        List<ImbueRecipe> out = new ArrayList<>();
        JsonElement root;
        try { root = JsonParser.parseString(json); } catch (RuntimeException e) { VocanaCompat.LOG.warn("emi module: unreadable imbue recipe sync: {}", e.getMessage()); return out; }
        if (!root.isJsonArray()) return out;
        for (JsonElement e : root.getAsJsonArray()) {
            if (!e.isJsonObject()) continue;
            var o = e.getAsJsonObject();
            String id = o.has("id") ? o.get("id").getAsString() : "?";
            try { out.add(ImbueRecipe.parse(id, o)); }
            catch (RuntimeException x) { VocanaCompat.LOG.warn("emi module: synced imbue recipe {} is skipped: {}", id, x.getMessage()); }
        }
        return out;
    }
}
