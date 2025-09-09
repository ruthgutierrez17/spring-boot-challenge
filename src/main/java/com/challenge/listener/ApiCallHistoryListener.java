package com.challenge.listener;

import com.challenge.entity.CallHistory;
import com.challenge.event.ApiCallEvent;
import com.challenge.event.ApiErrorEvent;
import com.challenge.repository.CallHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de API que registra las llamadas en la base de datos
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiCallHistoryListener {

    private final CallHistoryRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Maneja eventos de llamadas exitosas
     */
    @EventListener
    @Async("taskExecutor")
    public void handleApiCall(ApiCallEvent event) {
        try {
            CallHistory callHistory = new CallHistory();
            callHistory.setEndpoint(event.endpoint());
            callHistory.setHttpMethod(event.httpMethod());
            callHistory.setClientIp(event.clientIp());
            callHistory.setParameters(serializeObject(event.parameters()));
            callHistory.setResponseData(serializeObject(event.response()));
            callHistory.setExecutionTimeMs(event.executionTime());
            callHistory.setStatusCode(event.statusCode());

            repository.save(callHistory);

            log.debug("Llamada registrada exitosamente: {} {}", event.httpMethod(), event.endpoint());

        } catch (Exception e) {
            log.error("Error al registrar llamada: {}", e.getMessage(), e);
        }
    }

    /**
     * Maneja eventos de errores
     */
    @EventListener
    @Async("taskExecutor")
    public void handleApiError(ApiErrorEvent event) {
        try {
            CallHistory callHistory = new CallHistory();
            callHistory.setEndpoint(event.endpoint());
            callHistory.setHttpMethod(event.httpMethod());
            callHistory.setClientIp(event.clientIp());
            callHistory.setParameters(serializeObject(event.parameters()));
            callHistory.setErrorMessage(event.errorMessage());
            callHistory.setExecutionTimeMs(event.executionTime());
            callHistory.setStatusCode(event.statusCode());

            repository.save(callHistory);

            log.debug("Error registrado exitosamente: {} {}", event.httpMethod(), event.endpoint());

        } catch (Exception e) {
            log.error("Error al registrar error: {}", e.getMessage(), e);
        }
    }

    /**
     * Serializa objeto a JSON de forma segura
     */
    private String serializeObject(Object obj) {
        if (obj == null) return null;

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Error al serializar objeto: {}", e.getMessage());
            return obj.toString();
        }
    }
}
