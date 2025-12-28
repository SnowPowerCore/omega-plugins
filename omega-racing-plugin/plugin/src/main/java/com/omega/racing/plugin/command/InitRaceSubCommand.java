package com.omega.racing.plugin.command;

import com.omega.racing.plugin.runtime.GlobalRaceSelectionService;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.shared.base.SubCommand;

import jakarta.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Sets the current race globally (server-wide).
 */
public final class InitRaceSubCommand extends SubCommand {

    private final RaceManager races;
    private final GlobalRaceSelectionService globalSelection;

    @Inject
    public InitRaceSubCommand(RaceManager races, GlobalRaceSelectionService globalSelection) {
        super("init");
        this.races = races;
        this.globalSelection = globalSelection;
    }

    @Override
    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String name = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        if (name == null || name.isBlank()) {
            sender.sendMessage("Usage: /race init <name>");
            return true;
        }

        if (races.get(name).isEmpty()) {
            sender.sendMessage("No such race.");
            return true;
        }

        globalSelection.set(name);
        sender.sendMessage("Current race set globally: " + name);
        return true;
    }
}
