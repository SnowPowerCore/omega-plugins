package com.omega.racing.plugin.di;

import com.google.gson.Gson;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.omega.racing.core.OmegaRacingConstants;
import com.omega.racing.core.api.RaceDataApi;
import com.omega.racing.core.api.RacingSessionsApi;
import com.omega.racing.core.api.RacingPromptsApi;
import com.omega.racing.core.api.RaceUiApi;
import com.omega.racing.infrastructure.interop.InventoryRegistryResolver;
import com.omega.racing.infrastructure.interop.ItemRegistryResolver;
import com.omega.racing.infrastructure.storage.JsonRaceRepository;
import com.omega.racing.infrastructure.storage.RaceRepository;
import com.omega.racing.plugin.command.RaceCommand;
import com.omega.racing.plugin.listener.FreePracticeBoatListener;
import com.omega.racing.plugin.listener.FreePracticeCountdownLockListener;
import com.omega.racing.plugin.listener.RaceResultsInventoryListener;
import com.omega.racing.plugin.listener.RaceBlocksInventoryListener;
import com.omega.racing.plugin.listener.RaceBlocksPlacedListener;
import com.omega.racing.plugin.listener.RaceFreezeListener;
import com.omega.racing.plugin.listener.RacerPositionToolListener;
import com.omega.racing.plugin.runtime.RaceDataService;
import com.omega.racing.plugin.runtime.RaceManager;
import com.omega.racing.plugin.runtime.RaceTeamManager;
import com.omega.racing.plugin.runtime.RacingPromptService;
import com.omega.racing.plugin.runtime.RaceUiService;
import com.omega.racing.plugin.runtime.RacingSessionStore;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksPlacedStore;
import com.omega.racing.plugin.runtime.blocks.RaceBlocksUiService;
import com.omega.racing.plugin.runtime.stage.RaceFreezeService;
import com.omega.racing.plugin.runtime.stage.RaceScoreboardService;
import com.omega.racing.plugin.runtime.stage.RaceStageService;
import com.omega.racing.plugin.runtime.stage.RaceStageStateRepository;
import com.omega.racing.plugin.runtime.prompt.ChatPrompt;
import com.omega.racing.plugin.runtime.prompt.RemoveRacerPrompt;
import com.omega.racing.plugin.runtime.prompt.RenameRacePrompt;
import com.omega.racing.plugin.runtime.prompt.RenameTeamPrompt;
import com.omega.racing.plugin.runtime.ui.ConfirmInventoryUi;
import com.omega.racing.plugin.runtime.ui.FreePracticeSettingsInventoryUi;
import com.omega.racing.plugin.runtime.ui.InventoryRenderer;
import com.omega.racing.plugin.runtime.ui.MainInventoryUi;
import com.omega.racing.plugin.runtime.ui.NumberAdjustInventoryUi;
import com.omega.racing.plugin.runtime.ui.PlayerSelectionInventoryUi;
import com.omega.racing.plugin.runtime.ui.TeamEditInventoryUi;
import com.omega.racing.plugin.runtime.ui.TeamsInventoryUi;
import com.omega.racing.plugin.runtime.ui.TimeAdjustInventoryUi;
import com.omega.racing.plugin.runtime.ui.UiComponents;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Singleton;
import java.nio.file.Path;

public final class OmegaRacingModule extends AbstractModule {

    private final JavaPlugin plugin;

    public OmegaRacingModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);

        Path dataFolder = plugin.getDataFolder().toPath();
        bind(Path.class).annotatedWith(Names.named("dataFolder")).toInstance(dataFolder);

        bind(RaceRepository.class).to(JsonRaceRepository.class);
        bind(InventoryRegistryResolver.class);
        bind(ItemRegistryResolver.class);

        bind(RacingSessionStore.class).in(Singleton.class);
        bind(RaceManager.class).in(Singleton.class);
        bind(RaceTeamManager.class).in(Singleton.class);
        bind(RaceDataService.class).in(Singleton.class);
        bind(RaceUiService.class).in(Singleton.class);
        bind(RacingPromptService.class).in(Singleton.class);

        bind(RaceBlocksUiService.class).in(Singleton.class);
        bind(RaceBlocksPlacedStore.class).in(Singleton.class);
        bind(RaceBlocksInventoryListener.class);
        bind(RaceBlocksPlacedListener.class);
        bind(RacerPositionToolListener.class);

        bind(RaceStageStateRepository.class).in(Singleton.class);
        bind(RaceFreezeService.class).in(Singleton.class);
        bind(RaceScoreboardService.class).in(Singleton.class);
        bind(RaceStageService.class).in(Singleton.class);
        bind(RaceFreezeListener.class);
        bind(FreePracticeBoatListener.class);
        bind(FreePracticeCountdownLockListener.class);
        bind(RaceResultsInventoryListener.class);

        bind(RaceCommand.class);

        Multibinder<InventoryRenderer> renderers = Multibinder.newSetBinder(binder(), InventoryRenderer.class);
        renderers.addBinding().to(MainInventoryUi.class);
        renderers.addBinding().to(TeamsInventoryUi.class);
        renderers.addBinding().to(TeamEditInventoryUi.class);
        renderers.addBinding().to(PlayerSelectionInventoryUi.class);
        renderers.addBinding().to(ConfirmInventoryUi.class);
        renderers.addBinding().to(NumberAdjustInventoryUi.class);
        renderers.addBinding().to(FreePracticeSettingsInventoryUi.class);
        renderers.addBinding().to(TimeAdjustInventoryUi.class);

        Multibinder<ChatPrompt> prompts = Multibinder.newSetBinder(binder(), ChatPrompt.class);
        prompts.addBinding().to(RenameRacePrompt.class);
        prompts.addBinding().to(RenameTeamPrompt.class);
        prompts.addBinding().to(RemoveRacerPrompt.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public UiComponents provideUiComponents(ItemRegistryResolver itemRegistry, Gson gson) {
        return new UiComponents(itemRegistry, gson);
    }

    @Provides
    @Singleton
    public RaceUiApi provideUiApi(RaceUiService ui) {
        return ui;
    }

    @Provides
    @Singleton
    public RaceDataApi provideRaceDataApi(RaceDataService data) {
        return data;
    }

    @Provides
    @Singleton
    public RacingPromptsApi providePromptsApi(RacingPromptService prompts) {
        return prompts;
    }

    @Provides
    @Singleton
    public RacingSessionsApi provideSessionsApi(RacingSessionStore sessions) {
        return sessions;
    }

    @Provides
    @Singleton
    public Path racesFile(@com.google.inject.name.Named("dataFolder") Path dataFolder) {
        return dataFolder.resolve(OmegaRacingConstants.RACES_FILE_NAME);
    }
}
