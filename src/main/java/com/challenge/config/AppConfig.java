package com.challenge.config;

import com.challenge.config.properties.ExternalServiceProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para integraciones externas
 */
@Configuration
@EnableConfigurationProperties(ExternalServiceProperties.class)
public class AppConfig {

    private final ExternalServiceProperties externalServiceProperties;

    public AppConfig(ExternalServiceProperties externalServiceProperties) {
        this.externalServiceProperties = externalServiceProperties;
    }

    /**
     * WebClient para llamadas al servicio externo de porcentajes
     *
     * Configurado con:
     * - Headers por defecto para JSON
     * - URL base del servicio externo
     * - Límite de memoria para buffers
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(externalServiceProperties.percentageUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
