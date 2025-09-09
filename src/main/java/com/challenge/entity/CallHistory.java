package com.challenge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de llamadas a la API
 */
@Entity
@Table(
    name = "call_history",
    indexes = {
        @Index(name = "idx_callhistory_endpoint_timestamp", columnList = "endpoint, timestamp"),
        @Index(name = "idx_callhistory_timestamp", columnList = "timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @NotNull
    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    public CallHistory(String endpoint, String httpMethod, String parameters) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.parameters = parameters;
    }
}
