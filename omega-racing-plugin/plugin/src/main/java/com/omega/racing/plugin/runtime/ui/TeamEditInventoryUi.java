package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public final class TeamEditInventoryUi implements InventoryRenderer {

    public static final String ID = "racing_team_edit";

    private final UiComponents ui;

    @Inject
    public TeamEditInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return ID;
    }

    @Override
    public String defaultTitleOverride(RacingSessionStore.Session session, String editingRaceName, RaceDefinition race) {
        if (session == null || race == null || race.getTeams().isEmpty()) {
            return "&bTeam Editor";
        }

        int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
        int idx = Math.min(Math.max(0, selectedTeamIndex), race.getTeams().size() - 1);
        String teamName = race.getTeams().get(idx).getName();
        if (teamName == null || teamName.isBlank()) {
            return "&bTeam Editor";
        }
        return "&bTeam Editor: &f" + teamName;
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
        int idx = Math.min(Math.max(0, selectedTeamIndex), Math.max(0, race.getTeams().size() - 1));
        RaceTeam team = race.getTeams().isEmpty() ? null : race.getTeams().get(idx);

        int infoSlot = ui.findFirstSlotWithReferenceId(inv, "racing:ui/team_info");
        if (infoSlot < 0) {
            infoSlot = 4;
        }
        if (team != null) {
            ItemStack info = inv.getItem(infoSlot);
            if (info == null) {
                info = new ItemStack(Material.PAPER);
            }
            ItemMeta meta = info.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "Team: " + team.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Suit: " + SuitColorDisplay.display(team.getSuitColorHex()));
                lore.add(ChatColor.GRAY + "Racers: " + team.getRacers().size());
                meta.setLore(lore);
                info.setItemMeta(meta);
            }
            ui.set(inv, infoSlot, info);
        } else {
            ui.set(inv, infoSlot, null);
        }
    }
}
