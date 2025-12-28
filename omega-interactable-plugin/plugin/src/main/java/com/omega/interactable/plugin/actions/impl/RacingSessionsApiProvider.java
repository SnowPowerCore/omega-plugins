package com.omega.interactable.plugin.actions.impl;

import com.google.inject.Singleton;
import com.omega.racing.core.api.RacingSessionsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public final class RacingSessionsApiProvider {

    private final Logger logger;

    @Inject
    public RacingSessionsApiProvider(Logger logger) {
        this.logger = logger;
    }

    public Optional<RacingSessionsApi> resolve() {
        try {
            RegisteredServiceProvider<RacingSessionsApi> reg = Bukkit.getServicesManager().getRegistration(RacingSessionsApi.class);
            if (reg == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(reg.getProvider());
        } catch (Throwable t) {
            logger.fine(() -> "RacingSessionsApiProvider resolve failed: " + t.getMessage());
            return Optional.empty();
        }
    }
}
