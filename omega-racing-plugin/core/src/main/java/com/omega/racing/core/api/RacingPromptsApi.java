package com.omega.racing.core.api;

import com.omega.racing.core.model.PromptKind;
import org.bukkit.entity.Player;

/**
 * Starts/cancels chat-based prompts.
 *
 * This is separate from {@link RaceUiApi} so that other plugins can initiate prompts
 * without depending on higher-level UI workflow methods.
 */
public interface RacingPromptsApi {

    void startPrompt(Player player, PromptKind promptKind);
}
