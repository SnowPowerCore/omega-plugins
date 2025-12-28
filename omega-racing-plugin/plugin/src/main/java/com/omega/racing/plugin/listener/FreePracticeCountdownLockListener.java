package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.stage.RaceStageService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import jakarta.inject.Inject;

public final class FreePracticeCountdownLockListener implements Listener {

    private final RaceStageService stages;

    @Inject
    public FreePracticeCountdownLockListener(RaceStageService stages) {
        this.stages = stages;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (!stages.isFreePracticeCountdownLocked(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from == null || to == null) {
            return;
        }

        // Allow looking around; block any XYZ movement.
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location locked = from.clone();
            locked.setYaw(to.getYaw());
            locked.setPitch(to.getPitch());
            event.setTo(locked);
            try {
                player.setVelocity(new Vector(0, 0, 0));
            } catch (Exception ignored) {
            }
        }
    }
}
