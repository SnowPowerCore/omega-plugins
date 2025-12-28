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
 * Cancels the currently pending confirmation in OmegaRacing.
 */
public final class RacingConfirmCancelAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingConfirmCancelAction(Logger logger, RaceUiApiProvider apiProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

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
            String returnId = session.getString(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID);

            session.set(RacingSessionKeys.CONFIRM_KIND, null);
            session.set(RacingSessionKeys.CONFIRM_RACE_NAME, null);
            session.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
            session.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
            session.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, null);
            session.set(RacingSessionKeys.CONFIRM_TITLE, null);
            session.set(RacingSessionKeys.CONFIRM_LORE, null);

            String target = returnId != null ? returnId : RacingUiIds.MAIN;
            api.openEditor(player, target, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingConfirmCancelAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
