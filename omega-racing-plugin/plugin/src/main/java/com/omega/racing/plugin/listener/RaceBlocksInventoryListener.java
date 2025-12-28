package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.blocks.RaceBlocksInventoryHolder;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksKeys;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksUiService;
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

public final class RaceBlocksInventoryListener implements Listener {

    private final RaceBlocksUiService ui;

    @Inject
    public RaceBlocksInventoryListener(RaceBlocksUiService ui) {
        this.ui = ui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RaceBlocksInventoryHolder holder)) {
            return;
        }

        // Only handle clicks in our inventory.
        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }

        String action = actionOf(clicked);
        if (action != null) {
            switch (action) {
                case RaceBlocksKeys.ACTION_CLOSE -> player.closeInventory();
                case RaceBlocksKeys.ACTION_POSITION_TOOL -> {
                    ItemStack stick = new ItemStack(org.bukkit.Material.STICK, 1);
                    ItemMeta meta = stick.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(org.bukkit.ChatColor.GREEN + "Position Tool");
                        meta.setLore(java.util.List.of(
                            org.bukkit.ChatColor.GRAY + "Right-click a block",
                            org.bukkit.ChatColor.GRAY + "to set a racer position."
                        ));
                        meta.getPersistentDataContainer().set(RaceBlocksKeys.POSITION_TOOL, PersistentDataType.BYTE, (byte) 1);
                        meta.getPersistentDataContainer().set(RaceBlocksKeys.POSITION_TOOL_RACE, PersistentDataType.STRING, holder.raceName());
                        stick.setItemMeta(meta);
                    }

                    var leftovers = player.getInventory().addItem(stick);
                    if (!leftovers.isEmpty()) {
                        for (ItemStack remaining : leftovers.values()) {
                            if (remaining == null) {
                                continue;
                            }
                            player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                        }
                    }

                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Position tool received. Right-click a block.");
                    player.closeInventory();
                }
                case RaceBlocksKeys.ACTION_PREV -> ui.open(player, holder.raceName(), holder.page() - 1);
                case RaceBlocksKeys.ACTION_NEXT -> ui.open(player, holder.raceName(), holder.page() + 1);
            }
            return;
        }

        // Give a full stack of the clicked item (meta preserved).
        ItemStack toGive = clicked.clone();
        int max = Math.max(1, toGive.getType().getMaxStackSize());
        toGive.setAmount(max);

        var leftovers = player.getInventory().addItem(toGive);
        if (!leftovers.isEmpty()) {
            for (ItemStack remaining : leftovers.values()) {
                if (remaining == null) {
                    continue;
                }
                player.getWorld().dropItemNaturally(player.getLocation(), remaining);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RaceBlocksInventoryHolder)) {
            return;
        }
        // Prevent dragging items into/out of the UI.
        event.setCancelled(true);
    }

    private String actionOf(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceBlocksKeys.UI_ACTION, PersistentDataType.STRING);
    }
}
