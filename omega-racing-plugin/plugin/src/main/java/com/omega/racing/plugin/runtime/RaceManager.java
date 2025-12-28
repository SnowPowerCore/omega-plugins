package com.omega.racing.plugin.runtime;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.infrastructure.storage.RaceRepository;

import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class RaceManager {

    private final Logger logger;
    private final RaceRepository repository;
    private final Path racesFile;

    private final Map<String, RaceDefinition> races = new LinkedHashMap<>();

    @Inject
    public RaceManager(Logger logger, RaceRepository repository, Path racesFile) {
        this.logger = logger;
        this.repository = repository;
        this.racesFile = racesFile;
    }

    public void load() {
        races.clear();
        races.putAll(repository.load(racesFile));
        logger.info("Loaded " + races.size() + " races");
    }

    public void save() {
        repository.save(racesFile, races);
    }

    public boolean create(String raceName) {
        String key = normalize(raceName);
        if (key.isEmpty() || races.containsKey(key)) {
            return false;
        }

        RaceDefinition race = new RaceDefinition(raceName.trim());
        races.put(key, race);
        save();
        return true;
    }

    public boolean delete(String raceName) {
        String key = normalize(raceName);
        boolean removed = races.remove(key) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Optional<RaceDefinition> get(String raceName) {
        String key = normalize(raceName);
        return Optional.ofNullable(races.get(key));
    }

    public boolean rename(String oldName, String newName) {
        String oldKey = normalize(oldName);
        String newKey = normalize(newName);
        if (oldKey.isEmpty() || newKey.isEmpty()) {
            return false;
        }
        if (!races.containsKey(oldKey)) {
            return false;
        }
        if (!oldKey.equals(newKey) && races.containsKey(newKey)) {
            return false;
        }

        RaceDefinition race = races.remove(oldKey);
        if (race == null) {
            return false;
        }

        race.setName(newName.trim());
        races.put(newKey, race);
        save();
        return true;
    }

    public boolean setQualificationLaps(String raceName, int laps) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.getQualification().setLaps(laps);
        save();
        return true;
    }

    public boolean setQualificationTimeLimitSeconds(String raceName, int timeLimitSeconds) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.getQualification().setTimeLimitSeconds(timeLimitSeconds);
        save();
        return true;
    }

    public boolean setRaceLaps(String raceName, int laps) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.getRace().setLaps(laps);
        save();
        return true;
    }

    public boolean setSections(String raceName, int sections) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.setSections(sections);
        save();
        return true;
    }

    public boolean setPositions(String raceName, int positions) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.setPositions(positions);
        save();
        return true;
    }

    public boolean setFreePracticeTimeLimitSeconds(String raceName, int timeLimitSeconds) {
        String key = normalize(raceName);
        RaceDefinition race = races.get(key);
        if (race == null) {
            return false;
        }
        race.getFreePractice().setTimeLimitSeconds(timeLimitSeconds);
        save();
        return true;
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.trim().toLowerCase(Locale.ROOT);
        return cleaned.replace(' ', '_');
    }
}
