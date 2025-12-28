package com.omega.itemloader.core.model;

import org.bukkit.inventory.ItemStack;

public record ItemDefinition(
        String id,
        ItemStack template
) {
}
