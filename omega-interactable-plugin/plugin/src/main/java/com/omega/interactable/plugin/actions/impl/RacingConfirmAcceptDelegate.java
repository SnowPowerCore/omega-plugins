package com.omega.interactable.plugin.actions.impl;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

/**
 * Handles a specific confirmation-accept delegate.
 *
 * The delegate key is supplied by the confirm accept ItemStack (additionalInfo.delegate).
 */
public interface RacingConfirmAcceptDelegate {

    /**
     * The delegate key, typically one of {@code RacingConfirmKinds.*}.
     */
    String key();

    void accept(
            Player player,
            RaceUiApi ui,
            RaceDataApi data,
            RacingSessionsApi sessionsApi,
            RacingSession session,
            String raceName,
            int teamIndex,
            String racerUuid,
            String returnInventoryId
    );
}
