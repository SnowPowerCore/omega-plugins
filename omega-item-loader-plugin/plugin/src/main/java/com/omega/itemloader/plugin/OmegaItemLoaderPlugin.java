package com.omega.itemloader.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.omega.itemloader.plugin.bootstrap.OmegaItemLoaderBootstrap;
import com.omega.itemloader.plugin.di.OmegaItemLoaderModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class OmegaItemLoaderPlugin extends JavaPlugin {

    private Injector injector;
    private OmegaItemLoaderBootstrap bootstrap;
    private AutoCloseable glowIntegration;

    @Override
    public void onLoad() {
        this.injector = Guice.createInjector(new OmegaItemLoaderModule(this));
        this.bootstrap = injector.getInstance(OmegaItemLoaderBootstrap.class);
    }

    @Override
    public void onEnable() {
        bootstrap.onEnable();

        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            try {
                Class<?> clazz = Class.forName("com.omega.itemloader.plugin.protocollib.ProtocolLibGlowIntegration");
                Object instance = clazz.getConstructor(JavaPlugin.class, java.util.logging.Logger.class)
                        .newInstance(this, getLogger());
                this.glowIntegration = (AutoCloseable) instance;
                getLogger().info("ProtocolLib glow integration enabled (additionalInfo.glow)");
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "ProtocolLib is present but glow integration failed to initialize", t);
                this.glowIntegration = null;
            }
        } else {
            getLogger().info("ProtocolLib not found; additionalInfo.glow will be ignored.");
        }
    }

    @Override
    public void onDisable() {
        if (glowIntegration != null) {
            try {
                glowIntegration.close();
            } catch (Exception ignored) {
            } finally {
                glowIntegration = null;
            }
        }

        if (bootstrap != null) {
            bootstrap.onDisable();
        }
    }
}
