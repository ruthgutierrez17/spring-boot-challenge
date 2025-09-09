package com.challenge.service;

import com.challenge.config.CacheConfig;
import com.challenge.config.properties.ExternalServiceProperties;
import com.challenge.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Servicio para obtener porcentajes de un servicio externo
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PercentageService {

    private final WebClient webClient;
    private final ExternalServiceProperties externalServiceProperties;

    /**
     * Obtiene el porcentaje del servicio externo y actualiza el caché
     *
     * @return El porcentaje obtenido del servicio externo
     * @throws ServiceException Si no se puede obtener el porcentaje
     */
    @CachePut(value = CacheConfig.PERCENTAGE_CACHE_NAME, key = "'" + CacheConfig.CURRENT_PERCENTAGE_KEY + "'", unless = "#result == null")
    public BigDecimal getPercentage() {
        log.info("Obteniendo porcentaje del servicio externo...");

        try {
            BigDecimal percentage = webClient.get()
                    .uri("/percentage")
                    .retrieve()
                    .bodyToMono(PercentageResponse.class)
                    .timeout(externalServiceProperties.timeout())
                    .retryWhen(Retry.backoff(externalServiceProperties.retryAttempts(), Duration.ofMillis(500)))
                    .map(PercentageResponse::getValue)
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("Error del servicio externo: {} - {}", ex.getStatusCode(), ex.getMessage());
                        return new ServiceException("Servicio externo no disponible", ex);
                    })
                    .onErrorMap(Exception.class, ex -> {
                        log.error("Error inesperado al obtener porcentaje", ex);
                        return new ServiceException("Error al obtener porcentaje", ex);
                    })
                    .block();

            log.info("Porcentaje obtenido exitosamente y guardado en caché: {}%", percentage);
            return percentage;

        } catch (Exception e) {
            log.error("Error crítico al obtener porcentaje", e);
            throw new ServiceException("No se pudo obtener el porcentaje", e);
        }
    }

    /**
     * Obtiene el porcentaje desde el caché únicamente
     *
     * @return El porcentaje desde caché o null si no existe
     */
    @Cacheable(value = CacheConfig.PERCENTAGE_CACHE_NAME, key = "'" + CacheConfig.CURRENT_PERCENTAGE_KEY + "'")
    public BigDecimal getPercentageFromCache() {
        log.debug("No hay valor en caché para porcentaje");
        return null;
    }

    /**
     * DTO interno para la respuesta del servicio externo
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class PercentageResponse {
        private BigDecimal value;
    }
}
