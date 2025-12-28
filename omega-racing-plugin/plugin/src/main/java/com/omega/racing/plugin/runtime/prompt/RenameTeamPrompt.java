package com.omega.racing.plugin.runtime.prompt;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.PromptKind;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.plugin.runtime.ui.TeamEditInventoryUi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;


public final class RenameTeamPrompt implements ChatPrompt {

    private final RaceDataApi raceData;

    @Inject
    public RenameTeamPrompt(RaceDataApi raceData) {
        this.raceData = raceData;
    }

    @Override
    public PromptKind kind() {
        return PromptKind.RENAME_TEAM;
    }

    @Override
    public String startMessage(String cancelString) {
        return "Type the new team name in chat (or type '" + cancelString + "' to cancel).";
    }

    @Override
    public String handle(Player player, RacingSessionStore.Session session, String raceName, String input) {
        raceData.get(raceName).ifPresent(race -> {
            int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
            int idx = Math.min(selectedTeamIndex, Math.max(0, race.getTeams().size() - 1));
            boolean ok = raceData.renameTeam(raceName, idx, input);

            player.sendMessage(ok ? "Team renamed." : "Rename failed.");
        });
        return TeamEditInventoryUi.ID;
    }
}
