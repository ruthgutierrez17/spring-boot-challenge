package com.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Record DTO para la respuesta del cálculo
 */
@Schema(description = "Respuesta del cálculo con porcentaje aplicado")
public record CalculationResponse(
    @Schema(description = "Primer número utilizado", example = "100.50")
    BigDecimal num1,

    @Schema(description = "Segundo número utilizado", example = "200.75")
    BigDecimal num2,

    @Schema(description = "Suma de los dos números", example = "301.25")
    BigDecimal sum,

    @Schema(description = "Porcentaje aplicado", example = "15.5")
    BigDecimal percentage,

    @Schema(description = "Resultado final con porcentaje aplicado", example = "346.44")
    BigDecimal finalResult,

    @Schema(description = "Origen del porcentaje", example = "EXTERNAL_SERVICE", allowableValues = {"EXTERNAL_SERVICE", "CACHE", "DEFAULT"})
    String percentageSource,

    @Schema(description = "Mensaje informativo sobre el cálculo")
    String message
) implements BaseResponse {}
