package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic selection inventory for choosing a racer from the currently selected team.
 *
 * This single renderer is registered for both:
 * - racing_racers (view-only: success delegate is typically NOOP)
 * - racing_remove_racer (remove flow: success delegate typically removes)
 */
public final class PlayerSelectionInventoryUi implements InventoryRenderer {

    public static final String RACERS_ID = "racing_racers";
    public static final String REMOVE_RACER_ID = "racing_remove_racer";
    public static final String ADD_RACER_ID = "racing_add_racer";

    private final UiComponents ui;
    private final PlayerUiDecorator playerUi;

    @Inject
    public PlayerSelectionInventoryUi(UiComponents ui, PlayerUiDecorator playerUi) {
        this.ui = ui;
        this.playerUi = playerUi;
    }

    @Override
    public String inventoryId() {
        // Primary registration is overridden by register(...)
        return RACERS_ID;
    }

    @Override
    public void register(Map<String, InventoryRenderer> registry) {
        registry.put(RACERS_ID, this);
        registry.put(REMOVE_RACER_ID, this);
        registry.put(ADD_RACER_ID, this);
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        if (ADD_RACER_ID.equals(inventoryId)) {
            renderAllPlayers(inv, session);
            return;
        }

        if (race == null || race.getTeams().isEmpty()) {
            return;
        }

        int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
        int idx = Math.min(Math.max(0, selectedTeamIndex), race.getTeams().size() - 1);
        RaceTeam team = race.getTeams().get(idx);

        List<RaceTeam.Racer> racers = new ArrayList<>(team.getRacers());
        racers.removeIf(r -> r == null || r.getUuid() == null || r.getUuid().isBlank());
        racers.sort(Comparator.comparing(r -> r.getUuid().toLowerCase()));

        List<Integer> slots = ui.findSlotsWithReferenceId(inv, "racing:ui/racer_select_entry");
        if (slots.isEmpty()) {
            // Prefer rendering into empty content slots (templates should only include controls).
            slots = ui.findEmptySlots(inv, 0, Math.min(44, inv.getSize() - 1));
        }

        int total = racers.size();
        int pageSize = slots.size();
        int maxPage = total == 0 ? 0 : (total - 1) / pageSize;
        int page = session.getInt(RacingSessionKeys.RACERS_PAGE, 0);
        if (page > maxPage) {
            page = maxPage;
            session.set(RacingSessionKeys.RACERS_PAGE, page);
        }

        int start = page * pageSize;
        int end = Math.min(total, start + pageSize);

        ui.clearSlots(inv, slots);
        int slotIndex = 0;
        for (int i = start; i < end; i++) {
            RaceTeam.Racer racer = racers.get(i);
            UUID uuid;
            try {
                uuid = UUID.fromString(racer.getUuid());
            } catch (Exception ignored) {
                continue;
            }

            ItemStack base = inv.getItem(slots.get(slotIndex));
            if (base == null) {
                base = ui.uiItem("racing:ui/racer_select_entry", Material.PLAYER_HEAD, "Racer");
            }
            ItemStack button = ui.withAdditionalInfo(base, Map.of("uuid", uuid.toString()));
            playerUi.decoratePlayerButton(button, org.bukkit.Bukkit.getOfflinePlayer(uuid));

            String positionText = formatGridPosition(racer.getRacePosition());
            ItemMeta meta = button.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                List<String> nextLore = new ArrayList<>();
                if (lore != null) {
                    for (String line : lore) {
                        if (line == null) {
                            continue;
                        }
                        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', line));
                        if (stripped != null && stripped.startsWith("Position:")) {
                            continue;
                        }
                        nextLore.add(line);
                    }
                }
                nextLore.add(ChatColor.GRAY + "Position: " + ChatColor.WHITE + positionText);
                meta.setLore(nextLore);
                button.setItemMeta(meta);
            }

            ui.set(inv, slots.get(slotIndex), button);
            slotIndex++;
        }
    }

    private static String formatGridPosition(RaceGridPosition pos) {
        if (pos == null) {
            return "Not set";
        }
        int idx = Math.max(1, pos.getPositionIndex());
        String world = pos.getWorld();
        String facingPart = " yaw=" + Math.round(pos.getYaw()) + " pitch=" + Math.round(pos.getPitch());
        if (world == null || world.isBlank()) {
            return "#" + idx + " " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + facingPart;
        }
        return "#" + idx + " " + world + " " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + facingPart;
    }

    private void renderAllPlayers(Inventory inv, RacingSessionStore.Session session) {
        var players = Bukkit.getOfflinePlayers();
        List<org.bukkit.OfflinePlayer> list = new ArrayList<>(players.length);
        for (var p : players) {
            list.add(p);
        }

        list.sort(Comparator.comparing(
            p -> p.getName() == null ? "~" + p.getUniqueId() : p.getName().toLowerCase(),
            Comparator.naturalOrder()
        ));

        List<Integer> slots = ui.findSlotsWithReferenceId(inv, "racing:ui/racer_select_entry");
        if (slots.isEmpty()) {
            // Prefer rendering into empty content slots (templates should only include controls).
            slots = ui.findEmptySlots(inv, 0, Math.min(44, inv.getSize() - 1));
        }

        int total = list.size();
        int pageSize = slots.size();
        int maxPage = total == 0 ? 0 : (total - 1) / pageSize;
        int page = session.getInt(RacingSessionKeys.PLAYER_PICKER_PAGE, 0);
        if (page > maxPage) {
            page = maxPage;
            session.set(RacingSessionKeys.PLAYER_PICKER_PAGE, page);
        }

        int start = page * pageSize;
        int end = Math.min(total, start + pageSize);

        ui.clearSlots(inv, slots);
        int slotIndex = 0;
        for (int i = start; i < end; i++) {
            var offline = list.get(i);
            String uuid = offline.getUniqueId().toString();

            ItemStack base = inv.getItem(slots.get(slotIndex));
            if (base == null) {
                base = ui.uiItem("racing:ui/racer_select_entry", Material.PLAYER_HEAD, "Player");
            }
            ItemStack button = ui.withAdditionalInfo(base, Map.of("uuid", uuid));
            playerUi.decoratePlayerButton(button, offline);

            ui.set(inv, slots.get(slotIndex), button);
            slotIndex++;
        }
    }
}
