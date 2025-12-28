package com.omega.racing.core.api;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceStageStartResult;

import java.util.Optional;

/**
 * Low-level race persistence API.
 *
 * This is intentionally UI-agnostic so higher-level workflows can live elsewhere
 * (e.g., omega-interactable-plugin actions).
 */
public interface RaceDataApi {

    void load();

    void save();

    boolean create(String raceName);

    boolean delete(String raceName);

    Optional<RaceDefinition> get(String raceName);

    boolean renameRace(String oldName, String newName);

    boolean setQualificationLaps(String raceName, int laps);

    /**
     * Sets qualification time limit in seconds; 0 means unlimited.
     */
    boolean setQualificationTimeLimitSeconds(String raceName, int timeLimitSeconds);

    boolean setRaceLaps(String raceName, int laps);

    /**
     * Sets race track sections. This is a race-level characteristic.
     */
    boolean setSections(String raceName, int sections);

    boolean setPositions(String raceName, int positions);

    /**
     * Sets Free Practice time limit in seconds; 0 means unlimited.
     */
    boolean setFreePracticeTimeLimitSeconds(String raceName, int timeLimitSeconds);

    int addTeam(String raceName);

    boolean removeTeam(String raceName, int teamIndex);

    boolean renameTeam(String raceName, int teamIndex, String newName);

    boolean setTeamSuitColor(String raceName, int teamIndex, String suitColorHex);

    boolean addRacerToTeamUnique(String raceName, int teamIndex, String racerUuid);

    boolean removeRacerFromTeam(String raceName, int teamIndex, String racerUuid);

    /**
     * Sets (or clears, if null) a racer's starter-grid position.
     * The same position cannot be assigned to multiple racers; the last assignment wins.
     */
    boolean setRacerRacePosition(String raceName, String racerUuid, RaceGridPosition racePosition);

    /**
     * Starts the Free Practice stage for the given race.
     *
     * If any racer has no position defined, the stage is NOT started.
     */
    RaceStageStartResult startFreePractice(String raceName);
}
