package com.omega.racing.plugin.runtime.results;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceStageType;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.stage.RaceStageState;
import com.omega.racing.plugin.runtime.stage.RaceStageStateRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public final class RaceResultsUiService {

    private static final int INV_SIZE = 54;
    private static final int PAGE_SIZE = 45;

    private final RaceManager races;
    private final RaceStageStateRepository states;

    @Inject
    public RaceResultsUiService(RaceManager races, RaceStageStateRepository states) {
        this.races = races;
        this.states = states;
    }

    public void open(Player player, String raceName, RaceStageType stageType, int page) {
        if (player == null) {
            return;
        }
        if (raceName == null || raceName.isBlank()) {
            player.sendMessage("No race selected.");
            return;
        }
        if (stageType == null) {
            player.sendMessage("Missing stage type.");
            return;
        }

        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            player.sendMessage("Unknown race: " + raceName);
            return;
        }

        List<String> racerUuids = collectRacers(raceOpt.get());
        int total = racerUuids.size();
        int maxPage = total == 0 ? 0 : (total - 1) / PAGE_SIZE;
        int safePage = Math.max(0, Math.min(page, maxPage));

        String title = ChatColor.GOLD + "Results: " + stageType.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        title = title + ChatColor.DARK_GRAY + " (" + (safePage + 1) + "/" + (maxPage + 1) + ")";

        RaceResultsInventoryHolder holder = new RaceResultsInventoryHolder(raceName, stageType, safePage);
        Inventory inv = Bukkit.createInventory(holder, INV_SIZE, title);
        holder.setInventory(inv);

        // Nav buttons
        inv.setItem(45, navItem(Material.ARROW, ChatColor.YELLOW + "Prev", RaceResultsKeys.ACTION_PREV));
        inv.setItem(49, navItem(Material.BARRIER, ChatColor.RED + "Close", RaceResultsKeys.ACTION_CLOSE));
        inv.setItem(53, navItem(Material.ARROW, ChatColor.YELLOW + "Next", RaceResultsKeys.ACTION_NEXT));

        Map<String, RaceStageState.FreePracticeRacerProgress> freePractice = loadFreePracticeResults(stageType, raceName);

        int start = safePage * PAGE_SIZE;
        int end = Math.min(total, start + PAGE_SIZE);
        int slot = 0;
        for (int i = start; i < end; i++) {
            String uuidStr = racerUuids.get(i);
            inv.setItem(slot++, racerItem(uuidStr, stageType, freePractice));
        }

        player.openInventory(inv);
    }

    private Map<String, RaceStageState.FreePracticeRacerProgress> loadFreePracticeResults(RaceStageType stageType, String raceName) {
        if (stageType != RaceStageType.FREE_PRACTICE) {
            return Map.of();
        }
        try {
            Optional<RaceStageState> stateOpt = states.load(raceName);
            if (stateOpt.isEmpty()) {
                return Map.of();
            }
            Map<String, RaceStageState.FreePracticeRacerProgress> map = stateOpt.get().getFreePracticeRacers();
            if (map == null) {
                return Map.of();
            }
            return new LinkedHashMap<>(map);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private static List<String> collectRacers(RaceDefinition race) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (RaceTeam team : race.getTeams()) {
            if (team == null) {
                continue;
            }
            for (RaceTeam.Racer r : team.getRacers()) {
                if (r == null || r.getUuid() == null || r.getUuid().isBlank()) {
                    continue;
                }
                seen.put(r.getUuid(), true);
            }
        }
        List<String> out = new ArrayList<>(seen.keySet());
        out.sort(Comparator.naturalOrder());
        return out;
    }

    private static ItemStack navItem(Material material, String name, String action) {
        ItemStack it = new ItemStack(material, 1);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(RaceResultsKeys.UI_ACTION, PersistentDataType.STRING, action);
            it.setItemMeta(meta);
        }
        return it;
    }

    private static ItemStack racerItem(String uuidStr,
                                      RaceStageType stageType,
                                      Map<String, RaceStageState.FreePracticeRacerProgress> freePractice)
    {
        ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1);

        UUID id;
        try {
            id = UUID.fromString(uuidStr);
        } catch (Exception e) {
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "Invalid UUID");
                meta.setLore(List.of(ChatColor.GRAY + String.valueOf(uuidStr)));
                it.setItemMeta(meta);
            }
            return it;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(id);
        String displayName = offline.getName() != null ? offline.getName() : uuidStr;

        ItemMeta raw = it.getItemMeta();
        if (raw instanceof SkullMeta skull) {
            try {
                skull.setOwningPlayer(offline);
            } catch (Exception ignored) {
            }
            skull.setDisplayName(ChatColor.AQUA + displayName);
            skull.setLore(buildLore(uuidStr, stageType, freePractice));
            it.setItemMeta(skull);
        } else if (raw != null) {
            raw.setDisplayName(ChatColor.AQUA + displayName);
            raw.setLore(buildLore(uuidStr, stageType, freePractice));
            it.setItemMeta(raw);
        }

        return it;
    }

    private static List<String> buildLore(String uuidStr,
                                         RaceStageType stageType,
                                         Map<String, RaceStageState.FreePracticeRacerProgress> freePractice)
    {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stage: " + ChatColor.WHITE + stageType.name().toLowerCase(Locale.ROOT));

        if (stageType != RaceStageType.FREE_PRACTICE) {
            lore.add(ChatColor.DARK_GRAY + "No results persisted for this stage yet.");
            return lore;
        }

        RaceStageState.FreePracticeRacerProgress r = freePractice.get(uuidStr);
        if (r == null) {
            lore.add(ChatColor.DARK_GRAY + "No results.");
            return lore;
        }

        lore.add(ChatColor.GRAY + "Lap: " + ChatColor.WHITE + r.getLap());
        lore.add(ChatColor.GRAY + "Section: " + ChatColor.WHITE + r.getSection());
        lore.add(ChatColor.GRAY + "Best Lap: " + ChatColor.WHITE + formatLapTime(r.getBestLapMillis()));
        return lore;
    }

    private static String formatLapTime(long millis) {
        if (millis <= 0) {
            return "--:--.--";
        }
        long centis = millis / 10;
        long minutes = centis / 6000;
        long seconds = (centis / 100) % 60;
        long hundredths = centis % 100;
        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
    }
}
