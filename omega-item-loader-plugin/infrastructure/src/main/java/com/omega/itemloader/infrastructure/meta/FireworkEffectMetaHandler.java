package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class FireworkEffectMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public FireworkEffectMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("fireworkeffect"),
                MetaParsing.normalizeType("FireworkEffectMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof FireworkEffectMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof FireworkEffectMeta fireworkEffectMeta)) {
            logger.warning("meta.type 'fireworkeffect' provided but ItemMeta is not FireworkEffectMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalObject(metaObject, "effect")
                .flatMap(effectObj -> parseEffect(itemId, effectObj))
                .ifPresent(fireworkEffectMeta::setEffect);
    }

    static java.util.Optional<FireworkEffect> parseEffect(String itemId, JsonObject effectObj) {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        JsonRead.optionalString(effectObj, "type")
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .ifPresent(raw -> {
                    try {
                        builder.with(FireworkEffect.Type.valueOf(raw));
                    } catch (Exception ignored) {
                        // default type
                    }
                });

        JsonRead.optionalBoolean(effectObj, "flicker").ifPresent(f -> {
            if (f) {
                builder.withFlicker();
            }
        });
        JsonRead.optionalBoolean(effectObj, "trail").ifPresent(t -> {
            if (t) {
                builder.withTrail();
            }
        });

        List<Color> colors = parseColors(effectObj.get("colors"));
        if (!colors.isEmpty()) {
            builder.withColor(colors);
        }

        List<Color> fade = parseColors(effectObj.get("fadeColors"));
        if (!fade.isEmpty()) {
            builder.withFade(fade);
        }

        try {
            return java.util.Optional.of(builder.build());
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private static List<Color> parseColors(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return List.of();
        }

        List<Color> colors = new ArrayList<>();
        for (JsonElement el : element.getAsJsonArray()) {
            if (el == null || el.isJsonNull()) {
                continue;
            }

            if (el.isJsonPrimitive()) {
                String s = JsonRead.asString(el).orElse("").trim();
                if (!s.isEmpty()) {
                    try {
                        DyeColor dye = DyeColor.valueOf(s.toUpperCase(Locale.ROOT));
                        colors.add(dye.getFireworkColor());
                        continue;
                    } catch (Exception ignored) {
                    }
                }
            }

            MetaParsing.asColor(el).ifPresent(colors::add);
        }

        return colors;
    }
}
