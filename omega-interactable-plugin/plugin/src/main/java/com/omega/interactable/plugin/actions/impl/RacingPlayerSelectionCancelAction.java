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

public final class RacingPlayerSelectionCancelAction implements InteractableAction {

    private final Logger logger;
    private final RacingSessionsApiProvider sessionsProvider;
    private final Map<String, RacingPlayerSelectionCancelDelegate> delegates;

    @Inject
    public RacingPlayerSelectionCancelAction(Logger logger, RacingSessionsApiProvider sessionsProvider, Map<String, RacingPlayerSelectionCancelDelegate> delegates) {
        this.logger = logger;
        this.sessionsProvider = sessionsProvider;
        this.delegates = delegates;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

        try {
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String kind = session.getString(RacingSessionKeys.PLAYER_SELECTION_CANCEL_DELEGATE);
            if (kind == null || kind.isBlank()) {
                player.sendMessage("Selection not configured (missing cancel delegate)." );
                return InteractionResult.continueChain();
            }

            RacingPlayerSelectionCancelDelegate delegate = delegates.get(kind);
            if (delegate == null) {
                player.sendMessage("Unknown cancel delegate: " + kind);
                return InteractionResult.continueChain();
            }

            delegate.onCancel(context);
        } catch (Exception e) {
            logger.fine(() -> "RacingPlayerSelectionCancelAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }
}
