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
 * Changes the teams list page.
 *
 * additionalInfo keys:
 * - delta: integer (e.g. -1, +1)
 */
public final class RacingChangeTeamsPageAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingChangeTeamsPageAction(Logger logger, RaceUiApiProvider apiProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        String raw = context.additionalInfo().get("delta");
        if (raw == null || raw.isBlank()) {
            player.sendMessage("Missing additionalInfo.delta");
            return InteractionResult.continueChain();
        }

        int delta;
        try {
            delta = Integer.parseInt(raw.trim());
        } catch (Exception e) {
            player.sendMessage("Invalid delta: " + raw);
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
            int next = session.getInt(RacingSessionKeys.TEAMS_PAGE, 0) + delta;
            session.set(RacingSessionKeys.TEAMS_PAGE, Math.max(0, next));

            api.openEditor(player, RacingUiIds.TEAMS, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingChangeTeamsPageAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
