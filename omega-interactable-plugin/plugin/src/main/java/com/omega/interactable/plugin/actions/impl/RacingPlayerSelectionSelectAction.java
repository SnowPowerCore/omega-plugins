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
 * Generic selection handler for the player/racer selection inventory.
 *
 * additionalInfo keys:
 * - uuid (preferred)
 */
public final class RacingPlayerSelectionSelectAction implements InteractableAction {

    private final Logger logger;
    private final RacingSessionsApiProvider sessionsProvider;
    private final Map<String, RacingPlayerSelectionSuccessDelegate> delegates;

    @Inject
    public RacingPlayerSelectionSelectAction(Logger logger, RacingSessionsApiProvider sessionsProvider, Map<String, RacingPlayerSelectionSuccessDelegate> delegates) {
        this.logger = logger;
        this.sessionsProvider = sessionsProvider;
        this.delegates = delegates;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        String uuid = context.additionalInfo().get("uuid");
        if (uuid == null || uuid.isBlank()) {
            player.sendMessage("Missing additionalInfo.uuid");
            return InteractionResult.continueChain();
        }

        try {
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String kind = session.getString(RacingSessionKeys.PLAYER_SELECTION_SUCCESS_DELEGATE);
            if (kind == null || kind.isBlank()) {
                player.sendMessage("Selection not configured (missing success delegate)." );
                return InteractionResult.continueChain();
            }

            RacingPlayerSelectionSuccessDelegate delegate = delegates.get(kind);
            if (delegate == null) {
                player.sendMessage("Unknown success delegate: " + kind);
                return InteractionResult.continueChain();
            }

            delegate.onSelect(context, uuid);
        } catch (Exception e) {
            logger.fine(() -> "RacingPlayerSelectionSelectAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
