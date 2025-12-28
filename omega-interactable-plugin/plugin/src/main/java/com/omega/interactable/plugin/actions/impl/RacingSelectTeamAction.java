package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Selects a team index for the current race edit session.
 *
 * additionalInfo keys:
 * - teamIndex: integer
 */
public final class RacingSelectTeamAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingSelectTeamAction(Logger logger, RaceUiApiProvider apiProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();
        String raw = context.additionalInfo().get("teamIndex");
        if (raw == null || raw.isBlank()) {
            player.sendMessage("Missing additionalInfo.teamIndex");
            return InteractionResult.continueChain();
        }

        int idx;
        try {
            idx = Integer.parseInt(raw.trim());
        } catch (Exception e) {
            player.sendMessage("Invalid teamIndex: " + raw);
            return InteractionResult.continueChain();
        }

        try {
            RaceUiApi api = apiProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (api == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return InteractionResult.continueChain();
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            int selectedTeamIndex = Math.max(0, idx);
            session.set(RacingSessionKeys.SELECTED_TEAM_INDEX, selectedTeamIndex);
            session.set(RacingSessionKeys.RACERS_PAGE, 0);

            api.openEditor(player, RacingUiIds.TEAM_EDIT, true);
        } catch (Exception e) {
            logger.fine(() -> "RacingSelectTeamAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
