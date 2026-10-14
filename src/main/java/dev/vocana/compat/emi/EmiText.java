package dev.vocana.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure formatting for the EMI pages: numbers as the bench prints them, the proficiency thresholds as the server counts
 * them, and the words of a chant key. No Minecraft, no EMI, no Vocana classes, so the unit tests can check it.
 */
public final class EmiText {
    private EmiText() {}
    /** The share of {@code masteryCasts} each proficiency level needs (Unpractised, Practised, Adept, Expert, Mastered). */
    static final double[] SHARES = {0, .1, .3, .6, 1};

    /** A number without a needless fraction: 25 for 25.0, 12.5 for 12.5, 0.96 for 0.96. */
    public static String number(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) return Long.toString((long) v);
        String s = String.format(Locale.ROOT, "%.2f", v);
        if (s.indexOf('.') >= 0) { s = s.replaceAll("0+$", ""); if (s.endsWith(".")) s = s.substring(0, s.length() - 1); }
        return s;
    }
    /** Releases needed to reach a level (0–4) when {@code masteryCasts} releases master a chant, as {@code Mastery.threshold}. */
    public static int threshold(int level, int masteryCasts) {
        int l = Math.max(0, Math.min(4, level));
        return (int) Math.ceil(SHARES[l] * Math.max(0, masteryCasts));
    }
    /** The level (0–4) a number of releases has reached. */
    public static int level(int casts, int masteryCasts) {
        int l = 0;
        for (int i = 1; i < 5; i++) if (casts >= threshold(i, masteryCasts)) l = i;
        return l;
    }
    /** The Thrum, Strain and voicing factors of a level, as the server applies them: 1 − 0.04·L, 1 − 0.10·L, 1 − 0.10·L. */
    public static double thrumFactor(int level) { return 1 - .04 * level; }
    public static double strainFactor(int level) { return 1 - .10 * level; }
    public static double voicingFactor(int level) { return 1 - .10 * level; }

    /** The words of a chant key ({@code open saunta tenhu close}): lower case, split on single spaces, empty for none. */
    public static List<String> words(String chantKey) {
        List<String> out = new ArrayList<>();
        if (chantKey == null) return out;
        for (String w : chantKey.strip().toLowerCase(Locale.ROOT).split("\\s+")) if (!w.isEmpty()) out.add(w);
        return out;
    }
    /** Whether a chant key says this form as one of its words. */
    public static boolean uses(String chantKey, String form) {
        if (form == null || form.isEmpty()) return false;
        return words(chantKey).contains(form.toLowerCase(Locale.ROOT));
    }
    /** A list as prose: "a", "a and b", "a, b and c". */
    public static String prose(List<String> items) {
        if (items.isEmpty()) return "";
        if (items.size() == 1) return items.get(0);
        return String.join(", ", items.subList(0, items.size() - 1)) + " and " + items.get(items.size() - 1);
    }
    /** A recipe id ({@code vocana:fire_weapon}) as a path segment EMI's identifiers accept: {@code vocana/fire_weapon}. */
    public static String slug(String id) {
        if (id == null || id.isEmpty()) return "unnamed";
        StringBuilder b = new StringBuilder(id.length());
        for (char c : id.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ':') b.append('/');
            else if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.' || c == '_' || c == '-') b.append(c);
            else b.append('_');
        }
        return b.toString();
    }
    /** A gloss as prose when its form may not be named: FIRE → fire, SOUL_FLAME → soul flame. */
    public static String glossWord(String gloss) { return gloss == null ? "" : gloss.toLowerCase(Locale.ROOT).replace('_', ' '); }
}
