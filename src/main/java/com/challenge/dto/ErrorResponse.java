package com.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Record DTO para respuestas de error estandarizadas
 */
@Schema(description = "Respuesta de error estandarizada")
public record ErrorResponse(
    @Schema(description = "Código del error", example = "VALIDATION_ERROR")
    String code,

    @Schema(description = "Mensaje descriptivo del error", example = "Los números no pueden ser nulos")
    String message,

    @Schema(description = "Endpoint donde ocurrió el error", example = "/api/calculate")
    String path,

    @Schema(description = "Timestamp del error", example = "2025-09-02T10:30:00Z")
    String timestamp
) implements BaseResponse {

    // Constructor de conveniencia que genera automáticamente el timestamp
    public ErrorResponse(String code, String message, String path) {
        this(code, message, path, Instant.now().toString());
    }
}
