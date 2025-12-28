package com.omega.invholder.infrastructure.model;

import com.omega.invholder.infrastructure.resolve.ItemReferenceResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public sealed interface InventorySlotItem permits InventorySlotItem.MaterialItem, InventorySlotItem.ReferenceItem {

    int slot();

    ItemStack resolve(ItemReferenceResolver resolver);

    record ReferenceItem(int slot, String referenceId) implements InventorySlotItem {
        @Override
        public ItemStack resolve(ItemReferenceResolver resolver) {
            if (referenceId == null || referenceId.isBlank()) {
                return null;
            }
            return resolver.resolve(referenceId).orElse(null);
        }
    }

    record MaterialItem(int slot, Material material, int amount) implements InventorySlotItem {
        @Override
        public ItemStack resolve(ItemReferenceResolver resolver) {
            if (material == null || material.isAir()) {
                return null;
            }
            int amt = Math.max(1, amount);
            int max = Math.max(1, material.getMaxStackSize());
            if (amt > max) {
                amt = max;
            }
            return new ItemStack(material, amt);
        }
    }
}
