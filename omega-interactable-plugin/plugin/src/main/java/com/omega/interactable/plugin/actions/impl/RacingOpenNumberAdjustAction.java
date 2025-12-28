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
 * Opens a number adjust inventory and configures session delegates.
 *
 * additionalInfo keys:
 * - inventoryId: inventory template id to open
 * - successDelegate: map key for RacingNumberAdjustAction
 * - cancelDelegate: map key for RacingNumberAdjustCancelAction
 */
public final class RacingOpenNumberAdjustAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingOpenNumberAdjustAction(Logger logger, RaceUiApiProvider uiProvider, RacingSessionsApiProvider sessionsProvider) {
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
            session.set(RacingSessionKeys.NUMBER_ADJUST_SUCCESS_DELEGATE, successDelegate);
            session.set(RacingSessionKeys.NUMBER_ADJUST_CANCEL_DELEGATE, cancelDelegate);

            ui.openEditor(player, inventoryId);
        } catch (Exception e) {
            logger.fine(() -> "RacingOpenNumberAdjustAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
