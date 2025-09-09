package com.challenge.controller;

import com.challenge.dto.BaseResponse;
import com.challenge.dto.CalculationRequest;
import com.challenge.dto.CalculationResponse;
import com.challenge.dto.ErrorResponse;
import com.challenge.constants.ErrorMessages;
import com.challenge.exception.PercentageUnavailableException;
import com.challenge.service.CalculationService;
import com.challenge.service.CallHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para operaciones de cálculo
 */
@RestController
@RequestMapping("/api/calculate")
@Tag(name = "Calculation", description = "API para cálculos con porcentaje dinámico")
@Slf4j
@RequiredArgsConstructor
public class CalculationController {


    private final CalculationService calculationService;
    private final CallHistoryService callHistoryService;

    /**
     * Suma dos números y aplica porcentaje dinámico
     */
    @PostMapping
    @Operation(summary = "Realizar cálculo con porcentaje dinámico",
               description = "Suma dos números y aplica un porcentaje adicional obtenido de un servicio externo. Flujo de porcentaje: " +
                           "\n1. Intenta obtener desde servicio externo, " +
                           "\n2. Si falla, usa valor en caché, " +
                           "\n3. Si no hay caché, retorna error")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo realizado exitosamente", content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible y sin caché", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BaseResponse> calculate(
            @Parameter(description = "Números a calcular", required = true) @Valid @RequestBody CalculationRequest request,
            HttpServletRequest httpRequest) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("Recibida solicitud de cálculo: {}", request);

            CalculationResponse response = calculationService.calculate(request);
            long executionTime = System.currentTimeMillis() - startTime;

            callHistoryService.logCall(httpRequest, request, response,
                    executionTime, HttpStatus.OK.value());

            log.info("Calculo completado en {}ms", executionTime);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return handleError(e, ErrorMessages.ErrorCodes.VALIDATION_ERROR, e.getMessage(),
                             HttpStatus.BAD_REQUEST, httpRequest, request, startTime);

        } catch (PercentageUnavailableException e) {
            return handleError(e, ErrorMessages.ErrorCodes.CALCULATION_ERROR,
                             ErrorMessages.CALCULATION_PERCENTAGE_UNAVAILABLE,
                             HttpStatus.SERVICE_UNAVAILABLE, httpRequest, request, startTime);

        } catch (Exception e) {
            return handleError(e, ErrorMessages.ErrorCodes.INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR,
                             HttpStatus.INTERNAL_SERVER_ERROR, httpRequest, request, startTime);
        }
    }

    /**
     * Helper para manejar errores
     *
     * @param exception La excepción que ocurrió
     * @param errorCode Código del error para la respuesta
     * @param userMessage Mensaje para el usuario final
     * @param httpStatus Status HTTP a retornar
     * @param httpRequest Request HTTP para logging
     * @param calculationRequest Request del cálculo para logging
     * @param startTime Tiempo de inicio para calcular duración
     * @return ResponseEntity con ErrorResponse apropiada
     */
    private ResponseEntity<BaseResponse> handleError(Exception exception, String errorCode,
                                                    String userMessage, HttpStatus httpStatus,
                                                    HttpServletRequest httpRequest,
                                                    CalculationRequest calculationRequest,
                                                    long startTime) {

        long executionTime = System.currentTimeMillis() - startTime;

        // Log segun el tipo de error
        switch (httpStatus) {
            case BAD_REQUEST -> log.warn("Error de validación: {}", exception.getMessage());
            case SERVICE_UNAVAILABLE -> log.error("Error al realizar cálculo: {}", exception.getMessage(), exception);
            default -> log.error("Error inesperado: {}", exception.getMessage(), exception);
        }

        ErrorResponse errorResponse = new ErrorResponse(errorCode, userMessage, httpRequest.getRequestURI());

        callHistoryService.logError(httpRequest, calculationRequest, exception.getMessage(),
                executionTime, httpStatus.value());

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
