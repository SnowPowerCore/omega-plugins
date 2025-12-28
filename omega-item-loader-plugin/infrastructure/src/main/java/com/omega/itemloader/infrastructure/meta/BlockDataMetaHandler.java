package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class BlockDataMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public BlockDataMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("blockdata"),
                MetaParsing.normalizeType("BlockDataMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof BlockDataMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof BlockDataMeta blockDataMeta)) {
            logger.warning("meta.type 'blockdata' provided but ItemMeta is not BlockDataMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalString(metaObject, "blockData")
                .ifPresent(raw -> {
                    try {
                        BlockData data = Bukkit.createBlockData(raw);
                        blockDataMeta.setBlockData(data);
                    } catch (Exception exception) {
                        logger.warning("Invalid blockData '" + raw + "' (item id=" + itemId + ")");
                    }
                });
    }
}
