package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class PotionMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public PotionMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("potion"),
                MetaParsing.normalizeType("PotionMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof PotionMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof PotionMeta potionMeta)) {
            logger.warning("meta.type 'potion' provided but ItemMeta is not PotionMeta (item id=" + itemId + ")");
            return;
        }

        // basePotionData: { type: "SPEED", extended: true, upgraded: false }
        JsonRead.optionalObject(metaObject, "basePotionData")
                .ifPresent(base -> {
                    String typeRaw = JsonRead.optionalString(base, "type").orElse("").trim();
                    if (typeRaw.isEmpty()) {
                        return;
                    }

                    boolean extended = JsonRead.optionalBoolean(base, "extended").orElse(false);
                    boolean upgraded = JsonRead.optionalBoolean(base, "upgraded").orElse(false);

                    try {
                        PotionType potionType = PotionType.valueOf(typeRaw.toUpperCase(Locale.ROOT));
                        potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
                    } catch (Exception exception) {
                        logger.warning("Unknown PotionType '" + typeRaw + "' (item id=" + itemId + ")");
                    }
                });

        MetaParsing.optionalColor(metaObject, "color").ifPresent(potionMeta::setColor);

        for (PotionEffect effect : MetaParsing.parsePotionEffectsArray(metaObject, "customEffects")) {
            potionMeta.addCustomEffect(effect, true);
        }

        JsonRead.optionalString(metaObject, "mainEffect")
                .flatMap(MetaParsing::resolvePotionEffectType)
                .ifPresent(potionMeta::setMainEffect);
    }
}
