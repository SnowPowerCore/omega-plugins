package com.omega.racing.plugin.command;

import com.omega.racing.core.model.RaceStageType;
import com.omega.racing.plugin.runtime.GlobalRaceSelectionService;
import com.omega.racing.plugin.runtime.results.RaceResultsUiService;
import com.omega.shared.base.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class ResultsRaceSubCommand extends SubCommand {

    private final GlobalRaceSelectionService globalSelection;
    private final RaceResultsUiService resultsUi;

    @Inject
    public ResultsRaceSubCommand(GlobalRaceSelectionService globalSelection, RaceResultsUiService resultsUi) {
        super("results");
        this.globalSelection = globalSelection;
        this.resultsUi = resultsUi;
    }

    @Override
    public boolean onCommand(Player player, CommandSender sender, String label, String[] args) {
        String rawType = args.length >= 2 ? args[1] : null;
        if (rawType == null || rawType.isBlank()) {
            sender.sendMessage("Usage: /race results <free_practice>");
            return true;
        }

        RaceStageType type = RaceStageType.parse(rawType);
        if (type == null) {
            sender.sendMessage("Unknown stage type: " + rawType);
            return true;
        }

        String raceName = globalSelection.get();
        if (raceName == null || raceName.isBlank()) {
            sender.sendMessage("No race selected. Use /race init <name> first.");
            return true;
        }

        resultsUi.open(player, raceName, type, 0);
        return true;
    }
}
