package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.Map;

/**
 * Generic +/− time editor inventory (seconds), rendered as mm:ss.
 *
 * Uses the existing number adjust action pipeline (delta in seconds).
 */
public final class TimeAdjustInventoryUi implements InventoryRenderer {

    public static final String FREE_PRACTICE_TIME_LIMIT_ID = "racing_free_practice_time_limit";
    public static final String QUALIFICATION_TIME_LIMIT_ID = "racing_qualification_time_limit";

    private final UiComponents ui;

    @Inject
    public TimeAdjustInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return FREE_PRACTICE_TIME_LIMIT_ID;
    }

    @Override
    public void register(Map<String, InventoryRenderer> registry) {
        registry.put(FREE_PRACTICE_TIME_LIMIT_ID, this);
        registry.put(QUALIFICATION_TIME_LIMIT_ID, this);
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        int seconds;
        if (race == null) {
            seconds = 0;
        } else if (QUALIFICATION_TIME_LIMIT_ID.equals(inventoryId)) {
            seconds = race.getQualification().getTimeLimitSeconds();
        } else {
            seconds = race.getFreePractice().getTimeLimitSeconds();
        }

        int currentSlot = ui.findFirstSlotWithReferenceId(inv, "racing:ui/time_adjust_current");
        if (currentSlot < 0) {
            currentSlot = 13;
        }

        ItemStack current = inv.getItem(currentSlot);
        if (current == null) {
            current = new ItemStack(Material.PAPER);
        }
        ItemMeta meta = current.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eTime Limit: &f" + formatTime(seconds)));
            current.setItemMeta(meta);
        }
        ui.set(inv, currentSlot, current);
    }

    private static String formatTime(int seconds) {
        int s = Math.max(0, seconds);
        if (s == 0) {
            return "Unlimited";
        }
        int mins = s / 60;
        int rem = s % 60;
        return String.format("%d:%02d", mins, rem);
    }
}
