package com.omega.invholder.core.api;

import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Public API exposed via Bukkit ServicesManager.
 */
public interface InventoryRegistry {

    Set<String> getIds();

    Optional<Inventory> create(String id);

    Map<String, Inventory> createAll();
}
