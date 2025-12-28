package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractionContext;

import jakarta.inject.Inject;

public final class RacingPlayerSelectionSuccessNoopDelegate implements RacingPlayerSelectionSuccessDelegate {

    @Inject
    public RacingPlayerSelectionSuccessNoopDelegate() {
    }

    @Override
    public void onSelect(InteractionContext context, String uuid) {
        // Intentionally no-op.
    }
}
