package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import jakarta.inject.Inject;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class SpawnEggMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public SpawnEggMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("spawnegg"),
                MetaParsing.normalizeType("SpawnEggMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof SpawnEggMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof SpawnEggMeta spawnEggMeta)) {
            logger.warning("meta.type 'spawnegg' provided but ItemMeta is not SpawnEggMeta (item id=" + itemId + ")");
            return;
        }

        String typeRaw = JsonRead.optionalString(metaObject, "spawnedType")
                .or(() -> JsonRead.optionalString(metaObject, "entityType"))
                .orElse("")
                .trim();
        if (typeRaw.isEmpty()) {
            return;
        }

        try {
            spawnEggMeta.setSpawnedType(EntityType.valueOf(typeRaw.toUpperCase(Locale.ROOT)));
        } catch (Exception exception) {
            logger.warning("Unknown EntityType '" + typeRaw + "' (item id=" + itemId + ")");
        }
    }
}
