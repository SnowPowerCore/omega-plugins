package com.omega.interactable.core.constants;

/**
 * Title helpers for OmegaRacing editor inventories.
 */
public final class RacingUiTitles {

    private RacingUiTitles() {
    }

    public static String raceEditor(String raceName) {
        if (raceName == null || raceName.isBlank()) {
            return "&bRace Editor";
        }
        return "&bRace Editor: &f" + raceName;
    }

    public static String teamEditor(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return "&bTeam Editor";
        }
        return "&bTeam Editor: &f" + teamName;
    }
}
