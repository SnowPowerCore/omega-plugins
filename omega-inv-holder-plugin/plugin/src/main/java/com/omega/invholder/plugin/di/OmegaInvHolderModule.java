package com.omega.invholder.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.omega.invholder.infrastructure.InventoryFileLoader;
import com.omega.invholder.infrastructure.json.JsonInventoryFileLoader;
import com.omega.invholder.infrastructure.resolve.ItemReferenceResolver;
import com.omega.invholder.plugin.commands.DynamicCommandRegistrar;
import com.omega.invholder.plugin.registry.InMemoryInventoryRegistry;
import com.omega.invholder.core.api.InventoryRegistry;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Singleton;
import java.nio.file.Path;

public final class OmegaInvHolderModule extends AbstractModule {

    private final JavaPlugin plugin;

    public OmegaInvHolderModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Plugin.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);

        Path dataFolder = plugin.getDataFolder().toPath();
        bind(Path.class).annotatedWith(Names.named("dataFolder")).toInstance(dataFolder);

        bind(InventoryFileLoader.class).to(JsonInventoryFileLoader.class);

        bind(ItemReferenceResolver.class);
        bind(DynamicCommandRegistrar.class);
    }

    @Provides
    @Singleton
    public InventoryRegistry provideInventoryRegistry(ItemReferenceResolver resolver) {
        return new InMemoryInventoryRegistry(resolver);
    }
}
