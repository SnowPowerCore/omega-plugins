package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class FireworkMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public FireworkMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("firework"),
                MetaParsing.normalizeType("FireworkMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof FireworkMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof FireworkMeta fireworkMeta)) {
            logger.warning("meta.type 'firework' provided but ItemMeta is not FireworkMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalInt(metaObject, "power").ifPresent(fireworkMeta::setPower);

        JsonElement effectsEl = metaObject.get("effects");
        if (effectsEl == null || effectsEl.isJsonNull() || !effectsEl.isJsonArray()) {
            return;
        }

        List<FireworkEffect> effects = new ArrayList<>();
        for (JsonElement el : effectsEl.getAsJsonArray()) {
            if (!el.isJsonObject()) {
                continue;
            }
            FireworkEffectMetaHandler.parseEffect(itemId, el.getAsJsonObject()).ifPresent(effects::add);
        }

        if (!effects.isEmpty()) {
            fireworkMeta.clearEffects();
            fireworkMeta.addEffects(effects);
        }
    }
}
