package com.omega.interactable.core.api;

public final class InteractionResult {

    private static final InteractionResult CONTINUE = new InteractionResult(true);
    private static final InteractionResult STOP = new InteractionResult(false);

    private final boolean shouldContinue;

    private InteractionResult(boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
    }

    public boolean shouldContinue() {
        return shouldContinue;
    }

    public static InteractionResult continueChain() {
        return CONTINUE;
    }

    public static InteractionResult stop() {
        return STOP;
    }
}
