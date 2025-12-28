package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

/**
 * Renders a specific OmegaRacing UI inventory.
 */
public interface InventoryRenderer {

    /**
     * Inventory ID as used by {@link com.omega.racing.infrastructure.interop.InventoryRegistryResolver}.
     */
    String inventoryId();

    /**
     * Allows this renderer to register itself into RaceUiService.
     */
    default void register(Map<String, InventoryRenderer> registry) {
        registry.put(inventoryId(), this);
    }

    /**
     * Optional dynamic title override computed at open-time.
     *
     * Returning null means "no default override" and will fall back to any persisted title override.
     */
    default String defaultTitleOverride(RacingSessionStore.Session session, String editingRaceName, RaceDefinition race) {
        return null;
    }

    void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session);
}
