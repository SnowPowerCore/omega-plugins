package com.omega.racing.plugin.runtime.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.omega.racing.plugin.runtime.RaceManager;

import jakarta.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class RaceStageStateRepository {

    private final JavaPlugin plugin;
    private final Gson gson;

    @Inject
    public RaceStageStateRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Optional<RaceStageState> load(String raceName) {
        try {
            Path path = fileForRace(raceName);
            if (!Files.exists(path)) {
                return Optional.empty();
            }
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            JsonElement el = gson.fromJson(raw, JsonElement.class);
            if (el == null) {
                return Optional.empty();
            }
            RaceStageState state = gson.fromJson(el, RaceStageState.class);
            return Optional.ofNullable(state);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void save(String raceName, RaceStageState state) {
        try {
            Path path = fileForRace(raceName);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, gson.toJson(state), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private Path fileForRace(String raceName) {
        String safe = normalizeKey(raceName);
        if (safe.isBlank()) {
            safe = "unknown";
        }
        return plugin.getDataFolder().toPath()
            .resolve("stage")
            .resolve(safe + ".json");
    }

    private static String normalizeKey(String name) {
        if (name == null) {
            return "";
        }
        String s = name.trim().toLowerCase(Locale.ROOT);
        // Keep filenames conservative on Windows.
        s = s.replaceAll("[^a-z0-9._-]+", "_");
        while (s.contains("__")) {
            s = s.replace("__", "_");
        }
        return s;
    }
}
