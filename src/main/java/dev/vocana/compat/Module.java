package dev.vocana.compat;

/**
 * The compatibility modules, one per target mod. A module's {@link #id} is at once its config key
 * ({@code modules.<id>.enabled}), its sub-package ({@code dev.vocana.compat.<id>}), its data namespace prefix
 * ({@code data/vocanacompat/.../<id>/...}) and the name of its lexicon and Grimoire files
 * ({@code src/main/lexicon/<id>.json}). Mod ids were read from each mod's {@code neoforge.mods.toml}
 * (see {@code docs/targets.md}).
 */
public enum Module {
    CREATE("create", "create", "Create", "CreateCompat"),
    WYRMROOST("wyrmroost", "wyrmroost", "Wyrmroost", "WyrmroostCompat"),
    ICE_AND_FIRE("iceandfire", "iceandfire", "Ice and Fire: Community Edition", "IceAndFireCompat"),
    EMI("emi", "emi", "EMI", "EmiCompat"),
    CURIOS("curios", "curios", "Curios API", "CuriosCompat"),
    HEXCAST("hexcasting", "hexcasting", "Hex Casting (HexCast NEOFORGED)", "HexcastingCompat"),
    ARS_NOUVEAU("ars_nouveau", "ars_nouveau", "Ars Nouveau", "ArsNouveauCompat");

    /** The module name: config key, sub-package and file name. */
    public final String id;
    /** The target mod's id, as {@code ModList} knows it. */
    public final String modId;
    /** The target's name for logs and config comments. */
    public final String displayName;
    private final String simpleClassName;

    Module(String id, String modId, String displayName, String simpleClassName) {
        this.id = id; this.modId = modId; this.displayName = displayName; this.simpleClassName = simpleClassName;
    }

    /** The fully qualified name of the module's entry class, looked up reflectively so a missing mod never loads it. */
    public String className() { return "dev.vocana.compat." + id + "." + simpleClassName; }
    /** The module's own config file, registered by the module itself through {@link VocanaCompat#registerModuleConfig}. */
    public String configFile() { return "vocanacompat-" + id + ".toml"; }

    /** The module with this id, or null. */
    public static Module byId(String id) {
        if (id == null) return null;
        for (var m : values()) if (m.id.equals(id)) return m;
        return null;
    }
}
