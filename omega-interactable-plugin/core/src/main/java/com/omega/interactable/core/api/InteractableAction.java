package com.omega.interactable.core.api;

public interface InteractableAction {

    /**
     * Executes this step. Returning {@link InteractionResult#stop()} interrupts the chain.
     */
    InteractionResult onInteract(InteractionContext context);
}
