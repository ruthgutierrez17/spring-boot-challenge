package com.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Record DTO para representar el historial de llamadas
 */
@Schema(description = "Registro del historial de llamadas a la API")
public record CallHistoryResponse(
    @Schema(description = "ID único del registro", example = "1")
    Long id,

    @Schema(description = "Fecha y hora de la llamada", example = "2025-09-02T10:30:00")
    LocalDateTime timestamp,

    @Schema(description = "Endpoint llamado", example = "/api/calculate")
    String endpoint,

    @Schema(description = "Método HTTP utilizado", example = "POST")
    String httpMethod,

    @Schema(description = "Parámetros enviados", example = "{\"num1\": 100.5, \"num2\": 200.75}")
    String parameters,

    @Schema(description = "Datos de respuesta o mensaje de error")
    String responseData,

    @Schema(description = "Código de estado HTTP", example = "200")
    Integer statusCode,

    @Schema(description = "Tiempo de ejecución en milisegundos", example = "150")
    Long executionTimeMs,

    @Schema(description = "IP del cliente", example = "192.168.1.100")
    String clientIp
) implements BaseResponse {
}
