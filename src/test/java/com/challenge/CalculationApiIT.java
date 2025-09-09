package com.challenge;

import com.challenge.dto.CalculationRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración end-to-end usando TestContainers
 *
 * Prueba la aplicación completa con:
 * - Base de datos PostgreSQL real
 * - Todos los componentes integrados
 * - Flujos completos de request/response
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Integration Tests")
class CalculationApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static WireMockServer wireMockServer;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        postgres.start(); // arranca el contenedor antes de que Spring cree el DataSource

        // Configurar WireMock para simular el servicio externo
        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();

        // Configurar respuesta del servicio externo
        wireMockServer.stubFor(get(urlEqualTo("/percentage"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"value\": 15.0}")));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Configurar mock del servicio externo
        registry.add("external-service.percentage-url", () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        // Configuración base para las pruebas
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("Debe guardar historial de llamadas")
    void shouldSaveCallHistory() throws InterruptedException {
        // Given
        CalculationRequest request = new CalculationRequest(new BigDecimal("200.0"), new BigDecimal("100.0"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CalculationRequest> entity = new HttpEntity<>(request, headers);

        // When - realizar cálculo
        ResponseEntity<String> calcResponse = restTemplate.postForEntity(
            "/api/calculate", entity, String.class);

        // Esperar un poco para que se procese el registro asíncrono
        Thread.sleep(1000);

        // Then - verificar que se guardó en el historial
        ResponseEntity<String> historyResponse = restTemplate.getForEntity(
            "/api/history", String.class);

        assertEquals(HttpStatus.OK, calcResponse.getStatusCode());
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertTrue(historyResponse.getBody().matches(".*\"totalElements\":\\s*[1-9].*"));
        assertTrue(historyResponse.getBody().contains("/api/calculate"));
    }

    @Test
    @DisplayName("Debe obtener historial con paginación")
    void shouldGetHistoryWithPagination() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/history?page=0&size=10", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"content\":"));
        assertTrue(response.getBody().contains("\"totalElements\":"));
        assertTrue(response.getBody().contains("\"totalPages\":"));
    }

    @Test
    @DisplayName("Debe estar disponible documentación Swagger")
    void shouldHaveSwaggerDocumentationAvailable() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/v3/api-docs", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"openapi\":"));
        assertTrue(response.getBody().contains("Challenge Backend API REST"));
    }

    @Test
    @DisplayName("Debe exponer métricas de Actuator")
    void shouldExposeActuatorMetrics() {
        // When
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
            "/actuator/health", String.class);

        // Then
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertTrue(healthResponse.getBody().contains("\"status\":\"UP\""));
    }
}
