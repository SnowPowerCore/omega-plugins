package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractionContext;
import com.omega.racing.core.api.RaceUiApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

public final class RacingNumberAdjustCancelBackDelegate implements RacingNumberAdjustCancelDelegate {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;

    @Inject
    public RacingNumberAdjustCancelBackDelegate(Logger logger, RaceUiApiProvider uiProvider) {
        this.logger = logger;
        this.uiProvider = uiProvider;
    }

    @Override
    public void onCancel(InteractionContext context) {
        Player player = context.player();

        try {
            RaceUiApi ui = uiProvider.resolve().orElse(null);
            if (ui == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return;
            }
            ui.back(player);
        } catch (Exception e) {
            logger.fine(() -> "RacingNumberAdjustCancelBackDelegate failed: " + e.getMessage());
        }
    }
}
