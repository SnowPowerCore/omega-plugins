package com.omega.racing.core.api;

import java.util.Deque;
import java.util.Map;

/**
 * Mutable per-player UI session state.
 *
 * This is intentionally generic so multiple plugins can share state
 * without a brittle, ever-growing typed API surface.
 */
public interface RacingSession {

    /**
     * Mutable key/value storage.
     *
     * See {@link RacingSessionKeys} for the canonical keys used by OmegaRacing.
     */
    Map<String, Object> data();

    /**
     * Navigation stack for UI history.
     */
    Deque<String> backStack();

    default Object get(String key) {
        return data().get(key);
    }

    default void set(String key, Object value) {
        if (value == null) {
            data().remove(key);
        } else {
            data().put(key, value);
        }
    }

    default String getString(String key) {
        Object value = get(key);
        return value instanceof String s ? s : null;
    }

    default int getInt(String key, int defaultValue) {
        Object value = get(key);
        return value instanceof Number n ? n.intValue() : defaultValue;
    }

    default long getLong(String key, long defaultValue) {
        Object value = get(key);
        return value instanceof Number n ? n.longValue() : defaultValue;
    }

    default <T> T getAs(String key, Class<T> type) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Session key '" + key + "' is " + value.getClass().getName() + ", expected " + type.getName());
        }
        return type.cast(value);
    }
}
