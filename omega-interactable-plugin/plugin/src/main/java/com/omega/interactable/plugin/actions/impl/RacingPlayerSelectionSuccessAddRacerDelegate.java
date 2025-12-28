package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

public final class RacingPlayerSelectionSuccessAddRacerDelegate implements RacingPlayerSelectionSuccessDelegate {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingPlayerSelectionSuccessAddRacerDelegate(Logger logger, RaceUiApiProvider uiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.uiProvider = uiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public void onSelect(InteractionContext context, String uuid) {
        Player player = context.player();

        try {
            RaceUiApi ui = uiProvider.resolve().orElse(null);
            RaceDataApi data = dataProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (ui == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return;
            }
            if (data == null) {
                player.sendMessage("OmegaRacing data service not available.");
                return;
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return;
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String raceName = session.getString(RacingSessionKeys.EDITING_RACE_NAME);
            if (raceName == null || raceName.isBlank()) {
                player.sendMessage("No race selected.");
                return;
            }

            int teamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
            data.addRacerToTeamUnique(raceName, teamIndex, uuid);

            // Navigate back to the team editor.
            if (session.backStack().isEmpty()) {
                ui.openEditor(player, RacingUiIds.TEAM_EDIT, false);
            } else {
                ui.back(player);
            }
        } catch (Exception e) {
            logger.fine(() -> "RacingPlayerSelectionSuccessAddRacerDelegate failed: " + e.getMessage());
        }
    }
}
