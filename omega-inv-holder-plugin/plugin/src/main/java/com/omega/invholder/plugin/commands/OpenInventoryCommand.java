package com.omega.invholder.plugin.commands;

import com.omega.invholder.core.api.InventoryRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class OpenInventoryCommand extends Command {

    private final String inventoryId;
    private final String permission;
    private final InventoryRegistry registry;

    public OpenInventoryCommand(String name, String inventoryId, String permission, InventoryRegistry registry) {
        super(name);
        this.inventoryId = inventoryId;
        this.permission = permission;
        this.registry = registry;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (permission != null && !permission.isBlank() && !sender.hasPermission(permission)) {
            sender.sendMessage("No permission.");
            return true;
        }

        Optional<Inventory> invOpt = registry.create(inventoryId);
        if (invOpt.isEmpty()) {
            sender.sendMessage("Unknown inventory: " + inventoryId);
            return true;
        }

        player.openInventory(invOpt.get());
        return true;
    }
}
