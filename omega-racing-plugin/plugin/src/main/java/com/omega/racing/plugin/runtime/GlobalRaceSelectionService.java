package com.omega.racing.plugin.runtime;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Stores the currently selected race globally (server-wide), independent of per-player /race edit state.
 */
@Singleton
public final class GlobalRaceSelectionService {

    private final Path selectionFile;

    private volatile String currentRaceName;

    @Inject
    public GlobalRaceSelectionService(@com.google.inject.name.Named("dataFolder") Path dataFolder) {
        this.selectionFile = dataFolder.resolve("current-race.txt");
        this.currentRaceName = loadBestEffort();
    }

    public String get() {
        String v = currentRaceName;
        return v == null || v.isBlank() ? null : v;
    }

    public void set(String raceName) {
        String cleaned = raceName == null ? null : raceName.trim();
        if (cleaned == null || cleaned.isBlank()) {
            currentRaceName = null;
            saveBestEffort(null);
            return;
        }
        currentRaceName = cleaned;
        saveBestEffort(cleaned);
    }

    private String loadBestEffort() {
        try {
            if (!Files.exists(selectionFile)) {
                return null;
            }
            String raw = Files.readString(selectionFile, StandardCharsets.UTF_8);
            if (raw == null) {
                return null;
            }
            String cleaned = raw.trim();
            return cleaned.isBlank() ? null : cleaned;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void saveBestEffort(String raceName) {
        try {
            if (selectionFile.getParent() != null) {
                Files.createDirectories(selectionFile.getParent());
            }
            String content = raceName == null ? "" : raceName;
            Files.writeString(selectionFile, content, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }
}
