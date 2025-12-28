package com.omega.racing.core.model;

import java.util.Objects;

/**
 * A block location for a racer on the starter grid, with a facing direction.
 *
 * Stored in the core model (no Bukkit dependency): world name + block coordinates + yaw/pitch.
 *
 * Notes:
 * - Coordinates are block coordinates; teleporting should use the block center (x+0.5, z+0.5).
 * - Facing is continuous (any direction), not limited to the 6 block faces.
 * - Equality/hashCode intentionally ignore yaw/pitch so a grid "position" is unique per block.
 */
public final class RaceGridPosition {

    private String world;
    private int x;
    private int y;
    private int z;
    private int positionIndex;
    private float yaw;
    private float pitch;

    public RaceGridPosition() {
    }

    public RaceGridPosition(String world, int x, int y, int z, int positionIndex, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.positionIndex = Math.max(1, positionIndex);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getPositionIndex() {
        positionIndex = Math.max(1, positionIndex);
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = Math.max(1, positionIndex);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RaceGridPosition that)) return false;
        return x == that.x
            && y == that.y
            && z == that.z
            && positionIndex == that.positionIndex
            && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, positionIndex);
    }
}
