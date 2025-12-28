package com.omega.racing.plugin.runtime;

import com.omega.racing.core.api.RacingPromptsApi;
import com.omega.racing.core.model.PromptKind;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class RacingPromptService implements RacingPromptsApi {

    private final RaceUiService ui;

    @Inject
    public RacingPromptService(RaceUiService ui) {
        this.ui = ui;
    }

    @Override
    public void startPrompt(Player player, PromptKind promptKind) {
        if (player == null || promptKind == null) {
            return;
        }
        ui.prompt(player, promptKind.name());
    }
}
