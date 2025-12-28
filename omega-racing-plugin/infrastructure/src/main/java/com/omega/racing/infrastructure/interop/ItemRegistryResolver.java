package com.omega.racing.infrastructure.interop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;

import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Resolves OmegaItemLoader's ItemRegistry via Bukkit ServicesManager using reflection.
 */
public final class ItemRegistryResolver {

    private static final String ITEM_LOADER_PLUGIN_NAME = "OmegaItemLoader";
    private static final String ITEM_REGISTRY_CLASS = "com.omega.itemloader.core.api.ItemRegistry";

    private final Logger logger;

    @Inject
    public ItemRegistryResolver(Logger logger) {
        this.logger = logger;
    }

    public Optional<ItemStack> create(String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            return Optional.empty();
        }

        Plugin itemLoader = Bukkit.getPluginManager().getPlugin(ITEM_LOADER_PLUGIN_NAME);
        if (itemLoader == null || !itemLoader.isEnabled()) {
            return Optional.empty();
        }

        try {
            ClassLoader cl = itemLoader.getClass().getClassLoader();
            Class<?> registryClass = Class.forName(ITEM_REGISTRY_CLASS, false, cl);

            ServicesManager sm = Bukkit.getServicesManager();
            Object registration = sm.getRegistration(registryClass);
            if (registration == null) {
                return Optional.empty();
            }

            Method getProvider = registration.getClass().getMethod("getProvider");
            Object provider = getProvider.invoke(registration);
            if (provider == null) {
                return Optional.empty();
            }

            Method create = registryClass.getMethod("create", String.class);
            Object optional = create.invoke(provider, referenceId);
            if (!(optional instanceof Optional<?> opt) || opt.isEmpty()) {
                return Optional.empty();
            }

            Object value = opt.get();
            if (!(value instanceof ItemStack stack)) {
                return Optional.empty();
            }

            return Optional.of(stack);
        } catch (Exception e) {
            logger.fine(() -> "Failed to create item-loader item '" + referenceId + "': " + e.getMessage());
            return Optional.empty();
        }
    }
}
