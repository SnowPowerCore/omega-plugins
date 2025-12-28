package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class BundleMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public BundleMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("bundle"),
                MetaParsing.normalizeType("BundleMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof BundleMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        // Bundle contents require parsing nested ItemStacks; this handler currently only validates support.
        if (!(meta instanceof BundleMeta)) {
            logger.warning("meta.type 'bundle' provided but ItemMeta is not BundleMeta (item id=" + itemId + ")");
        }
    }
}
