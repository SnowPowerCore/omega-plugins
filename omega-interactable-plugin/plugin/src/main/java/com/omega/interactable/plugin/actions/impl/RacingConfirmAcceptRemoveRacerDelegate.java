package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.constants.RacingConfirmKinds;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class RacingConfirmAcceptRemoveRacerDelegate implements RacingConfirmAcceptDelegate {

    @Inject
    public RacingConfirmAcceptRemoveRacerDelegate() {
    }

    @Override
    public String key() {
        return RacingConfirmKinds.REMOVE_RACER;
    }

    @Override
    public void accept(Player player, RaceUiApi ui, RaceDataApi data, RacingSessionsApi sessionsApi, RacingSession session, String raceName, int teamIndex, String racerUuid, String returnInventoryId) {
        boolean ok = data.removeRacerFromTeam(raceName, teamIndex, racerUuid);
        if (ok) {
            player.sendMessage("Removed racer.");
        }

        ui.openEditor(player, RacingUiIds.TEAM_EDIT, false);
    }
}
