package com.omega.racing.plugin.runtime.ui;

import com.google.gson.Gson;
import com.omega.racing.infrastructure.interop.ItemRegistryResolver;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UiComponents {

    private static final NamespacedKey ITEM_LOADER_ADDITIONAL_INFO = NamespacedKey.fromString("omegaitemloader:additional_info");
    private static final NamespacedKey ITEM_LOADER_INTERACTION = NamespacedKey.fromString("omegaitemloader:interaction");
    private static final NamespacedKey ITEM_LOADER_REFERENCE_ID = NamespacedKey.fromString("omegaitemloader:reference_id");

    private final ItemRegistryResolver itemRegistry;
    private final Gson gson;

    public UiComponents(ItemRegistryResolver itemRegistry, Gson gson) {
        this.itemRegistry = itemRegistry;
        this.gson = gson;
    }

    public void clear(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, null);
        }
    }

    public void clearSlots(Inventory inv, Iterable<Integer> slots) {
        if (inv == null || slots == null) {
            return;
        }
        for (Integer slot : slots) {
            if (slot == null) {
                continue;
            }
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
            inv.setItem(slot, null);
        }
    }

    public void clearRange(Inventory inv, int startInclusive, int endInclusive) {
        if (inv == null) {
            return;
        }
        int start = Math.max(0, startInclusive);
        int end = Math.min(inv.getSize() - 1, endInclusive);
        if (end < start) {
            return;
        }
        for (int i = start; i <= end; i++) {
            inv.setItem(i, null);
        }
    }

    public void set(Inventory inv, int slot, ItemStack stack) {
        if (slot < 0 || slot >= inv.getSize()) {
            return;
        }
        inv.setItem(slot, stack);
    }

    public int findFirstSlotWithAction(Inventory inv, String actionName) {
        if (inv == null || actionName == null || actionName.isBlank()) {
            return -1;
        }
        if (ITEM_LOADER_INTERACTION == null) {
            return -1;
        }

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) {
                continue;
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                continue;
            }
            String interactionJson = meta.getPersistentDataContainer().get(ITEM_LOADER_INTERACTION, PersistentDataType.STRING);
            if (interactionJson == null || interactionJson.isBlank()) {
                continue;
            }

            // interactionJson is an array of interaction configs; a simple contains check is enough.
            if (interactionJson.contains(actionName)) {
                return i;
            }
        }

        return -1;
    }

    public int findFirstSlotWithAdditionalInfo(Inventory inv, String key, String expectedValue) {
        if (inv == null || key == null || key.isBlank() || expectedValue == null) {
            return -1;
        }
        if (ITEM_LOADER_ADDITIONAL_INFO == null) {
            return -1;
        }

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) {
                continue;
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                continue;
            }

            String json = meta.getPersistentDataContainer().get(ITEM_LOADER_ADDITIONAL_INFO, PersistentDataType.STRING);
            if (json == null || json.isBlank()) {
                continue;
            }

            try {
                @SuppressWarnings("unchecked")
                Map<String, String> map = gson.fromJson(json, Map.class);
                if (map == null) {
                    continue;
                }
                Object v = map.get(key);
                if (expectedValue.equals(v)) {
                    return i;
                }
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    public List<Integer> findSlotsWithReferenceId(Inventory inv, String referenceId) {
        if (inv == null || referenceId == null || referenceId.isBlank()) {
            return List.of();
        }
        if (ITEM_LOADER_REFERENCE_ID == null) {
            return List.of();
        }

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) {
                continue;
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                continue;
            }
            String ref = meta.getPersistentDataContainer().get(ITEM_LOADER_REFERENCE_ID, PersistentDataType.STRING);
            if (referenceId.equals(ref)) {
                slots.add(i);
            }
        }
        slots.sort(Integer::compareTo);
        return slots;
    }

    public int findFirstSlotWithReferenceId(Inventory inv, String referenceId) {
        List<Integer> slots = findSlotsWithReferenceId(inv, referenceId);
        return slots.isEmpty() ? -1 : slots.get(0);
    }

    public List<Integer> findEmptySlots(Inventory inv) {
        if (inv == null) {
            return List.of();
        }
        return findEmptySlots(inv, 0, inv.getSize() - 1);
    }

    public List<Integer> findEmptySlots(Inventory inv, int startInclusive, int endInclusive) {
        if (inv == null) {
            return List.of();
        }
        int start = Math.max(0, startInclusive);
        int end = Math.min(inv.getSize() - 1, endInclusive);
        if (end < start) {
            return List.of();
        }

        List<Integer> slots = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType() == Material.AIR) {
                slots.add(i);
            }
        }
        return slots;
    }

    public ItemStack uiItem(String referenceId, Material fallback, String fallbackName) {
        return itemRegistry.create(referenceId).orElseGet(() -> {
            ItemStack stack = new ItemStack(fallback);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + fallbackName));
                stack.setItemMeta(meta);
            }
            return stack;
        });
    }

    public ItemStack withAdditionalInfo(ItemStack stack, Map<String, String> additionalInfo) {
        if (stack == null) {
            return null;
        }
        if (ITEM_LOADER_ADDITIONAL_INFO == null) {
            return stack;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        String json = gson.toJson(new HashMap<>(additionalInfo));
        meta.getPersistentDataContainer().set(ITEM_LOADER_ADDITIONAL_INFO, PersistentDataType.STRING, json);
        stack.setItemMeta(meta);
        return stack;
    }
}
