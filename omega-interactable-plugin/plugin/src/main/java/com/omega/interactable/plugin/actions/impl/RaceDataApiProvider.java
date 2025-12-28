package com.omega.interactable.plugin.actions.impl;

import com.google.inject.Singleton;
import com.omega.racing.core.api.RaceDataApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public final class RaceDataApiProvider {

    private final Logger logger;

    @Inject
    public RaceDataApiProvider(Logger logger) {
        this.logger = logger;
    }

    public Optional<RaceDataApi> resolve() {
        try {
            RegisteredServiceProvider<RaceDataApi> reg = Bukkit.getServicesManager().getRegistration(RaceDataApi.class);
            if (reg == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(reg.getProvider());
        } catch (Throwable t) {
            logger.fine(() -> "RaceDataApiProvider resolve failed: " + t.getMessage());
            return Optional.empty();
        }
    }
}
