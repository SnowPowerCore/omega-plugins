package com.omega.racing.plugin.runtime;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.PromptKind;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.infrastructure.interop.InventoryRegistryResolver;
import com.omega.racing.plugin.runtime.ui.InventoryRenderer;
import com.omega.racing.plugin.runtime.prompt.ChatPrompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

public final class RaceUiService implements RaceUiApi {

    private static final long PROMPT_TIMEOUT_TICKS = 20L * 30L;
    private static final String CANCEL_STRING = "_#cancel";

    private final JavaPlugin plugin;
    private final RaceDataApi raceData;
    private final RacingSessionStore sessions;
    private final InventoryRegistryResolver inventoryRegistry;

    private final Map<String, InventoryRenderer> inventoryRenderers;
    private final Map<PromptKind, ChatPrompt> prompts;

    @Inject
    public RaceUiService(
        JavaPlugin plugin,
        RaceDataApi raceData,
        RacingSessionStore sessions,
        InventoryRegistryResolver inventoryRegistry,
        Set<InventoryRenderer> renderers,
        Set<ChatPrompt> prompts
    ) {
        this.plugin = plugin;
        this.raceData = raceData;
        this.sessions = sessions;
        this.inventoryRegistry = inventoryRegistry;

        Map<String, InventoryRenderer> rendererMap = new HashMap<>();
        for (InventoryRenderer r : renderers) {
            if (r != null) {
                r.register(rendererMap);
            }
        }
        this.inventoryRenderers = Map.copyOf(rendererMap);

        Map<PromptKind, ChatPrompt> promptMap = new EnumMap<>(PromptKind.class);
        for (ChatPrompt prompt : prompts) {
            if (prompt != null) {
                prompt.register(promptMap);
            }
        }
        this.prompts = Map.copyOf(promptMap);
    }

