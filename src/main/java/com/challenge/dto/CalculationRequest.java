package com.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Record DTO para el request de cálculo
 */
@Schema(description = "Request para realizar cálculo con porcentaje dinámico")
public record CalculationRequest(
    @NotNull(message = "El número 1 es obligatorio")
    @Schema(description = "Primer número para el cálculo", example = "100.50", required = true)
    BigDecimal num1,

    @NotNull(message = "El número 2 es obligatorio")
    @Schema(description = "Segundo número para el cálculo", example = "200.75", required = true)
    BigDecimal num2
) {
    @Override
    public String toString() {
        return String.format("CalculationRequest{num1=%s, num2=%s}", num1, num2);
    }
}
