package dev.vocana.compat.iceandfire;

import java.util.List;

/**
 * The names of the module's boolean switches, as {@code config/vocanacompat-iceandfire.toml} and the data condition
 * {@code vocanacompat:module_flag} (module {@code iceandfire}) both spell them. Kept apart from {@link IceAndFireConfig} (which needs
 * NeoForge) so the data files' conditions can be checked against the list in a plain unit test.
 */
public final class IceAndFireSwitches {
    private IceAndFireSwitches() {}
    public static final String REMOVE_SIRENS = "removeSirens", REMOVE_SIREN_ISLANDS = "removeSirenIslands", REBALANCE = "rebalance";
    public static final List<String> ALL = List.of(REMOVE_SIRENS, REMOVE_SIREN_ISLANDS, REBALANCE);
}
