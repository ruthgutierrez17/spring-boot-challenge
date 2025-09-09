package com.challenge.service;

import com.challenge.dto.CalculationRequest;
import com.challenge.dto.CalculationResponse;
import com.challenge.exception.PercentageUnavailableException;
import com.challenge.exception.ServiceException;
import com.challenge.mapper.CalculationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CalculationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CalculationService Tests")
class CalculationServiceTest {

    @Mock
    private PercentageService percentageService;

    @Mock
    private CalculationMapper calculationMapper;

    @InjectMocks
    private CalculationService calculationService;

    private CalculationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CalculationRequest(new BigDecimal("100.0"), new BigDecimal("50.0"));

        lenient().when(calculationMapper.generateMessage(any(String.class))).thenReturn("Mensaje genérico");

        lenient().when(calculationMapper.createResponse(any(), any(), any(), any(), any(), any()))
            .thenAnswer(invocation -> {
                CalculationRequest request = invocation.getArgument(0);
                BigDecimal sum = invocation.getArgument(1);
                BigDecimal percentage = invocation.getArgument(2);
                BigDecimal finalResult = invocation.getArgument(3);
                String source = invocation.getArgument(4);
                String message = invocation.getArgument(5);

                return new CalculationResponse(
                    request.num1(), request.num2(), sum, percentage,
                    finalResult, source, message
                );
            });
    }

    @Test
    @DisplayName("Debe calcular correctamente con porcentaje del servicio externo")
    void shouldCalculateWithExternalServicePercentage() {
        // Given
        BigDecimal expectedPercentage = BigDecimal.valueOf(15.0);
        when(percentageService.getPercentage()).thenReturn(expectedPercentage);
        when(calculationMapper.generateMessage("EXTERNAL_SERVICE"))
            .thenReturn("Calculo realizado con porcentaje actualizado del servicio externo");

        // When
        CalculationResponse response = calculationService.calculate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("100.0"), response.num1());
        assertEquals(new BigDecimal("50.0"), response.num2());
        assertEquals(new BigDecimal("150.0"), response.sum());
        assertEquals(new BigDecimal("15.0"), response.percentage());
        assertEquals(new BigDecimal("172.50"), response.finalResult()); // 150 + (150 * 0.15)
        assertEquals("EXTERNAL_SERVICE", response.percentageSource());
        assertNotNull(response.message());
        assertTrue(response.message().contains("servicio externo"));

        verify(percentageService).getPercentage();
    }

    @Test
    @DisplayName("Debe usar caché cuando el servicio externo falla")
    void shouldUseCacheWhenExternalServiceFails() {
        // Given
        BigDecimal cachedPercentage = BigDecimal.valueOf(12.0);
        when(percentageService.getPercentage())
            .thenThrow(new ServiceException("External service failed"));
        when(percentageService.getPercentageFromCache()).thenReturn(cachedPercentage);
        when(calculationMapper.generateMessage("CACHE"))
            .thenReturn("Calculo realizado con porcentaje desde caché (servicio externo no disponible)");

        // When
        CalculationResponse response = calculationService.calculate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("150.0"), response.sum());
        assertEquals(new BigDecimal("12.0"), response.percentage());
        assertEquals(new BigDecimal("168.00"), response.finalResult()); // 150 + (150 * 0.12)
        assertEquals("CACHE", response.percentageSource());
        assertTrue(response.message().contains("caché"));

        verify(percentageService).getPercentage();
        verify(percentageService).getPercentageFromCache();
    }

    @Test
    @DisplayName("Debe fallar cuando no hay caché disponible")
    void shouldFailWhenNoCacheAvailable() {
        // Given
        when(percentageService.getPercentage())
            .thenThrow(new ServiceException("External service failed"));
        when(percentageService.getPercentageFromCache()).thenReturn(null);

        // When & Then
        PercentageUnavailableException exception = assertThrows(PercentageUnavailableException.class, () -> {
            calculationService.calculate(validRequest);
        });

        assertTrue(exception.getMessage().contains("No se pudo obtener porcentaje"));
        verify(percentageService).getPercentage();
        verify(percentageService).getPercentageFromCache();
    }

    @Test
    @DisplayName("Debe validar números nulos")
    void shouldValidateNullNumbers() {
        // Given
        CalculationRequest invalidRequest = new CalculationRequest(null, new BigDecimal("50.0"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculate(invalidRequest);
        });

        assertEquals("Los números no pueden ser nulos", exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar números decimales con precisión")
    void shouldHandleDecimalPrecision() {
        // Given
        CalculationRequest decimalRequest = new CalculationRequest(new BigDecimal("100.33"), new BigDecimal("200.67"));
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(10.5));

        // When
        CalculationResponse response = calculationService.calculate(decimalRequest);

        // Then
        assertEquals(new BigDecimal("301.00"), response.sum());
        assertEquals(new BigDecimal("10.5"), response.percentage());
        assertEquals(new BigDecimal("332.61"), response.finalResult()); // Resultado redondeado a 2 decimales
    }

    @Test
    @DisplayName("Debe manejar números negativos")
    void shouldHandleNegativeNumbers() {
        // Given
        CalculationRequest negativeRequest = new CalculationRequest(new BigDecimal("-50.0"), new BigDecimal("30.0"));
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(20.0));

        // When
        CalculationResponse response = calculationService.calculate(negativeRequest);

        // Then
        assertEquals(new BigDecimal("-20.0"), response.sum());
        assertEquals(new BigDecimal("20.0"), response.percentage());
        assertEquals(new BigDecimal("-24.00"), response.finalResult()); // -20 + (-20 * 0.20)
    }

    @Test
    @DisplayName("Debe manejar porcentaje cero")
    void shouldHandleZeroPercentage() {
        // Given
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(0.0));

        // When
        CalculationResponse response = calculationService.calculate(validRequest);

        // Then
        assertEquals(new BigDecimal("150.0"), response.sum());
        assertEquals(new BigDecimal("0.0"), response.percentage());
        assertEquals(new BigDecimal("150.00"), response.finalResult());
    }
}
