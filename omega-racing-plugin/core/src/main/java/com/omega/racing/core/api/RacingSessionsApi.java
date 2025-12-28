package com.omega.racing.core.api;

import java.util.UUID;

/**
 * Access to mutable per-player racing UI session state.
 */
public interface RacingSessionsApi {

    RacingSession session(UUID playerId);

    void clear(UUID playerId);
}