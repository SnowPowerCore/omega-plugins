package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.Locale;
import java.util.logging.Logger;

public final class BookMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public BookMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("book"),
                MetaParsing.normalizeType("BookMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof BookMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof BookMeta bookMeta)) {
            logger.warning("meta.type 'book' provided but ItemMeta is not BookMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalString(metaObject, "title").ifPresent(bookMeta::setTitle);
        JsonRead.optionalString(metaObject, "author").ifPresent(bookMeta::setAuthor);
        JsonRead.optionalStringArray(metaObject, "pages").ifPresent(bookMeta::setPages);

        JsonRead.optionalString(metaObject, "generation")
                .map(s -> s.toUpperCase(Locale.ROOT))
                .ifPresent(raw -> {
                    try {
                        bookMeta.setGeneration(BookMeta.Generation.valueOf(raw));
                    } catch (Exception exception) {
                        logger.warning("Unknown BookMeta generation '" + raw + "' (item id=" + itemId + ")");
                    }
                });
    }
}
