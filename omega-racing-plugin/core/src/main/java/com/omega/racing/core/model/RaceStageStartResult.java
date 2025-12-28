package com.omega.racing.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RaceStageStartResult {

    private final boolean started;
    private final List<String> missingPositionRacerUuids;

    public RaceStageStartResult(boolean started, List<String> missingPositionRacerUuids) {
        this.started = started;
        List<String> copy = new ArrayList<>();
        if (missingPositionRacerUuids != null) {
            for (String s : missingPositionRacerUuids) {
                if (s != null && !s.isBlank()) {
                    copy.add(s);
                }
            }
        }
        this.missingPositionRacerUuids = Collections.unmodifiableList(copy);
    }

    public boolean isStarted() {
        return started;
    }

    public List<String> getMissingPositionRacerUuids() {
        return missingPositionRacerUuids;
    }
}
