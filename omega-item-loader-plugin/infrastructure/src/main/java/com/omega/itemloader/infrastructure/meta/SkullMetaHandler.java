package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class SkullMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public SkullMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("skull"),
                MetaParsing.normalizeType("SkullMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof SkullMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof SkullMeta skullMeta)) {
            logger.warning("meta.type 'skull' provided but ItemMeta is not SkullMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalString(metaObject, "owner").ifPresent(skullMeta::setOwner);

        JsonRead.optionalString(metaObject, "noteBlockSound")
                .flatMap(MetaParsing::resolveKey)
                .ifPresent(skullMeta::setNoteBlockSound);
    }
}
