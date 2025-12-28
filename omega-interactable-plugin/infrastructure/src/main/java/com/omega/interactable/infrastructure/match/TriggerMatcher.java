package com.omega.interactable.infrastructure.match;

import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;
import java.util.Set;

/**
 * Matches a PlayerInteractEvent against a set of trigger tokens.
 *
 * Supported tokens:
 * - LEFT, RIGHT
 * - AIR, BLOCK
 * - SHIFT (requires player sneaking)
 * - MAIN_HAND, OFF_HAND
 * - PHYSICAL (matches Action.PHYSICAL)
 *
 * InventoryClickEvent supported tokens:
 * - INVENTORY (always true for inventory click)
 * - LEFT, RIGHT
 * - SHIFT (shift-click)
 * - NUMBER_KEY
 * - DOUBLE
 * - DROP
 * - SWAP_OFFHAND
 */
public final class TriggerMatcher {

    private TriggerMatcher() {
    }

    public static boolean matches(Set<String> triggerTokens, PlayerInteractEvent event) {
        if (triggerTokens == null || triggerTokens.isEmpty() || event == null) {
            return false;
        }

        Action action = event.getAction();

        for (String tokenRaw : triggerTokens) {
            if (tokenRaw == null) {
                continue;
            }

            String token = tokenRaw.trim().toUpperCase(Locale.ROOT);
            if (token.isEmpty()) {
                continue;
            }

            if (!matchesToken(token, event, action)) {
                return false;
            }
        }

        return true;
    }

    public static boolean matches(Set<String> triggerTokens, InventoryClickEvent event) {
        if (triggerTokens == null || triggerTokens.isEmpty() || event == null) {
            return false;
        }

        ClickType click = event.getClick();

        for (String tokenRaw : triggerTokens) {
            if (tokenRaw == null) {
                continue;
            }

            String token = tokenRaw.trim().toUpperCase(Locale.ROOT);
            if (token.isEmpty()) {
                continue;
            }

            if (!matchesToken(token, event, click)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesToken(String token, PlayerInteractEvent event, Action action) {
        return switch (token) {
            case "LEFT" -> action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
            case "RIGHT" -> action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
            case "AIR" -> action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR;
            case "BLOCK" -> action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
            case "PHYSICAL" -> action == Action.PHYSICAL;
            case "SHIFT" -> event.getPlayer().isSneaking();
            case "MAIN_HAND" -> event.getHand() == EquipmentSlot.HAND;
            case "OFF_HAND" -> event.getHand() == EquipmentSlot.OFF_HAND;
            case "INVENTORY" -> false;
            case "NUMBER_KEY" -> false;
            case "DOUBLE" -> false;
            case "DROP" -> false;
            case "SWAP_OFFHAND" -> false;
            default -> false;
        };
    }

    private static boolean matchesToken(String token, InventoryClickEvent event, ClickType click) {
        return switch (token) {
            case "INVENTORY" -> true;
            case "LEFT" -> event.isLeftClick();
            case "RIGHT" -> event.isRightClick();
            case "SHIFT" -> event.isShiftClick();
            case "NUMBER_KEY" -> click == ClickType.NUMBER_KEY;
            case "DOUBLE" -> click == ClickType.DOUBLE_CLICK;
            case "DROP" -> click == ClickType.DROP || click == ClickType.CONTROL_DROP;
            case "SWAP_OFFHAND" -> click == ClickType.SWAP_OFFHAND;

            // Not applicable for inventory clicks
            case "AIR" -> false;
            case "BLOCK" -> false;
            case "PHYSICAL" -> false;
            case "MAIN_HAND" -> false;
            case "OFF_HAND" -> false;
            default -> false;
        };
    }
}
