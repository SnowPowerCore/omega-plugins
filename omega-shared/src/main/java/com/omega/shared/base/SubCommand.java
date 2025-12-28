package com.omega.shared.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    private final String name;

    protected SubCommand(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract boolean onCommand(Player player, CommandSender sender, String label, String[] args);
}