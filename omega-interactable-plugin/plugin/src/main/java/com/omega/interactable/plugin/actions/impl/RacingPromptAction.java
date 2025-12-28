package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.racing.core.api.RacingPromptsApi;
import com.omega.racing.core.model.PromptKind;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Starts a chat prompt in OmegaRacing.
 *
 * additionalInfo keys:
 * - promptKind: RENAME_RACE | RENAME_TEAM | REMOVE_RACER
 */
public final class RacingPromptAction implements InteractableAction {

    private final Logger logger;
    private final RacingPromptsApiProvider promptsProvider;

    @Inject
    public RacingPromptAction(Logger logger, RacingPromptsApiProvider promptsProvider) {
        this.logger = logger;
        this.promptsProvider = promptsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();
        String promptKind = context.additionalInfo().get("promptKind");
        if (promptKind == null || promptKind.isBlank()) {
            player.sendMessage("Missing additionalInfo.promptKind");
            return InteractionResult.continueChain();
        }

        try {
            PromptKind kind;
            try {
                kind = PromptKind.valueOf(promptKind.trim().toUpperCase());
            } catch (Exception e) {
                player.sendMessage("Unknown prompt: " + promptKind);
                return InteractionResult.continueChain();
            }

            RacingPromptsApi prompts = promptsProvider.resolve().orElse(null);
            if (prompts == null) {
                player.sendMessage("OmegaRacing prompts service not available.");
                return InteractionResult.continueChain();
            }

            prompts.startPrompt(player, kind);
        } catch (Exception e) {
            logger.fine(() -> "RacingPromptAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
