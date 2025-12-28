package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

import jakarta.inject.Inject;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class TropicalFishBucketMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public TropicalFishBucketMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("tropicalfishbucket"),
                MetaParsing.normalizeType("TropicalFishBucketMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof TropicalFishBucketMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof TropicalFishBucketMeta fishMeta)) {
            logger.warning("meta.type 'tropicalfishbucket' provided but ItemMeta is not TropicalFishBucketMeta (item id=" + itemId + ")");
            return;
        }

        MetaParsing.optionalDyeColor(metaObject, "patternColor").ifPresent(fishMeta::setPatternColor);
        MetaParsing.optionalDyeColor(metaObject, "bodyColor").ifPresent(fishMeta::setBodyColor);

        JsonRead.optionalString(metaObject, "pattern")
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .ifPresent(raw -> {
                    try {
                        fishMeta.setPattern(TropicalFish.Pattern.valueOf(raw));
                    } catch (Exception exception) {
                        logger.warning("Unknown TropicalFish.Pattern '" + raw + "' (item id=" + itemId + ")");
                    }
                });
    }
}
