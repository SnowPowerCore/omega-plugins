package com.omega.racing.plugin.listener;

import com.omega.racing.plugin.runtime.stage.RaceStageService;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import jakarta.inject.Inject;

public final class FreePracticeBoatListener implements Listener {

    private final RaceStageService stages;

    @Inject
    public FreePracticeBoatListener(RaceStageService stages) {
        this.stages = stages;
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event == null) {
            return;
        }
        if (!(event.getVehicle() instanceof Boat boat)) {
            return;
        }
        if (!stages.isFreePracticeBoat(boat.getUniqueId())) {
            return;
        }
        stages.onFreePracticeBoatMove(boat, event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event == null) {
            return;
        }
        if (!(event.getVehicle() instanceof Boat boat)) {
            return;
        }
        if (!(event.getExited() instanceof Player)) {
            return;
        }
        if (!stages.isFreePracticeBoat(boat.getUniqueId())) {
            return;
        }
        // Keep racers seated during Free Practice.
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof Boat boat)) {
            return;
        }
        if (!stages.isFreePracticeBoat(boat.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        stages.onPlayerQuit(player.getUniqueId());
    }
}
