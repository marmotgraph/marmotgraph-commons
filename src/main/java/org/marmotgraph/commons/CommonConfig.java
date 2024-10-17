package org.marmotgraph.commons;

import org.marmotgraph.commons.controller.ThemeController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableCaching
@ComponentScan
@EnableAutoConfiguration
public class CommonConfig {

    public static final String ASSET_CACHE_MANAGER = "org.marmotgraph.commons.AssetCacheManager";

    // In-memory cache
    @Bean(name = ASSET_CACHE_MANAGER)
    public CacheManager assetCacheManager() {
        return new ConcurrentMapCacheManager(ThemeController.ASSET_CACHE);
    }
}
