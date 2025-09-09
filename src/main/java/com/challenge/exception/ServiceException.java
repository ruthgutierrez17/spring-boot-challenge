package com.challenge.exception;

/**
 * Excepción personalizada para errores de servicios externos
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
