package com.challenge.service;

import com.challenge.dto.CallHistoryResponse;
import com.challenge.entity.CallHistory;
import com.challenge.event.ApiCallEvent;
import com.challenge.event.ApiErrorEvent;
import com.challenge.mapper.CallHistoryMapper;
import com.challenge.repository.CallHistoryRepository;
import com.challenge.specification.CallHistorySpecifications;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar el historial de llamadas y publicación de eventos para registro asíncrono
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallHistoryService {

    private final CallHistoryRepository repository;
    private final CallHistoryMapper callHistoryMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publica un evento para registrar una llamada exitosa
     *
     * @param request       HttpServletRequest para extraer información
     * @param parameters    Parámetros de la llamada
     * @param response      Respuesta generada
     * @param executionTime Tiempo de ejecución en milisegundos
     * @param statusCode    Código de estado HTTP
     */
    public void logCall(HttpServletRequest request,
                       Object parameters,
                       Object response,
                       Long executionTime,
                       Integer statusCode) {

        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = getClientIpAddress(request);

        ApiCallEvent event = new ApiCallEvent(endpoint, httpMethod, clientIp,
                                             parameters, response, executionTime, statusCode);
        eventPublisher.publishEvent(event);

        log.debug("Evento de llamada publicado: {} {}", httpMethod, endpoint);
    }

    /**
     * Publica un evento para registrar un error
     *
     * @param request       HttpServletRequest para extraer información
     * @param parameters    Parámetros de la llamada
     * @param errorMessage  Mensaje de error
     * @param executionTime Tiempo de ejecución en milisegundos
     * @param statusCode    Código de estado HTTP
     */
    public void logError(HttpServletRequest request,
                        Object parameters,
                        String errorMessage,
                        Long executionTime,
                        Integer statusCode) {

        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = getClientIpAddress(request);

        ApiErrorEvent event = new ApiErrorEvent(endpoint, httpMethod, clientIp,
                                               parameters, errorMessage, executionTime, statusCode);
        eventPublisher.publishEvent(event);

        log.debug("Evento de error publicado: {} {}", httpMethod, endpoint);
    }

    /**
     * Obtiene el historial con paginación y filtros opcionales usando Specifications
     *
     * @param page      Página a consultar (base 0)
     * @param size      Tamaño de página
     * @param endpoint  Filtro por endpoint (opcional)
     * @param startDate Fecha de inicio (opcional)
     * @param endDate   Fecha de fin (opcional)
     * @return Página de resultados
     */
    public Page<CallHistoryResponse> getHistory(int page, int size,
            String endpoint,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // Configuración de paginación con ordenamiento por timestamp descendente
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        // Construcción dinámica de la consulta usando Specifications
        Specification<CallHistory> spec = Specification.where(CallHistorySpecifications.withEndpoint(endpoint))
                .and(CallHistorySpecifications.withStartDate(startDate))
                .and(CallHistorySpecifications.withEndDate(endDate));

        // Ejecución de la consulta con specifications
        Page<CallHistory> historyPage = repository.findAll(spec, pageable);

        // Conversión a DTOs usando el mapper
        return historyPage.map(callHistoryMapper::toResponse);
    }

    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}
