package com.challenge.service;

import com.challenge.dto.CalculationRequest;
import com.challenge.dto.CalculationResponse;
import com.challenge.exception.PercentageUnavailableException;
import com.challenge.exception.ServiceException;
import com.challenge.mapper.CalculationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio principal para realizar cálculos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CalculationService {

    private final PercentageService percentageService;
    private final CalculationMapper calculationMapper;

    /**
     * Realiza el cálculo principal con porcentaje dinámico
     *
     * @param request Los números a calcular
     * @return El resultado del cálculo con información detallada
     */
    public CalculationResponse calculate(CalculationRequest request) {
        log.info("Iniciando cálculo para: {}", request);

        // Validación de parámetros
        if (request.num1() == null || request.num2() == null) {
            throw new IllegalArgumentException("Los números no pueden ser nulos");
        }

        // Cálculo básico usando BigDecimal para precisión
        BigDecimal sum = request.num1().add(request.num2());
        log.debug("Suma calculada: {}", sum);

        // Obtención del porcentaje con manejo de errores
        PercentageResult percentageResult = getPercentageWithSource();

        // Aplicación del porcentaje
        BigDecimal finalResult = applyPercentage(sum, percentageResult.percentage());

        // Construcción de la respuesta usando el mapper
        String message = calculationMapper.generateMessage(percentageResult.source());
        CalculationResponse response = calculationMapper.createResponse(
                request, sum, percentageResult.percentage(), finalResult,
                percentageResult.source(), message
        );

        log.info("Calculo completado exitosamente. Resultado: {}", finalResult);
        return response;
    }

    /**
     * Obtiene el porcentaje con información de la fuente
     */
    private PercentageResult getPercentageWithSource() {
        try {
            // Intentar obtener del servicio externo (que actualiza el caché con @CachePut)
            BigDecimal percentage = percentageService.getPercentage();
            return new PercentageResult(percentage, "EXTERNAL_SERVICE");

        } catch (ServiceException e) {
            log.warn("Servicio externo falló ({}), intentando caché: {}", e.getClass().getSimpleName(), e.getMessage());

            // Intentar obtener del caché usando el método dedicado
            BigDecimal cachedPercentage = percentageService.getPercentageFromCache();
            if (cachedPercentage != null) {
                log.info("Usando porcentaje desde caché: {}%", cachedPercentage);
                return new PercentageResult(cachedPercentage, "CACHE");
            }

            // Si no hay valor en caché, lanzar excepción
            log.error("No hay valor en caché y el servicio externo falló");
            throw new PercentageUnavailableException("No se pudo obtener porcentaje de ninguna fuente", e);
        }
    }

    /**
     * Aplica el porcentaje al valor usando BigDecimal para precisión
     */
    private BigDecimal applyPercentage(BigDecimal value, BigDecimal percentage) {
        BigDecimal bdPercentage = percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal bdIncrease = value.multiply(bdPercentage);
        BigDecimal bdResult = value.add(bdIncrease);

        return bdResult.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Record para encapsular el resultado del porcentaje con su fuente
     */
    private record PercentageResult(BigDecimal percentage, String source) {
    }
}
