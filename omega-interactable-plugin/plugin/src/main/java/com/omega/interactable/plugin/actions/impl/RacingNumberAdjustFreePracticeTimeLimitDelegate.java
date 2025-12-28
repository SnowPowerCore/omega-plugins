package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.model.RaceDefinition;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public final class RacingNumberAdjustFreePracticeTimeLimitDelegate implements RacingNumberAdjustSuccessDelegate {

    private final Logger logger;
    private final RaceUiApiProvider uiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingNumberAdjustFreePracticeTimeLimitDelegate(Logger logger, RaceUiApiProvider uiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.uiProvider = uiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public void adjust(InteractionContext context, int delta) {
        Player player = context.player();

        try {
            RaceUiApi ui = uiProvider.resolve().orElse(null);
            RaceDataApi data = dataProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (ui == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return;
            }
            if (data == null) {
                player.sendMessage("OmegaRacing data service not available.");
                return;
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return;
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String invId = session.getString(RacingSessionKeys.CURRENT_INVENTORY_ID);
            String raceName = session.getString(RacingSessionKeys.EDITING_RACE_NAME);
            if (raceName == null || raceName.isBlank()) {
                player.sendMessage("No race selected.");
                return;
            }

            Optional<RaceDefinition> raceOpt = data.get(raceName);
            if (raceOpt.isEmpty()) {
                player.sendMessage("Unknown race: " + raceName);
                return;
            }

            int current = Math.max(0, raceOpt.get().getFreePractice().getTimeLimitSeconds());
            int next = clampNonNegativeIntAdd(current, delta);
            if (data.setFreePracticeTimeLimitSeconds(raceName, next)) {
                ui.openEditor(player, (invId == null || invId.isBlank()) ? RacingUiIds.FREE_PRACTICE_TIME_LIMIT : invId, false);
            }
        } catch (Exception e) {
            logger.fine(() -> "RacingNumberAdjustFreePracticeTimeLimitDelegate failed: " + e.getMessage());
        }
    }

    private static int clampNonNegativeIntAdd(int value, int delta) {
        long next = (long) value + (long) delta;
        if (next < 0L) {
            return 0;
        }
        if (next > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) next;
    }
}
