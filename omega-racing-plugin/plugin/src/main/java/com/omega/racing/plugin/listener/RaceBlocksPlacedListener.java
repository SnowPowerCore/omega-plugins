package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.blocks.RaceBlocksPlacedStore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import jakarta.inject.Inject;

public final class RaceBlocksPlacedListener implements Listener {

    private final RaceBlocksPlacedStore placed;

    @Inject
    public RaceBlocksPlacedListener(RaceBlocksPlacedStore placed) {
        this.placed = placed;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }

        // Only stage blocks have identification metadata.
        placed.stageIdFromItem(event.getItemInHand()).ifPresent(id -> {
            Block block = event.getBlockPlaced();
            if (block == null) {
                return;
            }
            // This UI currently uses BLUE_ICE for stages. Keep scope tight.
            if (block.getType() != Material.BLUE_ICE) {
                return;
            }
            placed.setStageId(block, id);
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        if (block == null) {
            return;
        }

        // Clean up persisted mapping when the block is removed.
        if (block.getType() == Material.BLUE_ICE) {
            placed.clearStageId(block);
        }
    }
}
