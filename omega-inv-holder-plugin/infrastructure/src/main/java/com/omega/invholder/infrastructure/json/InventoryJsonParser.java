package com.omega.invholder.infrastructure.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.invholder.infrastructure.model.InventoryCommand;
import com.omega.invholder.infrastructure.model.InventorySlotItem;
import com.omega.invholder.infrastructure.model.InventoryTemplate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public final class InventoryJsonParser {

    private final Logger logger;

    @Inject
    public InventoryJsonParser(Logger logger) {
        this.logger = logger;
    }

    public InventoryTemplate parse(JsonObject object) {
        String id = JsonRead.requiredString(object, "id");

        InventoryType type = JsonRead.optionalString(object, "type")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(this::resolveInventoryType)
                .orElse(InventoryType.CHEST);

        int size;
        if (type == InventoryType.CHEST) {
            size = JsonRead.optionalInt(object, "size").orElse(27);
            if (size <= 0 || size % 9 != 0) {
                throw new IllegalArgumentException("Inventory size must be divisible by 9 for CHEST (id=" + id + ")");
            }
        } else {
            int defaultSize = Math.max(0, type.getDefaultSize());
            size = defaultSize;

            JsonRead.optionalInt(object, "size").ifPresent(provided -> {
                if (provided != defaultSize) {
                    logger.warning("Ignoring size=" + provided + " for inventory type " + type + " (defaultSize=" + defaultSize + ") (id=" + id + ")");
                }
            });
        }

        String title = JsonRead.optionalString(object, "name").orElse("");

        InventoryCommand command = JsonRead.optionalObject(object, "command")
                .flatMap(cmd -> {
                    String name = JsonRead.optionalString(cmd, "name")
                            .map(String::trim)
                            .orElse("");
                    if (name.isEmpty()) {
                        return Optional.empty();
                    }

                    String permission = JsonRead.optionalString(cmd, "permission")
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .orElse(null);

                    return Optional.of(new InventoryCommand(name, permission));
                })
                .orElse(null);

        List<InventorySlotItem> items = new ArrayList<>();
        JsonElement itemsEl = object.get("items");
        if (itemsEl != null && !itemsEl.isJsonNull() && itemsEl.isJsonArray()) {
            for (JsonElement e : itemsEl.getAsJsonArray()) {
                if (e == null || e.isJsonNull() || !e.isJsonObject()) {
                    continue;
                }
                parseSlotItem(id, e.getAsJsonObject()).ifPresent(items::add);
            }
        }

        return new InventoryTemplate(id, type, size, title, items, command);
    }

    private Optional<InventorySlotItem> parseSlotItem(String invId, JsonObject obj) {
        int slot = JsonRead.optionalInt(obj, "slot").orElse(-1);
        if (slot < 0) {
            return Optional.empty();
        }

        String ref = JsonRead.optionalString(obj, "referenceId")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse("");
        if (!ref.isEmpty()) {
            return Optional.of(new InventorySlotItem.ReferenceItem(slot, ref));
        }

        String materialRaw = JsonRead.optionalString(obj, "material")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse("");
        if (materialRaw.isEmpty()) {
            return Optional.empty();
        }

        Optional<Material> matOpt = resolveMaterial(materialRaw);
        if (matOpt.isEmpty()) {
            logger.warning("Unknown material '" + materialRaw + "' in inventory '" + invId + "'");
            return Optional.empty();
        }

        int amount = JsonRead.optionalInt(obj, "amount").orElse(1);
        return Optional.of(new InventorySlotItem.MaterialItem(slot, matOpt.get(), amount));
    }

    private Optional<InventoryType> resolveInventoryType(String raw) {
        String cleaned = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(InventoryType.valueOf(cleaned));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Material> resolveMaterial(String raw) {
        String cleaned = raw.trim();
        int colon = cleaned.indexOf(':');
        if (colon >= 0) {
            cleaned = cleaned.substring(colon + 1);
        }
        cleaned = cleaned.replace(' ', '_').toUpperCase(Locale.ROOT);
        return Optional.ofNullable(Material.matchMaterial(cleaned));
    }
}
