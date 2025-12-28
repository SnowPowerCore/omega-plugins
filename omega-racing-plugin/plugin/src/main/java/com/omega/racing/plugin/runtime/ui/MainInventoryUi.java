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

public final class MainInventoryUi implements InventoryRenderer {

    public static final String ID = "racing_main";

    private final UiComponents ui;

    @Inject
    public MainInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return ID;
    }

    @Override
    public String defaultTitleOverride(RacingSessionStore.Session session, String editingRaceName, RaceDefinition race) {
        if (editingRaceName == null || editingRaceName.isBlank()) {
            return "&bRace Editor";
        }
        return "&bRace Editor: &f" + editingRaceName;
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        // Static layout is provided by racing-invs.json via OmegaInvHolder.
        if (inv == null || race == null) {
            return;
        }

        updateSingleLineLore(inv,
            "racing:ui/open_sections",
            "racing_sections",
            "Sections:",
            ChatColor.GRAY + "Sections: " + ChatColor.WHITE + Math.max(1, race.getSections())
        );

        updateSingleLineLore(inv,
            "racing:ui/open_qualification_settings",
            "racing_qualification_settings",
            "Qualification:",
            ChatColor.GRAY + "Qualification: " + ChatColor.WHITE
                + "Laps " + Math.max(1, race.getQualification().getLaps())
                + ChatColor.GRAY + ", Limit " + ChatColor.WHITE + formatTimeLimitSeconds(race.getQualification().getTimeLimitSeconds())
        );

        updateSingleLineLore(inv,
            "racing:ui/open_race_laps_direct",
            "racing_race_laps",
            "Laps:",
            ChatColor.GRAY + "Laps: " + ChatColor.WHITE + Math.max(1, race.getRace().getLaps())
        );

        updateSingleLineLore(inv,
            "racing:ui/open_positions",
            "racing_positions",
            "Positions:",
            ChatColor.GRAY + "Positions: " + ChatColor.WHITE + Math.max(1, race.getPositions())
        );
    }

    private void updateSingleLineLore(Inventory inv, String referenceId, String inventoryId, String stripPrefix, String newLine) {
        int slot = ui.findFirstSlotWithReferenceId(inv, referenceId);
        if (slot < 0) {
            slot = ui.findFirstSlotWithAdditionalInfo(inv, "inventoryId", inventoryId);
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
                if (stripped != null && stripped.startsWith(stripPrefix)) {
                    continue;
                }
                lore.add(line);
            }
        }
        lore.add(newLine);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        ui.set(inv, slot, stack);
    }

    private static String formatTimeLimitSeconds(int seconds) {
        if (seconds <= 0) {
            return "Unlimited";
        }
        return seconds + "s";
    }

    // Stage lore removed: sections is now race-level, and laps are edited directly.
}
