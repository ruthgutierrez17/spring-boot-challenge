package com.challenge.constants;

/**
 * Constantes para mensajes de error del sistema
 */
public final class ErrorMessages {

    // Constructor privado para evitar instanciación
    private ErrorMessages() {
        throw new UnsupportedOperationException("Esta clase de constantes no debe ser instanciada");
    }

    // === MENSAJES DE VALIDACIÓN ===
    public static final String VALIDATION_REQUIRED_NUMBERS = "Los números no pueden ser nulos";
    public static final String VALIDATION_INVALID_FORMAT = "El formato de los números es inválido";
    public static final String VALIDATION_INVALID_JSON = "Formato JSON inválido";

    // === MENSAJES DE CÁLCULO ===
    public static final String CALCULATION_PERCENTAGE_UNAVAILABLE = "No se pudo obtener el porcentaje para realizar el cálculo";
    public static final String CALCULATION_SERVICE_ERROR = "Error al procesar el cálculo";

    // === MENSAJES DE SISTEMA ===
    public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";
    public static final String SERVICE_UNAVAILABLE = "El servicio no está disponible temporalmente";
    public static final String UNEXPECTED_ERROR = "Ha ocurrido un error inesperado. Por favor intente nuevamente.";

    // === MENSAJES DE SERVICIOS EXTERNOS ===
    public static final String EXTERNAL_SERVICE_TIMEOUT = "El servicio externo no responde";
    public static final String EXTERNAL_SERVICE_ERROR = "Error en el servicio externo";
    public static final String CACHE_FALLBACK_USED = "Usando datos del caché debido a falla del servicio externo";

    // === CÓDIGOS DE ERROR ===
    public static final class ErrorCodes {
        private ErrorCodes() {}

        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String CALCULATION_ERROR = "CALCULATION_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String SERVICE_UNAVAILABLE_ERROR = "SERVICE_UNAVAILABLE";
        public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
        public static final String INVALID_JSON = "INVALID_JSON";
        public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    }
}
