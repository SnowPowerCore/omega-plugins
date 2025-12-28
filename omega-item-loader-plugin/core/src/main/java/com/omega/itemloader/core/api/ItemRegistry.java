package com.omega.itemloader.core.api;

import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Public API exposed via Bukkit ServicesManager.
 *
 * Consumers should treat returned ItemStacks as mutable and not cache them; request a new clone when needed.
 */
public interface ItemRegistry {

    Set<String> getIds();

    Optional<ItemStack> create(String id);

    /**
     * Returns cloned ItemStacks for all ids.
     */
    Map<String, ItemStack> createAll();
}
