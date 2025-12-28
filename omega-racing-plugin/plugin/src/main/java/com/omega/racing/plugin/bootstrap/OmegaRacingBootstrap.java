package com.omega.racing.plugin.bootstrap;

import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RacingPromptsApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.plugin.command.RaceCommand;
import com.omega.racing.plugin.listener.FreePracticeBoatListener;
import com.omega.racing.plugin.listener.FreePracticeCountdownLockListener;
import com.omega.racing.plugin.listener.RaceResultsInventoryListener;
import com.omega.racing.plugin.listener.RaceBlocksInventoryListener;
import com.omega.racing.plugin.listener.RaceBlocksPlacedListener;
import com.omega.racing.plugin.listener.RaceFreezeListener;
import com.omega.racing.plugin.listener.RacerPositionToolListener;
import com.omega.racing.plugin.listener.RacingChatListener;
import com.omega.racing.plugin.runtime.RaceManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Inject;

public final class OmegaRacingBootstrap {

    private final JavaPlugin plugin;
    private final RaceCommand raceCommand;
    private final RaceManager raceManager;
    private final RacingChatListener chatListener;
    private final RaceBlocksInventoryListener raceBlocksListener;
    private final RaceBlocksPlacedListener raceBlocksPlacedListener;
    private final RacerPositionToolListener racerPositionToolListener;
    private final RaceFreezeListener freezeListener;
    private final FreePracticeBoatListener freePracticeBoatListener;
    private final FreePracticeCountdownLockListener freePracticeCountdownLockListener;
    private final RaceResultsInventoryListener raceResultsInventoryListener;
    private final RaceUiApi uiApi;
    private final RacingPromptsApi promptsApi;
    private final RaceDataApi dataApi;
    private final RacingSessionsApi sessions;

    @Inject
    public OmegaRacingBootstrap(JavaPlugin plugin,
                                RaceCommand raceCommand,
                                RaceManager raceManager,
                                RacingChatListener chatListener,
                                RaceBlocksInventoryListener raceBlocksListener,
                                RaceBlocksPlacedListener raceBlocksPlacedListener,
                                RacerPositionToolListener racerPositionToolListener,
                                RaceFreezeListener freezeListener,
                                FreePracticeBoatListener freePracticeBoatListener,
                                FreePracticeCountdownLockListener freePracticeCountdownLockListener,
                                RaceResultsInventoryListener raceResultsInventoryListener,
                                RaceUiApi uiApi,
                                RacingPromptsApi promptsApi,
                                RaceDataApi dataApi,
                                RacingSessionsApi sessions)
    {
        this.plugin = plugin;
        this.raceCommand = raceCommand;
        this.raceManager = raceManager;
        this.chatListener = chatListener;
        this.raceBlocksListener = raceBlocksListener;
        this.raceBlocksPlacedListener = raceBlocksPlacedListener;
        this.racerPositionToolListener = racerPositionToolListener;
        this.freezeListener = freezeListener;
        this.freePracticeBoatListener = freePracticeBoatListener;
        this.freePracticeCountdownLockListener = freePracticeCountdownLockListener;
        this.raceResultsInventoryListener = raceResultsInventoryListener;
        this.uiApi = uiApi;
        this.promptsApi = promptsApi;
        this.dataApi = dataApi;
        this.sessions = sessions;
    }

    public void onEnable() {

        raceManager.load();

        PluginCommand cmd = plugin.getCommand("race");
        if (cmd != null) {
            cmd.setExecutor(raceCommand);
            cmd.setTabCompleter(raceCommand);
        }

        Bukkit.getPluginManager().registerEvents(chatListener, plugin);
        Bukkit.getPluginManager().registerEvents(raceBlocksListener, plugin);
        Bukkit.getPluginManager().registerEvents(raceBlocksPlacedListener, plugin);
        Bukkit.getPluginManager().registerEvents(racerPositionToolListener, plugin);
        Bukkit.getPluginManager().registerEvents(freezeListener, plugin);
        Bukkit.getPluginManager().registerEvents(freePracticeBoatListener, plugin);
        Bukkit.getPluginManager().registerEvents(freePracticeCountdownLockListener, plugin);
        Bukkit.getPluginManager().registerEvents(raceResultsInventoryListener, plugin);

        Bukkit.getServicesManager().register(RaceUiApi.class, uiApi, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(RacingPromptsApi.class, promptsApi, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(RaceDataApi.class, dataApi, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(RacingSessionsApi.class, sessions, plugin, ServicePriority.Normal);
    }

    public void onDisable() {

        raceManager.save();

        Bukkit.getServicesManager().unregister(RaceUiApi.class, uiApi);
        Bukkit.getServicesManager().unregister(RacingPromptsApi.class, promptsApi);
        Bukkit.getServicesManager().unregister(RaceDataApi.class, dataApi);
        Bukkit.getServicesManager().unregister(RacingSessionsApi.class, sessions);
    }
}