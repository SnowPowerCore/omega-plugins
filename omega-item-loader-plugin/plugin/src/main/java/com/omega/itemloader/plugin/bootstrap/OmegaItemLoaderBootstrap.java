package com.omega.itemloader.plugin.bootstrap;

import com.google.inject.name.Named;
import com.omega.itemloader.core.OmegaItemLoaderConstants;
import com.omega.itemloader.core.api.ItemRegistry;
import com.omega.itemloader.infrastructure.ItemStackFileLoader;
import com.omega.itemloader.plugin.registry.InMemoryItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OmegaItemLoaderBootstrap {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Path dataFolder;
    private final ItemStackFileLoader loader;

    private ItemRegistry registry;

    @Inject
        public OmegaItemLoaderBootstrap(
            JavaPlugin plugin,
            Logger logger,
            @Named("dataFolder") Path dataFolder,
            ItemStackFileLoader loader
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.loader = loader;
    }

    public void onEnable() {
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create plugin data folder: " + dataFolder, exception);
        }

        Path itemsPath = dataFolder.resolve(OmegaItemLoaderConstants.ITEMS_FILE_NAME);
        ensureDefaultItemsFile(itemsPath);

        Map<String, org.bukkit.inventory.ItemStack> items;
        try {
            items = loader.load(itemsPath);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Failed to load items from " + itemsPath + ". Plugin will still enable but registry will be empty.", exception);
            items = Map.of();
        }

        this.registry = new InMemoryItemRegistry(items);

        Bukkit.getServicesManager().register(
                ItemRegistry.class,
                registry,
                plugin,
                ServicePriority.Normal
        );

        logger.info("Loaded " + items.size() + " items from " + itemsPath);
    }

    public void onDisable() {
        if (registry != null) {
            Bukkit.getServicesManager().unregister(ItemRegistry.class, registry);
            registry = null;
        }
    }

    private void ensureDefaultItemsFile(Path itemsPath) {
        if (Files.exists(itemsPath)) {
            return;
        }

        String defaultJson = """
                [
                  {
                    \"id\": \"example_sword\",
                    \"material\": \"DIAMOND_SWORD\",
                    \"amount\": 1,
                    \"name\": \"&bExample Sword\",
                    \"lore\": [\"&7Loaded from items.json\"],
                    \"enchants\": {\"minecraft:sharpness\": 5},
                    \"unbreakable\": true,
                    \"flags\": [\"HIDE_ENCHANTS\"],
                                        \"additionalInfo\": {\"origin\": \"omega-item-loader\", \"tag\": \"demo\"}
                  },
                  {
                    \"id\": \"example_book\",
                    \"material\": \"WRITTEN_BOOK\",
                    \"name\": \"&dWelcome Book\",
                    \"meta\": {
                      \"type\": \"book\",
                      \"title\": \"Welcome\",
                      \"author\": \"Omega\",
                      \"pages\": [\"Page 1\", \"Page 2\"]
                    },
                    \"additionalInfo\": {\"category\": \"tutorial\"}
                  }
                ]
                """;

        try {
            Files.writeString(itemsPath, defaultJson, StandardCharsets.UTF_8);
            logger.info("Created default items file: " + itemsPath);
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Failed to create default items file: " + itemsPath, exception);
        }
    }
}
