package com.omega.invholder.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class DynamicCommandRegistrar {

    private final Plugin plugin;
    private final Logger logger;

    @Inject
    public DynamicCommandRegistrar(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean register(Command command) {
        Optional<CommandMap> mapOpt = resolveCommandMap();
        if (mapOpt.isEmpty()) {
            logger.warning("Unable to resolve Bukkit CommandMap; cannot register command " + command.getName());
            return false;
        }

        CommandMap map = mapOpt.get();
        try {
            return map.register(plugin.getName().toLowerCase(), command);
        } catch (Exception e) {
            logger.warning("Failed to register command " + command.getName() + ": " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void unregister(String commandName) {
        Optional<CommandMap> mapOpt = resolveCommandMap();
        if (mapOpt.isEmpty()) {
            return;
        }

        CommandMap map = mapOpt.get();
        if (map instanceof SimpleCommandMap simple) {
            try {
                Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
                f.setAccessible(true);
                Map<String, Command> known = (Map<String, Command>) f.get(simple);
                known.remove(commandName);
                known.remove(plugin.getName().toLowerCase() + ":" + commandName);
            } catch (Exception ignored) {
            }
        }
    }

    private Optional<CommandMap> resolveCommandMap() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object obj = method.invoke(Bukkit.getServer());
            if (obj instanceof CommandMap cm) {
                return Optional.of(cm);
            }
        } catch (Exception ignored) {
        }

        PluginManager pm = Bukkit.getPluginManager();
        try {
            Field field = pm.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            Object obj = field.get(pm);
            if (obj instanceof CommandMap cm) {
                return Optional.of(cm);
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }
}
