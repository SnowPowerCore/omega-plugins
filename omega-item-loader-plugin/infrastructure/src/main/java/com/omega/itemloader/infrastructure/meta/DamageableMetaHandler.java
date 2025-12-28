package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class DamageableMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public DamageableMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(MetaParsing.normalizeType("damageable"));
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof Damageable;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof Damageable damageable)) {
            logger.warning("meta.type 'damageable' provided but ItemMeta is not Damageable (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalInt(metaObject, "damage").ifPresent(damageable::setDamage);
    }
}
