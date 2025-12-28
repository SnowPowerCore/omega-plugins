package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.constants.RacingConfirmKinds;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.model.RaceStageStartResult;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;

public final class RacingConfirmAcceptStartFreePracticeDelegate implements RacingConfirmAcceptDelegate {

    @Inject
    public RacingConfirmAcceptStartFreePracticeDelegate() {
    }

    @Override
    public String key() {
        return RacingConfirmKinds.START_FREE_PRACTICE;
    }

    @Override
    public void accept(Player player, RaceUiApi ui, RaceDataApi data, RacingSessionsApi sessionsApi, RacingSession session, String raceName, int teamIndex, String racerUuid, String returnInventoryId) {
        RaceStageStartResult result = data.startFreePractice(raceName);
        if (result == null || !result.isStarted()) {
            player.sendMessage(ChatColor.RED + "Cannot start Free Practice: some racers have no position set.");
            if (result != null && !result.getMissingPositionRacerUuids().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "Missing positions (UUIDs): " + String.join(", ", result.getMissingPositionRacerUuids()));
            }
            player.closeInventory();
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Free Practice starting for race: " + raceName);
        player.closeInventory();
    }
}
