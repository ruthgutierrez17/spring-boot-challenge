package com.challenge.controller;

import com.challenge.dto.CallHistoryResponse;
import com.challenge.service.CallHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador para consultar el historial de llamadas
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "API para consultar historial de llamadas")
public class HistoryController {

    private final CallHistoryService callHistoryService;

    /**
     * Obtiene el historial de llamadas con paginación y filtros
     */
    @GetMapping
    @Operation(
        summary = "Obtener historial de llamadas",
        description = "Consulta el historial de llamadas a la API con opciones de paginación y filtrado. " +
                     "Características: \nPaginación automática (por defecto 20 elementos por página), " +
                     "\nFiltrado por endpoint, \nFiltrado por rango de fechas, " +
                     "\nInformación de cada llamada, Ordenamiento por timestamp descendente."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Historial obtenido exitosamente",
        content = @Content(schema = @Schema(implementation = Page.class))
    )
    public ResponseEntity<Page<CallHistoryResponse>> getHistory(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Filtro por endpoint (búsqueda parcial)", example = "calculate")
            @RequestParam(required = false) String endpoint,

            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)",
                      example = "2025-09-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)",
                      example = "2025-09-30T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Validación de parámetros
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        Page<CallHistoryResponse> history = callHistoryService.getHistory(
            page, size, endpoint, startDate, endDate);

        return ResponseEntity.ok(history);
    }

}
