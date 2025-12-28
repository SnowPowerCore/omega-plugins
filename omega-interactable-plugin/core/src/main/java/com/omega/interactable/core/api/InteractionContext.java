package com.omega.interactable.core.api;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InteractionContext {

    private final Plugin plugin;
    private final Player player;
    private final ItemStack item;
    private final Cancellable event;
    private final PlayerInteractEvent playerInteractEvent;
    private final InventoryClickEvent inventoryClickEvent;
    private final Map<String, Object> data;
    private final Map<String, String> additionalInfo;

    private InteractionContext(
            Plugin plugin,
            Player player,
            ItemStack item,
            Cancellable event,
            PlayerInteractEvent playerInteractEvent,
            InventoryClickEvent inventoryClickEvent,
            Map<String, Object> data,
            Map<String, String> additionalInfo
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.player = Objects.requireNonNull(player, "player");
        this.item = Objects.requireNonNull(item, "item");
        this.event = Objects.requireNonNull(event, "event");
        this.playerInteractEvent = playerInteractEvent;
        this.inventoryClickEvent = inventoryClickEvent;
        this.data = Objects.requireNonNull(data, "data");
        this.additionalInfo = Objects.requireNonNull(additionalInfo, "additionalInfo");
    }

    public static InteractionContext of(
            Plugin plugin,
            Player player,
            ItemStack item,
            Cancellable event,
            PlayerInteractEvent playerInteractEvent,
            InventoryClickEvent inventoryClickEvent,
            Map<String, Object> data,
            Map<String, String> additionalInfo
    ) {
        return new InteractionContext(plugin, player, item, event, playerInteractEvent, inventoryClickEvent, data, additionalInfo);
    }

    public Plugin plugin() {
        return plugin;
    }

    public Player player() {
        return player;
    }

    /**
     * The item involved in the interaction (as seen in the event).
     */
    public ItemStack item() {
        return item;
    }

    /**
     * Underlying cancellable event (PlayerInteractEvent or InventoryClickEvent).
     */
    public Cancellable event() {
        return event;
    }

    public Optional<PlayerInteractEvent> playerInteractEvent() {
        return Optional.ofNullable(playerInteractEvent);
    }

    public Optional<InventoryClickEvent> inventoryClickEvent() {
        return Optional.ofNullable(inventoryClickEvent);
    }

    /**
     * Shared mutable dictionary across the action chain.
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * Item-loader additionalInfo map (string->string) if present, else empty.
     */
    public Map<String, String> additionalInfo() {
        return additionalInfo;
    }

    public Optional<Action> action() {
        return playerInteractEvent().map(PlayerInteractEvent::getAction);
    }

    public Optional<EquipmentSlot> hand() {
        return playerInteractEvent().map(PlayerInteractEvent::getHand);
    }

    public Optional<Block> clickedBlock() {
        return playerInteractEvent().map(PlayerInteractEvent::getClickedBlock);
    }

    public Optional<BlockFace> blockFace() {
        return playerInteractEvent().map(PlayerInteractEvent::getBlockFace);
    }

    public boolean isSneaking() {
        return player.isSneaking();
    }
}
