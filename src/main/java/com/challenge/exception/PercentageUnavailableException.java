package com.challenge.exception;

/**
 * Excepción lanzada cuando no se puede obtener el porcentaje
 * de ninguna fuente disponible
 */
public class PercentageUnavailableException extends RuntimeException {

    public PercentageUnavailableException(String message) {
        super(message);
    }

    public PercentageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
