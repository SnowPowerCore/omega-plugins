package com.omega.interactable.plugin.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.omega.interactable.infrastructure.parse.InteractionConfigParser;
import com.omega.interactable.plugin.actions.ActionFactory;
import com.omega.interactable.plugin.actions.impl.RacingConfirmAcceptDelegate;
import com.omega.interactable.plugin.actions.impl.RacingConfirmAcceptDeleteRaceDelegate;
import com.omega.interactable.plugin.actions.impl.RacingConfirmAcceptStartFreePracticeDelegate;
import com.omega.interactable.plugin.actions.impl.RacingConfirmAcceptRemoveRacerDelegate;
import com.omega.interactable.plugin.actions.impl.RacingConfirmAcceptRemoveTeamDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustCancelBackDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustCancelDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustQualificationLapsDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustRaceLapsDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustSectionsDelegate;
import com.omega.interactable.plugin.actions.impl.RacingNumberAdjustSuccessDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionCancelBackDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionCancelDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionSuccessAddRacerDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionSuccessDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionSuccessNoopDelegate;
import com.omega.interactable.plugin.actions.impl.RacingPlayerSelectionSuccessRemoveRacerDelegate;
import com.omega.interactable.plugin.exec.InteractionExecutor;
import com.omega.interactable.plugin.listener.ItemInteractionListener;
import com.omega.interactable.plugin.listener.InventoryItemClickListener;
import org.bukkit.plugin.Plugin;


public final class OmegaInteractableModule extends AbstractModule {

    private final Plugin plugin;

    public OmegaInteractableModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Plugin.class).toInstance(plugin);

        bind(ClassLoader.class).toInstance(plugin.getClass().getClassLoader());

        bind(Gson.class).toInstance(new GsonBuilder().create());

        bind(InteractionConfigParser.class);
        bind(ActionFactory.class);
        bind(InteractionExecutor.class);
        bind(ItemInteractionListener.class);
        bind(InventoryItemClickListener.class);

        MapBinder<String, RacingConfirmAcceptDelegate> confirmDelegates =
            MapBinder.newMapBinder(binder(), String.class, RacingConfirmAcceptDelegate.class);
        confirmDelegates.addBinding(com.omega.interactable.core.constants.RacingConfirmKinds.DELETE_RACE)
            .to(RacingConfirmAcceptDeleteRaceDelegate.class);
        confirmDelegates.addBinding(com.omega.interactable.core.constants.RacingConfirmKinds.REMOVE_TEAM)
            .to(RacingConfirmAcceptRemoveTeamDelegate.class);
        confirmDelegates.addBinding(com.omega.interactable.core.constants.RacingConfirmKinds.REMOVE_RACER)
            .to(RacingConfirmAcceptRemoveRacerDelegate.class);

        confirmDelegates.addBinding(com.omega.interactable.core.constants.RacingConfirmKinds.START_FREE_PRACTICE)
            .to(RacingConfirmAcceptStartFreePracticeDelegate.class);

        MapBinder<String, RacingPlayerSelectionSuccessDelegate> selectionSuccessDelegates =
            MapBinder.newMapBinder(binder(), String.class, RacingPlayerSelectionSuccessDelegate.class);
        selectionSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingPlayerSelectionSuccessKinds.NOOP)
            .to(RacingPlayerSelectionSuccessNoopDelegate.class);
        selectionSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingPlayerSelectionSuccessKinds.REMOVE_RACER)
            .to(RacingPlayerSelectionSuccessRemoveRacerDelegate.class);
        selectionSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingPlayerSelectionSuccessKinds.ADD_RACER)
            .to(RacingPlayerSelectionSuccessAddRacerDelegate.class);

        MapBinder<String, RacingPlayerSelectionCancelDelegate> selectionCancelDelegates =
            MapBinder.newMapBinder(binder(), String.class, RacingPlayerSelectionCancelDelegate.class);
        selectionCancelDelegates.addBinding(com.omega.interactable.core.constants.RacingPlayerSelectionCancelKinds.BACK)
            .to(RacingPlayerSelectionCancelBackDelegate.class);

        MapBinder<String, RacingNumberAdjustSuccessDelegate> numberAdjustSuccessDelegates =
            MapBinder.newMapBinder(binder(), String.class, RacingNumberAdjustSuccessDelegate.class);
        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.SECTIONS)
            .to(RacingNumberAdjustSectionsDelegate.class);
        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.QUALIFICATION_LAPS)
            .to(RacingNumberAdjustQualificationLapsDelegate.class);
        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.RACE_LAPS)
            .to(RacingNumberAdjustRaceLapsDelegate.class);
        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.POSITIONS)
            .to(com.omega.interactable.plugin.actions.impl.RacingNumberAdjustPositionsDelegate.class);

        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.QUALIFICATION_TIME_LIMIT)
            .to(com.omega.interactable.plugin.actions.impl.RacingNumberAdjustQualificationTimeLimitDelegate.class);
        numberAdjustSuccessDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustSuccessKinds.FREE_PRACTICE_TIME_LIMIT)
            .to(com.omega.interactable.plugin.actions.impl.RacingNumberAdjustFreePracticeTimeLimitDelegate.class);

        MapBinder<String, RacingNumberAdjustCancelDelegate> numberAdjustCancelDelegates =
            MapBinder.newMapBinder(binder(), String.class, RacingNumberAdjustCancelDelegate.class);
        numberAdjustCancelDelegates.addBinding(com.omega.interactable.core.constants.RacingNumberAdjustCancelKinds.BACK)
            .to(RacingNumberAdjustCancelBackDelegate.class);
    }
}
