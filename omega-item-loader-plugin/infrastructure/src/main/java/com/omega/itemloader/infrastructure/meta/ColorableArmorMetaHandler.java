package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class ColorableArmorMetaHandler implements ItemMetaHandler {

    private final Logger logger;
    private final ArmorMetaHandler armorMetaHandler;

    @Inject
    public ColorableArmorMetaHandler(Logger logger, ArmorMetaHandler armorMetaHandler) {
        this.logger = logger;
        this.armorMetaHandler = armorMetaHandler;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("colorablearmor"),
                MetaParsing.normalizeType("ColorableArmorMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof ColorableArmorMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof ColorableArmorMeta colorableArmorMeta)) {
            logger.warning("meta.type 'colorablearmor' provided but ItemMeta is not ColorableArmorMeta (item id=" + itemId + ")");
            return;
        }

        // ColorableArmorMeta includes LeatherArmorMeta methods.
        MetaParsing.optionalColor(metaObject, "color").ifPresent(colorableArmorMeta::setColor);

        // And it also supports armor trims via ArmorMeta.
        armorMetaHandler.apply(itemId, metaObject, meta);
    }
}
