package com.omega.itemloader.infrastructure.json;

import org.bukkit.inventory.ItemStack;

public record ParsedItem(
        String id,
        String referenceId,
        ItemStack itemStack
) {
}
