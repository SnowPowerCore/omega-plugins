package com.omega.racing.plugin.runtime.stage;

import com.omega.racing.core.model.RaceGridPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import jakarta.inject.Inject;

public final class RaceScoreboardService {

    @Inject
    public RaceScoreboardService() {
    }

    public void showStartingScoreboard(Player player, String raceName, String stageName, RaceGridPosition pos, int secondsRemaining) {
        showScoreboard(player, raceName, stageName, pos, secondsRemaining, ChatColor.GRAY + "Get ready...");
    }

    public void showRunningScoreboard(Player player, String raceName, String stageName, RaceGridPosition pos) {
        showScoreboard(player, raceName, stageName, pos, null, ChatColor.GREEN + "Go!");
    }

    public void showFreePracticeCountdown(Player player, String raceName, RaceGridPosition pos, int countdownSecondsRemaining) {
        showFreePractice(player, raceName, pos, "Free Practice", countdownSecondsRemaining, null, null, null, null, null, ChatColor.GRAY + "Get ready...");
    }

    public void showFreePracticeWaiting(Player player, String raceName, RaceGridPosition pos, int lap, int totalLaps, int section, int totalSections) {
        showFreePractice(player, raceName, pos, "Free Practice", null, null, lapLine(lap, totalLaps), sectionLine(section, totalSections), null, null, ChatColor.YELLOW + "Waiting for Section 1...");
    }

    public void showFreePracticeRunning(Player player,
                                       String raceName,
                                       RaceGridPosition pos,
                                       int lap,
                                       int totalLaps,
                                       int section,
                                       int totalSections,
                                       int timeRemainingSeconds,
                                       int timeLimitSeconds,
                                       long currentLapMillis,
                                       long bestLapMillis)
    {
        showFreePractice(
                player,
                raceName,
                pos,
                "Free Practice",
                null,
                timeLine(timeRemainingSeconds, timeLimitSeconds),
                lapOnlyLine(lap),
                sectionLine(section, totalSections),
                "Current: " + formatLapTime(currentLapMillis),
                "Best: " + formatLapTime(bestLapMillis),
                ChatColor.GREEN + "Running"
        );
    }

    @SuppressWarnings("deprecation")
    private void showScoreboard(Player player, String raceName, String stageName, RaceGridPosition pos, Integer secondsRemaining, String statusLine) {
        if (player == null) {
            return;
        }
        Scoreboard sb = Bukkit.getScoreboardManager() == null ? null : Bukkit.getScoreboardManager().getNewScoreboard();
        if (sb == null) {
            return;
        }

        Objective obj = sb.registerNewObjective("omegaRace", "dummy", ChatColor.GOLD + "Racing");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String position = pos == null ? "N/A" : ("#" + pos.getPositionIndex());

        // Scoreboard lines must be unique; prefix with color codes.
        obj.getScore(ChatColor.YELLOW + "Race: " + ChatColor.WHITE + raceName).setScore(6);
        obj.getScore(ChatColor.YELLOW + "Stage: " + ChatColor.WHITE + stageName).setScore(5);
        obj.getScore(ChatColor.YELLOW + "Pos: " + ChatColor.WHITE + position).setScore(4);

        if (secondsRemaining != null) {
            String timer = formatTimer(secondsRemaining);
            obj.getScore(ChatColor.YELLOW + "Timer: " + ChatColor.WHITE + timer).setScore(3);
        }

        obj.getScore(ChatColor.DARK_GRAY + " ").setScore(2);
        obj.getScore(statusLine == null ? (ChatColor.GRAY + "") : statusLine).setScore(1);

        player.setScoreboard(sb);
    }

