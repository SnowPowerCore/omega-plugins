package com.omega.racing.core.model;

import java.util.Locale;

public enum RaceStageType {
    FREE_PRACTICE,
    QUALIFICATION,
    RACE;

    public static RaceStageType parse(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }

        // Allow a few common input styles.
        s = s.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);

        try {
            return RaceStageType.valueOf(s);
        } catch (Exception ignored) {
            return null;
        }
    }
}
