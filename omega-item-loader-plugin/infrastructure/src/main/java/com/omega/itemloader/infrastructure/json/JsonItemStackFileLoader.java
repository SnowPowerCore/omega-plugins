package com.omega.itemloader.infrastructure.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omega.itemloader.infrastructure.ItemStackFileLoader;
import org.bukkit.inventory.ItemStack;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JsonItemStackFileLoader implements ItemStackFileLoader {

    private final Logger logger;
    private final ItemStackJsonParser parser;

    private final Gson gson = new GsonBuilder().create();

    @Inject
    public JsonItemStackFileLoader(Logger logger, ItemStackJsonParser parser) {
        this.logger = logger;
        this.parser = parser;
    }

    @Override
    public Map<String, ItemStack> load(Path path) {
        String json;
        try {
            json = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read items file: " + path, exception);
        }

        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid JSON in items file: " + path, exception);
        }

        if (!root.isJsonArray()) {
            throw new IllegalStateException("Items file must be a JSON array: " + path);
        }

        JsonArray array = root.getAsJsonArray();
        Map<String, ItemStack> result = new LinkedHashMap<>();

        for (int index = 0; index < array.size(); index++) {
            JsonElement element = array.get(index);
            if (!element.isJsonObject()) {
                logger.warning("Skipping non-object element at index " + index + " in " + path);
                continue;
            }

            JsonObject object = element.getAsJsonObject();
            try {
                ParsedItem parsed = parser.parse(object);

                String primaryId = parsed.referenceId() != null ? parsed.referenceId() : parsed.id();
                putWithWarn(result, primaryId, parsed.itemStack(), path);

                // Backwards compatibility: keep the old "id" key too (unless it's identical).
                if (parsed.referenceId() != null && !parsed.referenceId().equals(parsed.id())) {
                    putWithWarn(result, parsed.id(), parsed.itemStack(), path);
                }
            } catch (Exception exception) {
                logger.log(Level.WARNING, "Failed to parse item at index " + index + " in " + path + ": " + exception.getMessage(), exception);
            }
        }

        return result;
    }

    private void putWithWarn(Map<String, ItemStack> result, String id, ItemStack itemStack, Path path) {
        if (result.containsKey(id)) {
            logger.warning("Duplicate item id '" + id + "' in " + path + "; overwriting");
        }
        result.put(id, itemStack);
    }
}
