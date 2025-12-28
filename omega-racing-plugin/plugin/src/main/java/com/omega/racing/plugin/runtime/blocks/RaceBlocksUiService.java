package com.omega.racing.plugin.runtime.blocks;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.plugin.runtime.RaceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public final class RaceBlocksUiService {

    public static final int SIZE = 54;
    public static final int CONTENT_SIZE = 45; // 0..44

    private final RaceManager races;

    @Inject
    public RaceBlocksUiService(RaceManager races) {
        this.races = races;
    }

    public void open(Player player, String raceName, int page) {
        if (player == null || raceName == null || raceName.isBlank()) {
            return;
        }

        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            player.sendMessage("No such race.");
            return;
        }

        RaceDefinition race = raceOpt.get();
        List<ItemStack> items = buildItems(race);

        int total = items.size();
        int maxPage = total == 0 ? 0 : (total - 1) / CONTENT_SIZE;
        int clampedPage = Math.max(0, Math.min(page, maxPage));

        String title = ChatColor.translateAlternateColorCodes('&', "&bRace Blocks: &f" + race.getName() + " &7(" + (clampedPage + 1) + "/" + (maxPage + 1) + ")");
        RaceBlocksInventoryHolder holder = new RaceBlocksInventoryHolder(race.getName(), clampedPage);
        Inventory inv = Bukkit.createInventory(holder, SIZE, title);
        holder.setInventory(inv);

        // Fill content.
        int start = clampedPage * CONTENT_SIZE;
        int end = Math.min(total, start + CONTENT_SIZE);
        int slot = 0;
        for (int i = start; i < end; i++) {
            ItemStack item = items.get(i);
            if (item != null) {
                inv.setItem(slot, item);
            }
            slot++;
        }

        // Controls (bottom row)
        inv.setItem(45, controlItem(Material.BARRIER, ChatColor.RED + "Close", RaceBlocksKeys.ACTION_CLOSE));
        inv.setItem(47, controlItem(Material.STICK, ChatColor.GREEN + "Position Tool", RaceBlocksKeys.ACTION_POSITION_TOOL));
        inv.setItem(48, controlItem(Material.ARROW, ChatColor.YELLOW + "Prev Page", RaceBlocksKeys.ACTION_PREV));
        inv.setItem(50, controlItem(Material.ARROW, ChatColor.YELLOW + "Next Page", RaceBlocksKeys.ACTION_NEXT));

        player.openInventory(inv);
    }

    private ItemStack controlItem(Material material, String name, String action) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(RaceBlocksKeys.UI_ACTION, PersistentDataType.STRING, action);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private List<ItemStack> buildItems(RaceDefinition race) {
        List<ItemStack> out = new ArrayList<>();

        int sections = Math.max(1, race.getSections());
        for (int i = 0; i < sections; i++) {
            out.add(stageItem(race.getName(), RaceBlocksKeys.KIND_SECTION, i));
        }

        alignToNextRow(out);
        // Light blocks (0..15)
        for (int level = 0; level <= 15; level++) {
            out.add(lightItem(level));
        }

        trimTrailingNulls(out);

        return out;
    }

    private static void alignToNextRow(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int pageOffset = items.size() % CONTENT_SIZE;
        int col = pageOffset % 9;
        if (col == 0) {
            return;
        }

        int nextRowStart = pageOffset - col + 9;
        int pad;
        if (nextRowStart >= CONTENT_SIZE) {
            // The next row would be beyond the content area; start on next page.
            pad = CONTENT_SIZE - pageOffset;
        } else {
            pad = nextRowStart - pageOffset;
        }

        for (int i = 0; i < pad; i++) {
            items.add(null);
        }
    }

    private static void trimTrailingNulls(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) != null) {
                break;
            }
            items.remove(i);
        }
    }

    private ItemStack stageItem(String raceName, String kind, int index) {
        // Stage blocks are blue ice blocks. Identification is via PDC.
        ItemStack stack = new ItemStack(Material.BLUE_ICE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String title = ChatColor.AQUA + "Section" + ChatColor.WHITE + " #" + (index + 1);
            meta.setDisplayName(title);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(RaceBlocksKeys.STAGE_ID, PersistentDataType.STRING, RaceBlocksPlacedStore.encodeStageId(raceName, kind, index));

            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack lightItem(int level) {
        ItemStack stack = new ItemStack(Material.LIGHT);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof BlockDataMeta blockDataMeta) {
            try {
                blockDataMeta.setBlockData(Bukkit.createBlockData("minecraft:light[level=" + level + "]"));
            } catch (Exception ignored) {
            }
            blockDataMeta.setDisplayName(ChatColor.YELLOW + "Light (Level " + level + ")");
            stack.setItemMeta(blockDataMeta);
        } else if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Light (Level " + level + ")");
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
