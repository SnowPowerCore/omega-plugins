package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class KnowledgeBookMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public KnowledgeBookMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("knowledgebook"),
                MetaParsing.normalizeType("KnowledgeBookMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof KnowledgeBookMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof KnowledgeBookMeta knowledgeBookMeta)) {
            logger.warning("meta.type 'knowledgebook' provided but ItemMeta is not KnowledgeBookMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalStringArray(metaObject, "recipes")
                .ifPresent(rawKeys -> {
                    List<NamespacedKey> keys = new ArrayList<>(rawKeys.size());
                    for (String raw : rawKeys) {
                        MetaParsing.resolveKey(raw).ifPresent(keys::add);
                    }
                    knowledgeBookMeta.setRecipes(keys);
                });
    }
}
