package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
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
import java.util.logging.Logger;

/**
 * Removes a racer from the currently selected team.
 *
 * additionalInfo keys:
 * - racerUuid: UUID string for the racer to remove
 */
public final class RacingRemoveRacerFromTeamAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingRemoveRacerFromTeamAction(Logger logger, RaceUiApiProvider apiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        String racerUuid = context.additionalInfo().get("racerUuid");
        if (racerUuid == null || racerUuid.isBlank()) {
            player.sendMessage("Missing additionalInfo.racerUuid");
            return InteractionResult.continueChain();
        }

        try {
            RaceUiApi api = apiProvider.resolve().orElse(null);
            RaceDataApi data = dataProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (api == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return InteractionResult.continueChain();
            }
            if (data == null) {
                player.sendMessage("OmegaRacing data service not available.");
                return InteractionResult.continueChain();
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String raceName = session.getString(RacingSessionKeys.EDITING_RACE_NAME);
            if (raceName == null || raceName.isBlank()) {
                player.sendMessage("No race selected.");
                return InteractionResult.continueChain();
            }

            int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);

            Optional<RaceDefinition> raceOpt = data.get(raceName);
            if (raceOpt.isEmpty() || raceOpt.get().getTeams().isEmpty()) {
                player.sendMessage("No team selected.");
                return InteractionResult.continueChain();
            }

            int idx = Math.min(Math.max(0, selectedTeamIndex), raceOpt.get().getTeams().size() - 1);
            boolean ok = data.removeRacerFromTeam(raceName, idx, racerUuid);
            player.sendMessage(ok ? "Racer removed." : "Remove failed.");

            api.openEditor(player, RacingUiIds.REMOVE_RACER, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingRemoveRacerFromTeamAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
