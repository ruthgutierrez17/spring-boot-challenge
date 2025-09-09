package com.challenge.event;

import java.time.LocalDateTime;

/**
 * Evento para registrar un error en el API
 */
public record ApiErrorEvent(
    String endpoint,
    String httpMethod,
    String clientIp,
    Object parameters,
    String errorMessage,
    Long executionTime,
    Integer statusCode,
    LocalDateTime timestamp
) {
    public ApiErrorEvent(String endpoint, String httpMethod, String clientIp,
                        Object parameters, String errorMessage, Long executionTime, Integer statusCode) {
        this(endpoint, httpMethod, clientIp, parameters, errorMessage, executionTime, statusCode, LocalDateTime.now());
    }
}
