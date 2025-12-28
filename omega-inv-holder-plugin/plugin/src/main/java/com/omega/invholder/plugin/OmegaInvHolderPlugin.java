package com.omega.invholder.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.omega.invholder.plugin.bootstrap.OmegaInvHolderBootstrap;
import com.omega.invholder.plugin.di.OmegaInvHolderModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class OmegaInvHolderPlugin extends JavaPlugin {

    private Injector injector;
    private OmegaInvHolderBootstrap bootstrap;
    private Throwable bootstrapFailure;

    @Override
    public void onLoad() {
        tryInitBootstrap();
    }

    @Override
    public void onEnable() {
        if (bootstrap == null) {
            tryInitBootstrap();
        }

        if (bootstrap == null) {
            getLogger().severe("Failed to initialize OmegaInvHolder bootstrap; disabling plugin.");
            if (bootstrapFailure != null) {
                bootstrapFailure.printStackTrace();
            }
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        bootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.onDisable();
        }
    }

    private void tryInitBootstrap() {
        if (this.bootstrap != null) {
            return;
        }

        try {
            this.injector = Guice.createInjector(new OmegaInvHolderModule(this));
            this.bootstrap = injector.getInstance(OmegaInvHolderBootstrap.class);
            this.bootstrapFailure = null;
        } catch (Throwable throwable) {
            this.bootstrapFailure = throwable;
            getLogger().severe("OmegaInvHolder failed to create injector/bootstrap: " + throwable.getMessage());
        }
    }
}
