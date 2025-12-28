package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Opens a player/racer selection inventory and configures session delegates.
 *
 * additionalInfo keys:
 * - inventoryId: inventory template id to open (e.g. racing_racers)
 * - successDelegate: map key for RacingPlayerSelectionSelectAction
 * - cancelDelegate: map key for RacingPlayerSelectionCancelAction
 */
public final class RacingOpenPlayerSelectionAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingOpenPlayerSelectionAction(Logger logger, RaceUiApiProvider uiProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.uiProvider = uiProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        String inventoryId = context.additionalInfo().get("inventoryId");
        String successDelegate = context.additionalInfo().get("successDelegate");
        String cancelDelegate = context.additionalInfo().get("cancelDelegate");

        if (inventoryId == null || inventoryId.isBlank()) {
            player.sendMessage("Missing additionalInfo.inventoryId");
            return InteractionResult.continueChain();
        }
        if (successDelegate == null || successDelegate.isBlank()) {
            player.sendMessage("Missing additionalInfo.successDelegate");
            return InteractionResult.continueChain();
        }
        if (cancelDelegate == null || cancelDelegate.isBlank()) {
            player.sendMessage("Missing additionalInfo.cancelDelegate");
            return InteractionResult.continueChain();
        }

        try {
            RaceUiApi ui = uiProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (ui == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return InteractionResult.continueChain();
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            session.set(RacingSessionKeys.PLAYER_SELECTION_SUCCESS_DELEGATE, successDelegate);
            session.set(RacingSessionKeys.PLAYER_SELECTION_CANCEL_DELEGATE, cancelDelegate);

            // Reset list page when opening selection.
            session.set(RacingSessionKeys.RACERS_PAGE, 0);

            ui.openEditor(player, inventoryId);
        } catch (Exception e) {
            logger.fine(() -> "RacingOpenPlayerSelectionAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
