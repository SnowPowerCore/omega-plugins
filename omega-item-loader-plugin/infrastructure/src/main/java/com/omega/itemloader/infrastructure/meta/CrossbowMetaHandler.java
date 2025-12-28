package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class CrossbowMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public CrossbowMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("crossbow"),
                MetaParsing.normalizeType("CrossbowMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof CrossbowMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof CrossbowMeta crossbowMeta)) {
            logger.warning("meta.type 'crossbow' provided but ItemMeta is not CrossbowMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalStringArray(metaObject, "chargedProjectiles")
                .ifPresent(projectiles -> {
                    List<ItemStack> stacks = new ArrayList<>(projectiles.size());
                    for (String matRaw : projectiles) {
                        MetaParsing.resolveMaterial(matRaw).ifPresentOrElse(
                                material -> stacks.add(new ItemStack(material, 1)),
                                () -> logger.warning("Unknown material for charged projectile: " + matRaw + " (item id=" + itemId + ")")
                        );
                    }
                    crossbowMeta.setChargedProjectiles(stacks);
                });
    }
}
