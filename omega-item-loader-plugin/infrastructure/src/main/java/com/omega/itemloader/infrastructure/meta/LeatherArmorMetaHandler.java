package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class LeatherArmorMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public LeatherArmorMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("leatherarmor"),
                MetaParsing.normalizeType("LeatherArmorMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof LeatherArmorMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof LeatherArmorMeta leatherArmorMeta)) {
            logger.warning("meta.type 'leatherarmor' provided but ItemMeta is not LeatherArmorMeta (item id=" + itemId + ")");
            return;
        }

        MetaParsing.optionalColor(metaObject, "color").ifPresent(leatherArmorMeta::setColor);
    }
}
