package com.omega.invholder.plugin.bootstrap;

import com.google.inject.name.Named;
import com.omega.invholder.core.OmegaInvHolderConstants;
import com.omega.invholder.core.api.InventoryRegistry;
import com.omega.invholder.infrastructure.InventoryFileLoader;
import com.omega.invholder.infrastructure.model.InventoryCommand;
import com.omega.invholder.infrastructure.model.InventoryTemplate;
import com.omega.invholder.plugin.commands.DynamicCommandRegistrar;
import com.omega.invholder.plugin.commands.OpenInventoryCommand;
import com.omega.invholder.plugin.registry.InMemoryInventoryRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OmegaInvHolderBootstrap {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Path dataFolder;
    private final InventoryFileLoader loader;
    private final InventoryRegistry registry;
    private final DynamicCommandRegistrar commandRegistrar;

    private final Set<String> registeredCommands = new HashSet<>();

    @Inject
    public OmegaInvHolderBootstrap(
            JavaPlugin plugin,
            Logger logger,
            @Named("dataFolder") Path dataFolder,
            InventoryFileLoader loader,
            InventoryRegistry registry,
            DynamicCommandRegistrar commandRegistrar
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.loader = loader;
        this.registry = registry;
        this.commandRegistrar = commandRegistrar;
    }

    public void onEnable() {
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create plugin data folder: " + dataFolder, exception);
        }

        Path invsPath = dataFolder.resolve(OmegaInvHolderConstants.INVS_FILE_NAME);
        ensureDefaultInvsFile(invsPath);

        Map<String, InventoryTemplate> templates;
        try {
            templates = loader.load(invsPath);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Failed to load inventories from " + invsPath + ". Plugin will still enable but registry will be empty.", exception);
            templates = Map.of();
        }

        if (registry instanceof InMemoryInventoryRegistry mem) {
            mem.setTemplates(templates);
        }

        Bukkit.getServicesManager().register(
                InventoryRegistry.class,
                registry,
                plugin,
                ServicePriority.Normal
        );

        registerConfiguredCommands(templates);

        logger.info("Loaded " + templates.size() + " inventories from " + invsPath);
    }

    public void onDisable() {
        for (String cmd : registeredCommands) {
            commandRegistrar.unregister(cmd);
        }
        registeredCommands.clear();

        Bukkit.getServicesManager().unregister(InventoryRegistry.class, registry);
    }

    private void registerConfiguredCommands(Map<String, InventoryTemplate> templates) {
        for (InventoryTemplate template : templates.values()) {
            InventoryCommand cmd = template.command();
            if (cmd == null) {
                continue;
            }

            String name = cmd.name();
            if (name == null || name.isBlank()) {
                continue;
            }

            OpenInventoryCommand command = new OpenInventoryCommand(name, template.id(), cmd.permission(), registry);
            boolean ok = commandRegistrar.register(command);
            if (ok) {
                registeredCommands.add(name);
            }
        }
    }

    private void ensureDefaultInvsFile(Path invsPath) {
        if (Files.exists(invsPath)) {
            return;
        }

        String defaultJson = """
                [
                  {
                    \"id\": \"example_shop\",
                    \"type\": \"CHEST\",
                    \"size\": 27,
                    \"name\": \"&aExample Shop\",
                    \"command\": {\"name\": \"exampleshop\"},
                    \"items\": [
                      {\"slot\": 0, \"referenceId\": \"omega:demo/sword\"},
                      {\"slot\": 1, \"material\": \"DIAMOND\", \"amount\": 3},
                      {\"slot\": 8, \"material\": \"BARRIER\", \"amount\": 1}
                    ]
                  },
                  {
                    \"id\": \"example_anvil\",
                    \"type\": \"ANVIL\",
                    \"name\": \"&eExample Anvil UI\",
                    \"items\": [
                      {\"slot\": 0, \"material\": \"IRON_INGOT\", \"amount\": 1},
                      {\"slot\": 1, \"referenceId\": \"omega:welcome_book\"}
                    ]
                  }
                ]
                """;

        try {
            Files.writeString(invsPath, defaultJson, StandardCharsets.UTF_8);
            logger.info("Created default invs file: " + invsPath);
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Failed to create default invs file: " + invsPath, exception);
        }
    }
}
