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
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Accepts the currently pending confirmation in OmegaRacing.
 */
public final class RacingConfirmAcceptAction implements InteractableAction {

    private final Logger logger;
    private final RaceUiApiProvider apiProvider;
    private final RaceDataApiProvider dataProvider;
    private final RacingSessionsApiProvider sessionsProvider;
    private final Map<String, RacingConfirmAcceptDelegate> delegates;

    @Inject
    public RacingConfirmAcceptAction(Logger logger, RaceUiApiProvider apiProvider, RaceDataApiProvider dataProvider, RacingSessionsApiProvider sessionsProvider, Map<String, RacingConfirmAcceptDelegate> delegates) {
        this.logger = logger;
        this.apiProvider = apiProvider;
        this.dataProvider = dataProvider;
        this.sessionsProvider = sessionsProvider;
        this.delegates = delegates;
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

            String kind = session.getString(RacingSessionKeys.CONFIRM_KIND);
            String raceName = session.getString(RacingSessionKeys.CONFIRM_RACE_NAME);
            int teamIndex = session.getInt(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
            String racerUuid = session.getString(RacingSessionKeys.CONFIRM_RACER_UUID);
            String returnId = session.getString(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID);

            session.set(RacingSessionKeys.CONFIRM_KIND, null);
            session.set(RacingSessionKeys.CONFIRM_RACE_NAME, null);
            session.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
            session.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
            session.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, null);
            session.set(RacingSessionKeys.CONFIRM_TITLE, null);
            session.set(RacingSessionKeys.CONFIRM_LORE, null);

            // Delegate is supplied by the clicked ItemStack (additionalInfo.delegate).
            // Fallback to session confirm kind for safety/legacy callers.
            String delegateKey = context.additionalInfo().get("delegate");
            if (delegateKey == null || delegateKey.isBlank()) {
                delegateKey = kind;
            }

            if (kind == null || raceName == null || raceName.isBlank()) {
                String target = returnId != null ? returnId : RacingUiIds.MAIN;
                api.openEditor(player, target, false);
                return InteractionResult.continueChain();
            }

            RacingConfirmAcceptDelegate delegate = delegateKey == null ? null : delegates.get(delegateKey);
            if (delegate != null) {
                delegate.accept(player, api, data, sessionsApi, session, raceName, teamIndex, racerUuid, returnId);
            } else {
                // Unknown delegate: navigate back safely.
                String target = returnId != null ? returnId : RacingUiIds.MAIN;
                api.openEditor(player, target, false);
            }
        } catch (Exception e) {
            logger.fine(() -> "RacingConfirmAcceptAction failed: " + e.getMessage());
        }

        return InteractionResult.continueChain();
    }

}