    @SuppressWarnings("deprecation")
    private void showFreePractice(Player player,
                                 String raceName,
                                 RaceGridPosition pos,
                                 String stageName,
                                 Integer countdownSecondsRemaining,
                                 String timerLine,
                                 String lapLine,
                                 String sectionLine,
                                 String extraLine1,
                                 String extraLine2,
                                 String statusLine)
    {
        if (player == null) {
            return;
        }
        Scoreboard sb = Bukkit.getScoreboardManager() == null ? null : Bukkit.getScoreboardManager().getNewScoreboard();
        if (sb == null) {
            return;
        }

        Objective obj = sb.registerNewObjective("omegaRace", "dummy", ChatColor.GOLD + "Racing");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String position = pos == null ? "N/A" : ("#" + pos.getPositionIndex());

        int score = 12;
        obj.getScore(ChatColor.YELLOW + "Race: " + ChatColor.WHITE + (raceName == null ? "" : raceName)).setScore(score--);
        obj.getScore(ChatColor.YELLOW + "Stage: " + ChatColor.WHITE + (stageName == null ? "" : stageName)).setScore(score--);
        obj.getScore(ChatColor.YELLOW + "Pos: " + ChatColor.WHITE + position).setScore(score--);

        if (countdownSecondsRemaining != null) {
            obj.getScore(ChatColor.YELLOW + "Countdown: " + ChatColor.WHITE + formatTimer(countdownSecondsRemaining)).setScore(score--);
        }
        if (timerLine != null && !timerLine.isBlank()) {
            obj.getScore(ChatColor.YELLOW + "Timer: " + ChatColor.WHITE + timerLine).setScore(score--);
        }
        if (lapLine != null && !lapLine.isBlank()) {
            obj.getScore(ChatColor.YELLOW + "Lap: " + ChatColor.WHITE + lapLine).setScore(score--);
        }
        if (sectionLine != null && !sectionLine.isBlank()) {
            obj.getScore(ChatColor.YELLOW + "Section: " + ChatColor.WHITE + sectionLine).setScore(score--);
        }
        if (extraLine1 != null && !extraLine1.isBlank()) {
            obj.getScore(ChatColor.GRAY + extraLine1).setScore(score--);
        }
        if (extraLine2 != null && !extraLine2.isBlank()) {
            obj.getScore(ChatColor.GRAY + extraLine2).setScore(score--);
        }

        obj.getScore(ChatColor.DARK_GRAY + " ").setScore(score--);
        obj.getScore(statusLine == null ? (ChatColor.GRAY + "") : statusLine).setScore(score);

        player.setScoreboard(sb);
    }

    private static String timeLine(int timeRemainingSeconds, int timeLimitSeconds) {
        if (timeLimitSeconds <= 0) {
            return "Unlimited";
        }
        return formatTimer(timeRemainingSeconds);
    }

    private static String lapLine(int lap, int totalLaps) {
        int l = Math.max(0, lap);
        int t = Math.max(1, totalLaps);
        return l + "/" + t;
    }

    private static String lapOnlyLine(int lap) {
        return String.valueOf(Math.max(0, lap));
    }

    private static String sectionLine(int section, int totalSections) {
        int s = Math.max(0, section);
        int t = Math.max(1, totalSections);
        return s + "/" + t;
    }

    public void updateTimer(Player player, int secondsRemaining) {
        if (player == null) {
            return;
        }

        Scoreboard sb = player.getScoreboard();
        if (sb == null) {
            return;
        }

        Objective obj = sb.getObjective(DisplaySlot.SIDEBAR);
        if (obj == null) {
            return;
        }

        // Rebuild scoreboard by clearing and re-adding the timer line is annoying with Bukkit.
        // Minimal approach: just replace whole scoreboard when needed (caller can do that).
        // Here we no-op; caller should call showStartingScoreboard with updated time.
    }

    public void clear(Player player) {
        if (player != null) {
            player.setScoreboard(Bukkit.getScoreboardManager() == null ? Bukkit.getScoreboardManager().getMainScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private static String formatTimer(int secondsRemaining) {
        int s = Math.max(0, secondsRemaining);
        int m = s / 60;
        int sec = s % 60;
        return String.format("%02d:%02d", m, sec);
    }

    private static String formatLapTime(long millis) {
        if (millis <= 0) {
            return "--:--.--";
        }
        long centis = millis / 10;
        long minutes = centis / 6000;
        long seconds = (centis / 100) % 60;
        long hundredths = centis % 100;
        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
    }
}
