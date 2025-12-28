package com.omega.racing.plugin.runtime.prompt;

import com.omega.racing.core.model.PromptKind;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Chat prompt business logic.
 *
 * Prompt lifecycle (timeout/cancel/actionbar) remains in the UI service; each prompt
 * owns its own start message + input handling.
 */
public interface ChatPrompt {

    PromptKind kind();

    default void register(Map<PromptKind, ChatPrompt> registry) {
        registry.put(kind(), this);
    }

    String startMessage(String cancelString);

    /**
     * @return inventory id to open afterwards (null means return to prior inventory)
     */
    String handle(Player player, RacingSessionStore.Session session, String raceName, String input);
}
