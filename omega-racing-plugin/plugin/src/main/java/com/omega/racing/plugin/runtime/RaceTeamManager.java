package com.omega.racing.plugin.runtime;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceTeam;

import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Team + racer operations for a race.
 *
 * This intentionally sits outside {@link RaceManager} so that RaceManager can stay focused on
 * race persistence + CRUD.
 */
public final class RaceTeamManager {

    private final RaceManager races;

    @Inject
    public RaceTeamManager(RaceManager races) {
        this.races = races;
    }

    public int addTeam(String raceName) {
        if (raceName == null || raceName.isBlank()) {
            return -1;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return -1;
        }

        RaceDefinition race = raceOpt.get();
        int idx = race.getTeams().size();
        String teamName = "Team " + (idx + 1);
        race.getTeams().add(new RaceTeam(teamName, "#FFFFFF"));
        races.save();
        return idx;
    }

    public boolean removeTeam(String raceName, int teamIndex) {
        if (raceName == null || raceName.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }
        int idx = Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1);
        race.getTeams().remove(idx);
        races.save();
        return true;
    }

    public boolean renameTeam(String raceName, int teamIndex, String newName) {
        if (raceName == null || raceName.isBlank() || newName == null || newName.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }
        int idx = Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1);
        race.getTeams().get(idx).setName(newName.trim());
        races.save();
        return true;
    }

    public boolean setTeamSuitColor(String raceName, int teamIndex, String suitColorHex) {
        if (raceName == null || raceName.isBlank() || suitColorHex == null || suitColorHex.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }
        int idx = Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1);
        race.getTeams().get(idx).setSuitColorHex(suitColorHex.trim());
        races.save();
        return true;
    }

    public boolean addRacerToTeamUnique(String raceName, int teamIndex, String racerUuid) {
        if (raceName == null || raceName.isBlank() || racerUuid == null || racerUuid.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }
        int idx = Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1);

        // Enforce uniqueness: remove racer from all teams first, then add to selected.
        for (var team : race.getTeams()) {
            team.removeRacer(racerUuid);
        }
        race.getTeams().get(idx).addRacer(racerUuid);
        races.save();
        return true;
    }

    public boolean removeRacerFromTeam(String raceName, int teamIndex, String racerUuid) {
        if (raceName == null || raceName.isBlank() || racerUuid == null || racerUuid.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }
        int idx = Math.min(Math.max(0, teamIndex), race.getTeams().size() - 1);
        boolean removed = race.getTeams().get(idx).removeRacer(racerUuid);
        if (removed) {
            races.save();
        }
        return removed;
    }

    public boolean setRacerRacePosition(String raceName, String racerUuid, RaceGridPosition racePosition) {
        if (raceName == null || raceName.isBlank() || racerUuid == null || racerUuid.isBlank()) {
            return false;
        }
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return false;
        }
        RaceDefinition race = raceOpt.get();
        if (race.getTeams().isEmpty()) {
            return false;
        }

        RaceTeam.Racer target = null;
        for (RaceTeam team : race.getTeams()) {
            if (team == null) {
                continue;
            }
            RaceTeam.Racer racer = team.getRacer(racerUuid);
            if (racer != null) {
                target = racer;
                break;
            }
        }
        if (target == null) {
            return false;
        }

        // Enforce uniqueness: clear the position index from any other racer first.
        Integer pickedIndex = racePosition == null ? null : racePosition.getPositionIndex();
        for (RaceTeam team : race.getTeams()) {
            if (team == null) {
                continue;
            }
            for (RaceTeam.Racer racer : team.getRacers()) {
                if (racer == null) {
                    continue;
                }
                if (racer == target) {
                    continue;
                }
                RaceGridPosition other = racer.getRacePosition();
                if (pickedIndex != null && other != null && other.getPositionIndex() == pickedIndex) {
                    racer.setRacePosition(null);
                }
            }
        }

        target.setRacePosition(racePosition);
        races.save();
        return true;
    }
}
