package com.omega.interactable.plugin.listener;

import com.omega.interactable.plugin.exec.InteractionExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

public final class InventoryItemClickListener implements Listener {

    private final InteractionExecutor executor;

    @Inject
    public InventoryItemClickListener(InteractionExecutor executor) {
        this.executor = executor;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        executor.execute(player, item, meta, event);
    }
}
