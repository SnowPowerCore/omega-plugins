package com.omega.racing.plugin.runtime.blocks;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RaceBlocksInventoryHolder implements InventoryHolder {

    private final String raceName;
    private final int page;
    private Inventory inventory;

    public RaceBlocksInventoryHolder(String raceName, int page) {
        this.raceName = raceName;
        this.page = page;
    }

    public String raceName() {
        return raceName;
    }

    public int page() {
        return page;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
