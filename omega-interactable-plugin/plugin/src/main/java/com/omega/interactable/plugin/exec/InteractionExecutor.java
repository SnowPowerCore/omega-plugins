package com.omega.interactable.plugin.exec;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.omega.interactable.core.OmegaItemLoaderInterop;
import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.interactable.infrastructure.match.TriggerMatcher;
import com.omega.interactable.infrastructure.model.InteractionDefinition;
import com.omega.interactable.infrastructure.parse.InteractionConfigParser;
import com.omega.interactable.plugin.actions.ActionFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class InteractionExecutor {

    private final Plugin plugin;
    private final Logger logger;
    private final Gson gson;
    private final InteractionConfigParser parser;
    private final ActionFactory actionFactory;

    private final Map<String, List<InteractionDefinition>> parseCache = new ConcurrentHashMap<>();

    @Inject
    public InteractionExecutor(Plugin plugin, Logger logger, Gson gson, InteractionConfigParser parser, ActionFactory actionFactory) {
        this.plugin = plugin;
        this.logger = logger;
        this.gson = gson;
        this.parser = parser;
        this.actionFactory = actionFactory;
    }

    public void execute(Player player, ItemStack item, ItemMeta meta, Cancellable event) {
        execute(player, item, meta, event, null, null);
    }

    public void execute(Player player, ItemStack item, ItemMeta meta, PlayerInteractEvent event) {
        execute(player, item, meta, event, event, null);
    }

    public void execute(Player player, ItemStack item, ItemMeta meta, InventoryClickEvent event) {
        execute(player, item, meta, event, null, event);
    }

    private void execute(Player player, ItemStack item, ItemMeta meta, Cancellable cancellable, PlayerInteractEvent interactEvent, InventoryClickEvent inventoryClickEvent) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String raw = pdc.get(OmegaItemLoaderInterop.INTERACTION, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) {
            return;
        }

        List<InteractionDefinition> defs = parseCache.computeIfAbsent(raw, parser::parse);
        if (defs.isEmpty()) {
            return;
        }

        Map<String, String> additionalInfo = readAdditionalInfo(pdc);
        Map<String, Object> shared = new HashMap<>();

        InteractionContext ctx = InteractionContext.of(plugin, player, item, cancellable, interactEvent, inventoryClickEvent, shared, additionalInfo);

        for (InteractionDefinition def : defs) {
            boolean matches = false;
            if (interactEvent != null) {
                matches = TriggerMatcher.matches(def.triggerTokens(), interactEvent);
            } else if (inventoryClickEvent != null) {
                matches = TriggerMatcher.matches(def.triggerTokens(), inventoryClickEvent);
            }

            if (!matches) {
                continue;
            }

            for (String actionType : def.actionTypes()) {
                Optional<InteractableAction> actionOpt = actionFactory.create(actionType);
                if (actionOpt.isEmpty()) {
                    continue;
                }

                InteractionResult result;
                try {
                    result = actionOpt.get().onInteract(ctx);
                } catch (Exception e) {
                    logger.warning("Action threw exception (" + actionType + "): " + e.getMessage());
                    continue;
                }

                if (result == null || !result.shouldContinue()) {
                    return;
                }
            }
        }
    }

    private Map<String, String> readAdditionalInfo(PersistentDataContainer pdc) {
        String rawAdditional = pdc.get(OmegaItemLoaderInterop.ADDITIONAL_INFO, PersistentDataType.STRING);
        if (rawAdditional == null || rawAdditional.isBlank()) {
            return Map.of();
        }

        try {
            JsonElement el = gson.fromJson(rawAdditional, JsonElement.class);
            if (el == null || !el.isJsonObject()) {
                return Map.of();
            }
            Map<String, String> out = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : el.getAsJsonObject().entrySet()) {
                JsonElement v = entry.getValue();
                if (v == null || v.isJsonNull()) {
                    continue;
                }
                if (v.isJsonPrimitive()) {
                    try {
                        out.put(entry.getKey(), v.getAsString());
                    } catch (Exception ignored) {
                    }
                } else {
                    out.put(entry.getKey(), gson.toJson(v));
                }
            }
            return Map.copyOf(out);
        } catch (Exception ignored) {
            return Map.of();
        }
    }
}
