package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

public final class SuspiciousStewMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public SuspiciousStewMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("suspiciousstew"),
                MetaParsing.normalizeType("SuspiciousStewMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof SuspiciousStewMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof SuspiciousStewMeta stewMeta)) {
            logger.warning("meta.type 'suspiciousstew' provided but ItemMeta is not SuspiciousStewMeta (item id=" + itemId + ")");
            return;
        }

        for (PotionEffect effect : MetaParsing.parsePotionEffectsArray(metaObject, "customEffects")) {
            stewMeta.addCustomEffect(effect, true);
        }
    }
}
