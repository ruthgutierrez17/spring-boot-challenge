package com.challenge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Propiedades de configuración para el cache
 */
@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
    Duration expireAfterWrite,
    long maximumSize
) {
}
