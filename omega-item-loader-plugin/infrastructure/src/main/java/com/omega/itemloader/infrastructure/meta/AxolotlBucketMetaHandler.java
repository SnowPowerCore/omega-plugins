package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class AxolotlBucketMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public AxolotlBucketMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("axolotlbucket"),
                MetaParsing.normalizeType("AxolotlBucketMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof AxolotlBucketMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof AxolotlBucketMeta bucketMeta)) {
            logger.warning("meta.type 'axolotlbucket' provided but ItemMeta is not AxolotlBucketMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalString(metaObject, "variant")
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .ifPresent(raw -> {
                    try {
                        bucketMeta.setVariant(Axolotl.Variant.valueOf(raw));
                    } catch (Exception exception) {
                        logger.warning("Unknown Axolotl.Variant '" + raw + "' (item id=" + itemId + ")");
                    }
                });
    }
}
