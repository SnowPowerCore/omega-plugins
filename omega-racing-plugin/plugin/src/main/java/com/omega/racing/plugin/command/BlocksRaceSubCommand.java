package com.omega.racing.plugin.command;

import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksUiService;
import com.omega.shared.base.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.Arrays;

public final class BlocksRaceSubCommand extends SubCommand {

    private final RaceManager races;
    private final RaceBlocksUiService ui;

    @Inject
    public BlocksRaceSubCommand(RaceManager races, RaceBlocksUiService ui) {
        super("blocks");
        this.races = races;
        this.ui = ui;
    }

    @Override
    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String name = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        if (name == null || name.isBlank()) {
            sender.sendMessage("Usage: /race blocks <name>");
            return true;
        }
        if (races.get(name).isEmpty()) {
            sender.sendMessage("No such race.");
            return true;
        }

        ui.open(player, name, 0);
        return true;
    }
}
