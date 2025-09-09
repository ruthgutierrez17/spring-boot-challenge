package com.challenge.mapper;

import com.challenge.dto.CalculationRequest;
import com.challenge.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper para crear respuestas de cálculo
 */
@Component
@RequiredArgsConstructor
public class CalculationMapper {

    /**
     * Crea una respuesta de cálculo completa
     *
     * @param request Los datos de entrada del cálculo
     * @param sum La suma de los números
     * @param percentage El porcentaje aplicado
     * @param finalResult El resultado final
     * @param percentageSource La fuente del porcentaje
     * @param message El mensaje informativo
     * @return DTO de respuesta del cálculo
     */
    public CalculationResponse createResponse(CalculationRequest request, BigDecimal sum,
                                           BigDecimal percentage, BigDecimal finalResult,
                                           String percentageSource, String message) {
        return new CalculationResponse(
            request.num1(),
            request.num2(),
            sum,
            percentage,
            finalResult,
            percentageSource,
            message
        );
    }

    /**
     * Genera mensaje informativo basado en la fuente del porcentaje
     *
     * @param source La fuente del porcentaje
     * @return Mensaje descriptivo
     */
    public String generateMessage(String source) {
        return switch (source) {
            case "EXTERNAL_SERVICE" -> "Calculo realizado con porcentaje actualizado del servicio externo";
            case "CACHE" -> "Calculo realizado con porcentaje desde caché (servicio externo no disponible)";
            default -> "Calculo realizado con porcentaje por defecto";
        };
    }
}
