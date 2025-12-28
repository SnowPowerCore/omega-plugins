package com.omega.racing.plugin.runtime.ui;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfirmInventoryUi implements InventoryRenderer {

    public static final String ID = "racing_confirm";

    private final UiComponents ui;

    @Inject
    public ConfirmInventoryUi(UiComponents ui) {
        this.ui = ui;
    }

    @Override
    public String inventoryId() {
        return ID;
    }

    @Override
    public void render(String inventoryId, Player player, Inventory inv, RaceDefinition race, RacingSessionStore.Session session) {
        // Static layout (back/accept/cancel) is provided by racing-invs.json via OmegaInvHolder.
        // The accept button needs a runtime delegate injected into additionalInfo.
        {
            int acceptSlot = ui.findFirstSlotWithAction(inv, "RacingConfirmAcceptAction");
            if (acceptSlot >= 0) {
                ItemStack accept = inv.getItem(acceptSlot);
                if (accept == null) {
                    accept = ui.uiItem("racing:ui/confirm_accept", Material.LIME_WOOL, "Accept");
                }

                String confirmKind = session.getString(RacingSessionKeys.CONFIRM_KIND);
                if (confirmKind != null && !confirmKind.isBlank()) {
                    accept = ui.withAdditionalInfo(accept, Map.of("delegate", confirmKind));
                }
                ui.set(inv, acceptSlot, accept);
            }
        }

        int infoSlot = ui.findFirstSlotWithReferenceId(inv, "racing:ui/confirm_info");
        if (infoSlot < 0) {
            infoSlot = 13;
        }

        ItemStack info = inv.getItem(infoSlot);
        if (info == null) {
            info = new ItemStack(Material.PAPER);
        }
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            String rawTitle = session.getString(RacingSessionKeys.CONFIRM_TITLE);
            String title = (rawTitle == null || rawTitle.isBlank())
                    ? (ChatColor.RED + "Confirm")
                    : ChatColor.translateAlternateColorCodes('&', rawTitle);

            List<String> lore = new ArrayList<>();
            Object rawLore = session.get(RacingSessionKeys.CONFIRM_LORE);
            if (rawLore instanceof Iterable<?> iterable) {
                for (Object lineObj : iterable) {
                    if (!(lineObj instanceof String line) || line == null) {
                        continue;
                    }
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            lore.add(ChatColor.GRAY + "Click " + ChatColor.GREEN + "Accept" + ChatColor.GRAY + " to continue.");
            lore.add(ChatColor.GRAY + "Click " + ChatColor.RED + "Cancel" + ChatColor.GRAY + " to go back.");

            meta.setDisplayName(title);
            meta.setLore(lore);
            info.setItemMeta(meta);
        }

        ui.set(inv, infoSlot, info);
    }
}
