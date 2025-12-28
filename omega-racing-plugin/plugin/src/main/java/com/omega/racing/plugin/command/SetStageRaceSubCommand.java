package com.omega.racing.plugin.command;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.RaceStageType;
import com.omega.racing.plugin.runtime.GlobalRaceSelectionService;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.plugin.runtime.ui.ConfirmInventoryUi;
import com.omega.shared.base.SubCommand;

import jakarta.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class SetStageRaceSubCommand extends SubCommand {

    private final RaceManager races;
    private final RacingSessionStore sessions;
    private final RaceUiApi ui;
    private final GlobalRaceSelectionService globalSelection;

    @Inject
    public SetStageRaceSubCommand(RaceManager races, RacingSessionStore sessions, RaceUiApi ui, GlobalRaceSelectionService globalSelection) {
        super("setstage");
        this.races = races;
        this.sessions = sessions;
        this.ui = ui;
        this.globalSelection = globalSelection;
    }

    @Override
    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String rawType = args.length >= 2 ? args[1] : null;
        if (rawType == null || rawType.isBlank()) {
            sender.sendMessage("Usage: /race setstage <free_practice>");
            return true;
        }

        RaceStageType type = RaceStageType.parse(rawType);
        if (type == null) {
            sender.sendMessage("Unknown stage type: " + rawType);
            return true;
        }

        // Enforce globally selected race (/race init). No /race edit required.
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        String raceName = globalSelection.get();
        if (raceName == null || raceName.isBlank()) {
            sender.sendMessage("No race selected. Use /race init <name> first.");
            return true;
        }

        if (races.get(raceName).isEmpty()) {
            sender.sendMessage("Selected race does not exist anymore. Use /race init <name> again.");
            return true;
        }

        if (type != RaceStageType.FREE_PRACTICE) {
            sender.sendMessage("Only FREE_PRACTICE is implemented right now.");
            return true;
        }

        // Ensure the confirm UI can render (RaceUiService requires EDITING_RACE_NAME).
        s.set(RacingSessionKeys.EDITING_RACE_NAME, raceName);

        // Confirmation is handled by omega-interactable-plugin (RacingConfirmAcceptAction).
        s.set(RacingSessionKeys.CONFIRM_KIND, "START_FREE_PRACTICE");
        s.set(RacingSessionKeys.CONFIRM_RACE_NAME, raceName);
        s.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
        s.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
        s.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, s.getString(RacingSessionKeys.CURRENT_INVENTORY_ID));
        s.set(RacingSessionKeys.CONFIRM_TITLE, "&eStart Free Practice?");
        s.set(RacingSessionKeys.CONFIRM_LORE, List.of(
            "&7Race: &f" + raceName,
            "&7This will teleport all racers and start a 1-minute countdown."
        ));

        ui.openEditor(player, ConfirmInventoryUi.ID, false);
        sender.sendMessage("Confirm stage start in the UI.");
        return true;
    }
}
