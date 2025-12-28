package com.omega.invholder.infrastructure.model;

import com.omega.invholder.infrastructure.resolve.ItemReferenceResolver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public final class InventoryTemplate {

    private final String id;
    private final InventoryType type;
    private final int size;
    private final String title;
    private final List<InventorySlotItem> items;
    private final InventoryCommand command;

    public InventoryTemplate(
            String id,
            InventoryType type,
            int size,
            String title,
            List<InventorySlotItem> items,
            InventoryCommand command
    ) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.title = title;
        this.items = items;
        this.command = command;
    }

    public String id() {
        return id;
    }

    public InventoryCommand command() {
        return command;
    }

    public Inventory create(ItemReferenceResolver resolver) {
        String invTitle = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(title, ""));

        Inventory inv;
        if (type == InventoryType.CHEST) {
            inv = Bukkit.createInventory(null, size, invTitle);
        } else {
            inv = Bukkit.createInventory(null, type, invTitle);
        }

        for (InventorySlotItem slotItem : items) {
            if (slotItem.slot() < 0 || slotItem.slot() >= inv.getSize()) {
                continue;
            }

            ItemStack stack = slotItem.resolve(resolver);
            if (stack != null) {
                inv.setItem(slotItem.slot(), stack);
            }
        }

        return inv;
    }
}
