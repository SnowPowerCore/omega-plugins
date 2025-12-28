package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public interface ItemMetaHandler {

    /**
     * The values accepted in JSON under meta.type.
     *
     * Values should already be normalized via {@link MetaParsing#normalizeType(String)}.
     */
    Set<String> types();

    /**
     * Whether this handler can apply to the given ItemMeta instance.
     */
    boolean supports(ItemMeta meta);

    /**
     * Apply meta-specific fields. Implementations should validate fields as needed.
     */
    void apply(String itemId, JsonObject metaObject, ItemMeta meta);
}
