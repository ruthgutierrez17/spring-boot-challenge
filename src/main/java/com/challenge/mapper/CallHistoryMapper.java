package com.challenge.mapper;

import com.challenge.dto.CallHistoryResponse;
import com.challenge.entity.CallHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades CallHistory a DTOs de respuesta
 */
@Component
@RequiredArgsConstructor
public class CallHistoryMapper {

    /**
     * Convierte entidad CallHistory a DTO CallHistoryResponse
     *
     * @param entity La entidad a convertir
     * @return DTO de respuesta
     */
    public CallHistoryResponse toResponse(CallHistory entity) {
        if (entity == null) {
            return null;
        }

        // Priorizar respuesta sobre error para el campo responseData
        String responseData = determineResponseData(entity);

        return new CallHistoryResponse(
            entity.getId(),
            entity.getTimestamp(),
            entity.getEndpoint(),
            entity.getHttpMethod(),
            entity.getParameters(),
            responseData,
            entity.getStatusCode(),
            entity.getExecutionTimeMs(),
            entity.getClientIp()
        );
    }

    /**
     * Determina los datos de respuesta basado en el estado de la entidad
     */
    private String determineResponseData(CallHistory entity) {
        if (entity.getResponseData() != null) {
            return entity.getResponseData();
        } else if (entity.getErrorMessage() != null) {
            return "ERROR: " + entity.getErrorMessage();
        } else {
            return null;
        }
    }
}
