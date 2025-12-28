package com.omega.racing.infrastructure.storage;

import com.omega.racing.core.model.RaceDefinition;

import java.nio.file.Path;
import java.util.Map;

public interface RaceRepository {

    Map<String, RaceDefinition> load(Path path);

    void save(Path path, Map<String, RaceDefinition> races);
}
