package com.omega.itemloader.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.omega.itemloader.infrastructure.ItemStackFileLoader;
import com.omega.itemloader.infrastructure.json.JsonItemStackFileLoader;
import com.omega.itemloader.infrastructure.meta.ArmorMetaHandler;
import com.omega.itemloader.infrastructure.meta.AxolotlBucketMetaHandler;
import com.omega.itemloader.infrastructure.meta.BannerMetaHandler;
import com.omega.itemloader.infrastructure.meta.BlockDataMetaHandler;
import com.omega.itemloader.infrastructure.meta.BlockStateMetaHandler;
import com.omega.itemloader.infrastructure.meta.BookMetaHandler;
import com.omega.itemloader.infrastructure.meta.BundleMetaHandler;
import com.omega.itemloader.infrastructure.meta.ColorableArmorMetaHandler;
import com.omega.itemloader.infrastructure.meta.CompassMetaHandler;
import com.omega.itemloader.infrastructure.meta.CrossbowMetaHandler;
import com.omega.itemloader.infrastructure.meta.DamageableMetaHandler;
import com.omega.itemloader.infrastructure.meta.EnchantmentStorageMetaHandler;
import com.omega.itemloader.infrastructure.meta.FireworkEffectMetaHandler;
import com.omega.itemloader.infrastructure.meta.FireworkMetaHandler;
import com.omega.itemloader.infrastructure.meta.ItemMetaHandler;
import com.omega.itemloader.infrastructure.meta.KnowledgeBookMetaHandler;
import com.omega.itemloader.infrastructure.meta.LeatherArmorMetaHandler;
import com.omega.itemloader.infrastructure.meta.MapMetaHandler;
import com.omega.itemloader.infrastructure.meta.MusicInstrumentMetaHandler;
import com.omega.itemloader.infrastructure.meta.PotionMetaHandler;
import com.omega.itemloader.infrastructure.meta.RepairableMetaHandler;
import com.omega.itemloader.infrastructure.meta.SkullMetaHandler;
import com.omega.itemloader.infrastructure.meta.SpawnEggMetaHandler;
import com.omega.itemloader.infrastructure.meta.SuspiciousStewMetaHandler;
import com.omega.itemloader.infrastructure.meta.TropicalFishBucketMetaHandler;
import org.bukkit.plugin.java.JavaPlugin;

import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.util.Set;

public final class OmegaItemLoaderModule extends AbstractModule {

    private final JavaPlugin plugin;

    public OmegaItemLoaderModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);

        Path dataFolder = plugin.getDataFolder().toPath();
        bind(Path.class).annotatedWith(Names.named("dataFolder")).toInstance(dataFolder);

        bind(ItemStackFileLoader.class).to(JsonItemStackFileLoader.class);
    }

    @Provides
    @Singleton
    public Set<ItemMetaHandler> provideItemMetaHandlers(
            ArmorMetaHandler armorMetaHandler,
            AxolotlBucketMetaHandler axolotlBucketMetaHandler,
            BannerMetaHandler bannerMetaHandler,
            BlockDataMetaHandler blockDataMetaHandler,
            BlockStateMetaHandler blockStateMetaHandler,
            BookMetaHandler bookMetaHandler,
            BundleMetaHandler bundleMetaHandler,
            ColorableArmorMetaHandler colorableArmorMetaHandler,
            CompassMetaHandler compassMetaHandler,
            CrossbowMetaHandler crossbowMetaHandler,
            DamageableMetaHandler damageableMetaHandler,
            EnchantmentStorageMetaHandler enchantmentStorageMetaHandler,
            FireworkEffectMetaHandler fireworkEffectMetaHandler,
            FireworkMetaHandler fireworkMetaHandler,
            KnowledgeBookMetaHandler knowledgeBookMetaHandler,
            LeatherArmorMetaHandler leatherArmorMetaHandler,
            MapMetaHandler mapMetaHandler,
            MusicInstrumentMetaHandler musicInstrumentMetaHandler,
            PotionMetaHandler potionMetaHandler,
            RepairableMetaHandler repairableMetaHandler,
            SkullMetaHandler skullMetaHandler,
            SpawnEggMetaHandler spawnEggMetaHandler,
            SuspiciousStewMetaHandler suspiciousStewMetaHandler,
            TropicalFishBucketMetaHandler tropicalFishBucketMetaHandler
    ) {
        return Set.of(
                armorMetaHandler,
                axolotlBucketMetaHandler,
                bannerMetaHandler,
                blockDataMetaHandler,
                blockStateMetaHandler,
                bookMetaHandler,
                bundleMetaHandler,
                colorableArmorMetaHandler,
                compassMetaHandler,
                crossbowMetaHandler,
                damageableMetaHandler,
                enchantmentStorageMetaHandler,
                fireworkEffectMetaHandler,
                fireworkMetaHandler,
                knowledgeBookMetaHandler,
                leatherArmorMetaHandler,
                mapMetaHandler,
                musicInstrumentMetaHandler,
                potionMetaHandler,
                repairableMetaHandler,
                skullMetaHandler,
                spawnEggMetaHandler,
                suspiciousStewMetaHandler,
                tropicalFishBucketMetaHandler
        );
    }
}
