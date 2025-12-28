package com.omega.racing.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class RaceTeam {

    private String name;
    private String suitColorHex;

    /**
     * New model: racers are objects (uuid + optional grid position).
     */
    private final List<Racer> racers = new ArrayList<>();

    public RaceTeam(String name, String suitColorHex) {
        this.name = name;
        this.suitColorHex = suitColorHex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuitColorHex() {
        return suitColorHex;
    }

    public void setSuitColorHex(String suitColorHex) {
        this.suitColorHex = suitColorHex;
    }

    public List<Racer> getRacers() {
        return racers;
    }

    public boolean hasRacer(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        for (Racer racer : racers) {
            if (racer != null && uuid.equals(racer.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public Racer getRacer(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }
        for (Racer racer : racers) {
            if (racer != null && uuid.equals(racer.getUuid())) {
                return racer;
            }
        }
        return null;
    }

    public boolean addRacer(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        if (hasRacer(uuid)) {
            return false;
        }
        racers.add(new Racer(uuid.trim(), null));
        return true;
    }

    public boolean removeRacer(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        boolean removed = false;
        for (int i = racers.size() - 1; i >= 0; i--) {
            Racer racer = racers.get(i);
            if (racer != null && uuid.equals(racer.getUuid())) {
                racers.remove(i);
                removed = true;
            }
        }
        return removed;
    }

    public static final class Racer {
        private String uuid;
        private RaceGridPosition racePosition;

        public Racer(String uuid, RaceGridPosition racePosition) {
            this.uuid = uuid;
            this.racePosition = racePosition;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public RaceGridPosition getRacePosition() {
            return racePosition;
        }

        public void setRacePosition(RaceGridPosition racePosition) {
            this.racePosition = racePosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Racer racer)) return false;
            return Objects.equals(uuid, racer.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

        @Override
        public String toString() {
            return uuid == null ? "" : uuid.toLowerCase(Locale.ROOT);
        }
    }
}
