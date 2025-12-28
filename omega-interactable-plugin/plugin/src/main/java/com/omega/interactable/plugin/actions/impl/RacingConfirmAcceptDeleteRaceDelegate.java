package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.constants.RacingConfirmKinds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class RacingConfirmAcceptDeleteRaceDelegate implements RacingConfirmAcceptDelegate {

    @Inject
    public RacingConfirmAcceptDeleteRaceDelegate() {
    }

    @Override
    public String key() {
        return RacingConfirmKinds.DELETE_RACE;
    }

    @Override
    public void accept(Player player, RaceUiApi ui, RaceDataApi data, RacingSessionsApi sessionsApi, RacingSession session, String raceName, int teamIndex, String racerUuid, String returnInventoryId) {
        boolean ok = data.delete(raceName);
        player.sendMessage(ok ? ("Deleted race: " + raceName) : "Delete failed.");
        session.set(RacingSessionKeys.EDITING_RACE_NAME, null);
        player.closeInventory();
    }
}
