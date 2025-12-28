package com.omega.interactable.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.omega.interactable.plugin.bootstrap.OmegaInteractableBootstrap;
import com.omega.interactable.plugin.di.OmegaInteractableModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class OmegaInteractablePlugin extends JavaPlugin {

    private Injector injector;
    private OmegaInteractableBootstrap bootstrap;

    @Override
    public void onLoad() {
        this.injector = Guice.createInjector(new OmegaInteractableModule(this));
        this.bootstrap = injector.getInstance(OmegaInteractableBootstrap.class);
    }

    @Override
    public void onEnable() {
        bootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.onDisable();
        }
    }
}
