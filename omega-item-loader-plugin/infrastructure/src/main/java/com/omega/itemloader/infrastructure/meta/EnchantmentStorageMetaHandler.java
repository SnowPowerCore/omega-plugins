package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class EnchantmentStorageMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public EnchantmentStorageMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("enchantmentstorage"),
                MetaParsing.normalizeType("EnchantmentStorageMeta"),
                MetaParsing.normalizeType("enchantedbook")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof EnchantmentStorageMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof EnchantmentStorageMeta storageMeta)) {
            logger.warning("meta.type 'enchantmentstorage' provided but ItemMeta is not EnchantmentStorageMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalObject(metaObject, "storedEnchants")
                .ifPresent(enchants -> {
                    for (Map.Entry<String, JsonElement> entry : enchants.entrySet()) {
                        String enchantRaw = entry.getKey();
                        int level = JsonRead.asInt(entry.getValue()).orElse(1);

                        MetaParsing.resolveEnchantment(enchantRaw).ifPresentOrElse(
                                enchantment -> storageMeta.addStoredEnchant(enchantment, level, true),
                                () -> logger.warning("Unknown enchantment: " + enchantRaw + " (item id=" + itemId + ")")
                        );
                    }
                });
    }
}
