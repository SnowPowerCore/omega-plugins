package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import org.bukkit.ChatColor;

import jakarta.inject.Inject;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Sends a chat message to the player.
 *
 * Uses item-loader additionalInfo key: "interactMessage" (string).
 */
public final class SendMessageAction implements InteractableAction {

    private final Logger logger;

    @Inject
    public SendMessageAction(Logger logger) {
        this.logger = logger;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        String raw = context.additionalInfo().getOrDefault("interactMessage", "&7(interacted)");
        String msg = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(raw, ""));
        if (!msg.isBlank()) {
            context.player().sendMessage(msg);
        }
        // Example of using injected dependency (kept minimal/noisy logging avoided).
        logger.fine(() -> "SendMessageAction executed for " + context.player().getName());
        return InteractionResult.continueChain();
    }
}
