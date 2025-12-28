package com.omega.interactable.infrastructure.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JsonRead {
    private JsonRead() {
    }

    public static Optional<String> optionalString(JsonObject object, String key) {
        return asString(object.get(key));
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
        Optional<JsonArray> arrayOpt = optionalArray(object, key);
        if (arrayOpt.isEmpty()) {
            return Optional.empty();
        }

        JsonArray array = arrayOpt.get();
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
}
