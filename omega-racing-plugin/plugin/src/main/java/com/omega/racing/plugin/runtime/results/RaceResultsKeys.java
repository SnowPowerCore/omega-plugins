package com.omega.racing.plugin.runtime.results;

import com.omega.racing.core.OmegaRacingConstants;
import org.bukkit.NamespacedKey;

import java.util.Objects;

public final class RaceResultsKeys {

    private RaceResultsKeys() {
    }

    private static NamespacedKey key(String path) {
        return Objects.requireNonNull(
            NamespacedKey.fromString(OmegaRacingConstants.NAMESPACE + ":" + path)
        );
    }

    public static final NamespacedKey UI_ACTION = key("race_results.action");

    public static final String ACTION_PREV = "prev";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_CLOSE = "close";
}
