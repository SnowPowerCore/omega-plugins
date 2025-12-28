package com.omega.racing.plugin.runtime.results;

import com.omega.racing.core.model.RaceStageType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RaceResultsInventoryHolder implements InventoryHolder {

    private final String raceName;
    private final RaceStageType stageType;
    private final int page;
    private Inventory inventory;

    public RaceResultsInventoryHolder(String raceName, RaceStageType stageType, int page) {
        this.raceName = raceName;
        this.stageType = stageType;
        this.page = page;
    }

    public String raceName() {
        return raceName;
    }

    public RaceStageType stageType() {
        return stageType;
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

