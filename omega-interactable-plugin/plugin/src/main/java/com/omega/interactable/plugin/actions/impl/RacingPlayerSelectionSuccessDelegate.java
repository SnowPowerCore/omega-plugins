package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractionContext;

public interface RacingPlayerSelectionSuccessDelegate {

    void onSelect(InteractionContext context, String uuid);
}
