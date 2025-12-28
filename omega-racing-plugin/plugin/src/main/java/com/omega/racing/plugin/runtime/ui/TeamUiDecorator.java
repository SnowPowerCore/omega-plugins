package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.model.RaceTeam;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public final class TeamUiDecorator {

    @Inject
    public TeamUiDecorator() {
    }

    public void decorateTeamButton(ItemStack stack, RaceTeam team, int idx) {
        if (stack == null || team == null) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + team.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Team #" + (idx + 1));
        lore.add(ChatColor.GRAY + "Suit: " + SuitColorDisplay.display(team.getSuitColorHex()));
        lore.add(ChatColor.GRAY + "Racers: " + team.getRacers().size());
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }
}
