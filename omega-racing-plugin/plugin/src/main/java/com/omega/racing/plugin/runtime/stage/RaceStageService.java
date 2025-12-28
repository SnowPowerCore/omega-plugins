package com.omega.racing.plugin.runtime.stage;

import com.omega.racing.core.model.RaceDefinition;
import com.omega.racing.core.model.RaceGridPosition;
import com.omega.racing.core.model.RaceStageStartResult;
import com.omega.racing.core.model.RaceStageStatus;
import com.omega.racing.core.model.RaceStageType;
import com.omega.racing.core.model.RaceTeam;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksKeys;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksPlacedStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RaceStageService {

    private static final int COUNTDOWN_SECONDS = 20;
    private static final int POPUP_LAST_SECONDS = 10;
    private static final int GO_TITLE_STAY_TICKS = 40;
    private static final int GO_TITLE_FADE_OUT_TICKS = 20;

    private final JavaPlugin plugin;
    private final RaceManager races;
    private final RaceFreezeService freeze;
    private final RaceScoreboardService scoreboards;
    private final RaceStageStateRepository states;
    private final RaceBlocksPlacedStore placed;

    private final Map<String, Integer> runningTaskIdsByRace = new HashMap<>();

    private final Set<UUID> freePracticeCountdownLockedPlayers = new HashSet<>();

    private final Map<String, FreePracticeRun> freePracticeRunsByRace = new HashMap<>();
    private final Map<UUID, FreePracticeBoatRef> freePracticeBoats = new HashMap<>();

    @Inject
    public RaceStageService(JavaPlugin plugin, RaceManager races, RaceFreezeService freeze, RaceScoreboardService scoreboards, RaceStageStateRepository states, RaceBlocksPlacedStore placed) {
        this.plugin = plugin;
        this.races = races;
        this.freeze = freeze;
        this.scoreboards = scoreboards;
        this.states = states;
        this.placed = placed;
    }

    private static final class FreePracticeBoatRef {
        private final String raceKey;
        private final String racerUuid;

        private FreePracticeBoatRef(String raceKey, String racerUuid) {
            this.raceKey = raceKey;
            this.racerUuid = racerUuid;
        }
    }

    private static final class FreePracticeRacerRuntime {
        private final String racerUuid;
        private final RaceGridPosition grid;
        private final Location spawn;
        private UUID boatId;

        private int lap;
        private int section;
        private int lastSectionIndex = Integer.MIN_VALUE;

        private long currentLapStartedAtMillis;
        private long bestLapMillis;

        private UUID lastWorldId;
        private int lastBlockX;
        private int lastBlockY;
        private int lastBlockZ;

        private FreePracticeRacerRuntime(String racerUuid, RaceGridPosition grid, Location spawn) {
            this.racerUuid = racerUuid;
            this.grid = grid;
            this.spawn = spawn;
        }
    }

    private static final class FreePracticeRun {
        private final String raceName;
        private final String raceKey;
        private final Map<String, FreePracticeRacerRuntime> racers;
        private final int totalLaps;
        private final int totalSections;
        private final int timeLimitSeconds;

        private int countdownRemainingSeconds;
        private boolean countdownFinishedHandled;
        private boolean officialStarted;
        private long officialStartedAtMillis;
        private int timeRemainingSeconds;

        private FreePracticeRun(String raceName,
                               String raceKey,
                               Map<String, FreePracticeRacerRuntime> racers,
                               int totalLaps,
                               int totalSections,
                               int timeLimitSeconds,
                               int countdownRemainingSeconds)
        {
            this.raceName = raceName;
            this.raceKey = raceKey;
            this.racers = racers;
            this.totalLaps = totalLaps;
            this.totalSections = totalSections;
            this.timeLimitSeconds = timeLimitSeconds;
            this.countdownRemainingSeconds = countdownRemainingSeconds;
            this.timeRemainingSeconds = timeLimitSeconds;
        }
    }

    public boolean isFreePracticeBoat(UUID boatId) {
        return boatId != null && freePracticeBoats.containsKey(boatId);
    }

    public boolean isFreePracticeCountdownLocked(UUID playerId) {
        return playerId != null && freePracticeCountdownLockedPlayers.contains(playerId);
    }

    public void unlockFreePracticeCountdown(UUID playerId) {
        if (playerId != null) {
            freePracticeCountdownLockedPlayers.remove(playerId);
        }
    }

    public void onPlayerQuit(UUID playerId) {
        if (playerId == null) {
            return;
        }

        freePracticeCountdownLockedPlayers.remove(playerId);

        // If the quitter is in any tracked run, clean up their boat mapping.
        for (FreePracticeRun run : new ArrayList<>(freePracticeRunsByRace.values())) {
            for (FreePracticeRacerRuntime rr : run.racers.values()) {
                if (rr == null) {
                    continue;
                }
                if (!playerId.toString().equals(rr.racerUuid)) {
                    continue;
                }
                if (rr.boatId != null) {
                    freePracticeBoats.remove(rr.boatId);
                }
                rr.boatId = null;
            }
        }
    }

    public void onFreePracticeBoatMove(Boat boat, Location from, Location to) {
        if (boat == null) {
            return;
        }
        FreePracticeBoatRef ref = freePracticeBoats.get(boat.getUniqueId());
        if (ref == null) {
            return;
        }

        FreePracticeRun run = freePracticeRunsByRace.get(ref.raceKey);
        if (run == null) {
            return;
        }
        FreePracticeRacerRuntime rr = run.racers.get(ref.racerUuid);
        if (rr == null) {
            return;
        }

        if (run.countdownRemainingSeconds > 0) {
            lockBoatToSpawn(boat, rr.spawn, from, to);
            return;
        }

        if (to == null || to.getWorld() == null) {
            return;
        }

        // Only evaluate when entering a new candidate block coordinate.
        UUID worldId = to.getWorld().getUID();
        int bx = to.getBlockX();
        int by = (int) Math.floor(to.getY() - 0.2);
        int bz = to.getBlockZ();
        if (worldId.equals(rr.lastWorldId) && bx == rr.lastBlockX && by == rr.lastBlockY && bz == rr.lastBlockZ) {
            return;
        }
        rr.lastWorldId = worldId;
        rr.lastBlockX = bx;
        rr.lastBlockY = by;
        rr.lastBlockZ = bz;

        int sectionIndex = findSectionIndex(run.raceName, to.getWorld(), bx, by, bz);
        if (sectionIndex < 0) {
            return;
        }

        int prevSectionIndex = rr.lastSectionIndex;
        if (sectionIndex == prevSectionIndex) {
            return;
        }

        rr.lastSectionIndex = sectionIndex;
        rr.section = sectionIndex + 1;

        if (!run.officialStarted && sectionIndex == 0) {
            startOfficialFreePractice(run);
        }

        if (run.officialStarted && sectionIndex == 0 && prevSectionIndex != 0) {
            long now = System.currentTimeMillis();
            if (rr.currentLapStartedAtMillis > 0) {
                long lapTime = Math.max(0, now - rr.currentLapStartedAtMillis);
                if (lapTime > 0 && (rr.bestLapMillis <= 0 || lapTime < rr.bestLapMillis)) {
                    rr.bestLapMillis = lapTime;
                }
            }
            rr.currentLapStartedAtMillis = now;

            if (rr.lap <= 0) {
                rr.lap = 1;
            } else {
                rr.lap = rr.lap + 1;
            }

            // Persist progress each lap.
            saveFreePracticeProgress(run);
        }

        // Update scoreboard for this racer.
        showFreePracticeScoreboardForRacer(run, rr);
    }

    private void lockBoatToSpawn(Boat boat, Location spawn, Location fallback, Location to) {
        try {
            Location base = spawn != null ? spawn : fallback;
            if (base != null) {
                Location target = base.clone();
                // Preserve rotation so the rider can freely look around.
                if (to != null) {
                    target.setYaw(to.getYaw());
                    target.setPitch(to.getPitch());
                } else {
                    Location current = boat.getLocation();
                    target.setYaw(current.getYaw());
                    target.setPitch(current.getPitch());
                }
                boat.teleport(target);
            }
            boat.setVelocity(new Vector(0, 0, 0));
        } catch (Exception ignored) {
        }
    }

    private void showGoTitle(Player player) {
        if (player == null) {
            return;
        }
        // Let vanilla title timing handle disappearing naturally (no manual clears).
        player.sendTitle(ChatColor.GOLD + "FREE PRACTICE", ChatColor.GREEN + "GO!", 0, GO_TITLE_STAY_TICKS, GO_TITLE_FADE_OUT_TICKS);
    }

    private void lockPlayerForFreePracticeCountdown(UUID playerId) {
        if (playerId != null) {
            freePracticeCountdownLockedPlayers.add(playerId);
        }
    }

    private int findSectionIndex(String raceName, World world, int bx, int by, int bz) {
        if (world == null) {
            return -1;
        }

        // Never force-load chunks from movement events.
        int cx = bx >> 4;
        int cz = bz >> 4;
        try {
            if (!world.isChunkLoaded(cx, cz)) {
                return -1;
            }
        } catch (Exception ignored) {
            // If API differs, fall through; getBlockAt might load, so be conservative.
            return -1;
        }

        // Check the block at/under the boat. We prefer tight checks before reading chunk PDC.
        for (int dy = 0; dy >= -1; dy--) {
            Block block = world.getBlockAt(bx, by + dy, bz);
            if (block == null || block.getType() != org.bukkit.Material.BLUE_ICE) {
                continue;
            }
            var idOpt = placed.getStageId(block);
            if (idOpt.isEmpty()) {
                continue;
            }
            var id = idOpt.get();
            if (id == null) {
                continue;
            }
            if (!RaceBlocksKeys.KIND_SECTION.equals(id.kind())) {
                continue;
            }
            if (raceName != null && !raceName.equals(id.raceName())) {
                continue;
            }
            return id.index();
        }
        return -1;
    }

    private void startOfficialFreePractice(FreePracticeRun run) {
        run.officialStarted = true;
        run.officialStartedAtMillis = System.currentTimeMillis();
        run.timeRemainingSeconds = run.timeLimitSeconds;

        for (FreePracticeRacerRuntime rr : run.racers.values()) {
            showFreePracticeScoreboardForRacer(run, rr);
            try {
                UUID id = UUID.fromString(rr.racerUuid);
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    p.sendTitle(ChatColor.GOLD + "FREE PRACTICE", ChatColor.AQUA + "STARTED", 0, 25, 10);
                }
            } catch (Exception ignored) {
            }
        }

        saveFreePracticeProgress(run);
    }

    private void showFreePracticeScoreboardForRacer(FreePracticeRun run, FreePracticeRacerRuntime rr) {
        if (run == null || rr == null) {
            return;
        }
        Player p;
        try {
            p = Bukkit.getPlayer(UUID.fromString(rr.racerUuid));
        } catch (Exception ignored) {
            p = null;
        }
        if (p == null) {
            return;
        }

        if (run.countdownRemainingSeconds > 0) {
            scoreboards.showFreePracticeCountdown(p, run.raceName, rr.grid, run.countdownRemainingSeconds);
            return;
        }

        if (!run.officialStarted) {
            scoreboards.showFreePracticeWaiting(p, run.raceName, rr.grid, rr.lap, run.totalLaps, rr.section, run.totalSections);
            return;
        }

        int remaining = run.timeLimitSeconds <= 0 ? 0 : Math.max(0, run.timeRemainingSeconds);
        long now = System.currentTimeMillis();
        long currentLapMillis = rr.currentLapStartedAtMillis <= 0 ? 0 : Math.max(0, now - rr.currentLapStartedAtMillis);
        scoreboards.showFreePracticeRunning(p, run.raceName, rr.grid, rr.lap, run.totalLaps, rr.section, run.totalSections, remaining, run.timeLimitSeconds, currentLapMillis, rr.bestLapMillis);
    }

    private void saveFreePracticeProgress(FreePracticeRun run) {
        try {
            RaceStageState state = states.load(run.raceName).orElse(new RaceStageState());
            state.setRaceName(run.raceName);
            state.setStageType(RaceStageType.FREE_PRACTICE);
            // status stays whatever the countdown task last wrote.
            state.setFreePracticeOfficialStarted(run.officialStarted);
            state.setFreePracticeOfficialStartedAtMillis(run.officialStartedAtMillis);
            state.setFreePracticeTimeLimitSeconds(run.timeLimitSeconds);
            state.setFreePracticeTimeRemainingSeconds(run.timeLimitSeconds <= 0 ? 0 : Math.max(0, run.timeRemainingSeconds));

            java.util.Map<String, RaceStageState.FreePracticeRacerProgress> racers = new java.util.LinkedHashMap<>();
            for (FreePracticeRacerRuntime rr : run.racers.values()) {
                racers.put(rr.racerUuid, new RaceStageState.FreePracticeRacerProgress(rr.lap, rr.section, rr.bestLapMillis));
            }
            state.setFreePracticeRacers(racers);
            states.save(run.raceName, state);
        } catch (Exception ignored) {
        }
    }

    public RaceStageStartResult startFreePractice(String raceName) {
        Optional<RaceDefinition> raceOpt = races.get(raceName);
        if (raceOpt.isEmpty()) {
            return new RaceStageStartResult(false, List.of());
        }

        RaceDefinition race = raceOpt.get();

        // Validate: every racer must have a position.
        List<String> missing = new ArrayList<>();
        Map<String, RaceGridPosition> positionsByRacer = new LinkedHashMap<>();
        for (RaceTeam team : race.getTeams()) {
            if (team == null) {
                continue;
            }
            for (RaceTeam.Racer racer : team.getRacers()) {
                if (racer == null || racer.getUuid() == null || racer.getUuid().isBlank()) {
                    continue;
                }
                RaceGridPosition pos = racer.getRacePosition();
                if (pos == null || pos.getWorld() == null || pos.getWorld().isBlank()) {
                    missing.add(racer.getUuid());
                    continue;
                }
                positionsByRacer.put(racer.getUuid(), pos);
            }
        }

        if (!missing.isEmpty()) {
            return new RaceStageStartResult(false, missing);
        }

        // Cancel any existing stage countdown/runtime for this race.
        stopFreePractice(race.getName(), false);

        // Persist STARTING state.
        RaceStageState state = new RaceStageState(race.getName(), RaceStageType.FREE_PRACTICE, RaceStageStatus.STARTING, System.currentTimeMillis(), COUNTDOWN_SECONDS);
        state.setFreePracticeOfficialStarted(false);
        state.setFreePracticeOfficialStartedAtMillis(0L);
        state.setFreePracticeTimeLimitSeconds(race.getFreePractice().getTimeLimitSeconds());
        state.setFreePracticeTimeRemainingSeconds(race.getFreePractice().getTimeLimitSeconds());
        state.setFreePracticeEndedAtMillis(0L);
        states.save(race.getName(), state);

        int totalLaps = Math.max(1, race.getRace().getLaps());
        int totalSections = Math.max(1, race.getSections());
        int timeLimitSeconds = race.getFreePractice().getTimeLimitSeconds();

        Map<String, FreePracticeRacerRuntime> racerRuntime = new LinkedHashMap<>();

        // Teleport racers to positions and lock movement until countdown ends.
        for (Map.Entry<String, RaceGridPosition> entry : positionsByRacer.entrySet()) {
            UUID id;
            try {
                id = UUID.fromString(entry.getKey());
            } catch (Exception ignored) {
                continue;
            }

            Player p = Bukkit.getPlayer(id);
            if (p == null) {
                continue;
            }

            RaceGridPosition pos = entry.getValue();
            World world = Bukkit.getWorld(pos.getWorld());
            if (world == null) {
                continue;
            }

            Location loc = new Location(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, pos.getYaw(), pos.getPitch());

            // Ensure player isn't in another vehicle.
            try {
                if (p.isInsideVehicle()) {
                    p.leaveVehicle();
                }
            } catch (Exception ignored) {
            }

            p.teleport(loc);

            lockPlayerForFreePracticeCountdown(id);
            racerRuntime.put(entry.getKey(), new FreePracticeRacerRuntime(entry.getKey(), pos, loc));

            p.sendTitle(ChatColor.GOLD + "FREE PRACTICE", ChatColor.YELLOW + "Get ready", 10, 40, 10);
            scoreboards.showFreePracticeCountdown(p, race.getName(), pos, COUNTDOWN_SECONDS);
        }

        FreePracticeRun run = new FreePracticeRun(race.getName(), normalizeKey(race.getName()), racerRuntime, totalLaps, totalSections, timeLimitSeconds, COUNTDOWN_SECONDS);
        freePracticeRunsByRace.put(run.raceKey, run);

        // Countdown task (once per second).
        String key = normalizeKey(race.getName());
        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int remaining = COUNTDOWN_SECONDS;

            @Override
            public void run() {
                remaining = Math.max(0, remaining - 1);

                FreePracticeRun currentRun = freePracticeRunsByRace.get(key);
                if (currentRun != null) {
                    currentRun.countdownRemainingSeconds = remaining;
                }

                state.setCountdownSecondsRemaining(remaining);
                states.save(race.getName(), state);

                for (Map.Entry<String, RaceGridPosition> entry : positionsByRacer.entrySet()) {
                    UUID id;
                    try {
                        id = UUID.fromString(entry.getKey());
                    } catch (Exception ignored) {
                        continue;
                    }

                    Player p = Bukkit.getPlayer(id);
                    if (p == null) {
                        continue;
                    }

                    RaceGridPosition pos = entry.getValue();
                    if (currentRun != null) {
                        FreePracticeRacerRuntime rr = currentRun.racers.get(entry.getKey());
                        if (rr != null) {
                            showFreePracticeScoreboardForRacer(currentRun, rr);
                        } else {
                            scoreboards.showFreePracticeCountdown(p, race.getName(), pos, remaining);
                        }
                    } else {
                        scoreboards.showFreePracticeCountdown(p, race.getName(), pos, remaining);
                    }

                    if (remaining <= POPUP_LAST_SECONDS && remaining > 0) {
                        p.sendTitle(ChatColor.RED.toString() + remaining, "", 0, 20, 0);
                    }
                }

                if (remaining <= 0) {
                    if (currentRun != null && !currentRun.countdownFinishedHandled) {
                        currentRun.countdownFinishedHandled = true;

                        // Spawn boats now, seat racers, and unlock movement.
                        for (FreePracticeRacerRuntime rr : currentRun.racers.values()) {
                            if (rr == null || rr.grid == null || rr.spawn == null) {
                                continue;
                            }
                            UUID pid;
                            try {
                                pid = UUID.fromString(rr.racerUuid);
                            } catch (Exception ignored) {
                                continue;
                            }

                            unlockFreePracticeCountdown(pid);

                            Player p = Bukkit.getPlayer(pid);
                            if (p == null) {
                                continue;
                            }
                            World w = rr.spawn.getWorld();
                            if (w == null) {
                                continue;
                            }

                            try {
                                if (p.isInsideVehicle()) {
                                    p.leaveVehicle();
                                }
                            } catch (Exception ignored) {
                            }

                            Boat boat = null;
                            try {
                                var entity = w.spawnEntity(rr.spawn, EntityType.BOAT);
                                if (entity instanceof Boat b) {
                                    boat = b;
                                }
                            } catch (Exception ignored) {
                            }

                            if (boat != null) {
                                try {
                                    boat.setRotation(rr.grid.getYaw(), 0.0f);
                                    boat.setPersistent(true);
                                } catch (Exception ignored) {
                                }

                                try {
                                    boat.addPassenger(p);
                                } catch (Exception ignored) {
                                }

                                rr.boatId = boat.getUniqueId();
                                freePracticeBoats.put(rr.boatId, new FreePracticeBoatRef(normalizeKey(race.getName()), rr.racerUuid));
                            }
                        }

                        // Boats can move now.
                        state.setStatus(RaceStageStatus.RUNNING);
                        states.save(race.getName(), state);

                        for (String uuid : positionsByRacer.keySet()) {
                            try {
                                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                                if (p != null) {
                                    showGoTitle(p);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }

                // After countdown: if official started and there is a time limit, tick it down.
                if (remaining <= 0 && currentRun != null && currentRun.officialStarted && currentRun.timeLimitSeconds > 0) {
                    currentRun.timeRemainingSeconds = Math.max(0, currentRun.timeRemainingSeconds - 1);

                    // Update scoreboards (timer display) once per second.
                    for (FreePracticeRacerRuntime rr : currentRun.racers.values()) {
                        showFreePracticeScoreboardForRacer(currentRun, rr);
                    }

                    // Persist remaining time.
                    state.setFreePracticeOfficialStarted(true);
                    state.setFreePracticeOfficialStartedAtMillis(currentRun.officialStartedAtMillis);
                    state.setFreePracticeTimeLimitSeconds(currentRun.timeLimitSeconds);
                    state.setFreePracticeTimeRemainingSeconds(currentRun.timeRemainingSeconds);
                    states.save(race.getName(), state);

                    if (currentRun.timeRemainingSeconds <= 0) {
                        endFreePractice(currentRun);
                        cancelRaceTask(race.getName());
                    }
                }
            }
        }, 20L, 20L);

        runningTaskIdsByRace.put(key, taskId);
        return new RaceStageStartResult(true, List.of());
    }

    private void endFreePractice(FreePracticeRun run) {
        if (run == null) {
            return;
        }

        // Stop stage task defensively (end can be reached via timer).
        cancelRaceTask(run.raceName);

        // Persist final progress.
        try {
            RaceStageState state = states.load(run.raceName).orElse(new RaceStageState());
            state.setRaceName(run.raceName);
            state.setStageType(RaceStageType.FREE_PRACTICE);
            state.setStatus(RaceStageStatus.STOPPED);
            state.setFreePracticeEndedAtMillis(System.currentTimeMillis());
            state.setFreePracticeTimeRemainingSeconds(0);

            java.util.Map<String, RaceStageState.FreePracticeRacerProgress> racers = new java.util.LinkedHashMap<>();
            for (FreePracticeRacerRuntime rr : run.racers.values()) {
                racers.put(rr.racerUuid, new RaceStageState.FreePracticeRacerProgress(rr.lap, rr.section, rr.bestLapMillis));
            }
            state.setFreePracticeRacers(racers);
            states.save(run.raceName, state);
        } catch (Exception ignored) {
        }

        // Notify + cleanup boats.
        for (FreePracticeRacerRuntime rr : run.racers.values()) {
            try {
                Player p = Bukkit.getPlayer(UUID.fromString(rr.racerUuid));
                if (p != null) {
                    p.sendTitle(ChatColor.GOLD + "FREE PRACTICE", ChatColor.RED + "TIME'S UP", 0, 40, 20);
                    scoreboards.clear(p);
                }
            } catch (Exception ignored) {
            }

            if (rr.boatId != null) {
                try {
                    var entity = Bukkit.getEntity(rr.boatId);
                    if (entity != null) {
                        entity.remove();
                    }
                } catch (Exception ignored) {
                }
                freePracticeBoats.remove(rr.boatId);
            }

            try {
                unlockFreePracticeCountdown(UUID.fromString(rr.racerUuid));
            } catch (Exception ignored) {
            }
        }

        freePracticeRunsByRace.remove(run.raceKey);
    }

    private void stopFreePractice(String raceName, boolean silent) {
        cancelRaceTask(raceName);
        String key = normalizeKey(raceName);
        FreePracticeRun run = freePracticeRunsByRace.remove(key);
        if (run == null) {
            return;
        }

        for (FreePracticeRacerRuntime rr : run.racers.values()) {
            if (rr.boatId != null) {
                freePracticeBoats.remove(rr.boatId);
                try {
                    var entity = Bukkit.getEntity(rr.boatId);
                    if (entity != null) {
                        entity.remove();
                    }
                } catch (Exception ignored) {
                }
            }

            try {
                unlockFreePracticeCountdown(UUID.fromString(rr.racerUuid));
            } catch (Exception ignored) {
            }
            try {
                Player p = Bukkit.getPlayer(UUID.fromString(rr.racerUuid));
                if (p != null) {
                    freeze.unfreeze(p.getUniqueId());
                    scoreboards.clear(p);
                    if (!silent) {
                        p.sendMessage(ChatColor.GRAY + "Free Practice stopped.");
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void cancelRaceTask(String raceName) {
        String key = normalizeKey(raceName);
        Integer existing = runningTaskIdsByRace.remove(key);
        if (existing != null) {
            plugin.getServer().getScheduler().cancelTask(existing);
        }
    }

    private static String normalizeKey(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
