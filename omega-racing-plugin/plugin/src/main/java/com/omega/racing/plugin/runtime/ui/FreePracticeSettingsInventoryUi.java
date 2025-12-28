package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FreePracticeSettingsInventoryUi implements InventoryRenderer {

    public static final String FREE_PRACTICE_SETTINGS_ID = "racing_free_practice_settings";

    private final UiComponents ui;

    @Inject
    public FreePracticeSettingsInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return FREE_PRACTICE_SETTINGS_ID;
    }

    @Override
    public void register(Map<String, InventoryRenderer> registry) {
        registry.put(FREE_PRACTICE_SETTINGS_ID, this);
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        if (inv == null || race == null) {
            return;
        }

        int seconds = race.getFreePractice().getTimeLimitSeconds();
        updateLore(inv,
            "racing:ui/open_free_practice_time_limit",
            "inventoryId",
            "racing_free_practice_time_limit",
            "Current: " + formatTimeLimit(seconds)
        );
    }

    private void updateLore(Inventory inv, String referenceId, String additionalInfoKey, String additionalInfoExpected, String currentLine) {
        int slot = ui.findFirstSlotWithReferenceId(inv, referenceId);
        if (slot < 0) {
            slot = ui.findFirstSlotWithAdditionalInfo(inv, additionalInfoKey, additionalInfoExpected);
        }
        if (slot < 0) {
            return;
        }

        ItemStack stack = inv.getItem(slot);
        if (stack == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = new ArrayList<>();
        List<String> existing = meta.getLore();
        if (existing != null) {
            for (String line : existing) {
                if (line == null) {
                    continue;
                }
                String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', line));
                if (stripped != null && stripped.startsWith("Current:")) {
                    continue;
                }
                lore.add(line);
            }
        }

        lore.add(ChatColor.GRAY + currentLine);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        ui.set(inv, slot, stack);
    }

    private static String formatTimeLimit(int seconds) {
        int s = Math.max(0, seconds);
        if (s == 0) {
            return "Unlimited";
        }
        int mins = s / 60;
        int rem = s % 60;
        return String.format("%d:%02d", mins, rem);
    }
}
