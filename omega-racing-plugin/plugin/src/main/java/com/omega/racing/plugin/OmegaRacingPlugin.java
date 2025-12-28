package com.omega.racing.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.omega.racing.plugin.bootstrap.OmegaRacingBootstrap;
import com.omega.racing.plugin.di.OmegaRacingModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class OmegaRacingPlugin extends JavaPlugin {

    private Injector injector;
    private OmegaRacingBootstrap bootstrap;

    @Override
    public void onLoad() {
        injector = Guice.createInjector(new OmegaRacingModule(this));
        bootstrap = injector.getInstance(OmegaRacingBootstrap.class);
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
