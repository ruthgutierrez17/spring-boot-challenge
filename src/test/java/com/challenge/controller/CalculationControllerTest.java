package com.challenge.controller;

import com.challenge.dto.CalculationRequest;
import com.challenge.dto.CalculationResponse;
import com.challenge.exception.PercentageUnavailableException;
import com.challenge.service.CalculationService;
import com.challenge.service.CallHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para CalculationController
 */
@WebMvcTest(CalculationController.class)
@DisplayName("CalculationController Tests")
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalculationService calculationService;

    @MockBean
    private CallHistoryService callHistoryService;

    private CalculationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CalculationRequest(new BigDecimal("100.0"), new BigDecimal("50.0"));
    }

    @Test
    @DisplayName("POST /calculate debe retornar cálculo exitoso")
    void shouldReturnSuccessfulCalculation() throws Exception {
        // Given
        CalculationResponse mockResponse = new CalculationResponse(
            new BigDecimal("100.0"), new BigDecimal("50.0"), new BigDecimal("150.0"),
            new BigDecimal("15.0"), new BigDecimal("172.5"), "EXTERNAL_SERVICE",
            "Calculo realizado exitosamente"
        );

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.num1").value(100.0))
                .andExpect(jsonPath("$.num2").value(50.0))
                .andExpect(jsonPath("$.sum").value(150.0))
                .andExpect(jsonPath("$.percentage").value(15.0))
                .andExpect(jsonPath("$.finalResult").value(172.5))
                .andExpect(jsonPath("$.percentageSource").value("EXTERNAL_SERVICE"))
                .andExpect(jsonPath("$.message").exists());

        verify(calculationService).calculate(any(CalculationRequest.class));
        verify(callHistoryService).logCall(any(), any(), any(), anyLong(), eq(200));
    }

    @Test
    @DisplayName("POST /calculate debe retornar error de validación para números nulos")
    void shouldReturnValidationErrorForNullNumbers() throws Exception {
        // Given - request con num1 nulo
        CalculationRequest nullRequest = new CalculationRequest(new BigDecimal("50.0"), null);

        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Errores de validación: {num2=El número 2 es obligatorio}"));

        // Los errores de validación se manejan en GlobalExceptionHandler, no en el controlador
        verify(calculationService, never()).calculate(any(CalculationRequest.class));
        verify(callHistoryService, never()).logError(any(), any(), anyString(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("POST /calculate debe retornar error de validación para ambos números nulos")
    void shouldReturnValidationErrorForBothNullNumbers() throws Exception {
        // Given - request con ambos números nulos
        CalculationRequest bothNullRequest = new CalculationRequest(null, null);

        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bothNullRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Errores de validación: {num1=El número 1 es obligatorio, num2=El número 2 es obligatorio}"));

        // Los errores de validación se manejan en GlobalExceptionHandler, no en el controlador
        verify(calculationService, never()).calculate(any(CalculationRequest.class));
        verify(callHistoryService, never()).logError(any(), any(), anyString(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("POST /calculate debe manejar PercentageUnavailableException específicamente")
    void shouldHandlePercentageUnavailableExceptionSpecifically() throws Exception {
        // Given
        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenThrow(new PercentageUnavailableException("Servicio externo y caché no disponibles"));

        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CALCULATION_ERROR"))
                .andExpect(jsonPath("$.message").value("No se pudo obtener el porcentaje para realizar el cálculo"));

        verify(callHistoryService).logError(any(), any(), anyString(), anyLong(), eq(503));
    }

    @Test
    @DisplayName("POST /calculate debe rechazar JSON malformado")
    void shouldRejectMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": json}"))
                .andExpect(status().isBadRequest());

        verify(calculationService, never()).calculate(any());
    }


    @Test
    @DisplayName("POST /calculate debe manejar números con muchos decimales")
    void shouldHandleHighPrecisionNumbers() throws Exception {
        // Given
        CalculationRequest precisionRequest = new CalculationRequest(new BigDecimal("100.123456789"), new BigDecimal("50.987654321"));
        CalculationResponse precisionResponse = new CalculationResponse(
            new BigDecimal("100.123456789"), new BigDecimal("50.987654321"), new BigDecimal("151.11111111"),
            new BigDecimal("15.0"), new BigDecimal("173.78"), "EXTERNAL_SERVICE",
            "Calculo realizado exitosamente"
        );

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenReturn(precisionResponse);

        // When & Then
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(precisionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.num1").value(100.123456789))
                .andExpect(jsonPath("$.num2").value(50.987654321));

        verify(calculationService).calculate(any(CalculationRequest.class));
    }
}
