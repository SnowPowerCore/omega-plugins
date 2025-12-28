package com.omega.interactable.infrastructure.parse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.interactable.infrastructure.json.JsonRead;
import com.omega.interactable.infrastructure.model.InteractionDefinition;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class InteractionConfigParser {

    private final Gson gson;

    @Inject
    public InteractionConfigParser(Gson gson) {
        this.gson = gson;
    }

    public List<InteractionDefinition> parse(String rawJsonArray) {
        if (rawJsonArray == null || rawJsonArray.isBlank()) {
            return List.of();
        }

        JsonElement el;
        try {
            el = gson.fromJson(rawJsonArray, JsonElement.class);
        } catch (Exception e) {
            return List.of();
        }

        if (el == null || !el.isJsonArray()) {
            return List.of();
        }

        JsonArray arr = el.getAsJsonArray();
        List<InteractionDefinition> result = new ArrayList<>(arr.size());

        for (JsonElement entry : arr) {
            if (entry == null || entry.isJsonNull() || !entry.isJsonObject()) {
                continue;
            }

            JsonObject obj = entry.getAsJsonObject();

            // "type" is an array of tokens describing the interaction.
            Set<String> tokens = new HashSet<>();
            JsonRead.optionalStringArray(obj, "type").ifPresent(list -> {
                for (String t : list) {
                    if (t == null) {
                        continue;
                    }
                    String normalized = t.trim().toUpperCase(Locale.ROOT);
                    if (!normalized.isEmpty()) {
                        tokens.add(normalized);
                    }
                }
            });

            List<String> actions = JsonRead.optionalStringArray(obj, "actions")
                    .orElse(List.of());

            if (tokens.isEmpty() || actions.isEmpty()) {
                continue;
            }

            result.add(new InteractionDefinition(Set.copyOf(tokens), List.copyOf(actions)));
        }

        return List.copyOf(result);
    }
}
