package com.omega.racing.plugin.listener;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.PromptKind;
import com.omega.racing.plugin.runtime.RaceUiService;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import jakarta.inject.Inject;

public final class RacingChatListener implements Listener {

    private final JavaPlugin plugin;
    private final RacingSessionStore sessions;
    private final RaceUiService ui;

    @Inject
    public RacingChatListener(JavaPlugin plugin, RacingSessionStore sessions, RaceUiService ui) {
        this.plugin = plugin;
        this.sessions = sessions;
        this.ui = ui;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        var session = sessions.session(player.getUniqueId());
        PromptKind pending = session.getAs(RacingSessionKeys.PENDING_PROMPT, PromptKind.class);
        if (pending == null) {
            return;
        }

        long expiresAtMillis = session.getLong(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
        if (expiresAtMillis > 0L && System.currentTimeMillis() > expiresAtMillis) {
            session.set(RacingSessionKeys.PENDING_PROMPT, null);
            session.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
            return;
        }

        event.setCancelled(true);
        String msg = event.getMessage();

        // Switch to main thread for Bukkit API access.
        player.getServer().getScheduler().runTask(plugin, () -> ui.handleChat(player, msg));
    }
}
