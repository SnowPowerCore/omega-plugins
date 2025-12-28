package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Generic +/− action for number adjust inventories.
 *
 * additionalInfo keys:
 * - delta: integer
 */
public final class RacingNumberAdjustAction implements InteractableAction {

    private final Logger logger;
    private final RacingSessionsApiProvider sessionsProvider;
    private final Map<String, RacingNumberAdjustSuccessDelegate> delegates;

    @Inject
    public RacingNumberAdjustAction(Logger logger, RacingSessionsApiProvider sessionsProvider, Map<String, RacingNumberAdjustSuccessDelegate> delegates) {
        this.logger = logger;
        this.sessionsProvider = sessionsProvider;
        this.delegates = delegates;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        String deltaRaw = context.additionalInfo().get("delta");
        if (deltaRaw == null || deltaRaw.isBlank()) {
            player.sendMessage("Missing additionalInfo.delta");
            return InteractionResult.continueChain();
        }

        int delta;
        try {
            delta = Integer.parseInt(deltaRaw.trim());
        } catch (Exception e) {
            player.sendMessage("Invalid additionalInfo.delta");
            return InteractionResult.continueChain();
        }

        try {
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String kind = session.getString(RacingSessionKeys.NUMBER_ADJUST_SUCCESS_DELEGATE);
            if (kind == null || kind.isBlank()) {
                player.sendMessage("Number adjust not configured (missing success delegate)." );
                return InteractionResult.continueChain();
            }

            RacingNumberAdjustSuccessDelegate delegate = delegates.get(kind);
            if (delegate == null) {
                player.sendMessage("Unknown success delegate: " + kind);
                return InteractionResult.continueChain();
            }

            delegate.adjust(context, delta);
        } catch (Exception e) {
            logger.fine(() -> "RacingNumberAdjustAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
