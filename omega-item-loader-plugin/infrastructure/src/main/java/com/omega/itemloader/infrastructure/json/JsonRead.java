package com.omega.itemloader.infrastructure.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JsonRead {
    private JsonRead() {
    }

    public static String requiredString(JsonObject object, String key) {
        return optionalString(object, key)
                .orElseThrow(() -> new IllegalArgumentException("Missing required field '" + key + "'"));
    }

    public static Optional<String> optionalString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return asString(element);
    }

    public static Optional<Integer> optionalInt(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return asInt(element);
    }

    public static Optional<Boolean> optionalBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return asBoolean(element);
    }

    public static Optional<JsonObject> optionalObject(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonObject()) {
            return Optional.empty();
        }
        return Optional.of(element.getAsJsonObject());
    }

    public static Optional<JsonArray> optionalArray(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonArray()) {
            return Optional.empty();
        }
        return Optional.of(element.getAsJsonArray());
    }

    public static Optional<List<String>> optionalStringArray(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonArray()) {
            return Optional.empty();
        }

        JsonArray array = element.getAsJsonArray();
        List<String> result = new ArrayList<>(array.size());
        for (JsonElement item : array) {
            asString(item).ifPresent(result::add);
        }
        return Optional.of(result);
    }

    public static Optional<String> asString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonPrimitive()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(element.getAsString());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> asInt(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonPrimitive()) {
            return Optional.empty();
        }
        try {
            return Optional.of(element.getAsInt());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> asBoolean(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (!element.isJsonPrimitive()) {
            return Optional.empty();
        }
        try {
            return Optional.of(element.getAsBoolean());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
