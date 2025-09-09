package com.challenge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para procesamiento asíncrono
 */
@ConfigurationProperties(prefix = "async")
public record AsyncProperties(
    int corePoolSize,
    int maxPoolSize,
    int queueCapacity
) {
}
