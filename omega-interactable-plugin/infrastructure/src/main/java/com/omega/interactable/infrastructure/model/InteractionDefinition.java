package com.omega.interactable.infrastructure.model;

import java.util.List;
import java.util.Set;

public record InteractionDefinition(Set<String> triggerTokens, List<String> actionTypes) {
}
