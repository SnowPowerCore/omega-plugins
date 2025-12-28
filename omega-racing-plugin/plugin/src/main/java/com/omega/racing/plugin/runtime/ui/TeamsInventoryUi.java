package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.core.api.RacingSessionKeys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

public final class TeamsInventoryUi implements InventoryRenderer {

    public static final String ID = "racing_teams";

    private final UiComponents ui;
    private final TeamUiDecorator teamUi;

    @Inject
    public TeamsInventoryUi(UiComponents ui, TeamUiDecorator teamUi) {
        this.ui = ui;
        this.teamUi = teamUi;
    }

    @Override
    public String inventoryId() {
        return ID;
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        // Match racer list layout: render list entries into top 45 slots.
        List<Integer> slots = ui.findEmptySlots(inv, 0, Math.min(44, inv.getSize() - 1));
        ui.clearSlots(inv, slots);
        int pageSize = slots.size();
        int total = race.getTeams().size();
        int maxPage = total == 0 ? 0 : (total - 1) / pageSize;
        int page = session.getInt(RacingSessionKeys.TEAMS_PAGE, 0);
        if (page > maxPage) {
            page = maxPage;
            session.set(RacingSessionKeys.TEAMS_PAGE, page);
        }

        int start = page * pageSize;
        int end = Math.min(total, start + pageSize);
        int slotIndex = 0;
        for (int i = start; i < end; i++) {
            RaceTeam team = race.getTeams().get(i);
            ItemStack base = inv.getItem(slots.get(slotIndex));
            if (base == null) {
                base = ui.uiItem("racing:ui/team_button", Material.WHITE_WOOL, "Team");
            }
            ItemStack button = ui.withAdditionalInfo(base, Map.of("teamIndex", String.valueOf(i)));
            teamUi.decorateTeamButton(button, team, i);
            ui.set(inv, slots.get(slotIndex), button);
            slotIndex++;
        }
    }
}