    public void beginEditing(Player player, String raceName) {
        UUID id = player.getUniqueId();
        RacingSessionStore.Session s = sessions.session(id);
        s.set(RacingSessionKeys.EDITING_RACE_NAME, raceName);
        s.set(RacingSessionKeys.PENDING_PROMPT, null);
        s.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
        s.set(RacingSessionKeys.PROMPT_TOKEN, 0L);
        s.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, null);
        s.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
        s.backStack().clear();
        s.set(RacingSessionKeys.CURRENT_INVENTORY_ID, null);
        s.set(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
        s.set(RacingSessionKeys.TEAMS_PAGE, 0);
        s.set(RacingSessionKeys.RACERS_PAGE, 0);
        s.set(RacingSessionKeys.PLAYER_PICKER_PAGE, 0);

        s.set(RacingSessionKeys.CONFIRM_KIND, null);
        s.set(RacingSessionKeys.CONFIRM_RACE_NAME, null);
        s.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
        s.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
        s.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, null);
        s.set(RacingSessionKeys.CONFIRM_TITLE, null);
        s.set(RacingSessionKeys.CONFIRM_LORE, null);
    }

    @Override
    public void openEditor(Player player, String inventoryId) {
        open(player, inventoryId, true, null);
    }

    @Override
    public void openEditor(Player player, String inventoryId, boolean pushHistory) {
        open(player, inventoryId, pushHistory, null);
    }

    @Override
    public void openEditor(Player player, String inventoryId, boolean pushHistory, String titleOverride) {
        open(player, inventoryId, pushHistory, titleOverride);
    }

    public void open(Player player, String inventoryId, boolean pushHistory, String titleOverride) {
        if (player == null) {
            return;
        }

        if (inventoryId == null || inventoryId.isBlank()) {
            return;
        }

        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        String editingRaceName = s.getString(RacingSessionKeys.EDITING_RACE_NAME);
        if (editingRaceName == null || editingRaceName.isBlank()) {
            player.sendMessage("No race selected. Use /race edit <name>.");
            return;
        }

        Optional<RaceDefinition> raceOpt = raceData.get(editingRaceName);
        if (raceOpt.isEmpty()) {
            player.sendMessage("Unknown race: " + editingRaceName);
            return;
        }

        Optional<Inventory> invOpt = inventoryRegistry.create(inventoryId);
        if (invOpt.isEmpty()) {
            player.sendMessage("Missing inventory template: " + inventoryId + " (check OmegaInvHolder invs.json)");
            return;
        }

        Inventory inv = invOpt.get();

        String effectiveTitleOverride = (titleOverride != null && !titleOverride.isBlank()) ? titleOverride : null;
        if (effectiveTitleOverride == null) {
            InventoryRenderer rendererForTitle = inventoryRenderers.get(inventoryId);
            String computedTitleOverride = rendererForTitle == null ? null : rendererForTitle.defaultTitleOverride(s, editingRaceName, raceOpt.get());
            if (computedTitleOverride != null && !computedTitleOverride.isBlank()) {
                effectiveTitleOverride = computedTitleOverride;
            }
        }

        if (effectiveTitleOverride != null && !effectiveTitleOverride.isBlank()) {
            String title = ChatColor.translateAlternateColorCodes('&', effectiveTitleOverride);
            Inventory titled = Bukkit.createInventory(player, inv.getSize(), title);
            titled.setContents(inv.getContents());
            inv = titled;
        }

        String previousInventoryId = s.getString(RacingSessionKeys.CURRENT_INVENTORY_ID);
        if (pushHistory && previousInventoryId != null) {
            s.backStack().push(previousInventoryId);
        }

        // Set current inventory id before rendering so single renderers can branch on context.
        s.set(RacingSessionKeys.CURRENT_INVENTORY_ID, inventoryId);

        render(player, inventoryId, inv);
        player.openInventory(inv);
    }

    @Override
    public void back(Player player) {
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        if (s.backStack().isEmpty()) {
            player.closeInventory();
            return;
        }
        String prev = s.backStack().pop();
        open(player, prev, false, null);
    }

    public void prompt(Player player, String promptKind) {
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        PromptKind parsed;
        try {
            parsed = PromptKind.valueOf(promptKind);
        } catch (Exception e) {
            player.sendMessage("Unknown prompt: " + promptKind);
            return;
        }
        s.set(RacingSessionKeys.PENDING_PROMPT, parsed);

        // Return to the inventory we were in (if any) when the prompt started.
        s.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, s.getString(RacingSessionKeys.CURRENT_INVENTORY_ID));

        // Close inventory so chat input feels intentional.
        player.closeInventory();

        // Cancel any previous actionbar countdown.
        int existingTaskId = s.getInt(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
        if (existingTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(existingTaskId);
            s.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
        }

        long token = s.getLong(RacingSessionKeys.PROMPT_TOKEN, 0L) + 1L;
        s.set(RacingSessionKeys.PROMPT_TOKEN, token);
        s.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, System.currentTimeMillis() + (PROMPT_TIMEOUT_TICKS * 50L));

        UUID playerId = player.getUniqueId();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            RacingSessionStore.Session current = sessions.session(playerId);
            PromptKind currentPrompt = current.getAs(RacingSessionKeys.PENDING_PROMPT, PromptKind.class);
            if (currentPrompt == null) {
                return;
            }
            if (current.getLong(RacingSessionKeys.PROMPT_TOKEN, 0L) != token) {
                return;
            }

            String returnId = current.getString(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID);

            current.set(RacingSessionKeys.PENDING_PROMPT, null);
            current.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
            current.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, null);

            int currentTaskId = current.getInt(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
            if (currentTaskId != -1) {
                plugin.getServer().getScheduler().cancelTask(currentTaskId);
                current.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
            }

            Player online = plugin.getServer().getPlayer(playerId);
            if (online != null && online.isOnline()) {
                online.sendMessage("Prompt timed out (auto-cancelled).");
                sendActionBar(online, "");
                open(online, returnId != null ? returnId : com.omega.racing.plugin.runtime.ui.MainInventoryUi.ID, false, null);
            }
        }, PROMPT_TIMEOUT_TICKS);

        // Actionbar countdown (non-spammy).
        int taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            RacingSessionStore.Session current = sessions.session(player.getUniqueId());
            PromptKind currentPrompt = current.getAs(RacingSessionKeys.PENDING_PROMPT, PromptKind.class);
            if (currentPrompt == null || current.getLong(RacingSessionKeys.PROMPT_TOKEN, 0L) != token) {
                int currentTaskId = current.getInt(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
                if (currentTaskId != -1) {
                    plugin.getServer().getScheduler().cancelTask(currentTaskId);
                    current.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
                }
                return;
            }
            long now = System.currentTimeMillis();
            long msLeft = Math.max(0L, current.getLong(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L) - now);
            long secondsLeft = (msLeft + 999L) / 1000L;

            if (msLeft <= 0L) {
                String returnId = current.getString(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID);

                current.set(RacingSessionKeys.PENDING_PROMPT, null);
                current.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
                current.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, null);

                int currentTaskId = current.getInt(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
                if (currentTaskId != -1) {
                    plugin.getServer().getScheduler().cancelTask(currentTaskId);
                    current.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, -1);
                }

                player.sendMessage("Prompt timed out (auto-cancelled).");
                sendActionBar(player, "");
                open(player, returnId != null ? returnId : com.omega.racing.plugin.runtime.ui.MainInventoryUi.ID, false, null);
                return;
            }

            sendActionBar(player, ChatColor.translateAlternateColorCodes('&', "&e" + secondsLeft + "s left &7(type '&f" + CANCEL_STRING + "&7' to cancel)"));
        }, 0L, 20L).getTaskId();

        s.set(RacingSessionKeys.PROMPT_ACTION_BAR_TASK_ID, taskId);

        ChatPrompt prompt = prompts.get(parsed);
        String start = prompt == null ? null : prompt.startMessage(CANCEL_STRING);
        if (start == null || start.isBlank()) {
            start = "Type your response in chat (or type '" + CANCEL_STRING + "' to cancel).";
        }
        player.sendMessage(start);
    }

    public void handleChat(Player player, String message) {
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        PromptKind pending = s.getAs(RacingSessionKeys.PENDING_PROMPT, PromptKind.class);
        if (pending == null) {
            return;
        }

        long expiresAtMillis = s.getLong(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
        if (expiresAtMillis > 0L && System.currentTimeMillis() > expiresAtMillis) {
            s.set(RacingSessionKeys.PENDING_PROMPT, null);
            s.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
            return;
        }

        String raceName = s.getString(RacingSessionKeys.EDITING_RACE_NAME);
        if (raceName == null || raceName.isBlank()) {
            s.set(RacingSessionKeys.PENDING_PROMPT, null);
            return;
        }

        String input = message == null ? "" : message.trim();
        if (input.isEmpty()) {
            player.sendMessage("Empty input.");
            return;
        }

        if (input.equalsIgnoreCase(CANCEL_STRING)) {
            s.set(RacingSessionKeys.PENDING_PROMPT, null);
            s.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
            sendActionBar(player, "");

            String returnId = s.getString(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID);
            s.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, null);
            if (returnId != null) {
                open(player, returnId, false, null);
            } else {
                open(player, com.omega.racing.plugin.runtime.ui.MainInventoryUi.ID, false, null);
            }
            player.sendMessage("Cancelled.");
            return;
        }

        String returnId = s.getString(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID);

        ChatPrompt prompt = prompts.get(pending);
        String nextInventoryId = prompt == null ? null : prompt.handle(player, s, raceName, input);

        s.set(RacingSessionKeys.PENDING_PROMPT, null);
        s.set(RacingSessionKeys.PROMPT_EXPIRES_AT_MILLIS, 0L);
        s.set(RacingSessionKeys.PROMPT_RETURN_INVENTORY_ID, null);
        sendActionBar(player, "");

        if (nextInventoryId != null && !nextInventoryId.isBlank()) {
            open(player, nextInventoryId, false, null);
        } else {
            open(player, returnId != null ? returnId : com.omega.racing.plugin.runtime.ui.MainInventoryUi.ID, false, null);
        }
    }

    private void render(Player player, String inventoryId, Inventory inv) {
        if (player == null || inventoryId == null) {
            return;
        }
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        String raceName = s.getString(RacingSessionKeys.EDITING_RACE_NAME);
        if (raceName == null || raceName.isBlank()) {
            return;
        }

        Optional<RaceDefinition> raceOpt = raceData.get(raceName);
        if (raceOpt.isEmpty()) {
            return;
        }

        RaceDefinition race = raceOpt.get();

        InventoryRenderer renderer = inventoryRenderers.get(inventoryId);
        if (renderer == null) {
            return;
        }
        renderer.render(inventoryId, player, inv, race, s);
    }

    private void sendActionBar(Player player, String message) {
        if (player == null) {
            return;
        }
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Throwable ignored) {
            // Best-effort; some server builds may not support actionbar.
        }
    }
}