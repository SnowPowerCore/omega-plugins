package com.omega.racing.plugin.runtime.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import jakarta.inject.Inject;

import java.util.UUID;

public final class PlayerUiDecorator {

    private final UiComponents ui;

    @Inject
    public PlayerUiDecorator(UiComponents ui) {
        this.ui = ui;
    }

    public void decoratePlayerButton(ItemStack stack, OfflinePlayer offline) {
        if (stack == null || offline == null) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(offline);
            String name = offline.getName() == null ? offline.getUniqueId().toString() : offline.getName();
            skull.setDisplayName(ChatColor.AQUA + name);
            stack.setItemMeta(skull);
        } else if (meta != null) {
            String name = offline.getName() == null ? offline.getUniqueId().toString() : offline.getName();
            meta.setDisplayName(ChatColor.AQUA + name);
            stack.setItemMeta(meta);
        }
    }

    public ItemStack racerHead(UUID uuid) {
        ItemStack head = ui.uiItem("racing:ui/racer_entry", Material.PLAYER_HEAD, "Racer");
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            var offline = Bukkit.getOfflinePlayer(uuid);
            skull.setOwningPlayer(offline);
            String name = offline.getName() == null ? uuid.toString() : offline.getName();
            skull.setDisplayName(ChatColor.AQUA + name);
            head.setItemMeta(skull);
        }
        return head;
    }
}
