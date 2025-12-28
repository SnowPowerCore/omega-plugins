package com.omega.racing.plugin.runtime.prompt;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.PromptKind;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.plugin.runtime.ui.MainInventoryUi;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class RenameRacePrompt implements ChatPrompt {

    private final RaceDataApi raceData;

    @Inject
    public RenameRacePrompt(RaceDataApi raceData) {
        this.raceData = raceData;
    }

    @Override
    public PromptKind kind() {
        return PromptKind.RENAME_RACE;
    }

    @Override
    public String startMessage(String cancelString) {
        return "Type the new race name in chat (or type '" + cancelString + "' to cancel).";
    }

    @Override
    public String handle(Player player, RacingSessionStore.Session session, String raceName, String input) {
        boolean ok = raceData.renameRace(raceName, input);
        if (ok) {
            session.set(RacingSessionKeys.EDITING_RACE_NAME, input);
            player.sendMessage("Race renamed to: " + input);
        } else {
            player.sendMessage("Rename failed (name in use?).");
        }
        return MainInventoryUi.ID;
    }
}
