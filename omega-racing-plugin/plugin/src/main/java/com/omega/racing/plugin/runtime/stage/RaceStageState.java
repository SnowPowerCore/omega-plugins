package com.omega.racing.plugin.runtime.stage;

import com.omega.racing.core.model.RaceStageStatus;
import com.omega.racing.core.model.RaceStageType;

public final class RaceStageState {

    private String raceName;
    private RaceStageType stageType;
    private RaceStageStatus status;
    private long startedAtMillis;
    private int countdownSecondsRemaining;

    // Free Practice runtime fields (optional)
    private boolean freePracticeOfficialStarted;
    private long freePracticeOfficialStartedAtMillis;
    private int freePracticeTimeLimitSeconds;
    private int freePracticeTimeRemainingSeconds;
    private long freePracticeEndedAtMillis;
    private java.util.Map<String, FreePracticeRacerProgress> freePracticeRacers;

    public static final class FreePracticeRacerProgress {
        private int lap;
        private int section;
        private long bestLapMillis;

        public FreePracticeRacerProgress() {
        }

        public FreePracticeRacerProgress(int lap, int section) {
            this.lap = lap;
            this.section = section;
        }

        public FreePracticeRacerProgress(int lap, int section, long bestLapMillis) {
            this.lap = lap;
            this.section = section;
            this.bestLapMillis = bestLapMillis;
        }

        public int getLap() {
            return lap;
        }

        public void setLap(int lap) {
            this.lap = lap;
        }

        public int getSection() {
            return section;
        }

        public void setSection(int section) {
            this.section = section;
        }

        public long getBestLapMillis() {
            return bestLapMillis;
        }

        public void setBestLapMillis(long bestLapMillis) {
            this.bestLapMillis = bestLapMillis;
        }
    }

    public RaceStageState() {
    }

    public RaceStageState(String raceName, RaceStageType stageType, RaceStageStatus status, long startedAtMillis, int countdownSecondsRemaining) {
        this.raceName = raceName;
        this.stageType = stageType;
        this.status = status;
        this.startedAtMillis = startedAtMillis;
        this.countdownSecondsRemaining = countdownSecondsRemaining;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public RaceStageType getStageType() {
        return stageType;
    }

    public void setStageType(RaceStageType stageType) {
        this.stageType = stageType;
    }

    public RaceStageStatus getStatus() {
        return status;
    }

    public void setStatus(RaceStageStatus status) {
        this.status = status;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public void setStartedAtMillis(long startedAtMillis) {
        this.startedAtMillis = startedAtMillis;
    }

    public int getCountdownSecondsRemaining() {
        return countdownSecondsRemaining;
    }

    public void setCountdownSecondsRemaining(int countdownSecondsRemaining) {
        this.countdownSecondsRemaining = countdownSecondsRemaining;
    }

    public boolean isFreePracticeOfficialStarted() {
        return freePracticeOfficialStarted;
    }

    public void setFreePracticeOfficialStarted(boolean freePracticeOfficialStarted) {
        this.freePracticeOfficialStarted = freePracticeOfficialStarted;
    }

    public long getFreePracticeOfficialStartedAtMillis() {
        return freePracticeOfficialStartedAtMillis;
    }

    public void setFreePracticeOfficialStartedAtMillis(long freePracticeOfficialStartedAtMillis) {
        this.freePracticeOfficialStartedAtMillis = freePracticeOfficialStartedAtMillis;
    }

    public int getFreePracticeTimeLimitSeconds() {
        return freePracticeTimeLimitSeconds;
    }

    public void setFreePracticeTimeLimitSeconds(int freePracticeTimeLimitSeconds) {
        this.freePracticeTimeLimitSeconds = freePracticeTimeLimitSeconds;
    }

    public int getFreePracticeTimeRemainingSeconds() {
        return freePracticeTimeRemainingSeconds;
    }

    public void setFreePracticeTimeRemainingSeconds(int freePracticeTimeRemainingSeconds) {
        this.freePracticeTimeRemainingSeconds = freePracticeTimeRemainingSeconds;
    }

    public long getFreePracticeEndedAtMillis() {
        return freePracticeEndedAtMillis;
    }

    public void setFreePracticeEndedAtMillis(long freePracticeEndedAtMillis) {
        this.freePracticeEndedAtMillis = freePracticeEndedAtMillis;
    }

    public java.util.Map<String, FreePracticeRacerProgress> getFreePracticeRacers() {
        return freePracticeRacers;
    }

    public void setFreePracticeRacers(java.util.Map<String, FreePracticeRacerProgress> freePracticeRacers) {
        this.freePracticeRacers = freePracticeRacers;
    }
}
