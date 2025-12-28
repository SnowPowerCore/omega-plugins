package com.omega.racing.plugin.listener;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.blocks.PickPositionInventoryHolder;
import com.omega.racing.plugin.runtime.blocks.PickRacerInventoryHolder;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksKeys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RacerPositionToolListener implements Listener {

    private static final String PICK_POSITION_TITLE = "Pick a position";
    private static final String PICK_RACER_TITLE = "Pick a racer";

    private final RaceManager races;
    private final RaceDataApi data;

    @Inject
    public RacerPositionToolListener(RaceManager races, RaceDataApi data) {
        this.races = races;
        this.data = data;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isPositionTool(item)) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        event.setCancelled(true);

        String raceName = toolRaceName(item);
        if (raceName == null || raceName.isBlank()) {
            player.sendMessage(ChatColor.RED + "This position tool has no race assigned.");
            return;
        }

        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Unknown race: " + raceName);
            return;
        }

        float yaw = snapYawToNearest90(normalizeYaw(player.getLocation().getYaw()));
        float pitch = 0f;

        openPickPosition(player, raceOpt.get(), clicked, yaw, pitch, 0);
    }

    private void openPickPosition(Player player, RaceDefinition race, Block clicked, float yaw, float pitch, int page) {
        int total = Math.max(1, race.getPositions());
        int maxPage = (total - 1) / 45;
        int clampedPage = Math.max(0, Math.min(page, maxPage));

        PickPositionInventoryHolder holder = new PickPositionInventoryHolder(
            race.getName(),
            clicked.getWorld().getName(),
            clicked.getX(),
            clicked.getY(),
            clicked.getZ(),
            yaw,
            pitch,
            clampedPage
        );

        Inventory inv = Bukkit.createInventory(holder, 54, PICK_POSITION_TITLE);
        holder.setInventory(inv);

        // Controls
        inv.setItem(45, control(Material.BARRIER, ChatColor.RED + "Close", RaceBlocksKeys.ACTION_CLOSE));
        inv.setItem(48, control(Material.ARROW, ChatColor.YELLOW + "Prev Page", RaceBlocksKeys.ACTION_PREV));
        inv.setItem(50, control(Material.ARROW, ChatColor.YELLOW + "Next Page", RaceBlocksKeys.ACTION_NEXT));

        int start = holder.contentOffset();
        int endExclusive = Math.min(total, start + 45);
        int slot = 0;
        for (int i = start; i < endExclusive; i++) {
            int idx = i + 1;
            ItemStack stone = new ItemStack(Material.STONE);
            ItemMeta meta = stone.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GRAY + "Position #" + ChatColor.WHITE + idx);
                meta.setLore(java.util.List.of(ChatColor.DARK_GRAY + "Click to pick this position"));
                meta.getPersistentDataContainer().set(RaceBlocksKeys.PICK_POSITION_INDEX, PersistentDataType.INTEGER, idx);
                stone.setItemMeta(meta);
            }
            inv.setItem(slot, stone);
            slot++;
        }

        player.openInventory(inv);
        player.sendMessage(ChatColor.YELLOW + "Pick a position number.");
    }

    private static ItemStack control(Material material, String name, String action) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(RaceBlocksKeys.UI_ACTION, PersistentDataType.STRING, action);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @EventHandler
    public void onPickClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof PickPositionInventoryHolder holder) {
            if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
                return;
            }
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) {
                return;
            }

            String action = actionOf(clicked);
            if (action != null) {
                switch (action) {
                    case RaceBlocksKeys.ACTION_CLOSE -> player.closeInventory();
                    case RaceBlocksKeys.ACTION_PREV -> openPickPositionPage(player, holder, holder.page() - 1);
                    case RaceBlocksKeys.ACTION_NEXT -> openPickPositionPage(player, holder, holder.page() + 1);
                }
                return;
            }

            Integer idx = pickPositionIndex(clicked);
            if (idx == null || idx < 1) {
                return;
            }

            Optional<RaceDefinition> raceOpt = races.get(holder.raceName());
            if (raceOpt.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Unknown race: " + holder.raceName());
                player.closeInventory();
                return;
            }

            Set<String> racerUuids = new LinkedHashSet<>();
            for (RaceTeam team : raceOpt.get().getTeams()) {
                if (team == null) {
                    continue;
                }
                for (RaceTeam.Racer racer : team.getRacers()) {
                    if (racer == null || racer.getUuid() == null || racer.getUuid().isBlank()) {
                        continue;
                    }
                    racerUuids.add(racer.getUuid());
                }
            }

            if (racerUuids.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No racers in this race.");
                return;
            }

            RaceGridPosition pos = new RaceGridPosition(
                holder.worldName(),
                holder.blockX(),
                holder.blockY(),
                holder.blockZ(),
                idx,
                holder.yaw(),
                holder.pitch()
            );

            openPickRacer(player, holder.raceName(), pos, racerUuids);
            return;
        }

        if (!(top.getHolder() instanceof PickRacerInventoryHolder holder)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }

        String uuid = pickUuid(clicked);
        if (uuid == null || uuid.isBlank()) {
            return;
        }

        boolean ok = data.setRacerRacePosition(holder.raceName(), uuid, holder.position());
        if (ok) {
            player.sendMessage(ChatColor.GREEN + "Position set.");

            Player target = null;
            try {
                target = Bukkit.getPlayer(UUID.fromString(uuid));
            } catch (IllegalArgumentException ignored) {
                // uuid is not a valid UUID; can't teleport
            }

            if (target != null) {
                Location loc = toTeleportLocation(holder.position());
                if (loc == null) {
                    player.sendMessage(ChatColor.RED + "Cannot teleport: world not found.");
                } else {
                    target.teleport(loc);
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set position.");
        }
        player.closeInventory();
    }

    private void openPickPositionPage(Player player, PickPositionInventoryHolder previous, int nextPage) {
        Optional<RaceDefinition> raceOpt = races.get(previous.raceName());
        if (raceOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Unknown race: " + previous.raceName());
            player.closeInventory();
            return;
        }

        // Rebuild using stored block + facing
        Block base = Bukkit.getWorld(previous.worldName()) == null
            ? null
            : Bukkit.getWorld(previous.worldName()).getBlockAt(previous.blockX(), previous.blockY(), previous.blockZ());
        if (base == null) {
            player.sendMessage(ChatColor.RED + "World not found: " + previous.worldName());
            player.closeInventory();
            return;
        }

        openPickPosition(player, raceOpt.get(), base, previous.yaw(), previous.pitch(), nextPage);
    }

    private void openPickRacer(Player player, String raceName, RaceGridPosition pos, Set<String> racerUuids) {
        PickRacerInventoryHolder holder = new PickRacerInventoryHolder(raceName, pos);
        Inventory inv = Bukkit.createInventory(holder, 54, PICK_RACER_TITLE);
        holder.setInventory(inv);

        int slot = 0;
        for (String uuidStr : racerUuids) {
            if (slot >= inv.getSize()) {
                break;
            }
            inv.setItem(slot, racerButton(uuidStr));
            slot++;
        }

        player.openInventory(inv);
        player.sendMessage(ChatColor.YELLOW + "Pick a racer for position #" + pos.getPositionIndex() + ".");
    }

    private static Location toTeleportLocation(RaceGridPosition pos) {
        if (pos == null) {
            return null;
        }
        String worldName = pos.getWorld();
        if (worldName == null || worldName.isBlank()) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        // Teleport on top of the chosen block, centered.
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        Location loc = new Location(world, x, y, z, pos.getYaw(), pos.getPitch());
        return loc;
    }

    @EventHandler
    public void onPickDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof PickRacerInventoryHolder || top.getHolder() instanceof PickPositionInventoryHolder) {
            event.setCancelled(true);
        }
    }

    private static boolean isPositionTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte b = pdc.get(RaceBlocksKeys.POSITION_TOOL, PersistentDataType.BYTE);
        return b != null && b == (byte) 1;
    }

    private static String toolRaceName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceBlocksKeys.POSITION_TOOL_RACE, PersistentDataType.STRING);
    }

    private static String pickUuid(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceBlocksKeys.PICK_RACER_UUID, PersistentDataType.STRING);
    }

    private static Integer pickPositionIndex(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceBlocksKeys.PICK_POSITION_INDEX, PersistentDataType.INTEGER);
    }

    private static String actionOf(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(RaceBlocksKeys.UI_ACTION, PersistentDataType.STRING);
    }

    private static ItemStack racerButton(String uuidStr) {
        String display = uuidStr;
        org.bukkit.OfflinePlayer offline = null;
        try {
            UUID uuid = UUID.fromString(uuidStr);
            offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline == null ? null : offline.getName();
            if (name != null && !name.isBlank()) {
                display = name;
            }
        } catch (IllegalArgumentException ignored) {
            // keep uuid
        }

        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skull && offline != null) {
            skull.setOwningPlayer(offline);
            meta = skull;
        }
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + display);
            meta.getPersistentDataContainer().set(RaceBlocksKeys.PICK_RACER_UUID, PersistentDataType.STRING, uuidStr);
            stack.setItemMeta(meta);
            return stack;
        }

        ItemStack fallback = new ItemStack(Material.PAPER);
        ItemMeta fallbackMeta = fallback.getItemMeta();
        if (fallbackMeta != null) {
            fallbackMeta.setDisplayName(ChatColor.AQUA + display);
            fallbackMeta.getPersistentDataContainer().set(RaceBlocksKeys.PICK_RACER_UUID, PersistentDataType.STRING, uuidStr);
            fallback.setItemMeta(fallbackMeta);
        }
        return fallback;

    }

    private static float normalizeYaw(float yaw) {
        float out = yaw % 360f;
        if (out < 0f) {
            out += 360f;
        }
        return out;
    }

    private static float snapYawToNearest90(float yaw) {
        // yaw is expected to be in [0, 360)
        float snapped = Math.round(yaw / 90f) * 90f;
        if (snapped >= 360f) {
            snapped -= 360f;
        }
        if (snapped < 0f) {
            snapped += 360f;
        }
        return snapped;
    }
}
