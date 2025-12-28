package com.omega.itemloader.core;

import org.bukkit.NamespacedKey;

public final class OmegaItemLoaderKeys {
    private OmegaItemLoaderKeys() {
    }

    /**
     * Stores a JSON string of a string->string map, originating from the "additionalInfo" node.
     */
    public static final NamespacedKey ADDITIONAL_INFO = new NamespacedKey(
            OmegaItemLoaderConstants.NAMESPACE,
            "additional_info"
    );

        /**
         * Stores a JSON string of the item's configured interaction array.
         */
        public static final NamespacedKey INTERACTION = new NamespacedKey(
            OmegaItemLoaderConstants.NAMESPACE,
            "interaction"
        );

            /**
             * Stores the item's resolved reference id (referenceId if present, else id).
             */
            public static final NamespacedKey REFERENCE_ID = new NamespacedKey(
                OmegaItemLoaderConstants.NAMESPACE,
                "reference_id"
            );
}
