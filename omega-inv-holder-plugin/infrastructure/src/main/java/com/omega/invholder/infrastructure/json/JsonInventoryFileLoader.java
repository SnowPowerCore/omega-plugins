package com.omega.invholder.infrastructure.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omega.invholder.infrastructure.InventoryFileLoader;
import com.omega.invholder.infrastructure.model.InventoryTemplate;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JsonInventoryFileLoader implements InventoryFileLoader {

    private final Logger logger;
    private final InventoryJsonParser parser;

    private final Gson gson = new GsonBuilder().create();

    @Inject
    public JsonInventoryFileLoader(Logger logger, InventoryJsonParser parser) {
        this.logger = logger;
        this.parser = parser;
    }

    @Override
    public Map<String, InventoryTemplate> load(Path path) {
        String json;
        try {
            json = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read invs file: " + path, exception);
        }

        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid JSON in invs file: " + path, exception);
        }

        if (!root.isJsonArray()) {
            throw new IllegalStateException("Invs file must be a JSON array: " + path);
        }

        JsonArray array = root.getAsJsonArray();
        Map<String, InventoryTemplate> result = new LinkedHashMap<>();

        for (int index = 0; index < array.size(); index++) {
            JsonElement element = array.get(index);
            if (!element.isJsonObject()) {
                logger.warning("Skipping non-object element at index " + index + " in " + path);
                continue;
            }

            JsonObject object = element.getAsJsonObject();
            try {
                InventoryTemplate inv = parser.parse(object);
                if (result.containsKey(inv.id())) {
                    logger.warning("Duplicate inventory id '" + inv.id() + "' in " + path + "; overwriting");
                }
                result.put(inv.id(), inv);
            } catch (Exception exception) {
                logger.log(Level.WARNING, "Failed to parse inventory at index " + index + " in " + path + ": " + exception.getMessage(), exception);
            }
        }

        return result;
    }
}
