package com.omega.racing.plugin.runtime;

import com.omega.racing.core.api.RacingSession;
import com.omega.racing.core.api.RacingSessionsApi;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RacingSessionStore implements RacingSessionsApi {

    public static final class Session implements RacingSession {
        private final Map<String, Object> data = new ConcurrentHashMap<>();
        private final Deque<String> backStack = new ArrayDeque<>();

        @Override
        public Map<String, Object> data() {
            return data;
        }

        @Override
        public Deque<String> backStack() {
            return backStack;
        }
    }

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public Session session(UUID playerId) {
        return sessions.computeIfAbsent(playerId, id -> new Session());
    }

    @Override
    public void clear(UUID playerId) {
        sessions.remove(playerId);
    }
}