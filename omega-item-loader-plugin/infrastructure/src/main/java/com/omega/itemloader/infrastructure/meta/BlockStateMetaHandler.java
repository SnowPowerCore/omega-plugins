package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class BlockStateMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public BlockStateMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("blockstate"),
                MetaParsing.normalizeType("BlockStateMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof BlockStateMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        // BlockStateMeta can represent many different block states (e.g., shulker boxes).
        // This handler currently only validates support; block state serialization is out of scope.
        if (!(meta instanceof BlockStateMeta)) {
            logger.warning("meta.type 'blockstate' provided but ItemMeta is not BlockStateMeta (item id=" + itemId + ")");
        }
    }
}
