package com.omega.interactable.plugin.bootstrap;

import com.omega.interactable.plugin.listener.ItemInteractionListener;
import com.omega.interactable.plugin.listener.InventoryItemClickListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import jakarta.inject.Inject;

public final class OmegaInteractableBootstrap {

    private final Plugin plugin;
    private final ItemInteractionListener itemListener;
    private final InventoryItemClickListener inventoryListener;

    @Inject
    public OmegaInteractableBootstrap(Plugin plugin, ItemInteractionListener itemListener, InventoryItemClickListener inventoryListener) {
        this.plugin = plugin;
        this.itemListener = itemListener;
        this.inventoryListener = inventoryListener;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(itemListener, plugin);
        Bukkit.getPluginManager().registerEvents(inventoryListener, plugin);
    }

    public void onDisable() {
        // no-op
    }
}
