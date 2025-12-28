package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class RepairableMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public RepairableMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(MetaParsing.normalizeType("repairable"));
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof Repairable;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof Repairable repairable)) {
            logger.warning("meta.type 'repairable' provided but ItemMeta is not Repairable (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalInt(metaObject, "repairCost").ifPresent(repairable::setRepairCost);
    }
}
