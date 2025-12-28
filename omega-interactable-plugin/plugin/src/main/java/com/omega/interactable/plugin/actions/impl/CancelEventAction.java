package com.omega.interactable.plugin.actions.impl;

import com.omega.interactable.core.api.InteractableAction;
import com.omega.interactable.core.api.InteractionContext;
import com.omega.interactable.core.api.InteractionResult;

public final class CancelEventAction implements InteractableAction {

    @Override
    public InteractionResult onInteract(InteractionContext context) {
        context.event().setCancelled(true);
        return InteractionResult.stop();
    }
}
