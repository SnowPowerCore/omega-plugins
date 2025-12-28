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
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

public final class RacingAddTeamAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingAddTeamAction(Logger logger, RaceUiApiProvider apiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

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

            int idx = data.addTeam(raceName);
            if (idx < 0) {
                player.sendMessage("Failed to add team.");
                return InteractionResult.continueChain();
            }

            session.set(RacingSessionKeys.SELECTED_TEAM_INDEX, idx);
            session.set(RacingSessionKeys.RACERS_PAGE, 0);

            api.openEditor(player, RacingUiIds.TEAM_EDIT, true);
        } catch (Exception e) {
            logger.fine(() -> "RacingAddTeamAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
