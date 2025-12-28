package com.omega.interactable.plugin.listener;

import com.omega.interactable.plugin.exec.InteractionExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

public final class ItemInteractionListener implements Listener {

    private final InteractionExecutor executor;

    @Inject
    public ItemInteractionListener(InteractionExecutor executor) {
        this.executor = executor;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
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
