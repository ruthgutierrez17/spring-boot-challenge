package com.challenge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Propiedades de configuración para el servicio externo
 */
@ConfigurationProperties(prefix = "external-service")
public record ExternalServiceProperties(
    String percentageUrl,
    Duration timeout,
    int retryAttempts
) {
}
