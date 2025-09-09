package com.challenge.service;

import com.challenge.exception.ServiceException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(PercentageServiceIT.TestConfig.class)
@DisplayName("PercentageService Integration Tests")
class PercentageServiceIT {

    private static final String PERCENTAGE_ENDPOINT = "/percentage";
    private static final int EXPECTED_RETRY_CALLS = 3;

    private static WireMockServer wireMockServer;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PercentageService percentageService;

    @TestConfiguration
    @EnableCaching
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public CacheManager cacheManager() {
            return new CaffeineCacheManager("percentageCache");
        }

        @Bean
        public com.challenge.config.properties.ExternalServiceProperties externalServiceProperties() {
            return new com.challenge.config.properties.ExternalServiceProperties(
                "http://localhost:9999",
                java.time.Duration.ofSeconds(3),
                2
            );
        }

        @Bean
        public WebClient webClient(com.challenge.config.properties.ExternalServiceProperties props) {
            return WebClient.builder()
                    .baseUrl(props.percentageUrl())
                    .build();
        }

        @Bean
        public PercentageService percentageService(WebClient webClient,
                                                   com.challenge.config.properties.ExternalServiceProperties props) {
            return new PercentageService(webClient, props);
        }
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();

        // Asegurar que el cache existe y está limpio
        var cache = cacheManager.getCache("percentageCache");
        assertNotNull(cache, "El cache 'percentageCache' debe existir");
        cache.clear();

        // Verificar que el cache está realmente vacío
        assertNull(cache.get("current_percentage"), "El cache debe estar vacío al inicio");
    }

    @Test
    @DisplayName("Debe obtener porcentaje exitosamente del servicio externo")
    void shouldGetPercentageSuccessfully() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 15.5}")));

        BigDecimal percentage = percentageService.getPercentage();
        assertEquals(BigDecimal.valueOf(15.5), percentage);
        wireMockServer.verify(1, getRequestedFor(urlEqualTo(PERCENTAGE_ENDPOINT)));
    }

    @Test
    @DisplayName("Debe fallar cuando el servicio externo devuelve error")
    void shouldFailWhenExternalServiceReturnsError() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Service unavailable\"}")));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> percentageService.getPercentage()
        );

        assertTrue(exception.getMessage().contains("No se pudo obtener el porcentaje") ||
                   exception.getMessage().contains("Servicio externo no disponible"));

        wireMockServer.verify(EXPECTED_RETRY_CALLS, getRequestedFor(urlEqualTo(PERCENTAGE_ENDPOINT)));
    }

    @Test
    @DisplayName("Debe reintentar cuando hay errores temporales")
    void shouldRetryOnTemporaryErrors() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"error\": \"Temporary error\"}"))
                .willSetStateTo("First Attempt Failed"));

        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Attempt Failed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 25.0}")));

        BigDecimal percentage = percentageService.getPercentage();
        assertEquals(BigDecimal.valueOf(25.0), percentage);
        wireMockServer.verify(2, getRequestedFor(urlEqualTo(PERCENTAGE_ENDPOINT)));
    }

    @Test
    @DisplayName("Debe fallar después de agotar todos los reintentos")
    void shouldFailAfterAllRetriesExhausted() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Service down\"}")));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> percentageService.getPercentage()
        );

        assertTrue(exception.getMessage().contains("No se pudo obtener el porcentaje") ||
                   exception.getMessage().contains("Servicio externo no disponible"));

        wireMockServer.verify(EXPECTED_RETRY_CALLS, getRequestedFor(urlEqualTo(PERCENTAGE_ENDPOINT)));
    }

    @Test
    @DisplayName("Debe guardar en caché después de una llamada exitosa")
    void shouldStoreValueInCacheAfterSuccessfulCall() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 20.0}")));

        BigDecimal percentage = percentageService.getPercentage();
        assertEquals(BigDecimal.valueOf(20.0), percentage);

        // Verificar que el valor está en caché usando el método del servicio
        BigDecimal cachedValue = percentageService.getPercentageFromCache();
        assertNotNull(cachedValue, "El valor en caché debería existir");
        assertEquals(BigDecimal.valueOf(20.0), cachedValue, "El valor en caché debería ser 20.0");
    }

    @Test
    @DisplayName("Debe usar el valor en caché cuando el servicio externo falla")
    void shouldUseCacheAsFallbackOnServiceFailure() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 12.5}")));

        percentageService.getPercentage(); // poblar caché

        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"error\": \"Service down\"}")));

        // Usar el método del servicio para obtener desde caché
        BigDecimal cachedValue = percentageService.getPercentageFromCache();
        assertNotNull(cachedValue, "Debería obtener el valor desde caché");
        assertEquals(BigDecimal.valueOf(12.5), cachedValue);
    }

    @Test
    @DisplayName("Debe fallar si no hay valor en caché y el servicio externo falla")
    void shouldFailWhenNoCacheAndServiceFails() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"error\": \"Service down\"}")));

        assertThrows(ServiceException.class, () -> percentageService.getPercentage());
    }

    @Test
    @DisplayName("Debe refrescar el valor en caché cuando el servicio externo responde nuevamente")
    void shouldRefreshCacheWhenServiceRespondsAgain() {
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 10.0}")));

        percentageService.getPercentage(); // poblar caché con 10.0

        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlEqualTo(PERCENTAGE_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"value\": 30.0}")));

        BigDecimal newPercentage = percentageService.getPercentage();
        assertEquals(BigDecimal.valueOf(30.0), newPercentage);

        // Verificar que el caché se actualizó usando el método del servicio
        BigDecimal cachedValue = percentageService.getPercentageFromCache();
        assertNotNull(cachedValue, "El valor en caché debería existir después de refrescar");
        assertEquals(BigDecimal.valueOf(30.0), cachedValue);
    }
}
