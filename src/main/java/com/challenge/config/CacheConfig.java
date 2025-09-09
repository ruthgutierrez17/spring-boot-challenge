package com.challenge.config;

import com.challenge.config.properties.CacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Cache con Caffeine
 */
@Configuration
@Slf4j
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    public static final String PERCENTAGE_CACHE_NAME = "percentageCache";
    public static final String CURRENT_PERCENTAGE_KEY = "current_percentage";

    private final CacheProperties cacheProperties;

    public CacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PERCENTAGE_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(cacheProperties.maximumSize())
                .expireAfterWrite(cacheProperties.expireAfterWrite())
                .recordStats() // Habilita métricas para Actuator
                .removalListener((key, value, cause) -> {
                    // Log para debugging
                    log.info("Cache entry removed - Key: " + key + ", Cause: " + cause);
                }));
        return cacheManager;
    }
}
