package com.omega.interactable.core;

import org.bukkit.NamespacedKey;

/**
 * Interop constants for reading data written by omega-item-loader-plugin.
 */
public final class OmegaItemLoaderInterop {
    private OmegaItemLoaderInterop() {
    }

    public static final String ITEM_LOADER_NAMESPACE = "omegaitemloader";

    public static final NamespacedKey INTERACTION = new NamespacedKey(ITEM_LOADER_NAMESPACE, "interaction");
    public static final NamespacedKey ADDITIONAL_INFO = new NamespacedKey(ITEM_LOADER_NAMESPACE, "additional_info");
}
