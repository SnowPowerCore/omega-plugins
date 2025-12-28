package com.omega.interactable.plugin.actions;

import com.google.inject.Injector;
import com.omega.interactable.core.api.InteractableAction;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public final class ActionFactory {

    private static final String DEFAULT_ACTIONS_PACKAGE = "com.omega.interactable.plugin.actions.impl.";

    private final Injector injector;
    private final ClassLoader classLoader;
    private final Logger logger;

    @Inject
    public ActionFactory(Injector injector, ClassLoader classLoader, Logger logger) {
        this.injector = injector;
        this.classLoader = classLoader;
        this.logger = logger;
    }

    public Optional<InteractableAction> create(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return Optional.empty();
        }

        String raw = typeName.trim();
        String fqcn = raw.contains(".") ? raw : (DEFAULT_ACTIONS_PACKAGE + raw);

        try {
            Class<?> cls = Class.forName(fqcn, true, classLoader);
            if (!InteractableAction.class.isAssignableFrom(cls)) {
                logger.warning("Action type does not implement InteractableAction: " + fqcn);
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Class<? extends InteractableAction> actionClass = (Class<? extends InteractableAction>) cls;

            // JIT binding allows constructor injection for concrete classes.
            return Optional.of(injector.getInstance(actionClass));
        } catch (ClassNotFoundException e) {
            logger.warning("Unknown action class: " + fqcn);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to construct action " + fqcn + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
