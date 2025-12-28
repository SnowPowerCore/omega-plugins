package com.omega.interactable.plugin.actions.impl;

import com.omega.racing.core.api.RacingPromptsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public final class RacingPromptsApiProvider {

    private final Logger logger;

    @Inject
    public RacingPromptsApiProvider(Logger logger) {
        this.logger = logger;
    }

    public Optional<RacingPromptsApi> resolve() {
        try {
            RegisteredServiceProvider<RacingPromptsApi> reg = Bukkit.getServicesManager().getRegistration(RacingPromptsApi.class);
            if (reg == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(reg.getProvider());
        } catch (Throwable t) {
            logger.fine(() -> "RacingPromptsApiProvider resolve failed: " + t.getMessage());
            return Optional.empty();
        }
    }
}
