package com.omega.racing.infrastructure.interop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;

import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Resolves OmegaInvHolder's InventoryRegistry via Bukkit ServicesManager using reflection.
 */
public final class InventoryRegistryResolver {

    private static final String INV_HOLDER_PLUGIN_NAME = "OmegaInvHolder";
    private static final String INV_REGISTRY_CLASS = "com.omega.invholder.core.api.InventoryRegistry";

    private final Logger logger;

    @Inject
    public InventoryRegistryResolver(Logger logger) {
        this.logger = logger;
    }

    public Optional<Inventory> create(String inventoryId) {
        if (inventoryId == null || inventoryId.isBlank()) {
            return Optional.empty();
        }

        Plugin invHolder = Bukkit.getPluginManager().getPlugin(INV_HOLDER_PLUGIN_NAME);
        if (invHolder == null || !invHolder.isEnabled()) {
            return Optional.empty();
        }

        try {
            ClassLoader cl = invHolder.getClass().getClassLoader();
            Class<?> registryClass = Class.forName(INV_REGISTRY_CLASS, false, cl);

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
            Object optional = create.invoke(provider, inventoryId);
            if (!(optional instanceof Optional<?> opt) || opt.isEmpty()) {
                return Optional.empty();
            }

            Object value = opt.get();
            if (!(value instanceof Inventory inv)) {
                return Optional.empty();
            }

            return Optional.of(inv);
        } catch (Exception e) {
            logger.fine(() -> "Failed to create inv-holder inventory '" + inventoryId + "': " + e.getMessage());
            return Optional.empty();
        }
    }
}
