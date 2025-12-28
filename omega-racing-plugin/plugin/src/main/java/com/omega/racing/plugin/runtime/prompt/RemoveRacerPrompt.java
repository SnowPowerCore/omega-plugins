package com.omega.racing.plugin.runtime.prompt;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RacingSessionKeys;
import com.omega.racing.core.model.PromptKind;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.plugin.runtime.ui.ConfirmInventoryUi;
import com.omega.racing.plugin.runtime.ui.TeamEditInventoryUi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

public final class RemoveRacerPrompt implements ChatPrompt {

    private final RaceDataApi raceData;

    @Inject
    public RemoveRacerPrompt(RaceDataApi raceData) {
        this.raceData = raceData;
    }

    @Override
    public PromptKind kind() {
        return PromptKind.REMOVE_RACER;
    }

    @Override
    public String startMessage(String cancelString) {
        return "Type the player name to remove from the team (or type '" + cancelString + "' to cancel).";
    }

    @Override
    public String handle(Player player, RacingSessionStore.Session session, String raceName, String input) {
        OfflinePlayer offline = resolveOfflinePlayer(input);

        String racerUuid = offline == null || offline.getUniqueId() == null ? null : offline.getUniqueId().toString();
        if (racerUuid == null || racerUuid.isBlank()) {
            player.sendMessage("Unknown player: " + input);
            return TeamEditInventoryUi.ID;
        }

        var raceOpt = raceData.get(raceName);
        if (raceOpt.isEmpty() || raceOpt.get().getTeams().isEmpty()) {
            player.sendMessage("No team selected.");
            return TeamEditInventoryUi.ID;
        }

        int selectedTeamIndex = session.getInt(RacingSessionKeys.SELECTED_TEAM_INDEX, 0);
        int idx = Math.min(Math.max(0, selectedTeamIndex), raceOpt.get().getTeams().size() - 1);
        var team = raceOpt.get().getTeams().get(idx);
        if (!team.hasRacer(racerUuid)) {
            player.sendMessage("That racer is not on this team.");
            return TeamEditInventoryUi.ID;
        }

        session.set(RacingSessionKeys.CONFIRM_KIND, "REMOVE_RACER");
        session.set(RacingSessionKeys.CONFIRM_RACE_NAME, raceName);
        session.set(RacingSessionKeys.CONFIRM_TEAM_INDEX, idx);
        session.set(RacingSessionKeys.CONFIRM_RACER_UUID, racerUuid);
        session.set(RacingSessionKeys.CONFIRM_RETURN_INVENTORY_ID, session.getString(RacingSessionKeys.CURRENT_INVENTORY_ID));

        String displayName = racerUuid;
        try {
            UUID uuid = UUID.fromString(racerUuid);
            var byUuid = Bukkit.getOfflinePlayer(uuid);
            if (byUuid.getName() != null) {
                displayName = byUuid.getName();
            }
        } catch (Exception ignored) {
        }

        session.set(RacingSessionKeys.CONFIRM_TITLE, "&cRemove racer?");
        session.set(RacingSessionKeys.CONFIRM_LORE, List.of("&7Racer: &f" + (displayName == null ? "" : displayName)));

        return ConfirmInventoryUi.ID;
    }

    private static OfflinePlayer resolveOfflinePlayer(String input) {
        try {
            var online = Bukkit.getPlayerExact(input);
            if (online != null) {
                return online;
            }
        } catch (Exception ignored) {
        }

        try {
            UUID asUuid = UUID.fromString(input);
            return Bukkit.getOfflinePlayer(asUuid);
        } catch (Exception ignored) {
        }

        // Use reflection to avoid direct compilation against deprecated methods and to stay
        // compatible with multiple Bukkit API versions.
        try {
            var maybeCached = Bukkit.class.getMethod("getOfflinePlayerIfCached", String.class);
            Object result = maybeCached.invoke(null, input);
            if (result instanceof OfflinePlayer) {
                return (OfflinePlayer) result;
            }
        } catch (Exception ignored) {
        }

        try {
            var byName = Bukkit.class.getMethod("getOfflinePlayer", String.class);
            Object result = byName.invoke(null, input);
            if (result instanceof OfflinePlayer) {
                return (OfflinePlayer) result;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
