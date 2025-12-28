package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;
import com.omega.interactable.core.constants.RacingConfirmKinds;
import com.omega.interactable.core.constants.RacingUiIds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.model.RaceDefinition;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Removes the currently selected team in the OmegaRacing editor.
 */
public final class RacingRemoveTeamAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;

    @Inject
    public RacingRemoveTeamAction(Logger logger, RaceUiApiProvider apiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
    }

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        Player player = context.player();

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

            Optional<RaceDefinition> raceOpt = data.get(raceName);
            if (raceOpt.isEmpty()) {
                player.sendMessage("Unknown race: " + raceName);
                return InteractionResult.continueChain();
            }

            RaceDefinition race = raceOpt.get();
            if (race.getTeams().isEmpty()) {
                player.sendMessage("No teams to remove.");
                return InteractionResult.continueChain();
            }

            int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
            int idx = Math.min(Math.max(0, selectedTeamIndex), race.getTeams().size() - 1);
            session.set(RacingSessionKeys.CONFIRM_KIND, RacingConfirmKinds.REMOVE_TEAM);
            session.set(RacingSessionKeys.CONFIRM_RACE_NAME, raceName);
            session.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, idx);
            session.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
            session.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, session.getString(RacingSessionKeys.CURRENT_INVENTORY_ID));

            String teamName = race.getTeams().get(idx).getName();
            session.set(RacingSessionKeys.CONFIRM_TITLE, "&cRemove team?");
            session.set(RacingSessionKeys.CONFIRM_LORE, List.of("&7Team: &f" + teamName));

            api.openEditor(player, RacingUiIds.CONFIRM, false);
        } catch (Exception e) {
            logger.fine(() -> "RacingRemoveTeamAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
