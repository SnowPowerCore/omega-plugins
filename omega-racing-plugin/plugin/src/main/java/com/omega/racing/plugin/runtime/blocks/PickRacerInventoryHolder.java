package com.omega.racing.plugin.runtime.blocks;

import com.omega.racing.core.model.RaceGridPosition;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PickRacerInventoryHolder implements InventoryHolder {

    private final String raceName;
    private final RaceGridPosition position;
    private Inventory inventory;

    public PickRacerInventoryHolder(String raceName, RaceGridPosition position) {
        this.raceName = raceName;
        this.position = position;
    }

    public String raceName() {
        return raceName;
    }

    public RaceGridPosition position() {
        return position;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
