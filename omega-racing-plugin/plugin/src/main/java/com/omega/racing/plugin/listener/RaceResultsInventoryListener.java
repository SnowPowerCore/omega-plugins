package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.results.RaceResultsInventoryHolder;
import com.omega.racing.plugin.runtime.results.RaceResultsKeys;
import com.omega.racing.plugin.runtime.results.RaceResultsUiService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Inject;

public final class RaceResultsInventoryListener implements Listener {

    private final RaceResultsUiService ui;

    @Inject
    public RaceResultsInventoryListener(RaceResultsUiService ui) {
        this.ui = ui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RaceResultsInventoryHolder holder)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }

        String action = actionOf(clicked);
        if (action == null) {
            return;
        }

        switch (action) {
            case RaceResultsKeys.ACTION_CLOSE -> player.closeInventory();
            case RaceResultsKeys.ACTION_PREV -> ui.open(player, holder.raceName(), holder.stageType(), holder.page() - 1);
            case RaceResultsKeys.ACTION_NEXT -> ui.open(player, holder.raceName(), holder.stageType(), holder.page() + 1);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RaceResultsInventoryHolder)) {
            return;
        }
        event.setCancelled(true);
    }

    private static String actionOf(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceResultsKeys.UI_ACTION, PersistentDataType.STRING);
    }
}
