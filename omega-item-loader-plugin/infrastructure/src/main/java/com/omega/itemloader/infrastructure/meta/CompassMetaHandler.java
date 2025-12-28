package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Location;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class CompassMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public CompassMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("compass"),
                MetaParsing.normalizeType("CompassMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof CompassMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof CompassMeta compassMeta)) {
            logger.warning("meta.type 'compass' provided but ItemMeta is not CompassMeta (item id=" + itemId + ")");
            return;
        }

        MetaParsing.optionalLocation(metaObject, "lodestone").ifPresent(compassMeta::setLodestone);
        JsonRead.optionalBoolean(metaObject, "lodestoneTracked").ifPresent(compassMeta::setLodestoneTracked);
    }
}
