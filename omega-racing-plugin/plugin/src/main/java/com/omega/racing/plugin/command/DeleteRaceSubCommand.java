package com.omega.racing.plugin.command;

import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.plugin.runtime.ui.ConfirmInventoryUi;
import com.omega.shared.base.SubCommand;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteRaceSubCommand extends SubCommand {

    private final RaceManager races;
    private final RacingSessionStore sessions;
    private final RaceUiApi ui;

    @Inject
    public DeleteRaceSubCommand(RaceManager races, RacingSessionStore sessions, RaceUiApi ui) {
        super("delete");
        this.races = races;
        this.sessions = sessions;
        this.ui = ui;
    }

    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String name = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        if (name == null || name.isBlank()) {
            sender.sendMessage("Usage: /race delete <name>");
            return true;
        }
        if (races.get(name).isEmpty()) {
            sender.sendMessage("No such race.");
            return true;
        }

        // Set up confirmation. Actual deletion is done by omega-interactable-plugin.
        RacingSessionStore.Session s = sessions.session(player.getUniqueId());
        s.set(RacingSessionKeys.EDITING_RACE_NAME, name);
        s.set(RacingSessionKeys.CONFIRM_KIND, "DELETE_RACE");
        s.set(RacingSessionKeys.CONFIRM_RACE_NAME, name);
        s.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, -1);
        s.set(RacingSessionKeys.CONFIRM_RACER_UUID, null);
        s.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, s.getString(RacingSessionKeys.CURRENT_INVENTORY_ID));
        s.set(RacingSessionKeys.CONFIRM_TITLE, "&cDelete race?");
        s.set(RacingSessionKeys.CONFIRM_LORE, List.of("&7Race: &f" + name));

        ui.openEditor(player, ConfirmInventoryUi.ID, false);
        sender.sendMessage("Confirm deletion in the UI.");
        return true;
    }
}
