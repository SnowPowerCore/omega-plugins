package com.omega.racing.core.model;

import java.util.ArrayList;
import java.util.List;

public final class RaceDefinition {

    private String name;
    private int sections = 1;
    private int positions = 1;
    private FreePractice freePractice;
    private StageConfig qualification;
    private StageConfig race;
    private final List<RaceTeam> teams = new ArrayList<>();

    public RaceDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Amount of track sections (blocks) in this race.
     */
    public int getSections() {
        sections = Math.max(1, sections);
        return sections;
    }

    public void setSections(int sections) {
        this.sections = Math.max(1, sections);
    }

    /**
     * Amount of starter grid positions available in this race.
     */
    public int getPositions() {
        positions = Math.max(1, positions);
        return positions;
    }

    public void setPositions(int positions) {
        this.positions = Math.max(1, positions);
    }

    /**
     * Qualification configuration.
     */
    public StageConfig getQualification() {
        if (qualification == null) {
            qualification = new StageConfig(1);
        }
        return qualification;
    }

    public void setQualification(StageConfig qualification) {
        this.qualification = (qualification == null) ? new StageConfig(1) : qualification;
    }

    /**
     * Free Practice configuration.
     */
    public FreePractice getFreePractice() {
        if (freePractice == null) {
            freePractice = new FreePractice(0);
        }
        return freePractice;
    }

    public void setFreePractice(FreePractice freePractice) {
        this.freePractice = (freePractice == null) ? new FreePractice(0) : freePractice;
    }

    /**
     * Race configuration.
     */
    public StageConfig getRace() {
        if (race == null) {
            race = new StageConfig(1);
        }
        return race;
    }

    public void setRace(StageConfig race) {
        this.race = (race == null) ? new StageConfig(1) : race;
    }

    public List<RaceTeam> getTeams() {
        return teams;
    }

    public static final class StageConfig {
        private int laps;
        private int timeLimitSeconds;

        public StageConfig(int laps) {
            setLaps(laps);
            setTimeLimitSeconds(0);
        }

        public int getLaps() {
            return laps;
        }

        public void setLaps(int laps) {
            this.laps = Math.max(1, laps);
        }

        /**
         * Time limit in seconds; 0 means unlimited.
         */
        public int getTimeLimitSeconds() {
            timeLimitSeconds = Math.max(0, timeLimitSeconds);
            return timeLimitSeconds;
        }

        public void setTimeLimitSeconds(int timeLimitSeconds) {
            this.timeLimitSeconds = Math.max(0, timeLimitSeconds);
        }
    }

    public static final class FreePractice {
        private int timeLimitSeconds;

        public FreePractice(int timeLimitSeconds) {
            setTimeLimitSeconds(timeLimitSeconds);
        }

        /**
         * Time limit in seconds; 0 means unlimited.
         */
        public int getTimeLimitSeconds() {
            timeLimitSeconds = Math.max(0, timeLimitSeconds);
            return timeLimitSeconds;
        }

        public void setTimeLimitSeconds(int timeLimitSeconds) {
            this.timeLimitSeconds = Math.max(0, timeLimitSeconds);
        }
    }
}
