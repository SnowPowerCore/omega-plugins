package com.omega.racing.plugin.runtime.stage;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Singleton
public final class RaceFreezeService {

    private final Set<UUID> frozen = Collections.synchronizedSet(new HashSet<>());

    @Inject
    public RaceFreezeService() {
    }

    public void freeze(UUID playerId) {
        if (playerId != null) {
            frozen.add(playerId);
        }
    }

    public void unfreeze(UUID playerId) {
        if (playerId != null) {
            frozen.remove(playerId);
        }
    }

    public void clear() {
        frozen.clear();
    }

    public boolean isFrozen(UUID playerId) {
        return playerId != null && frozen.contains(playerId);
    }
}
