package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Deque;
import java.util.logging.Logger;

public final class RacingBackAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingBackAction(Logger logger, RaceUiApiProvider apiProvider, RacingSessionsApiProvider sessionsProvider) {
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
            Deque<String> stack = session.backStack();
            if (stack.isEmpty()) {
                player.closeInventory();
                return InteractionResult.continueChain();
            }

            String prev = stack.pop();

            api.openEditor(player, prev, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingBackAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
