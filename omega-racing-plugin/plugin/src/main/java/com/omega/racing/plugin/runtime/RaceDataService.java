package com.omega.racing.plugin.runtime;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceStageStartResult;
import com.omega.racing.plugin.runtime.stage.RaceStageService;

import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Public RaceDataApi implementation.
 *
 * Delegates race CRUD/persistence to {@link RaceManager} and team/racer operations
 * to {@link RaceTeamManager}.
 */
public final class RaceDataService implements RaceDataApi {

    private final RaceManager races;
    private final RaceTeamManager teams;
    private final RaceStageService stages;

    @Inject
    public RaceDataService(RaceManager races, RaceTeamManager teams, RaceStageService stages) {
        this.races = races;
        this.teams = teams;
        this.stages = stages;
    }

    @Override
    public void load() {
        races.load();
    }

    @Override
    public void save() {
        races.save();
    }

    @Override
    public boolean create(String raceName) {
        return races.create(raceName);
    }

    @Override
    public boolean delete(String raceName) {
        return races.delete(raceName);
    }

    @Override
    public Optional<RaceDefinition> get(String raceName) {
        return races.get(raceName);
    }

    @Override
    public boolean renameRace(String oldName, String newName) {
        return races.rename(oldName, newName);
    }

    @Override
    public boolean setQualificationLaps(String raceName, int laps) {
        return races.setQualificationLaps(raceName, laps);
    }

    @Override
    public boolean setQualificationTimeLimitSeconds(String raceName, int timeLimitSeconds) {
        return races.setQualificationTimeLimitSeconds(raceName, timeLimitSeconds);
    }

    @Override
    public boolean setRaceLaps(String raceName, int laps) {
        return races.setRaceLaps(raceName, laps);
    }

    @Override
    public boolean setSections(String raceName, int sections) {
        return races.setSections(raceName, sections);
    }

    @Override
    public boolean setPositions(String raceName, int positions) {
        return races.setPositions(raceName, positions);
    }

    @Override
    public boolean setFreePracticeTimeLimitSeconds(String raceName, int timeLimitSeconds) {
        return races.setFreePracticeTimeLimitSeconds(raceName, timeLimitSeconds);
    }

    @Override
    public int addTeam(String raceName) {
        return teams.addTeam(raceName);
    }

    @Override
    public boolean removeTeam(String raceName, int teamIndex) {
        return teams.removeTeam(raceName, teamIndex);
    }

    @Override
    public boolean renameTeam(String raceName, int teamIndex, String newName) {
        return teams.renameTeam(raceName, teamIndex, newName);
    }

    @Override
    public boolean setTeamSuitColor(String raceName, int teamIndex, String suitColorHex) {
        return teams.setTeamSuitColor(raceName, teamIndex, suitColorHex);
    }

    @Override
    public boolean addRacerToTeamUnique(String raceName, int teamIndex, String racerUuid) {
        return teams.addRacerToTeamUnique(raceName, teamIndex, racerUuid);
    }

    @Override
    public boolean removeRacerFromTeam(String raceName, int teamIndex, String racerUuid) {
        return teams.removeRacerFromTeam(raceName, teamIndex, racerUuid);
    }

    @Override
    public boolean setRacerRacePosition(String raceName, String racerUuid, RaceGridPosition racePosition) {
        return teams.setRacerRacePosition(raceName, racerUuid, racePosition);
    }

    @Override
    public RaceStageStartResult startFreePractice(String raceName) {
        return stages.startFreePractice(raceName);
    }
}
