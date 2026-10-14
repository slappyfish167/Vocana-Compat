package dev.vocana.compat.emi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.vocana.client.ClientState;
import dev.vocana.engine.Vocabulary;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What this client knows the player has heard, read from the same snapshot the Grimoire reads ({@link ClientState}):
 * {@code forms[]} lists every heard form with its respelling and stage, {@code mastery[]} the practised chants. The
 * server sends that snapshot when the Chime or Grimoire is opened and while a session is live, not on login, so the
 * EMI pages read it at draw time and say so when none has come yet. The index of forms is rebuilt only when the
 * snapshot object changes. Unheard forms are never named here: the pages print a word's form only when it is heard.
 */
final class Heard {
    private Heard() {}
    private static JsonObject source;
    private static Map<String, JsonObject> forms = Map.of();

    /** The heard forms by name, from the current snapshot. */
    static synchronized Map<String, JsonObject> forms() {
        JsonObject data = ClientState.data;
        if (data != source) {
            Map<String, JsonObject> m = new HashMap<>();
            for (JsonElement e : ClientState.forms()) {
                if (!e.isJsonObject()) continue;
                var o = e.getAsJsonObject();
                if (o.has("form")) m.put(o.get("form").getAsString(), o);
            }
            source = data; forms = Map.copyOf(m);
        }
        return forms;
    }
    /** Whether a snapshot has arrived at all; before one, nothing can be told apart from unheard. */
    static boolean known() { return ClientState.data.has("forms"); }
    static boolean heard(String form) { return form != null && forms().containsKey(form); }
    static boolean heardGloss(String gloss) { var w = Vocabulary.INSTANCE.byGloss(gloss); return w != null && heard(w.form()); }

    /** An essence as the bench names it: its form and respelling once heard, else its gloss as plain words, marked unheard. */
    static Component essence(String gloss) {
        var w = Vocabulary.INSTANCE.byGloss(gloss);
        if (w == null) return Component.literal(EmiText.glossWord(gloss));
        if (heard(w.form())) return Component.literal(w.form() + " [" + w.say() + "]");
        return Component.translatable("vocanacompat.emi.imbue.unheard", EmiText.glossWord(gloss));
    }
    /** A proficiency level's name. */
    static Component level(int level) { return Component.translatable("vocanacompat.emi.level." + Math.max(0, Math.min(4, level))); }
    /** The caster's practised chants, most practised first: chant, releases, level. */
    record Practice(String chant, int casts, int level) {}
    static List<Practice> practised() {
        List<Practice> out = new ArrayList<>();
        for (JsonElement e : ClientState.mastery()) {
            if (!e.isJsonObject()) continue;
            var o = e.getAsJsonObject();
            if (!o.has("chant")) continue;
            int casts = o.has("casts") ? o.get("casts").getAsInt() : 0;
            out.add(new Practice(o.get("chant").getAsString(), casts, o.has("level") ? o.get("level").getAsInt() : ClientState.level(casts)));
        }
        return out;
    }
    /** The practised chants that say this form, as "chant · Level (releases/threshold)", at most {@code limit}. */
    static List<Component> practisedWith(String form, int limit) {
        List<Component> out = new ArrayList<>();
        for (var p : practised()) {
            if (out.size() >= limit) break;
            if (EmiText.uses(p.chant(), form)) out.add(line(p));
        }
        return out;
    }
    /** One practised chant as the Callings overview prints it. */
    static Component line(Practice p) {
        int next = p.level() >= 4 ? EmiText.threshold(4, ClientState.masteryCasts()) : EmiText.threshold(p.level() + 1, ClientState.masteryCasts());
        return Component.translatable("vocanacompat.emi.lexicon.practised", p.chant(), level(p.level()), p.casts(), next);
    }
}
