package com.omega.interactable.plugin.actions.impl;

import com.google.inject.Singleton;
import com.omega.racing.core.api.RaceUiApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public final class RaceUiApiProvider {

    private final Logger logger;

    @Inject
    public RaceUiApiProvider(Logger logger) {
        this.logger = logger;
    }

    public Optional<RaceUiApi> resolve() {
        try {
            RegisteredServiceProvider<RaceUiApi> reg = Bukkit.getServicesManager().getRegistration(RaceUiApi.class);
            if (reg == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(reg.getProvider());
        } catch (Throwable t) {
            logger.fine(() -> "RaceUiApiProvider resolve failed: " + t.getMessage());
            return Optional.empty();
        }
    }
}
