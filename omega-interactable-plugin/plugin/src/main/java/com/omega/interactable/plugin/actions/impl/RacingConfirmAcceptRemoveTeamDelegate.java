package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.constants.RacingConfirmKinds;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.model.RaceDefinition;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Optional;

public final class RacingConfirmAcceptRemoveTeamDelegate implements RacingConfirmAcceptDelegate {

    @Inject
    public RacingConfirmAcceptRemoveTeamDelegate() {
    }

    @Override
    public String key() {
        return RacingConfirmKinds.REMOVE_TEAM;
    }

    @Override
    public void accept(Player player, RaceUiApi ui, RaceDataApi data, RacingSessionsApi sessionsApi, RacingSession session, String raceName, int teamIndex, String racerUuid, String returnInventoryId) {
        boolean ok = data.removeTeam(raceName, teamIndex);
        if (ok) {
            Optional<RaceDefinition> raceOpt = data.get(raceName);
            raceOpt.ifPresent(race -> {
                if (race.getTeams().isEmpty()) {
                    session.set(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
                } else {
                    session.set(RacingSessionKeys.SELECTED_TEAM_INDEX, Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1));
                }
            });
            player.sendMessage("Removed team.");
        }
        ui.openEditor(player, RacingUiIds.TEAMS, false, null);
    }
}
