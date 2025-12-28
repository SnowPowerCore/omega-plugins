package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Sets suit/accent color for the currently selected team.
 *
 * additionalInfo keys:
 * - kind: SUIT | ACCENT
 * - hex: #RRGGBB
 */
public final class RacingSetTeamColorAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingSetTeamColorAction(Logger logger, RaceUiApiProvider apiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();
        String kind = context.additionalInfo().get("kind");
        String hex = context.additionalInfo().get("hex");
        if (kind == null || kind.isBlank() || hex == null || hex.isBlank()) {
            player.sendMessage("Missing additionalInfo.kind or additionalInfo.hex");
            return InteractionResult.continueChain();
        }

        if (!"SUIT".equalsIgnoreCase(kind)) {
            player.sendMessage("Only SUIT color is supported.");
            return InteractionResult.continueChain();
        }

        try {
            RaceUiApi api = apiProvider.resolve().orElse(null);
            RaceDataApi data = dataProvider.resolve().orElse(null);
            RacingSessionsApi sessionsApi = sessionsProvider.resolve().orElse(null);
            if (api == null) {
                player.sendMessage("OmegaRacing UI service not available.");
                return InteractionResult.continueChain();
            }

            if (data == null) {
                player.sendMessage("OmegaRacing data service not available.");
                return InteractionResult.continueChain();
            }
            if (sessionsApi == null) {
                player.sendMessage("OmegaRacing sessions service not available.");
                return InteractionResult.continueChain();
            }

            RacingSession session = sessionsApi.session(player.getUniqueId());
            String raceName = session.getString(RacingSessionKeys.EDITING_RACE_NAME);
            if (raceName == null || raceName.isBlank()) {
                player.sendMessage("No race selected.");
                return InteractionResult.continueChain();
            }

            // Accent color removed; suit color is the only editable color.
            int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
            boolean ok = data.setTeamSuitColor(raceName, selectedTeamIndex, hex);
            if (ok) {
                player.sendMessage("Updated team color.");
                api.openEditor(player, RacingUiIds.TEAM_EDIT, false);
            }
        } catch (Exception e) {
            logger.fine(() -> "RacingSetTeamColorAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
