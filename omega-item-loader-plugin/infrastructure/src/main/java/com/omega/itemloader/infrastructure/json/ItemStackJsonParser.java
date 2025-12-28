package com.omega.itemloader.infrastructure.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.core.OmegaItemLoaderKeys;
import com.omega.itemloader.infrastructure.meta.ItemMetaHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public final class ItemStackJsonParser {

    private final Logger logger;
    private final Set<ItemMetaHandler> metaHandlers;
    private final Gson gson = new GsonBuilder().create();

    @Inject
    public ItemStackJsonParser(Logger logger, Set<ItemMetaHandler> metaHandlers) {
        this.logger = logger;
        this.metaHandlers = metaHandlers;
    }

    public ParsedItem parse(JsonObject object) {
        String id = JsonRead.requiredString(object, "id");

        // referenceId is the preferred lookup key; customId is supported as a legacy alias.
        String referenceId = JsonRead.optionalString(object, "referenceId")
            .or(() -> JsonRead.optionalString(object, "customId"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElse(null);

        String primaryId = referenceId != null ? referenceId : id;
        String materialRaw = JsonRead.requiredString(object, "material");

        Material material = resolveMaterial(materialRaw)
                .orElseThrow(() -> new IllegalArgumentException("Unknown material: " + materialRaw));

        int amount = JsonRead.optionalInt(object, "amount").orElse(1);
        if (amount < 1) {
            amount = 1;
        }
        int maxStackSize = Math.max(1, material.getMaxStackSize());
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }

        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            // Always store the resolved reference id so other plugins can identify template items.
            meta.getPersistentDataContainer().set(OmegaItemLoaderKeys.REFERENCE_ID, PersistentDataType.STRING, primaryId);

            JsonRead.optionalString(object, "name")
                    .map(ItemStackJsonParser::colorize)
                    .ifPresent(meta::setDisplayName);

            JsonRead.optionalStringArray(object, "lore")
                    .map(list -> {
                        List<String> lore = new ArrayList<>(list.size());
                        for (String line : list) {
                            lore.add(colorize(line));
                        }
                        return lore;
                    })
                    .ifPresent(meta::setLore);

            JsonRead.optionalBoolean(object, "unbreakable").ifPresent(meta::setUnbreakable);
            JsonRead.optionalInt(object, "customModelData").ifPresent(meta::setCustomModelData);

            JsonRead.optionalStringArray(object, "flags")
                    .ifPresent(flags -> {
                        for (String flagRaw : flags) {
                            try {
                                meta.addItemFlags(ItemFlag.valueOf(flagRaw.toUpperCase(Locale.ROOT)));
                            } catch (Exception exception) {
                                logger.warning("Unknown ItemFlag: " + flagRaw + " (item id=" + id + ")");
                            }
                        }
                    });

            JsonRead.optionalObject(object, "enchants")
                    .ifPresent(enchants -> {
                        for (Map.Entry<String, JsonElement> entry : enchants.entrySet()) {
                            String enchantRaw = entry.getKey();
                            int level = JsonRead.asInt(entry.getValue()).orElse(1);

                            resolveEnchantment(enchantRaw).ifPresentOrElse(
                                    enchantment -> meta.addEnchant(enchantment, level, true),
                                    () -> logger.warning("Unknown enchantment: " + enchantRaw + " (item id=" + id + ")")
                            );
                        }
                    });

            JsonRead.optionalObject(object, "additionalInfo")
                    .ifPresent(additional -> {
                        Map<String, String> map = new HashMap<>();

                        for (Map.Entry<String, JsonElement> entry : additional.entrySet()) {
                            String key = entry.getKey();
                            JsonElement valueEl = entry.getValue();

                            // Preserve non-string primitives (booleans/numbers) by stringifying.
                            // For objects/arrays, store their JSON representation.
                            String value = JsonRead.asString(valueEl)
                                    .or(() -> {
                                        if (valueEl != null && valueEl.isJsonPrimitive()) {
                                            try {
                                                return Optional.of(valueEl.getAsJsonPrimitive().getAsString());
                                            } catch (Exception ignored) {
                                            }
                                        }
                                        return Optional.empty();
                                    })
                                    .orElseGet(() -> valueEl == null ? "" : gson.toJson(valueEl));
                            map.put(key, value);
                        }

                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(OmegaItemLoaderKeys.ADDITIONAL_INFO, PersistentDataType.STRING, gson.toJson(map));
                    });

            JsonRead.optionalArray(object, "interaction")
                    .ifPresent(interactionArray -> {
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(OmegaItemLoaderKeys.INTERACTION, PersistentDataType.STRING, gson.toJson(interactionArray));
                    });

            applyMetaType(id, object, meta);

            itemStack.setItemMeta(meta);
        }

        return new ParsedItem(id, referenceId, itemStack);
    }

    private void applyMetaType(String id, JsonObject object, ItemMeta meta) {
        Optional<JsonObject> metaObjOpt = JsonRead.optionalObject(object, "meta");
        if (metaObjOpt.isEmpty()) {
            return;
        }

        JsonObject metaObj = metaObjOpt.get();
        String typeRaw = JsonRead.optionalString(metaObj, "type").orElse("");
        String type = com.omega.itemloader.infrastructure.meta.MetaParsing.normalizeType(typeRaw);
        if (type.isEmpty()) {
            return;
        }

        for (ItemMetaHandler handler : metaHandlers) {
            if (!handler.types().contains(type)) {
                continue;
            }
            if (!handler.supports(meta)) {
                logger.warning("meta.type '" + type + "' provided but ItemMeta type is not supported by handler (item id=" + id + ")");
                return;
            }

            handler.apply(id, metaObj, meta);
            return;
        }

        logger.warning("Unsupported meta.type '" + typeRaw + "' (item id=" + id + ")");
    }

    private static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(input, ""));
    }

    private static Optional<Material> resolveMaterial(String raw) {
        String cleaned = raw.trim();
        int colon = cleaned.indexOf(':');
        if (colon >= 0) {
            cleaned = cleaned.substring(colon + 1);
        }
        cleaned = cleaned.replace(' ', '_').toUpperCase(Locale.ROOT);

        Material material = Material.matchMaterial(cleaned);
        return Optional.ofNullable(material);
    }

    private static Optional<Enchantment> resolveEnchantment(String raw) {
        String cleaned = raw.trim();

        NamespacedKey key = null;
        if (cleaned.contains(":")) {
            key = NamespacedKey.fromString(cleaned.toLowerCase(Locale.ROOT));
        } else {
            key = NamespacedKey.minecraft(cleaned.toLowerCase(Locale.ROOT));
        }

        if (key != null) {
            Enchantment byKey = Enchantment.getByKey(key);
            if (byKey != null) {
                return Optional.of(byKey);
            }
        }

        Enchantment byName = Enchantment.getByName(cleaned.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(byName);
    }
}
