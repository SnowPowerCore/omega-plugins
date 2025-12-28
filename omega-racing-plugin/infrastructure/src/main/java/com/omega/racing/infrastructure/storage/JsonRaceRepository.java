package com.omega.racing.infrastructure.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.omega.racing.core.model.RaceDefinition;

import jakarta.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonRaceRepository implements RaceRepository {

    private static final Type MAP_TYPE = new TypeToken<Map<String, RaceDefinition>>() {
    }.getType();

    private final Gson gson;

    @Inject
    public JsonRaceRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Map<String, RaceDefinition> load(Path path) {
        if (path == null || !Files.exists(path)) {
            return new LinkedHashMap<>();
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw == null || raw.isBlank()) {
                return new LinkedHashMap<>();
            }

            JsonElement el = gson.fromJson(raw, JsonElement.class);
            if (el == null || !el.isJsonObject()) {
                return new LinkedHashMap<>();
            }

            JsonObject obj = el.getAsJsonObject();
            Map<String, RaceDefinition> out = gson.fromJson(obj, MAP_TYPE);
            return out == null ? new LinkedHashMap<>() : new LinkedHashMap<>(out);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public void save(Path path, Map<String, RaceDefinition> races) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            String json = gson.toJson(races);
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
