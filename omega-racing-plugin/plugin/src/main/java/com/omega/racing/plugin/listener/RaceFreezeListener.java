package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.stage.RaceFreezeService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import jakarta.inject.Inject;

public final class RaceFreezeListener implements Listener {

    private final RaceFreezeService freeze;

    @Inject
    public RaceFreezeListener(RaceFreezeService freeze) {
        this.freeze = freeze;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) {
            return;
        }

        if (!freeze.isFrozen(event.getPlayer().getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        // Allow camera rotation only.
        if (from.getWorld() != null && to.getWorld() != null && !from.getWorld().equals(to.getWorld())) {
            // Don't allow world changes via movement while frozen.
            event.setTo(from);
            return;
        }

        final double eps = 1.0E-6;
        boolean moved = Math.abs(from.getX() - to.getX()) > eps
            || Math.abs(from.getY() - to.getY()) > eps
            || Math.abs(from.getZ() - to.getZ()) > eps;
        if (!moved) {
            return;
        }

        Location corrected = from.clone();
        corrected.setYaw(to.getYaw());
        corrected.setPitch(to.getPitch());
        event.setCancelled(true);
        event.setTo(corrected);
    }
}
