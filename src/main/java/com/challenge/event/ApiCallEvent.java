package com.challenge.event;

import java.time.LocalDateTime;

/**
 * Evento para registrar una llamada exitosa al API
 */
public record ApiCallEvent(
    String endpoint,
    String httpMethod,
    String clientIp,
    Object parameters,
    Object response,
    Long executionTime,
    Integer statusCode,
    LocalDateTime timestamp
) {
    public ApiCallEvent(String endpoint, String httpMethod, String clientIp,
                       Object parameters, Object response, Long executionTime, Integer statusCode) {
        this(endpoint, httpMethod, clientIp, parameters, response, executionTime, statusCode, LocalDateTime.now());
    }
}
