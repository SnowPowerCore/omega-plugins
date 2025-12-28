package com.omega.itemloader.plugin.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.PacketType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.omega.itemloader.core.OmegaItemLoaderKeys;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * ProtocolLib integration that applies an enchantment glint to items client-side
 * when additionalInfo.glow is true.
 */
public final class ProtocolLibGlowIntegration implements AutoCloseable {

    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private final Logger logger;
    private final Gson gson = new Gson();

    private final ProtocolManager protocolManager;
    private final PacketListener listener;

    public ProtocolLibGlowIntegration(JavaPlugin plugin, Logger logger) {
        this.logger = logger;
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.listener = new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS,
                PacketType.Play.Server.ENTITY_EQUIPMENT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handle(event);
            }
        };

        this.protocolManager.addPacketListener(listener);
    }

    @Override
    public void close() {
        protocolManager.removePacketListener(listener);
    }

    private void handle(PacketEvent event) {
        if (event == null || event.getPacket() == null) {
            return;
        }

        PacketType type = event.getPacketType();

        if (type == PacketType.Play.Server.SET_SLOT) {
            ItemStack item = event.getPacket().getItemModifier().read(0);
            ItemStack replaced = maybeApplyGlint(item);
            if (replaced != item) {
                event.getPacket().getItemModifier().write(0, replaced);
            }
            return;
        }

        if (type == PacketType.Play.Server.WINDOW_ITEMS) {
            List<ItemStack> items = event.getPacket().getItemListModifier().read(0);
            if (items == null || items.isEmpty()) {
                return;
            }

            boolean changed = false;
            List<ItemStack> out = new ArrayList<>(items.size());
            for (ItemStack item : items) {
                ItemStack replaced = maybeApplyGlint(item);
                if (replaced != item) {
                    changed = true;
                }
                out.add(replaced);
            }

            if (changed) {
                event.getPacket().getItemListModifier().write(0, out);
            }
            return;
        }

        if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = event.getPacket().getSlotStackPairLists().read(0);
            if (pairs == null || pairs.isEmpty()) {
                return;
            }

            boolean changed = false;
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> out = new ArrayList<>(pairs.size());
            for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : pairs) {
                if (pair == null) {
                    continue;
                }

                ItemStack item = pair.getSecond();
                ItemStack replaced = maybeApplyGlint(item);
                if (replaced != item) {
                    changed = true;
                }
                out.add(new Pair<>(pair.getFirst(), replaced));
            }

            if (changed) {
                event.getPacket().getSlotStackPairLists().write(0, out);
            }
        }
    }

    private ItemStack maybeApplyGlint(ItemStack original) {
        if (original == null || original.getType().isAir()) {
            return original;
        }

        if (!shouldGlow(original)) {
            return original;
        }

        ItemMeta meta = original.getItemMeta();
        if (meta == null) {
            return original;
        }

        // Already has glint.
        if (meta.hasEnchants()) {
            return original;
        }

        ItemStack copy = original.clone();
        ItemMeta copyMeta = copy.getItemMeta();
        if (copyMeta == null) {
            return original;
        }

        boolean added = copyMeta.addEnchant(Enchantment.LUCK, 1, true);
        if (!added) {
            return original;
        }

        copyMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        copy.setItemMeta(copyMeta);
        return copy;
    }

    private boolean shouldGlow(ItemStack itemStack) {
        try {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                return false;
            }

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            String json = pdc.get(OmegaItemLoaderKeys.ADDITIONAL_INFO, PersistentDataType.STRING);
            if (json == null || json.isBlank()) {
                return false;
            }

            // Fast path before JSON parsing.
            if (!json.toLowerCase(Locale.ROOT).contains("\"glow\"")) {
                return false;
            }

            Map<String, String> map = gson.fromJson(json, STRING_MAP_TYPE);
            if (map == null) {
                return false;
            }

            String glowValue = map.get("glow");
            if (glowValue == null) {
                return false;
            }

            glowValue = glowValue.trim();
            return Objects.equals(glowValue, "1") || Boolean.parseBoolean(glowValue);
        } catch (Exception e) {
            // Don't break item packets if parsing fails.
            logger.fine(() -> "Failed to parse additionalInfo for glow: " + e.getMessage());
            return false;
        }
    }
}
