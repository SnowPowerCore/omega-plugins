package com.omega.racing.plugin.command;

import com.omega.shared.base.SubCommand;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.omega.racing.core.model.RaceStageType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RaceCommand implements CommandExecutor, TabCompleter {

    private final ImmutableList<SubCommand> _subcommands;

    @Inject
    public RaceCommand(Injector injector) {
        
        CreateRaceSubCommand createRaceSubCommand = injector.getInstance(CreateRaceSubCommand.class);
        DeleteRaceSubCommand deleteRaceSubCommand = injector.getInstance(DeleteRaceSubCommand.class);
        EditRaceSubCommand editRaceSubCommand = injector.getInstance(EditRaceSubCommand.class);
        BlocksRaceSubCommand blocksRaceSubCommand = injector.getInstance(BlocksRaceSubCommand.class);
        InitRaceSubCommand initRaceSubCommand = injector.getInstance(InitRaceSubCommand.class);
        SetStageRaceSubCommand setStageRaceSubCommand = injector.getInstance(SetStageRaceSubCommand.class);
        ResultsRaceSubCommand resultsRaceSubCommand = injector.getInstance(ResultsRaceSubCommand.class);

        _subcommands = ImmutableList.of(
            createRaceSubCommand,
            deleteRaceSubCommand,
            editRaceSubCommand,
            blocksRaceSubCommand,
            initRaceSubCommand,
            setStageRaceSubCommand,
            resultsRaceSubCommand
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /race <create|delete|edit|blocks> <name>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        SubCommand cmd = null;
        for (SubCommand candidate : _subcommands) {
            if (candidate.getName().equalsIgnoreCase(sub)) {
                cmd = candidate;
                break;
            }
        }
        if (cmd == null) {
            sender.sendMessage("Unknown subcommand.");
            return true;
        }
        return cmd.onCommand(player, sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (SubCommand subcommand : _subcommands) {
                out.add(subcommand.getName());
            }
            out.sort(String::compareToIgnoreCase);
            return out;
        }

        if (args.length == 2 && args[0] != null && args[0].equalsIgnoreCase("setstage")) {
            List<String> out = new ArrayList<>();
            for (RaceStageType t : RaceStageType.values()) {
                out.add(t.name().toLowerCase(Locale.ROOT));
            }
            out.sort(String::compareToIgnoreCase);
            return out;
        }

        if (args.length == 2 && args[0] != null && args[0].equalsIgnoreCase("results")) {
            List<String> out = new ArrayList<>();
            for (RaceStageType t : RaceStageType.values()) {
                out.add(t.name().toLowerCase(Locale.ROOT));
            }
            out.sort(String::compareToIgnoreCase);
            return out;
        }
        return new ArrayList<>();
    }
}