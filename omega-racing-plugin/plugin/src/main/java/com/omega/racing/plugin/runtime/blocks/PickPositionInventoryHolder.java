package com.omega.racing.plugin.runtime.blocks;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PickPositionInventoryHolder implements InventoryHolder {

    private final String raceName;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final String worldName;
    private final float yaw;
    private final float pitch;
    private final int page;

    private Inventory inventory;

    public PickPositionInventoryHolder(String raceName, String worldName, int blockX, int blockY, int blockZ, float yaw, float pitch, int page) {
        this.raceName = raceName;
        this.worldName = worldName;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.page = page;
    }

    public String raceName() {
        return raceName;
    }

    public String worldName() {
        return worldName;
    }

    public int blockX() {
        return blockX;
    }

    public int blockY() {
        return blockY;
    }

    public int blockZ() {
        return blockZ;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public int page() {
        return page;
    }

    public int contentOffset() {
        return page * 45;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
