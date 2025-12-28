package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class BannerMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public BannerMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("banner"),
                MetaParsing.normalizeType("BannerMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof BannerMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof BannerMeta bannerMeta)) {
            logger.warning("meta.type 'banner' provided but ItemMeta is not BannerMeta (item id=" + itemId + ")");
            return;
        }

        MetaParsing.optionalDyeColor(metaObject, "baseColor").ifPresent(bannerMeta::setBaseColor);

        JsonElement patternsEl = metaObject.get("patterns");
        if (patternsEl == null || patternsEl.isJsonNull() || !patternsEl.isJsonArray()) {
            return;
        }

        List<Pattern> patterns = new ArrayList<>();
        for (JsonElement el : patternsEl.getAsJsonArray()) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject patternObj = el.getAsJsonObject();

            String colorRaw = JsonRead.optionalString(patternObj, "color").orElse("").trim();
            String patternRaw = JsonRead.optionalString(patternObj, "pattern").orElse("").trim();
            if (colorRaw.isEmpty() || patternRaw.isEmpty()) {
                continue;
            }

            DyeColor color;
            try {
                color = DyeColor.valueOf(colorRaw.toUpperCase(Locale.ROOT));
            } catch (Exception exception) {
                logger.warning("Unknown DyeColor '" + colorRaw + "' (item id=" + itemId + ")");
                continue;
            }

            PatternType type;
            try {
                type = PatternType.valueOf(patternRaw.toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {
                type = PatternType.getByIdentifier(patternRaw.toLowerCase(Locale.ROOT));
                if (type == null) {
                    logger.warning("Unknown PatternType '" + patternRaw + "' (item id=" + itemId + ")");
                    continue;
                }
            }

            patterns.add(new Pattern(color, type));
        }

        if (!patterns.isEmpty()) {
            bannerMeta.setPatterns(patterns);
        }
    }
}
