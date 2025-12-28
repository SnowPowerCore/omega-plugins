package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class MapMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public MapMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("map"),
                MetaParsing.normalizeType("MapMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof MapMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof MapMeta mapMeta)) {
            logger.warning("meta.type 'map' provided but ItemMeta is not MapMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalInt(metaObject, "mapId").ifPresent(mapMeta::setMapId);
        JsonRead.optionalBoolean(metaObject, "scaling").ifPresent(mapMeta::setScaling);
        JsonRead.optionalString(metaObject, "locationName").ifPresent(mapMeta::setLocationName);
        MetaParsing.optionalColor(metaObject, "color").ifPresent(mapMeta::setColor);
    }
}
