package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public final class ArmorMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public ArmorMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("armor"),
                MetaParsing.normalizeType("ArmorMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof ArmorMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof ArmorMeta armorMeta)) {
            logger.warning("meta.type 'armor' provided but ItemMeta is not ArmorMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalObject(metaObject, "trim")
                .ifPresent(trimObj -> {
                    String materialRaw = JsonRead.optionalString(trimObj, "material").orElse("").trim();
                    String patternRaw = JsonRead.optionalString(trimObj, "pattern").orElse("").trim();
                    if (materialRaw.isEmpty() || patternRaw.isEmpty()) {
                        return;
                    }

                    Optional<TrimMaterial> materialOpt = getStaticFieldValue(TrimMaterial.class, materialRaw);
                    Optional<TrimPattern> patternOpt = getStaticFieldValue(TrimPattern.class, patternRaw);

                    if (materialOpt.isEmpty() || patternOpt.isEmpty()) {
                        logger.warning("Invalid armor trim (material='" + materialRaw + "', pattern='" + patternRaw + "') (item id=" + itemId + ")");
                        return;
                    }

                    armorMeta.setTrim(new ArmorTrim(materialOpt.get(), patternOpt.get()));
                });
    }

    private static <T> Optional<T> getStaticFieldValue(Class<?> clazz, String raw) {
        String name = raw.trim().toUpperCase(Locale.ROOT);
        try {
            Field field = clazz.getField(name);
            Object value = field.get(null);
            @SuppressWarnings("unchecked")
            T cast = (T) value;
            return Optional.ofNullable(cast);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
