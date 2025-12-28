package com.omega.racing.core.api;

import org.bukkit.entity.Player;

/**
 * UI/session operations used by omega-interactable-plugin actions.
 */
public interface RaceUiApi {

    void openEditor(Player player, String inventoryId);

    /**
     * Opens a specific inventory in the editor and optionally pushes history.
     * This is useful for actions like paging where "Back" should not grow the stack.
     */
    void openEditor(Player player, String inventoryId, boolean pushHistory);

    /**
     * Opens a specific inventory in the editor with an optional title override.
     *
     * Note: Bukkit inventory titles cannot be mutated in-place; implementations typically reopen.
     */
    void openEditor(Player player, String inventoryId, boolean pushHistory, String titleOverride);

    void back(Player player);
}