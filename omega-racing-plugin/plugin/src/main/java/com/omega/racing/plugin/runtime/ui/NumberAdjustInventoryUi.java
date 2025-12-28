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
 * Generic +/− number editor inventory.
 *
 * This single renderer is registered for both round-count editors:
 * - racing_qualification_rounds
 * - racing_race_rounds
 *
 * The opening item configures behavior via session delegates.
 */
public final class NumberAdjustInventoryUi implements InventoryRenderer {

    public static final String SECTIONS_ID = "racing_sections";
    public static final String QUALIFICATION_LAPS_ID = "racing_qualification_laps";
    public static final String RACE_LAPS_ID = "racing_race_laps";
    public static final String POSITIONS_ID = "racing_positions";

    private final UiComponents ui;

    @Inject
    public NumberAdjustInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return SECTIONS_ID;
    }

    @Override
    public void register(Map<String, InventoryRenderer> registry) {
        registry.put(SECTIONS_ID, this);
        registry.put(QUALIFICATION_LAPS_ID, this);
        registry.put(RACE_LAPS_ID, this);
        registry.put(POSITIONS_ID, this);
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        int value;
        if (race == null) {
            value = 1;
        } else if (POSITIONS_ID.equals(inventoryId)) {
            value = race.getPositions();
        } else if (SECTIONS_ID.equals(inventoryId)) {
            value = race.getSections();
        } else if (RACE_LAPS_ID.equals(inventoryId)) {
            value = race.getRace().getLaps();
        } else if (QUALIFICATION_LAPS_ID.equals(inventoryId)) {
            value = race.getQualification().getLaps();
        } else {
            // Default to sections.
            value = race.getSections();
        }

        int currentSlot = ui.findFirstSlotWithReferenceId(inv, "racing:ui/number_adjust_current");
        if (currentSlot < 0) {
            currentSlot = 13;
        }

        ItemStack current = inv.getItem(currentSlot);
        if (current == null) {
            current = new ItemStack(Material.PAPER);
        }
        ItemMeta meta = current.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eCurrent: &f" + value));
            current.setItemMeta(meta);
        }
        ui.set(inv, currentSlot, current);
    }
}
