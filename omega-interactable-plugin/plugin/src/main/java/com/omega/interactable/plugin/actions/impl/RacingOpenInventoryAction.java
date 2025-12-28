package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.racing.core.api.RaceUiApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Opens a racing editor inventory via OmegaRacing's RaceUiApi.
 *
 * additionalInfo keys:
 * - inventoryId: inv-holder template id to open (e.g. racing_main)
 */
public final class RacingOpenInventoryAction implements InteractableAction {
    private final Logger logger;
    private final RaceUiApiProvider apiProvider;

    @Inject
    public RacingOpenInventoryAction(Logger logger, RaceUiApiProvider apiProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();
        String inventoryId = context.additionalInfo().get("inventoryId");
        if (inventoryId == null || inventoryId.isBlank()) {
            player.sendMessage("Missing additionalInfo.inventoryId");
            return InteractionResult.continueChain();
        }

        try {
            RaceUiApi api = apiProvider.resolve().orElse(null);
            if (api == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return InteractionResult.continueChain();
            }

            api.openEditor(player, inventoryId);
        } catch (Exception e) {
            logger.fine(() -> "RacingOpenInventoryAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
