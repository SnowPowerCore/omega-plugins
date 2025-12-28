package com.omega.itemloader.infrastructure;

import org.bukkit.inventory.ItemStack;

import java.nio.file.Path;
import java.util.Map;

public interface ItemStackFileLoader {
    Map<String, ItemStack> load(Path path);
}
