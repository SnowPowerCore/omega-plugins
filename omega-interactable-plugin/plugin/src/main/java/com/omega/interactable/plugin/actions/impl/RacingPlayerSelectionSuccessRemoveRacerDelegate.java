package com.omega.interactable.plugin.actions.impl;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.interactable.core.api.InteractionContext;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public final class RacingPlayerSelectionSuccessRemoveRacerDelegate implements RacingPlayerSelectionSuccessDelegate {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingPlayerSelectionSuccessRemoveRacerDelegate(Logger logger, RaceUiApiProvider uiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
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

            int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
            Optional<RaceDefinition> raceOpt = data.get(raceName);
            if (raceOpt.isEmpty() || raceOpt.get().getTeams().isEmpty()) {
                player.sendMessage("No team selected.");
                return;
            }

            int idx = Math.min(Math.max(0, selectedTeamIndex), raceOpt.get().getTeams().size() - 1);
            boolean ok = data.removeRacerFromTeam(raceName, idx, uuid);
            player.sendMessage(ok ? "Racer removed." : "Remove failed.");

            String invId = session.getString(RacingSessionKeys.CURRENT_INVENTORY_ID);
            ui.openEditor(player, invId == null || invId.isBlank() ? "racing_remove_racer" : invId, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingPlayerSelectionSuccessRemoveRacerDelegate failed: " + e.getMessage());
        }
    }
}
