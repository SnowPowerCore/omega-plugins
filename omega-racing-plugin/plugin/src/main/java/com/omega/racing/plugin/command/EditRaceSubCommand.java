package com.omega.racing.plugin.command;

import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.RaceUiService;
import com.omega.shared.base.SubCommand;

import jakarta.inject.Inject;
import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditRaceSubCommand extends SubCommand {

    private static final String MAIN_INVENTORY_ID = "racing_main";

    private final RaceManager races;
    private final RaceUiService ui;

    @Inject
    public EditRaceSubCommand(RaceManager races, RaceUiService ui) {
        super("edit");
        this.races = races;
        this.ui = ui;
    }

    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String name = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        if (name == null || name.isBlank()) {
            sender.sendMessage("Usage: /race edit <name>");
            return true;
        }
        if (races.get(name).isEmpty()) {
            sender.sendMessage("No such race.");
            return true;
        }
        ui.beginEditing(player, name);
        ui.openEditor(player, MAIN_INVENTORY_ID, false);
        sender.sendMessage("Editing race: " + name);
        return true;
    }
}
